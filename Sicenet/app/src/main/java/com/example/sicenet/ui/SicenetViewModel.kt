package com.example.sicenet.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sicenet.data.model.Alumno
import com.example.sicenet.data.repository.ISicenetRepository
import kotlinx.coroutines.launch

class SicenetViewModel(private val repository: ISicenetRepository) : ViewModel() {

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

            //que login sea true
            result.onSuccess { loginObj ->
                if (loginObj.acceso) {
                    loginState = LoginResult.Success
                    onSuccess()
                } else {
                    loginState = LoginResult.Error(loginObj.mensaje)
                }
            }.onFailure { error ->
                loginState = LoginResult.Error(error.message ?: "Error desconocido")
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            val result = repository.getProfile()

            result.onSuccess { alumno ->
                alumnoData = alumno
            }.onFailure {
                alumnoData = null
            }

            isLoading = false
        }
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
        fun provideFactory(repository: ISicenetRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SicenetViewModel(repository) as T
            }
        }
    }
}