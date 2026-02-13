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

class FetchCargaWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cookie = inputData.getString("session_cookie")
        Log.d("SICENET_WORKER", "Iniciando FetchCargaWorker con cookie: $cookie")

        val apiService = SicenetApiService.create()
        val db = SicenetDatabase.getDatabase(applicationContext)
        val repository = SicenetRepository(apiService, db.alumnoDao(), db.materiaDao())
        
        // Inyectar la cookie recibida
        repository.setSessionCookie(cookie)

        val result = repository.getCargaAcademicaRemote(cookie)

        return if (result.isSuccess) {
            val materias = result.getOrNull()
            Log.d("SICENET_WORKER", "Fetch exitoso. Materias obtenidas: ${materias?.size}")
            
            val jsonCarga = Gson().toJson(materias)
            val outputData = workDataOf("carga_json" to jsonCarga)
            Result.success(outputData)
        } else {
            val error = result.exceptionOrNull()?.message
            Log.e("SICENET_WORKER", "Error en FetchCargaWorker: $error")
            Result.failure()
        }
    }
}
