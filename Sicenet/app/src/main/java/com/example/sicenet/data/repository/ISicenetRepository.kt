package com.example.sicenet.data.repository

import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Login
import com.example.sicenet.data.model.Materia
import kotlinx.coroutines.flow.Flow

interface ISicenetRepository {
    // API (Remoto)
    suspend fun login(matricula: String, contrasenia: String): Result<Login>
    suspend fun getProfileRemote(cookie: String? = null): Result<Alumno>
    suspend fun getCargaAcademicaRemote(cookie: String? = null): Result<List<Materia>>
    
    // Cookie Management
    fun getSessionCookie(): String?
    fun setSessionCookie(cookie: String?)

    // DB (Local)
    fun getAlumnoLocal(): Flow<Alumno?>
    suspend fun saveAlumnoLocal(alumno: Alumno)
    
    fun getCargaLocal(): Flow<List<Materia>>
    suspend fun saveCargaLocal(materias: List<Materia>)
    suspend fun getLastCargaUpdate(): Long?

    fun logout()
}
