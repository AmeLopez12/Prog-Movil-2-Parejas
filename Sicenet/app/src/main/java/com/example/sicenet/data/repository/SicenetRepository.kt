package com.example.sicenet.data.repository

import android.util.Log
import com.example.sicenet.data.network.SicenetApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class SicenetRepository(private val apiService: SicenetApiService) {

    private var sessionCookie: String? = null
    private val xmlMediaType = "text/xml; charset=utf-8".toMediaType()

    suspend fun login(matricula: String, contrasenia: String): Result<Boolean> {
        return try {
            val cleanMatricula = matricula.trim()
            val cleanPassword = contrasenia.trim()
            
            Log.d("SICENET", "Intentando login para: $cleanMatricula")
            
            val soapBody = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <accesoLogin xmlns="http://tempuri.org/">
                      <strMatricula>$cleanMatricula</strMatricula>
                      <strContrasenia>$cleanPassword</strContrasenia>
                      <tipoUsuario>ALUMNO</tipoUsuario>
                    </accesoLogin>
                  </soap:Body>
                </soap:Envelope>
            """.trimIndent()

            val requestBody = soapBody.toRequestBody(xmlMediaType)
            val response = apiService.acceso(requestBody)

            if (response.isSuccessful) {
                val bodyString = response.body()?.string() ?: ""
                Log.d("SICENET", "Respuesta del servidor: $bodyString")

                if (bodyString.contains("\"acceso\":true")) {
                    val cookieHeader = response.headers()["Set-Cookie"]
                    sessionCookie = cookieHeader?.split(";")?.firstOrNull()
                    Log.d("SICENET", "¡Acceso concedido! Cookie guardada: $sessionCookie")
                    Result.success(true)
                } else if (bodyString.contains("<html>")) {
                    Log.e("SICENET", "Error: El servidor respondió con HTML. Posible problema de SOAPAction.")
                    Result.failure(Exception("Error de comunicación con el servidor"))
                } else {
                    Log.w("SICENET", "Acceso denegado: Credenciales incorrectas.")
                    Result.failure(Exception("Matrícula o contraseña incorrecta"))
                }
            } else {
                Log.e("SICENET", "Error HTTP: ${response.code()}")
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("SICENET", "Error de red", e)
            Result.failure(e)
        }
    }

    suspend fun getProfile(): String? {
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        return try {
            val response = apiService.getProfile(
                soapAction = "\"http://tempuri.org/getAlumnoAcademicoWithLineamiento\"",
                cookie = sessionCookie,
                soap = soapBody.toRequestBody(xmlMediaType)
            )
            if (response.isSuccessful) {
                response.body()?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        sessionCookie = null
    }
}
