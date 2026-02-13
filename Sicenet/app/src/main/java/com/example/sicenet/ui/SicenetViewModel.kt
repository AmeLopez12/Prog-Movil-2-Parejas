package com.example.sicenet.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.model.Materia
import com.example.sicenet.data.repository.ISicenetRepository
import com.example.sicenet.data.worker.FetchCargaWorker
import com.example.sicenet.data.worker.SaveCargaWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SicenetViewModel(
    application: Application,
    private val repository: ISicenetRepository
) : AndroidViewModel(application) {

    val alumnoLocal: StateFlow<Alumno?> = repository.getAlumnoLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cargaLocal: StateFlow<List<Materia>> = repository.getCargaLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var loginState by mutableStateOf<LoginResult?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var syncStatus by mutableStateOf<String?>(null)
        private set

    var lastUpdateCarga by mutableStateOf<String>("Nunca")
        private set

    init {
        updateLastUpdateText()
    }

    private fun updateLastUpdateText() {
        viewModelScope.launch {
            val last = repository.getLastCargaUpdate()
            if (last != null && last > 0) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                lastUpdateCarga = sdf.format(Date(last))
            }
        }
    }

    fun login(matricula: String, contrasenia: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.login(matricula, contrasenia)
            isLoading = false
            
            if (result.isSuccess && result.getOrNull()?.acceso == true) {
                loginState = LoginResult.Success
                syncProfile()
                onSuccess()
            } else {
                loginState = LoginResult.Error(result.getOrNull()?.mensaje ?: "Credenciales incorrectas")
            }
        }
    }

    private fun syncProfile() {
        viewModelScope.launch {
            val result = repository.getProfileRemote()
            if (result.isSuccess) {
                result.getOrNull()?.let { repository.saveAlumnoLocal(it) }
            }
        }
    }

    fun syncCargaAcademica() {
        val cookie = repository.getSessionCookie()
        Log.d("SICENET_VM", "Sincronizando carga con cookie: $cookie")
        
        if (cookie == null) {
            syncStatus = "Error: Sesión no válida"
            return
        }

        val workManager = WorkManager.getInstance(getApplication())
        
        val inputData = workDataOf("session_cookie" to cookie)
        
        val fetchRequest = OneTimeWorkRequestBuilder<FetchCargaWorker>()
            .setInputData(inputData)
            .build()
            
        val saveRequest = OneTimeWorkRequestBuilder<SaveCargaWorker>().build()

        workManager.beginUniqueWork(
            "sync_carga",
            ExistingWorkPolicy.REPLACE,
            fetchRequest
        ).then(saveRequest).enqueue()

        workManager.getWorkInfoByIdLiveData(fetchRequest.id).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> syncStatus = "Sincronizando..."
                    WorkInfo.State.SUCCEEDED -> {
                        syncStatus = "Carga actualizada"
                        updateLastUpdateText()
                    }
                    WorkInfo.State.FAILED -> {
                        syncStatus = "Error en sincronización"
                        Log.e("SICENET_VM", "FetchCargaWorker falló")
                    }
                    else -> {}
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        loginState = null
    }

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    companion object {
        fun provideFactory(application: Application, repository: ISicenetRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SicenetViewModel(application, repository) as T
            }
        }
    }
}
