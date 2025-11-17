package com.taskify.taskify_android.data.repository

import android.content.Context
import android.util.Log
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.CreateServiceRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.User
import com.taskify.taskify_android.data.models.entities.UserDraft
import com.taskify.taskify_android.data.network.ApiService
import retrofit2.Response
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
            val request = RegisterRequest(
                fullname = userDraft.fullName,
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

    // ---------- CREATE SERVICE ----------
    suspend fun createService(
        title: String,
        category: String,
        description: String,
        price: Double,
        context: Context,
        providerId: Long
    ): Response<ProviderService> {
        val now = LocalDateTime.now().toString() + "Z"

        val body = CreateServiceRequest(
            name = title,
            description = description,
            provider = providerId,
            category = category,
            price = price,
            createdAt = now,
            updatedAt = now
        )
        Log.d("AuthRepository", "createService body: $body")

        return api.createService(body)
    }

}
