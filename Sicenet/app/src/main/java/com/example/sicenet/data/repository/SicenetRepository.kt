package com.example.sicenet.data.repository

import android.util.Log
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Login
import com.example.sicenet.data.network.SicenetApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SicenetRepository(private val apiService: SicenetApiService) : ISicenetRepository {

    private var sessionCookie: String? = null
    private val xmlMediaType = "text/xml; charset=utf-8".toMediaType()

    override suspend fun login(matricula: String, contrasenia: String): Result<Login> {
        return try {
            val cleanMatricula = matricula.trim()
            val cleanPassword = contrasenia.trim()

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

                val jsonMatch = Regex("<accesoLoginResult>(.*?)</accesoLoginResult>").find(bodyString)
                val jsonContent = jsonMatch?.groups?.get(1)?.value ?: ""

                val jsonObject = JSONObject(jsonContent)
                val isSuccess = jsonObject.optBoolean("acceso", false)

                if (isSuccess) {
                    val cookieHeader = response.headers()["Set-Cookie"]
                    sessionCookie = cookieHeader?.split(";")?.firstOrNull()

                    Result.success(Login(true, "Acceso concedido", sessionCookie))
                } else {
                    Result.success(Login(false, "Matrícula o contraseña incorrecta"))
                }
            } else {
                Result.failure(Exception("Error en el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("SICENET", "Error de red en login", e)
            Result.failure(e)
        }
    }

    override suspend fun getProfile(): Result<Alumno> {
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
                val xmlResponse = response.body()?.string() ?: ""

                val jsonMatch = Regex("<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>")
                    .find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value

                if (jsonContent != null) {
                    val alumno = Alumno.fromJson(jsonContent)
                    Result.success(alumno)
                } else {
                    Result.failure(Exception("No se encontró el perfil en la respuesta"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("SICENET", "Error al obtener perfil", e)
            Result.failure(e)
        }
    }

    override fun logout() {
        sessionCookie = null
    }
}