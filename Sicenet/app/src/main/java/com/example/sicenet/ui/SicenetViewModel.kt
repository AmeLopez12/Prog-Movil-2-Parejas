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
import com.example.sicenet.data.model.*
import com.example.sicenet.data.repository.ISicenetRepository
import com.example.sicenet.data.worker.*
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

    val kardexLocal: StateFlow<List<Kardex>> = repository.getKardexLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val califUnidadesLocal: StateFlow<List<CalifUnidad>> = repository.getCalifUnidadesLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val califFinalLocal: StateFlow<List<CalifFinal>> = repository.getCalifFinalLocal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var loginState by mutableStateOf<LoginResult?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var syncStatus by mutableStateOf<String?>(null)
        private set

    var lastUpdateCarga by mutableStateOf<String>("Nunca")
        private set
        
    var lastUpdateKardex by mutableStateOf<String>("Nunca")
        private set

    var lastUpdateCalifUnidades by mutableStateOf<String>("Nunca")
        private set
        
    var lastUpdateCalifFinal by mutableStateOf<String>("Nunca")
        private set

    init {
        updateLastUpdateTexts()
    }

    private fun updateLastUpdateTexts() {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            repository.getLastCargaUpdate()?.let { 
                if (it > 0) lastUpdateCarga = sdf.format(Date(it))
            }
            repository.getLastKardexUpdate()?.let {
                if (it > 0) lastUpdateKardex = sdf.format(Date(it))
            }
            repository.getLastCalifUnidadUpdate()?.let {
                if (it > 0) lastUpdateCalifUnidades = sdf.format(Date(it))
            }
            repository.getLastCalifFinalUpdate()?.let {
                if (it > 0) lastUpdateCalifFinal = sdf.format(Date(it))
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
        val cookie = repository.getSessionCookie() ?: return
        val inputData = workDataOf("session_cookie" to cookie)
        val fetchRequest = OneTimeWorkRequestBuilder<FetchCargaWorker>().setInputData(inputData).build()
        val saveRequest = OneTimeWorkRequestBuilder<SaveCargaWorker>().build()
        
        WorkManager.getInstance(getApplication()).beginUniqueWork("sync_carga", ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(saveRequest).enqueue()

        monitorWork(fetchRequest.id, "Carga")
    }

    fun syncKardex() {
        val cookie = repository.getSessionCookie() ?: return
        val alumno = alumnoLocal.value
        val lineamiento = alumno?.lineamiento ?: 1
        
        val inputData = workDataOf(
            "session_cookie" to cookie,
            "lineamiento" to lineamiento
        )
        
        val fetchRequest = OneTimeWorkRequestBuilder<FetchKardexWorker>().setInputData(inputData).build()
        val saveRequest = OneTimeWorkRequestBuilder<SaveKardexWorker>().build()
        
        WorkManager.getInstance(getApplication()).beginUniqueWork("sync_kardex", ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(saveRequest).enqueue()

        monitorWork(fetchRequest.id, "Kardex")
    }

    fun syncCalifUnidades() {
        val cookie = repository.getSessionCookie() ?: return
        val inputData = workDataOf("session_cookie" to cookie)
        
        val fetchRequest = OneTimeWorkRequestBuilder<FetchCalifUnidadWorker>().setInputData(inputData).build()
        val saveRequest = OneTimeWorkRequestBuilder<SaveCalifUnidadWorker>().build()
        
        WorkManager.getInstance(getApplication()).beginUniqueWork("sync_calif_unidades", ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(saveRequest).enqueue()

        monitorWork(fetchRequest.id, "Unidades")
    }
    
    fun syncCalifFinal() {
        val cookie = repository.getSessionCookie() ?: return
        val alumno = alumnoLocal.value
        val modEducativo = alumno?.modEducativo ?: 1
        
        val inputData = workDataOf(
            "session_cookie" to cookie,
            "modEducativo" to modEducativo
        )
        
        val fetchRequest = OneTimeWorkRequestBuilder<FetchCalifFinalWorker>().setInputData(inputData).build()
        val saveRequest = OneTimeWorkRequestBuilder<SaveCalifFinalWorker>().build()
        
        WorkManager.getInstance(getApplication()).beginUniqueWork("sync_calif_final", ExistingWorkPolicy.REPLACE, fetchRequest)
            .then(saveRequest).enqueue()

        monitorWork(fetchRequest.id, "Finales")
    }

    private fun monitorWork(id: UUID, tag: String) {
        WorkManager.getInstance(getApplication()).getWorkInfoByIdLiveData(id).observeForever { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> syncStatus = "Sincronizando $tag..."
                    WorkInfo.State.SUCCEEDED -> {
                        syncStatus = "$tag actualizado"
                        updateLastUpdateTexts()
                    }
                    WorkInfo.State.FAILED -> syncStatus = "Error sincronizando $tag"
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
