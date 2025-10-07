package com.taskify.taskify_android.logic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.data.repository.AuthRepository
import com.taskify.taskify_android.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Representa l'estat de la UI en el procés de login
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val token: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState

    // ---------- LOGIN ----------
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is Resource.Success -> {
                    _authState.value = AuthUiState(
                        isSuccess = true,
                        token = result.data.token // LoginResponse té camp token
                    )
                }
                is Resource.Error -> {
                    _authState.value = AuthUiState(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }


    // ---------- LOGOUT ----------
    fun logout(token: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val success = repository.logout(token)
            if (success) {
                _authState.value = AuthUiState(isSuccess = true)
            } else {
                _authState.value = AuthUiState(
                    isLoading = false,
                    error = "Logout failed"
                )
            }
        }
    }

    // ---------- REGISTER ----------
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            // TODO: Replace when register endpoint is ready
            try {
                val response = repository.register(username, email, password)
                if (response != null) {
                    _authState.value = AuthUiState(isSuccess = true)
                } else {
                    _authState.value = AuthUiState(
                        isLoading = false,
                        error = "Register failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState(
                    isLoading = false,
                    error = "Error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun resetState() {
        _authState.value = AuthUiState()
    }
}