package com.taskify.taskify_android.logic.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.ServiceType
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.repository.AuthRepository
import com.taskify.taskify_android.data.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

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
    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val profileState: StateFlow<Resource<User>> = _profileState

    private val _serviceListState = MutableStateFlow<Resource<List<ProviderService>>>(Resource.Loading())
    val serviceListState: StateFlow<Resource<List<ProviderService>>> = _serviceListState

    // ---------- LOGIN ----------
    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            when (val result = repository.login(username, password)) {
                is Resource.Success -> {
                    val loginResponse = result.data
                    AuthPreferences.saveToken(context, loginResponse.token)

                    // 🔧 Simplificació: utilitzem toUser() que uneix firstName i lastName en fullName
                    val user = loginResponse.user.toUser()

                    saveLocalUser(user)

                    // FIX: Assegurem que isSuccess es posi a true
                    _authState.value = AuthUiState(
                        isSuccess = true,
                        token = loginResponse.token,
                        user = loginResponse.user
                    )
                    Log.d("AuthViewModel", "✅ Login successful. Token: ${loginResponse.token}")
                    Log.d("AuthViewModel", "✅ CurrentUser saved: $user")
                }

                is Resource.Error -> {
                    _authState.value = AuthUiState(
                        isLoading = false,
                        error = result.message
                    )
                    Log.e("AuthViewModel", "❌ Login error: ${result.message}")
                }

                is Resource.Loading<*> -> {}
            }
        }
    }


    // ---------- LOGOUT ----------
    fun logout(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            AuthPreferences.clearToken(context)

            _currentUser.value = null
            _profileState.value = Resource.Loading()

            _authState.value = AuthUiState(isSuccess = true)

            /*
            _authState.value = AuthUiState(isSuccess = true)
            val success = repository.logout(context)


            if (success) {
                AuthPreferences.clearToken(context)

                _currentUser.value = null
                _profileState.value = Resource.Loading()

                _authState.value = AuthUiState(isSuccess = true)

                Log.d("AuthViewModel", "✅ Logout complet: token i usuari esborrats")
            } else {
                _authState.value = AuthUiState(
                    isLoading = false,
                    error = "Logout failed"
                )
            }*/
        }
    }

    // ---------- REGISTER ----------
    fun register(
        userDraft: UserDraft,
        context: Context
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            when (val result = repository.register(userDraft, context)) {
                is Resource.Success -> {
                    val registerResponse = result.data
                    AuthPreferences.saveToken(context, registerResponse.token)

                    // 1. Creem l'usuari base a partir de la resposta de l'API
                    var user = registerResponse.user.toUser()

                    // FIX DEFINITIU: Sobrescrivim fullName amb el nom introduït originalment (userDraft.fullName)
                    // ja que l'API retorna nulls per a firstName/lastName a la resposta.
                    if (user.fullName.isNullOrEmpty()) {
                        user = when (user) {
                            is Provider -> user.copy(fullName = userDraft.fullName)
                            is Customer -> Customer(
                                id = user.id,
                                fullName = userDraft.fullName, // <-- Apliquem la sobrescriptura aquí
                                username = user.username,
                                email = user.email,
                                password = user.password,
                                phoneNumber = user.phoneNumber,
                                profilePic = user.profilePic,
                                role = user.role,
                                createdAt = user.createdAt,
                                updatedAt = user.updatedAt,
                                // Necessitem accedir als camps de Customer per la reconstrucció manual
                                address = user.address,
                            )

                            else -> user
                        }
                    }

                    // 💾 Guardem l'usuari localment
                    saveLocalUser(user)

                    // ✅ Actualitzem l’estat
                    _authState.value = AuthUiState(
                        isSuccess = true,
                        token = registerResponse.token,
                        user = registerResponse.user
                    )

                    Log.d(
                        "AuthViewModel",
                        "✅ Register successful. Token: ${registerResponse.token}"
                    )
                    Log.d("AuthViewModel", "✅ CurrentUser saved: $user")
                    Log.d("AuthViewModel", "Token: ${_authState.value.token}")
                }

                is Resource.Error -> {
                    _authState.value = AuthUiState(
                        isLoading = false,
                        error = result.message
                    )
                    Log.e("AuthViewModel", "❌ Register error: ${result.message}")
                }

                is Resource.Loading<*> -> {
                    _authState.value = AuthUiState(isLoading = true)
                }
            }
            Log.d("AuthViewModel", "User: ${_authState.value.user}")
            Log.d("AuthViewModel", "Token: ${_authState.value.token}")
        }
    }

    // ---------- SAVE USER LOCALLY ----------
    fun saveLocalUser(user: User) {
        _currentUser.value = user
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            val result = repository.getProfile()
            Log.d("AuthViewModel", "loadProfile result: $result")
            if (result is Resource.Success) {
                val user = result.data.toUser()

                saveLocalUser(user)
                _profileState.value = Resource.Success(user)
            } else if (result is Resource.Error) {
                _profileState.value = Resource.Error(result.message)
            }
        }
    }

    fun updateProfile(
        updates: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = repository.updateProfile(updates)) {
                is Resource.Success -> {
                    val mapped = result.data.toUser()
                    saveLocalUser(mapped)
                    Log.d("AuthViewModel", "Profile updated successfully")
                    _profileState.value = Resource.Success(mapped)
                    onSuccess()
                }

                is Resource.Error -> {
                    Log.e("AuthViewModel", "Error updating profile: ${result.message}")
                    onError(result.message)
                }

                else -> {}
            }
        }
    }

    fun createService(
        title: String,
        category: ServiceType,
        description: String,
        price: Int,
        onSuccess: (ProviderService) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _currentUser.value
        if (current !is Provider) {
            onError("Current user is not a provider")
            return
        }

        viewModelScope.launch {
            val providerId = current.id

            // Cridem al repositori amb el nom de l'enum com a categoria (String)
            when (val result = repository.createService(
                title = title,
                category = category.name, // Enviem l'string del ServiceType (p. ex., "PLUMBING")
                description = description,
                price = price,
                providerId = providerId
            )) {
                is Resource.Success -> {
                    val service = result.data

                    // 🎯 1. Actualitzar la llista local de serveis
                    val updatedServices = current.services + service

                    // 🎯 2. Actualitzar el provider local
                    val updatedProvider = current.copy(services = updatedServices)

                    _currentUser.value = updatedProvider
                    _profileState.value = Resource.Success(updatedProvider)

                    // 🎯 3. Retornar el servei creat
                    onSuccess(service)
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {
                    // Es podria afegir gestió de loading si es vol
                }
            }
        }
    }

    fun loadProviderServices() {
        viewModelScope.launch {
            val currentProvider = _currentUser.value as? Provider ?: return@launch
            _serviceListState.value = Resource.Loading()

            when (val result = repository.getServices()) {
                is Resource.Success -> {
                    val allServices = result.data
                    val currentProviderId = currentProvider.id
                    Log.d("AuthViweModel", "Service list: $allServices")

                    // 1. FILTRAT: Quedar-se només amb els serveis del provider actual
                    val filteredServices = allServices.filter { service ->
                        Log.d("AuthViewModel", "Service provider ID: ${service.providerId}")
                        service.providerId == currentProviderId
                    }

                    // 2. Actualitzar l'objecte Provider de l'estat _currentUser
                    val updatedProvider = currentProvider.copy(services = filteredServices)
                    saveLocalUser(updatedProvider) // Manté el Provider a _currentUser actualitzat

                    _serviceListState.value = Resource.Success(filteredServices)
                }
                is Resource.Error -> {
                    _serviceListState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> { /* Handled above */ }
            }
        }
    }

    fun getServices() {
        viewModelScope.launch {
            _serviceListState.value = Resource.Loading()

            when (val result = repository.getServices()) {
                is Resource.Success -> {
                    _serviceListState.value = Resource.Success(result.data)
                }
                is Resource.Error -> {
                    _serviceListState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> { /* Handled above */ }
            }
        }
    }

    fun updateService(
        serviceToUpdate: ProviderService,
        newTitle: String,
        newCategory: ServiceType,
        newDescription: String,
        newPrice: Int,
        onSuccess: (ProviderService) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _currentUser.value
        if (current !is Provider) {
            onError("Current user is not a provider")
            return
        }

        val updatedService = serviceToUpdate.copy(
            name = newTitle,
            category = newCategory,
            description = newDescription,
            price = newPrice,
            updatedAt = LocalDateTime.now()
        )

        // Actualitzar la llista de serveis
        val updatedServices = current.services.map {
            if (it.id == serviceToUpdate.id) updatedService else it
        }

        // Actualitzar el provider amb la nova llista
        val updatedProvider = current.copy(services = updatedServices)
        _currentUser.value = updatedProvider
        _profileState.value = Resource.Success(updatedProvider)

        onSuccess(updatedService)
    }

    fun resetState() {
        _authState.value = AuthUiState()
    }

}