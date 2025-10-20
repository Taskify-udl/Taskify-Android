package com.taskify.taskify_android.logic.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.OrderService
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.User
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
    val token: String? = null,
    val user: UserResponse? = null
)


class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser
    private val _createServiceState = MutableLiveData<Resource<ProviderService>>()
    val createServiceState: LiveData<Resource<ProviderService>> = _createServiceState


    // ---------- LOGIN ----------
    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            when (val result = repository.login(username, password)) {
                is Resource.Success -> {
                    AuthPreferences.saveToken(context, result.data.token)
                    _authState.value = AuthUiState(
                        isSuccess = true,
                        token = result.data.token // LoginResponse té camp token
                    )
                    Log.d("AuthViewModel", "Login successful. Token: ${result.data.token}")

                }

                is Resource.Error -> {
                    _authState.value = AuthUiState(
                        isLoading = false,
                        error = result.message
                    )
                }

                is Resource.Loading<*> -> {}
            }
        }
    }


    // ---------- LOGOUT ----------
    fun logout(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val success = repository.logout(context)
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
    fun register(
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        context: Context
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)
            val result =
                repository.register(firstName, lastName, username, email, password, context)
            _authState.value = when (result) {
                is Resource.Success -> AuthUiState(
                    user = result.data.user,
                    token = result.data.token
                )

                is Resource.Error -> AuthUiState(error = result.message)
                is Resource.Loading<*> -> TODO()
            }
        }
    }

    fun saveLocalUser(user: User) {
        _currentUser.value = user // _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    }

    fun createService(
        title: String,
        category: String,
        description: String,
        price: Double,
        context: Context,
        onSuccess: (ProviderService) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _createServiceState.value = Resource.Loading()
            try {
                val userId = currentUser.value?.id ?: authState.value.user?.id?.toLong() ?: 0L
                Log.d("AuthViewModel", "User ID: $userId")

                val response = repository.createService(
                    title = title,
                    category = category,
                    description = description,
                    price = price,
                    context = context,
                    providerId = userId
                )
                Log.d(
                    "AuthViewModel",
                    "CreateService response: ${response.code()} ${response.message()}"
                )

                if (response.isSuccessful && response.body() != null) {
                    val service = response.body()!!
                    _createServiceState.value = Resource.Success(service)
                    onSuccess(service)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val error = "Error creating service: ${response.code()} - $errorBody"
                    Log.e("AuthViewModel", error)
                    _createServiceState.value = Resource.Error(error)
                    onError(error)
                }
            } catch (e: Exception) {
                val error = "Network error: ${e.localizedMessage}"
                Log.e("AuthViewModel", error)
                _createServiceState.value = Resource.Error(error)
                onError(error)
            }
        }
    }


    fun resetState() {
        _authState.value = AuthUiState()
    }
}