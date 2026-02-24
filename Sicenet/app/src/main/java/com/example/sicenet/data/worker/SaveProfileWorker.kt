package com.example.sicenet.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenet.data.local.SicenetDatabase
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.network.SicenetApiService
import com.example.sicenet.data.repository.SicenetRepository
import com.google.gson.Gson

class SaveProfileWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val alumnoJson = inputData.getString("alumno_json")
        val sessionCookie = inputData.getString("session_cookie")

        if (alumnoJson == null || sessionCookie == null) {
            Log.e("SICENET_WORKER", "SaveProfileWorker: Missing input data")
            return Result.failure()
        }

        Log.d("SICENET_WORKER", "Iniciando SaveProfileWorker")

        val alumno = Gson().fromJson(alumnoJson, Alumno::class.java)
        
        val db = SicenetDatabase.getDatabase(applicationContext)
        val apiService = SicenetApiService.create() // No se usa, pero el repo lo necesita
        val repository = SicenetRepository(
            applicationContext,
            apiService, 
            db.alumnoDao(),
            db.materiaDao(),
            db.kardexDao(),
            db.califUnidadDao(),
            db.califFinalDao()
        )

        repository.setSessionCookie(sessionCookie)
        repository.saveAlumnoLocal(alumno)

        return Result.success()
    }
}
