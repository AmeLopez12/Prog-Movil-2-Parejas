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

class FetchCalifUnidadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cookie = inputData.getString("session_cookie")
        Log.d("SICENET_WORKER", "Iniciando FetchCalifUnidadWorker")

        val apiService = SicenetApiService.create()
        val db = SicenetDatabase.getDatabase(applicationContext)
        val repository = SicenetRepository(
            apiService, 
            db.alumnoDao(), 
            db.materiaDao(), 
            db.kardexDao(),
            db.califUnidadDao(),
            db.califFinalDao()
        )
        
        repository.setSessionCookie(cookie)

        val result = repository.getCalifUnidadesRemote(cookie)

        return if (result.isSuccess) {
            val json = Gson().toJson(result.getOrNull())
            val outputData = workDataOf("calif_unidad_json" to json)
            Result.success(outputData)
        } else {
            Log.e("SICENET_WORKER", "Error en FetchCalifUnidadWorker: ${result.exceptionOrNull()?.message}")
            Result.failure()
        }
    }
}
