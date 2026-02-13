package com.example.sicenet.data.repository

import android.util.Log
import com.example.sicenet.data.local.AlumnoDao
import com.example.sicenet.data.local.MateriaDao
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Login
import com.example.sicenet.data.model.Materia
import com.example.sicenet.data.network.SicenetApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SicenetRepository(
    private val apiService: SicenetApiService,
    private val alumnoDao: AlumnoDao,
    private val materiaDao: MateriaDao
) : ISicenetRepository {

    private var sessionCookie: String? = null
    private val xmlMediaType = "text/xml; charset=utf-8".toMediaType()

    override fun getSessionCookie(): String? = sessionCookie
    override fun setSessionCookie(cookie: String?) {
        sessionCookie = cookie
    }

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
                val jsonMatch = Regex("<accesoLoginResult>(.*?)</accesoLoginResult>", RegexOption.DOT_MATCHES_ALL).find(bodyString)
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
            Result.failure(e)
        }
    }

    override suspend fun getProfileRemote(cookie: String?): Result<Alumno> {
        val effectiveCookie = cookie ?: sessionCookie
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
                cookie = effectiveCookie,
                soap = soapBody.toRequestBody(xmlMediaType)
            )

            if (response.isSuccessful) {
                val xmlResponse = response.body()?.string() ?: ""
                val jsonMatch = Regex("<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>", RegexOption.DOT_MATCHES_ALL)
                    .find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value

                if (jsonContent != null) {
                    Result.success(Alumno.fromJson(jsonContent))
                } else {
                    Result.failure(Exception("No se encontró el perfil"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCargaAcademicaRemote(cookie: String?): Result<List<Materia>> {
        val effectiveCookie = cookie ?: sessionCookie
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        return try {
            val response = apiService.getCargaAcademica(cookie = effectiveCookie, soap = soapBody.toRequestBody(xmlMediaType))
            if (response.isSuccessful) {
                val xmlResponse = response.body()?.string() ?: ""
                Log.d("SICENET_RAW", "Respuesta Carga: $xmlResponse")
                
                val jsonMatch = Regex("<getCargaAcademicaByAlumnoResult>(.*?)</getCargaAcademicaByAlumnoResult>", RegexOption.DOT_MATCHES_ALL).find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value
                
                if (!jsonContent.isNullOrBlank() && jsonContent != "[]") {
                    Result.success(Materia.fromJsonList(jsonContent))
                } else {
                    Log.w("SICENET_REPO", "El JSON de carga vino vacío o nulo")
                    Result.failure(Exception("No se encontró la carga o el alumno no tiene materias"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local methods
    override fun getAlumnoLocal(): Flow<Alumno?> = alumnoDao.getAlumno()
    override suspend fun saveAlumnoLocal(alumno: Alumno) = alumnoDao.insertAlumno(alumno)

    override fun getCargaLocal(): Flow<List<Materia>> = materiaDao.getAllMaterias()
    override suspend fun saveCargaLocal(materias: List<Materia>) {
        materiaDao.deleteAll()
        materiaDao.insertAll(materias)
    }
    override suspend fun getLastCargaUpdate(): Long? = materiaDao.getLastUpdateTime()

    override fun logout() {
        sessionCookie = null
    }
}
