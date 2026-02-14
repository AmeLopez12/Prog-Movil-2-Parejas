package com.example.sicenet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.model.Kardex
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveKardexWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val kardexJson = inputData.getString("kardex_json") ?: return Result.failure()
        
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

        return try {
            val type = object : TypeToken<List<Kardex>>() {}.type
            val kardex: List<Kardex> = Gson().fromJson(kardexJson, type)
            repository.saveKardexLocal(kardex)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
