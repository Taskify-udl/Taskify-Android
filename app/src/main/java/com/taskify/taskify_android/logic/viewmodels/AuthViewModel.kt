package com.taskify.taskify_android.logic.viewmodels

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.ContractResponse
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.ContractStatus
import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.ServiceType
import com.taskify.taskify_android.data.models.entities.ServiceTypeLookup
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.repository.AuthRepository
import com.taskify.taskify_android.data.repository.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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

    // NOU ESTAT PER ALS CONTRACTES
    private val _contractsState = MutableStateFlow<Resource<List<ContractResponse>>>(Resource.Loading())
    val contractsState: StateFlow<Resource<List<ContractResponse>>> = _contractsState
    // Inicialment pot ser Loading o un estat buit si tinguessis Resource.Idle
    private val _contractDetailState = MutableStateFlow<Resource<ContractResponse>>(Resource.Loading())
    val contractDetailState: StateFlow<Resource<ContractResponse>> = _contractDetailState

    private val _serviceListState =
        MutableStateFlow<Resource<List<ProviderService>>>(Resource.Loading())
    val serviceListState: StateFlow<Resource<List<ProviderService>>> = _serviceListState

    private var pollingJob: kotlinx.coroutines.Job? = null

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

            // 1. Aturem el Polling 🚩
            stopFastPolling()

            // 2. Netegem el token del disc
            AuthPreferences.clearToken(context)

            // 3. Netegem els estats reactius
            _currentUser.value = null
            _profileState.value = Resource.Loading()
            _contractsState.value = Resource.Loading()

            // 4. IMPORTANT: Reiniciem l'estat de la UI perquè no hi hagi "isSuccess = true" residual
            _authState.value = AuthUiState(isSuccess = false) // 🚩 Ho posem a false aquí

            Log.d("AuthViewModel", "✅ Logout completat i estats reiniciats")
        }
    }

    fun stopFastPolling() {
        pollingJob?.cancel() // Atura el bucle immediatament
        pollingJob = null
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

    // Token
    /**
     * Obté el token emmagatzemat a AuthPreferences.
     * Útil per a la lògica de l'InitScreen (Splash Screen).
     */
    fun getToken(context: Context): String? {
        return AuthPreferences.getToken(context)
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
        imageUri: Uri?,
        context: Context,
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

            // FIX 1: Trobar l'ID de la categoria a partir de l'enum
            Log.d("AuthViewModel", "Category name: ${category.name}")
            val categoryId = ServiceTypeLookup.enumToId[category.name]

            if (categoryId == null) {
                onError("Invalid category selected: ID not found for ${category.name}")
                return@launch
            }

            // Cridem al repositori amb el nom de l'enum com a categoria (String)
            viewModelScope.launch {
                when (val result = repository.createService(
                    title = title,
                    categoryIds = listOf(categoryId),
                    description = description,
                    price = price,
                    providerId = providerId,
                    imageUri = imageUri, // 🚩 PASSANT URI
                    context = context // 🚩 PASSANT CONTEXT
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
                    saveLocalUser(updatedProvider)

                    _serviceListState.value = Resource.Success(filteredServices)
                }

                is Resource.Error -> {
                    _serviceListState.value = Resource.Error(result.message)
                }

                is Resource.Loading -> { /* Handled above */
                }
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

                is Resource.Loading -> { /* Handled above */
                }
            }
        }
    }

    fun updateService(
        serviceToUpdate: ProviderService,
        newTitle: String,
        newCategory: ServiceType,
        newDescription: String,
        newPrice: Int,
        newImageUri: Uri?, // 🚩 AFEGIT
        onSuccess: (ProviderService) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _currentUser.value
        if (current !is Provider) {
            onError("Current user is not a provider")
            return
        }

        // FIX 1: Trobem l'ID de la nova categoria a partir de l'enum
        val categoryId = ServiceTypeLookup.enumToId[newCategory.name]

        if (categoryId == null) {
            onError("Invalid category selected: ID not found.")
            return
        }

        // FIX 2: Creació del cos de la petició (Map amb snake_case/camps API)
        val updates = mutableMapOf<String, Any?>(
            "name" to newTitle,
            "description" to newDescription,
            "price" to newPrice,
            // Enviar la categoria com a llista d'IDs, que és el format JSON esperat
            "categories" to listOf(categoryId)
        )

        viewModelScope.launch {
            // La crida al repositori passa l'ID del servei i el cos de la petició
            when (val result = repository.updateService(serviceToUpdate.id, updates)) {
                is Resource.Success -> {
                    val updatedServiceFromApi = result.data

                    // FIX 3: Actualitzar la llista local amb el servei confirmat per l'API
                    val updatedServices = current.services.map {
                        if (it.id == updatedServiceFromApi.id) updatedServiceFromApi else it
                    }

                    // Actualitzar el provider amb la nova llista i l'estat local
                    val updatedProvider = current.copy(services = updatedServices)
                    _currentUser.value = updatedProvider
                    _profileState.value = Resource.Success(updatedProvider)

                    onSuccess(updatedServiceFromApi)
                }

                is Resource.Error -> {
                    onError(result.message)
                }

                is Resource.Loading -> {
                    // Es podria afegir gestió de loading
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthUiState()
    }

    // ============================================================================================
    // ---------- CONTRACTS LOGIC ----------
    // ============================================================================================

    /**
     * Carrega la llista de contractes de l'usuari actual.
     */
    fun getMyContracts(context: Context? = null) {
        viewModelScope.launch {
            // Nota: No posem Resource.Loading() si és polling per no fer pampallugues a la UI
            val result = repository.getMyContracts()

            if (result is Resource.Success && context != null) {
                val contracts = result.data
                val sharedPrefs = context.getSharedPreferences("taskify_prefs", Context.MODE_PRIVATE)

                contracts.forEach { contract ->
                    val lastStatus = sharedPrefs.getString("contract_status_${contract.id}", null)

                    // 🔍 LOG DE CONTROL
                    Log.d("NotificationCheck", "ID: ${contract.id} | Old: $lastStatus | New: ${contract.status.name}")

                    // 1. Si lastStatus és null -> És un contracte nou (notifiquem petició)
                    // 2. Si lastStatus != actual -> Ha canviat d'estat (notifiquem actualització)
                    if (lastStatus == null) {
                        Log.d("NotificationCheck", "🚩 NOU CONTRACTE DETECTAT!")
                        triggerManualNotification(context, contract, isNew = true)
                        // Guardem l'estat immediatament per no repetir la notificació de "nou"
                        sharedPrefs.edit().putString("contract_status_${contract.id}", contract.status.name).apply()
                    }
                    else if (lastStatus != contract.status.name) {
                        Log.d("NotificationCheck", "🚩 CANVI D'ESTAT DETECTAT!")
                        triggerManualNotification(context, contract, isNew = false)
                        // Guardem l'estat immediatament per no repetir la notificació de "canvi"
                        sharedPrefs.edit().putString("contract_status_${contract.id}", contract.status.name).apply()
                    }
                }
            }
            _contractsState.value = result
        }
    }

    // Funció helper per mostrar la notificació des del ViewModel amb títols dinàmics
    private fun triggerManualNotification(context: Context, contract: ContractResponse, isNew: Boolean) {
        val channelId = "contract_updates"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Contract Updates", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for contract status changes"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Títol dinàmic segons si és nou o canvi
        val title = if (isNew) {
            "New Service Request! 📩"
        } else {
            when (contract.status) {
                ContractStatus.ACCEPTED -> "Booking Accepted! ✅"
                ContractStatus.REJECTED -> "Booking Rejected 🚫"
                ContractStatus.CANCELLED -> "Booking Cancelled ❌"
                ContractStatus.ACTIVE -> "Service Started! 🚀"
                ContractStatus.FINISHED -> "Service Finished! 🏁"
                else -> "Contract Update"
            }
        }

        val message = "Your service '${contract.serviceName}' is now ${contract.status.name.lowercase()}."

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        // Use timestamps as ID to allow multiple different notifications
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Crea una nova sol·licitud de contracte.
     * @param serviceId L'ID del servei.
     * @param date Data seleccionada (LocalDate).
     * @param time Hora seleccionada (LocalTime).
     * @param price Preu del servei (Double o String que convertirem).
     * @param description Notes addicionals (opcional, encara que l'API actual no ho suporta, ho preparem).
     */
    fun createContract(
        serviceId: Int,
        date: java.time.LocalDate,
        time: java.time.LocalTime,
        price: Double,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            // 1. Formatar DATA: "YYYY-MM-DD" (Sense hora)
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 2. Formatar HORA: "HH:mm:ss" (o "HH:mm")
            // Utilitzem un patró per assegurar-nos que Django ho entén bé
            val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

            Log.d("AuthViewModel", "Sending Contract -> Date: $dateString, Time: $timeString")

            // 3. Cridar al repositori amb els camps separats
            when (val result = repository.createContract(serviceId, dateString, timeString, price, description)) {
                is Resource.Success -> {
                    getMyContracts()
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                else -> {}
            }
        }
    }

    /**
     * Carrega els detalls d'un contracte específic per ID.
     * S'utilitzarà a la pantalla de "Order Details" (Booking Details).
     */
    fun getContractDetail(contractId: Int) {
        viewModelScope.launch {
            // 1. Posem l'estat en Loading perquè la UI mostri l'spinner
            _contractDetailState.value = Resource.Loading()

            // 2. Cridem al repositori
            val result = repository.getContractDetail(contractId)

            // 3. Actualitzem l'estat amb el resultat (Success o Error)
            _contractDetailState.value = result
        }
    }

    /**
     * Canvia l'estat d'un contracte i refresca la llista
     */
    fun updateContractStatus(contract: ContractResponse, newStatus: ContractStatus) {
        viewModelScope.launch {
            val result = repository.updateContractStatus(contract, newStatus)
            if (result is Resource.Success) {
                delay(500)
                getMyContracts()
            }
        }
    }

    fun verifyContractCode(contractId: Int, code: String, isStart: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyServiceStep(contractId, code, isStart)
            if (result is Resource.Success) {
                getMyContracts() // Refresca la llista per canviar d'Accepted a Active o Finished
                onSuccess()
            } else if (result is Resource.Error) {
                onError(result.message)
            }
        }
    }

    /**
     * Opcional: Neteja l'estat del detall quan surts de la pantalla
     * per evitar veure dades antigues en obrir un altre contracte.
     */
    fun clearContractDetail() {
        _contractDetailState.value = Resource.Loading()
    }

    /**
     * Registra o refresca el Worker de notificacions.
     * Es pot cridar des del MainActivity un cop l'usuari ha fet login.
     */
    fun startNotificationWorker(context: Context) {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.taskify.taskify_android.logic.background.ContractUpdateWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        ).build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "ContractWatcher",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun startFastPolling(context: Context) {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (true) {
                Log.d("AuthViewModel", "🔄 Fast Polling: Checking and Notifying...")
                getMyContracts(context)
                delay(60000)
            }
        }
    }

    /**
     * Funció que cridarem des del InitScreen o MainActivity
     */
    fun checkAndLoadSession(context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val token = AuthPreferences.getToken(context)
            if (!token.isNullOrBlank()) {
                // Intentem carregar el perfil real del servidor per validar el token
                _profileState.value = Resource.Loading()
                val result = repository.getProfile()

                if (result is Resource.Success) {
                    val user = result.data.toUser()
                    saveLocalUser(user)
                    _profileState.value = Resource.Success(user)
                    onSuccess() // Sessió vàlida, anem a Home
                } else {
                    // Token caducat o error de xarxa
                    AuthPreferences.clearToken(context)
                    onError() // Cap a AuthScreen
                }
            } else {
                onError() // No hi ha token, cap a AuthScreen
            }
        }
    }
}