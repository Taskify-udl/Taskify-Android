package com.taskify.taskify_android.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.ContractResponse
import com.taskify.taskify_android.data.models.auth.CreateContractRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.models.auth.UpdateContractStatusRequest
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.auth.VerifyCodeRequest
import com.taskify.taskify_android.data.models.chat.ConversationResponse
import com.taskify.taskify_android.data.models.chat.CreateConversationRequest
import com.taskify.taskify_android.data.models.chat.MessagePaginationResponse
import com.taskify.taskify_android.data.models.chat.MessageResponse
import com.taskify.taskify_android.data.models.chat.SendMessageRequest
import com.taskify.taskify_android.data.models.entities.ContractStatus
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.time.LocalDateTime
import androidx.lifecycle.viewModelScope
import com.taskify.taskify_android.logic.viewmodels.AuthUiState
import kotlinx.coroutines.launch

class AuthRepository(private val api: ApiService) {
    // ---------- LOGIN ----------
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            Log.d("AuthRepository", "Login response: ${response.code()} ${response.message()}")
            Log.d("AuthRepository", "Login response body: ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Empty response from server")
                }
            } else {
                // response.code() HTTP code
                val errorMsg = when (response.code()) {
                    400 -> "Invalid credentials"
                    401 -> "Unauthorized"
                    else -> "Server error: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            // Network error, timeout, etc.
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ---------- LOGOUT ----------

    suspend fun logout(context: Context): Boolean {
        val token = AuthPreferences.getTokenBlocking(context)
        if (token.isNullOrEmpty()) return false

        val response = api.logout("Token $token")
        if (response.isSuccessful) {
            AuthPreferences.clearToken(context)
        }
        return response.isSuccessful
    }

    // ---------- REGISTER ----------
    suspend fun register(
        userDraft: UserDraft,
        context: Context
    ): Resource<RegisterResponse> {
        return try {
            // Split fullName en firstName i lastName
            val parts = userDraft.fullName.split(" ", limit = 2)
            val firstName = parts.getOrElse(0) { "" }
            val lastName = parts.getOrElse(1) { "" }

            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                username = userDraft.username,
                email = userDraft.email,
                password = userDraft.password,
                role = userDraft.role.toString()
            )
            Log.d("AuthRepository", "Register request: $request")
            val response = api.register(request)
            Log.d("AuthRepository", "Register response: ${response.code()} ${response.message()}")
            Log.d("AuthRepository", "Register response body: ${response.body()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AuthRepository", "Register body: $body")
                if (body != null) {
                    AuthPreferences.saveToken(context, body.token)
                    Resource.Success(body)
                } else {
                    Resource.Error("Empty response from server")
                }
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Invalid or duplicate data"
                    else -> "Server error: ${response.code()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ---------- GET PROFILE ----------
    suspend fun getProfile(): Resource<UserResponse> {
        return try {
            val response = api.getProfile() // interceptor ja envia el token
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty profile response")
            } else {
                Resource.Error("Failed to load profile: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ---------- UPDATE PROFILE ----------
    suspend fun updateProfile(
        updates: Map<String, Any?>
    ): Resource<UserResponse> {
        Log.d("AuthRepository", "updateProfile updates: $updates")
        return try {
            val response = api.updateProfile(updates)
            Log.d(
                "AuthRepository",
                "updateProfile response: ${response.code()} ${response.message()}"
            )

            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Empty update response")
            } else {
                Resource.Error("Error updating profile: ${response.code()}")
            }

        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // Funció Helper per crear RequestBody per a text simple (OK per a name, description, price...)
    private fun String.toTextRequestBody(): RequestBody {
        // Mantenim text/plain
        return this.toRequestBody(
            "text/plain".toMediaTypeOrNull() ?: throw IllegalStateException("Invalid Media Type")
        )
    }

    // 🚩 HELPER: Converteix una URI local en una Part de MultipartBody
    private fun Context.getMultipartImagePart(uri: Uri, fieldName: String): MultipartBody.Part? {
        val contentResolver = this.contentResolver

        // Intentem obtenir el tipus MIME i el nom del fitxer
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val filename = "service_image_${System.currentTimeMillis()}.jpg" // Nom de fitxer genèric

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            inputStream?.use { input ->
                val requestBody = input.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                // 🚨 IMPORTANT: "images" o "image"? Vam decidir "image" per a l'API anterior.
                // Si el teu backend espera "images", canvieu fieldName a "images".
                MultipartBody.Part.createFormData(fieldName, filename, requestBody)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error preparing image for upload: ${e.message}")
            null
        }
    }


    // ---------- CREATE SERVICE ----------
    suspend fun createService(
        title: String,
        categoryIds: List<Int>,
        description: String,
        price: Int,
        providerId: Long,
        imageUri: Uri?,
        context: Context
    ): Resource<ProviderService> {
        val now = LocalDateTime.now().toString() + "Z"

        val categoryIdString = categoryIds.firstOrNull()?.toString()
            ?: return Resource.Error("Category ID is missing.")

        val namePart = title.toTextRequestBody()
        val descriptionPart = description.toTextRequestBody()
        val providerPart = providerId.toString().toTextRequestBody()
        val categoriesPart = categoryIdString.toTextRequestBody()
        val pricePart = price.toString().toTextRequestBody()
        val createdAtPart = now.toTextRequestBody()
        val updatedAtPart = now.toTextRequestBody()


        val imagePart: MultipartBody.Part? = if (imageUri != null && imageUri.scheme == "content") {
            context.getMultipartImagePart(imageUri, "image")
        } else {
            null
        }

        Log.d("AuthRepository", "Image Part is null: ${imagePart == null}")

        return try {
            val response = api.createService(
                name = namePart,
                description = descriptionPart,
                provider = providerPart,
                categories = categoriesPart,
                price = pricePart,
                createdAt = createdAtPart,
                updatedAt = updatedAtPart,
                image = imagePart
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("AuthRepository", "Error creating service: ${response.code()} - $errorBody")
                Resource.Error("Error creating service: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ---------- GET PROVIDER SERVICES ----------
    suspend fun getServices(): Resource<List<ProviderService>> {
        return try {
            val response = api.getServices()

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load services: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateService(
        serviceId: Int,
        updates: Map<String, Any?>
    ): Resource<ProviderService> {
        Log.d("AuthRepository", "updateService request: Service ID $serviceId, updates: $updates")

        return try {
            // La crida a l'API requereix l'ID i el cos de la petició
            val response = api.updateService(serviceId, updates)

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Resource.Error("Error updating service: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ============================================================================================
    // ---------- CONTRACTS (BOOKINGS) ----------
    // ============================================================================================

    /**
     * Crea un nou contracte (reserva).
     * @param serviceId L'ID del servei a contractar.
     * @param startDate La data en format string (ex: "2026-01-11T14:30:00").
     * @param price El preu com a Double.
     */
    suspend fun createContract(
        serviceId: Int,
        startDate: String,
        startTime: String, // <--- NOU PARÀMETRE
        price: Double,
        description: String
    ): Resource<ContractResponse> {
        return try {
            val request = CreateContractRequest(
                service = serviceId,
                startDate = startDate,
                startTime = startTime, // <--- AFEGIT AL REQUEST
                price = price,
                description = description
            )
            Log.d("AuthRepository", "createContract request: $request")

            val response = api.createContract(request)

            Log.d(
                "AuthRepository",
                "createContract response: ${response.code()} ${response.message()}"
            )

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(
                    "AuthRepository",
                    "Error creating contract: $errorBody"
                ) // Log d'error millorat
                Resource.Error("Failed to create contract: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Obté la llista de contractes de l'usuari (provider o customer)
     * Endpoint: /api/contract/mine
     */
    suspend fun getMyContracts(): Resource<List<ContractResponse>> {
        return try {
            val response = api.getMyContracts()
            Log.d(
                "AuthRepository",
                "getMyContracts response: ${response.code()} ${response.message()}"
            )

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Resource.Error("Failed to load contracts: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Obté el detall d'un contracte específic per ID.
     */
    suspend fun getContractDetail(contractId: Int): Resource<ContractResponse> {
        return try {
            val response = api.getContractDetail(contractId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to load contract details: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    /**
     * Actualitza l'estat d'un contracte utilitzant l'Enum.
     */
    /**
     * Actualitza l'estat d'un contracte existent mitjançant PATCH.
     * Utilitza l'ID a la URL per evitar la creació de duplicats.
     */
    suspend fun updateContractStatus(
        contract: ContractResponse,
        newStatus: ContractStatus
    ): Resource<ContractResponse> {
        return try {
            // Creem l'objecte de petició basat en les dades actuals
            val request = UpdateContractStatusRequest(
                service = contract.serviceId,
                startDate = contract.startDate,
                startTime = contract.startTime ?: "12:00:00",
                price = contract.price?.toDoubleOrNull() ?: 0.0,
                description = contract.description ?: "",
                status = newStatus
            )

            Log.d(
                "AuthRepository",
                "Fent PATCH a ID: ${contract.id} per canviar a ${newStatus.apiValue}"
            )

            // 🚩 CLAU: Cridem a la ruta amb l'ID per actualitzar el registre
            val response = api.updateContractStatus(contract.id, request)

            if (response.isSuccessful && response.body() != null) {
                Log.d("AuthRepository", "Contracte ${contract.id} actualitzat amb èxit!")
                Resource.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("AuthRepository", "Error en l'actualització: $errorBody")
                Resource.Error("Error: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error de xarxa: ${e.localizedMessage}")
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun verifyServiceStep(
        contractId: Int,
        code: String,
        isStart: Boolean
    ): Resource<ContractResponse> {
        return try {
            val request = VerifyCodeRequest(code = code) // 🚩 Creem l'objecte correcte
            Log.d("AuthRepository", "Fent POST a ID: $contractId per verificar")

            val response = if (isStart) {
                api.startContract(contractId, request)
            } else {
                api.stopContract(contractId, request)
            }
            Log.d("AuthRepository", "Response: $response")


            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Invalid code"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    // Chats
    suspend fun getConversations(): Resource<List<ConversationResponse>> {
        return try {
            val response = api.getConversations()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) { Resource.Error(e.localizedMessage ?: "Unknown error") }
    }

    suspend fun getMessages(conversationId: Int): Resource<List<MessageResponse>> {
        return try {
            val response = api.getChatMessages(conversationId)
            if (response.isSuccessful && response.body() != null) {
                // Ara agafem el camp 'results' que gràcies al SerializedName llegirà 'messages' del JSON
                val messagesList = response.body()?.results ?: emptyList()
                Resource.Success(messagesList)
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun createConversation(participantId: Long): Resource<ConversationResponse> {
        return try {
            // Creem l'objecte de petició amb l'ID del participant
            val request = CreateConversationRequest(participantId)
            Log.d("AuthRepository", request.toString())
            val response = api.createConversation(request)
            Log.d("AuthRepository", response.toString())

            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error code: ${response.code()}"
                Resource.Error("Could not start conversation: $errorMsg")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun sendMessage(conversationId: Int, content: String): Resource<MessageResponse> {
        return try {
            val response = api.sendMessage(conversationId, SendMessageRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Failed to send message: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Connection lost")
        }
    }

    suspend fun markAsRead(conversationId: Int) {
        try {
            api.markAsRead(conversationId)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error marking as read: ${e.localizedMessage}")
        }
    }
}
