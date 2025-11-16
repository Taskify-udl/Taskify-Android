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
import com.taskify.taskify_android.data.models.entities.Role
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.models.entities.UserDraft
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
    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val profileState: StateFlow<Resource<User>> = _profileState

    // ---------- LOGIN ----------
    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            when (val result = repository.login(username, password)) {
                is Resource.Success -> {
                    val loginResponse = result.data
                    AuthPreferences.saveToken(context, loginResponse.token)

                    // 🔍 Obtenim el role
                    val roleString = loginResponse.user.role.uppercase()
                    val role = Role.valueOf(roleString)

                    val now = java.time.LocalDateTime.now()

                    // 🔧 Creem el tipus d'usuari segons el role
                    val user: User = when (role) {
                        Role.FREELANCER,
                        Role.COMPANY_ADMIN,
                        Role.COMPANY_WORKER,
                        Role.PROVIDER -> Provider(
                            id = loginResponse.user.id.toLong(),
                            fullName = loginResponse.user.username,
                            username = loginResponse.user.username,
                            email = loginResponse.user.email,
                            password = "",
                            phoneNumber = "",
                            profilePic = null,
                            role = role,
                            createdAt = now,
                            updatedAt = now,
                            address = "",
                            city = "",
                            country = "",
                            zipCode = "",
                            bio = "",
                            experienceYears = 0,
                            averageRating = 0.0,
                            isVerified = false,
                            services = emptyList()
                        )

                        Role.CUSTOMER -> Customer(
                            id = loginResponse.user.id.toLong(),
                            fullName = loginResponse.user.username,
                            username = loginResponse.user.username,
                            email = loginResponse.user.email,
                            password = "",
                            phoneNumber = "",
                            profilePic = null,
                            role = role,
                            createdAt = now,
                            updatedAt = now,
                            address = "",
                            city = "",
                            country = "",
                            zipCode = ""
                        )

                        Role.ADMIN -> {
                            // 🔒 De moment, no gestionem ADMIN
                            Log.w("AuthViewModel", "⚠️ Usuari ADMIN detectat, no gestionat.")
                            Customer(
                                id = loginResponse.user.id.toLong(),
                                fullName = loginResponse.user.username,
                                username = loginResponse.user.username,
                                email = loginResponse.user.email,
                                password = "",
                                phoneNumber = "",
                                profilePic = null,
                                role = role,
                                createdAt = now,
                                updatedAt = now,
                                address = "",
                                city = "",
                                country = "",
                                zipCode = ""
                            )
                        }
                    }

                    // 💾 Guardem l’usuari localment
                    saveLocalUser(user)

                    // ✅ Actualitzem estat d'autenticació
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
        userDraft: UserDraft,
        context: Context
    ) {
        viewModelScope.launch {
            _authState.value = AuthUiState(isLoading = true)

            val result = repository.register(userDraft, context)

            when (result) {
                is Resource.Success -> {
                    val registerResponse = result.data
                    AuthPreferences.saveToken(context, registerResponse.token)

                    // 🔍 Obtenim el role retornat pel backend
                    val roleString = registerResponse.user.role.uppercase()
                    val role = Role.valueOf(roleString)

                    val now = java.time.LocalDateTime.now()

                    // 🔧 Creem el tipus d'usuari segons el role
                    val user: User = when (role) {
                        Role.FREELANCER,
                        Role.COMPANY_ADMIN,
                        Role.COMPANY_WORKER,
                        Role.PROVIDER -> Provider(
                            id = registerResponse.user.id.toLong(),
                            fullName = registerResponse.user.username,
                            username = registerResponse.user.username,
                            email = registerResponse.user.email,
                            password = "",
                            phoneNumber = "",
                            profilePic = null,
                            role = role,
                            createdAt = now,
                            updatedAt = now,
                            address = "",
                            city = "",
                            country = "",
                            zipCode = "",
                            bio = "",
                            experienceYears = 0,
                            averageRating = 0.0,
                            isVerified = false,
                            services = emptyList()
                        )

                        Role.CUSTOMER -> Customer(
                            id = registerResponse.user.id.toLong(),
                            fullName = registerResponse.user.username,
                            username = registerResponse.user.username,
                            email = registerResponse.user.email,
                            password = "",
                            phoneNumber = "",
                            profilePic = null,
                            role = role,
                            createdAt = now,
                            updatedAt = now,
                            address = "",
                            city = "",
                            country = "",
                            zipCode = ""
                        )

                        Role.ADMIN -> {
                            Log.w(
                                "AuthViewModel",
                                "⚠️ Usuari ADMIN detectat al registre, no gestionat."
                            )
                            Customer(
                                id = registerResponse.user.id.toLong(),
                                fullName = registerResponse.user.username,
                                username = registerResponse.user.username,
                                email = registerResponse.user.email,
                                password = "",
                                phoneNumber = "",
                                profilePic = null,
                                role = role,
                                createdAt = now,
                                updatedAt = now,
                                address = "",
                                city = "",
                                country = "",
                                zipCode = ""
                            )
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
        val current = _currentUser.value

        if (current != null && current.id == user.id && current.username == user.username && current.role == user.role) {
            return
        }
        _currentUser.value = user
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            val result = repository.getProfile()
            if (result is Resource.Success) {
                // Convertim UserResponse a Provider/Customer segons el role
                val body = result.data
                val role = Role.valueOf(body.role.uppercase())
                val now = java.time.LocalDateTime.now()

                val user: User = when (role) {
                    Role.FREELANCER, Role.COMPANY_ADMIN, Role.COMPANY_WORKER, Role.PROVIDER -> Provider(
                        id = body.id.toLong(),
                        fullName = body.username,
                        username = body.username,
                        email = body.email,
                        password = "",
                        phoneNumber = "",
                        profilePic = null,
                        role = role,
                        createdAt = now,
                        updatedAt = now,
                        address = "",
                        city = "",
                        country = "",
                        zipCode = "",
                        bio = "",
                        experienceYears = 0,
                        averageRating = 0.0,
                        isVerified = false,
                        services = emptyList()
                    )

                    Role.CUSTOMER -> Customer(
                        id = body.id.toLong(),
                        fullName = body.username,
                        username = body.username,
                        email = body.email,
                        password = "",
                        phoneNumber = "",
                        profilePic = null,
                        role = role,
                        createdAt = now,
                        updatedAt = now,
                        address = "",
                        city = "",
                        country = "",
                        zipCode = ""
                    )

                    Role.ADMIN -> Customer(
                        id = body.id.toLong(),
                        fullName = body.username,
                        username = body.username,
                        email = body.email,
                        password = "",
                        phoneNumber = "",
                        profilePic = null,
                        role = role,
                        createdAt = now,
                        updatedAt = now,
                        address = "",
                        city = "",
                        country = "",
                        zipCode = ""
                    )
                }

                saveLocalUser(user)
                _profileState.value = Resource.Success(user)
            } else if (result is Resource.Error) {
                _profileState.value = Resource.Error(result.message)
            }
        }
    }


    fun updateProfile(
        context: Context,
        updates: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val token = AuthPreferences.getTokenBlocking(context) ?: return@launch

            when (val result = repository.updateProfile(context, updates)) {
                is Resource.Success -> {
                    saveLocalUser(result.data)
                    onSuccess()
                }

                is Resource.Error -> onError(result.message)
                else -> {}
            }
        }
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