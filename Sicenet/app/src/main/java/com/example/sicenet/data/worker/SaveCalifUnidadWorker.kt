package com.example.sicenet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.model.CalifUnidad
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveCalifUnidadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val json = inputData.getString("calif_unidad_json") ?: return Result.failure()
        
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

        return try {
            val type = object : TypeToken<List<CalifUnidad>>() {}.type
            val data: List<CalifUnidad> = Gson().fromJson(json, type)
            repository.saveCalifUnidadesLocal(data)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
