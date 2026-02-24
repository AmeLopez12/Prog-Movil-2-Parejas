package com.example.sicenet.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.google.gson.Gson

class LoginWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val matricula = inputData.getString("matricula")
        val contrasenia = inputData.getString("contrasenia")

        if (matricula == null || contrasenia == null) {
            Log.e("SICENET_WORKER", "LoginWorker: Missing credentials")
            return Result.failure()
        }
        
        Log.d("SICENET_WORKER", "Iniciando LoginWorker")

        val apiService = SicenetApiService.create()
        val db = SicenetDatabase.getDatabase(applicationContext)
        val repository = SicenetRepository(
            applicationContext,
            apiService,
            db.alumnoDao(),
            db.materiaDao(),
            db.kardexDao(),
            db.califUnidadDao(),
            db.califFinalDao()
        )

        val loginResult = repository.login(matricula, contrasenia)

        if (loginResult.isSuccess && loginResult.getOrNull()?.acceso == true) {
            val profileResult = repository.getProfileRemote()
            return if (profileResult.isSuccess) {
                val alumnoJson = Gson().toJson(profileResult.getOrNull())
                val sessionCookie = repository.getSessionCookie()
                val outputData = workDataOf(
                    "alumno_json" to alumnoJson,
                    "session_cookie" to sessionCookie
                )
                Result.success(outputData)
            } else {
                Log.e("SICENET_WORKER", "LoginWorker: Failed to fetch profile: ${profileResult.exceptionOrNull()?.message}")
                Result.failure()
            }
        } else {
            val errorMessage = loginResult.getOrNull()?.mensaje ?: "Credenciales incorrectas"
            Log.e("SICENET_WORKER", "LoginWorker: Login failed: $errorMessage")
            val outputData = workDataOf("error_message" to errorMessage)
            return Result.failure(outputData)
        }
    }
}
