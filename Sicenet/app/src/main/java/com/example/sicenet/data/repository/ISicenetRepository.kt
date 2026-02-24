package com.example.sicenet.data.repository

import com.example.sicenet.data.model.*
import kotlinx.coroutines.flow.Flow

interface ISicenetRepository {
    suspend fun login(matricula: String, contrasenia: String): Result<Login>
    suspend fun getProfileRemote(cookie: String? = null): Result<Alumno>
    suspend fun getCargaAcademicaRemote(cookie: String? = null): Result<List<Materia>>
    suspend fun getKardexRemote(cookie: String? = null, lineamiento: Int): Result<List<Kardex>>
    suspend fun getCalifUnidadesRemote(cookie: String? = null): Result<List<CalifUnidad>>
    suspend fun getCalifFinalRemote(cookie: String? = null, modEducativo: Int): Result<List<CalifFinal>>
    
    // Manejo de cookies
    fun getSessionCookie(): String?
    fun setSessionCookie(cookie: String?)

    // DB
    fun getAlumnoLocal(): Flow<Alumno?>
    suspend fun saveAlumnoLocal(alumno: Alumno)
    
    fun getCargaLocal(): Flow<List<Materia>>
    suspend fun saveCargaLocal(materias: List<Materia>)
    suspend fun getLastCargaUpdate(): Long?

    fun getKardexLocal(): Flow<List<Kardex>>
    suspend fun saveKardexLocal(kardex: List<Kardex>)
    suspend fun getLastKardexUpdate(): Long?

    fun getCalifUnidadesLocal(): Flow<List<CalifUnidad>>
    suspend fun saveCalifUnidadesLocal(califUnidades: List<CalifUnidad>)
    suspend fun getLastCalifUnidadUpdate(): Long?

    fun getCalifFinalLocal(): Flow<List<CalifFinal>>
    suspend fun saveCalifFinalLocal(califFinal: List<CalifFinal>)
    suspend fun getLastCalifFinalUpdate(): Long?

    fun logout()
}
