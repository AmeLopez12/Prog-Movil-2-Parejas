package com.example.sicenet.data.repository

import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Login
import kotlin.Result

interface ISicenetRepository {
    suspend fun login(matricula: String, contrasenia: String): Result<Login>

    suspend fun getProfile(): Result<Alumno>

    fun logout()
}