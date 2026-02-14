package com.example.sicenet.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.model.Materia
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveCargaWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cargaJson = inputData.getString("carga_json") ?: return Result.failure()
        
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
            val type = object : TypeToken<List<Materia>>() {}.type
            val materias: List<Materia> = Gson().fromJson(cargaJson, type)
            repository.saveCargaLocal(materias)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
