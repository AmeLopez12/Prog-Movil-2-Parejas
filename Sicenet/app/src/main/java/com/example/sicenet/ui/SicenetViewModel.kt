package com.example.sicenet.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.repository.SicenetRepository
import kotlinx.coroutines.launch

class SicenetViewModel(private val repository: SicenetRepository) : ViewModel() {

    var loginState by mutableStateOf<LoginResult?>(null)
        private set

    var alumnoData by mutableStateOf<Alumno?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun login(matricula: String, contrasenia: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            val result = repository.login(matricula, contrasenia)
            isLoading = false
            
            if (result.isSuccess) {
                loginState = LoginResult.Success
                onSuccess()
            } else {
                loginState = LoginResult.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            val rawXml = repository.getProfile()
            if (rawXml != null) {
                val jsonContent = extractJsonFromXml(rawXml)
                if (jsonContent != null) {
                    alumnoData = Alumno.fromJson(jsonContent)
                }
            }
            isLoading = false
        }
    }

    private fun extractJsonFromXml(xml: String): String? {
        val startTag = "<getAlumnoAcademicoWithLineamientoResult>"
        val endTag = "</getAlumnoAcademicoWithLineamientoResult>"
        
        val startIndex = xml.indexOf(startTag)
        val endIndex = xml.indexOf(endTag)
        
        if (startIndex != -1 && endIndex != -1) {
            return xml.substring(startIndex + startTag.length, endIndex)
        }
        return null
    }

    fun logout() {
        repository.logout()
        loginState = null
        alumnoData = null
    }

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    companion object {
        fun provideFactory(repository: SicenetRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SicenetViewModel(repository) as T
            }
        }
    }
}
