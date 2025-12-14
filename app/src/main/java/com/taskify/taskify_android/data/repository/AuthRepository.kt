package com.taskify.taskify_android.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.CreateServiceRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime

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
        return this.toRequestBody("text/plain".toMediaTypeOrNull() ?: throw IllegalStateException("Invalid Media Type"))
    }

    // ---------- CREATE SERVICE ----------
    suspend fun createService(
        title: String,
        categoryIds: List<Int>,
        description: String,
        price: Int,
        providerId: Long
    ): Resource<ProviderService> {
        val now = LocalDateTime.now().toString() + "Z"

        // 1. Preparem la ID de la categoria com a STRING SIMPLE (sense [ ] i sense Gson)
        // Agafem la primera (i esperem que sigui l'única) ID
        val categoryIdString = categoryIds.firstOrNull()?.toString()
            ?: return Resource.Error("Category ID is missing.")


        // 2. Construïm RequestBody per a cada camp:
        val namePart = title.toTextRequestBody()
        val descriptionPart = description.toTextRequestBody()
        val providerPart = providerId.toString().toTextRequestBody()

        // 🚩 FIX CLAU: Enviem la ID com a String simple (e.g., "39"), no com a JSON "[39]"
        val categoriesPart = categoryIdString.toTextRequestBody()

        val pricePart = price.toString().toTextRequestBody()
        val createdAtPart = now.toTextRequestBody()
        val updatedAtPart = now.toTextRequestBody()

        Log.d("AuthRepository", "Creating Multipart request body (categories ID as String): $categoryIdString")

        return try {
            // ... (la resta de la crida a api.createService)
            val response = api.createService(
                name = namePart,
                description = descriptionPart,
                provider = providerPart,
                categories = categoriesPart,
                price = pricePart,
                createdAt = createdAtPart,
                updatedAt = updatedAtPart
            )
            // ... (resta de la gestió d'errors)
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
}
