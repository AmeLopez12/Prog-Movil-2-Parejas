package com.example.sicenet.data.repository

import android.content.Context
import android.util.Log
import com.example.sicenet.data.local.*
import com.example.sicenet.data.model.*
import com.example.sicenet.data.network.SicenetApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SicenetRepository(
    context: Context,
    private val apiService: SicenetApiService,
    private val alumnoDao: AlumnoDao,
    private val materiaDao: MateriaDao,
    private val kardexDao: KardexDao,
    private val califUnidadDao: CalifUnidadDao,
    private val califFinalDao: CalifFinalDao
) : ISicenetRepository {

    private val prefs = context.getSharedPreferences("sicenet_prefs", Context.MODE_PRIVATE)
    private val xmlMediaType = "text/xml; charset=utf-8".toMediaType()

    override fun getSessionCookie(): String? = prefs.getString("session_cookie", null)
    
    override fun setSessionCookie(cookie: String?) {
        prefs.edit().putString("session_cookie", cookie).apply()
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
                    val cookie = cookieHeader?.split(";")?.firstOrNull()
                    setSessionCookie(cookie)
                    Result.success(Login(true, "Acceso concedido", cookie))
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
        val effectiveCookie = cookie ?: getSessionCookie()
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
        val effectiveCookie = cookie ?: getSessionCookie()
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
                val jsonMatch = Regex("<getCargaAcademicaByAlumnoResult>(.*?)</getCargaAcademicaByAlumnoResult>", RegexOption.DOT_MATCHES_ALL).find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value
                if (!jsonContent.isNullOrBlank() && jsonContent != "[]") {
                    Result.success(Materia.fromJsonList(jsonContent))
                } else {
                    Result.failure(Exception("No se encontró la carga"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getKardexRemote(cookie: String?, lineamiento: Int): Result<List<Kardex>> {
        val effectiveCookie = cookie ?: getSessionCookie()
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
                  <aluLineamiento>$lineamiento</aluLineamiento>
                </getAllKardexConPromedioByAlumno>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        return try {
            val response = apiService.getKardex(cookie = effectiveCookie, soap = soapBody.toRequestBody(xmlMediaType))
            if (response.isSuccessful) {
                val xmlResponse = response.body()?.string() ?: ""
                val jsonMatch = Regex("<getAllKardexConPromedioByAlumnoResult>(.*?)</getAllKardexConPromedioByAlumnoResult>", RegexOption.DOT_MATCHES_ALL).find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value
                if (!jsonContent.isNullOrBlank()) {
                    Result.success(Kardex.fromJsonList(jsonContent))
                } else {
                    Result.failure(Exception("Kardex vacío"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCalifUnidadesRemote(cookie: String?): Result<List<CalifUnidad>> {
        val effectiveCookie = cookie ?: getSessionCookie()
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        return try {
            val response = apiService.getCalifUnidades(cookie = effectiveCookie, soap = soapBody.toRequestBody(xmlMediaType))
            if (response.isSuccessful) {
                val xmlResponse = response.body()?.string() ?: ""
                val jsonMatch = Regex("<getCalifUnidadesByAlumnoResult>(.*?)</getCalifUnidadesByAlumnoResult>", RegexOption.DOT_MATCHES_ALL).find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value
                if (!jsonContent.isNullOrBlank()) {
                    Result.success(CalifUnidad.fromJsonList(jsonContent))
                } else {
                    Result.failure(Exception("Unidades vacío"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCalifFinalRemote(cookie: String?, modEducativo: Int): Result<List<CalifFinal>> {
        val effectiveCookie = cookie ?: getSessionCookie()
        val soapBody = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
                  <bytModEducativo>$modEducativo</bytModEducativo>
                </getAllCalifFinalByAlumnos>
              </soap:Body>
            </soap:Envelope>
        """.trimIndent()

        return try {
            val response = apiService.getCalifFinal(cookie = effectiveCookie, soap = soapBody.toRequestBody(xmlMediaType))
            if (response.isSuccessful) {
                val xmlResponse = response.body()?.string() ?: ""
                val jsonMatch = Regex("<getAllCalifFinalByAlumnosResult>(.*?)</getAllCalifFinalByAlumnosResult>", RegexOption.DOT_MATCHES_ALL).find(xmlResponse)
                val jsonContent = jsonMatch?.groups?.get(1)?.value
                if (!jsonContent.isNullOrBlank()) {
                    Result.success(CalifFinal.fromJsonList(jsonContent))
                } else {
                    Result.failure(Exception("Calif Final vacío"))
                }
            } else {
                Result.failure(Exception("Error HTTP: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAlumnoLocal(): Flow<Alumno?> = alumnoDao.getAlumno()
    override suspend fun saveAlumnoLocal(alumno: Alumno) = alumnoDao.insertAlumno(alumno)

    override fun getCargaLocal(): Flow<List<Materia>> = materiaDao.getAllMaterias()
    override suspend fun saveCargaLocal(materias: List<Materia>) {
        materiaDao.deleteAll()
        materiaDao.insertAll(materias)
    }
    override suspend fun getLastCargaUpdate(): Long? = materiaDao.getLastUpdateTime()

    override fun getKardexLocal(): Flow<List<Kardex>> = kardexDao.getAllKardex()
    override suspend fun saveKardexLocal(kardex: List<Kardex>) {
        kardexDao.deleteAll()
        kardexDao.insertAll(kardex)
    }
    override suspend fun getLastKardexUpdate(): Long? = kardexDao.getLastUpdateTime()

    override fun getCalifUnidadesLocal(): Flow<List<CalifUnidad>> = califUnidadDao.getAllCalifUnidades()
    override suspend fun saveCalifUnidadesLocal(califUnidades: List<CalifUnidad>) {
        califUnidadDao.deleteAll()
        califUnidadDao.insertAll(califUnidades)
    }
    override suspend fun getLastCalifUnidadUpdate(): Long? = califUnidadDao.getLastUpdateTime()

    override fun getCalifFinalLocal(): Flow<List<CalifFinal>> = califFinalDao.getAllCalifFinal()
    override suspend fun saveCalifFinalLocal(califFinal: List<CalifFinal>) {
        califFinalDao.deleteAll()
        califFinalDao.insertAll(califFinal)
    }
    override suspend fun getLastCalifFinalUpdate(): Long? = califFinalDao.getLastUpdateTime()

    override suspend fun logout() {
        setSessionCookie(null)
        materiaDao.deleteAll()
        kardexDao.deleteAll()
        alumnoDao.deleteAlumno()
        califUnidadDao.deleteAll()
        califFinalDao.deleteAll()
    }
}
