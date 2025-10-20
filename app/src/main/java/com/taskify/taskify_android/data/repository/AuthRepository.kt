package com.taskify.taskify_android.data.repository

import android.content.Context
import android.util.Log
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.CreateServiceRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.models.entities.OrderService
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.network.ApiService
import retrofit2.Response

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
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        context: Context
    ): Resource<RegisterResponse> {
        return try {
            val request = RegisterRequest(firstName, lastName, username, email, password)
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

    suspend fun createService(
        title: String,
        category: String,
        description: String,
        price: Double,
        context: Context,
        providerId: Long
    ): Response<ProviderService> {
        val now = java.time.LocalDateTime.now().toString() + "Z"

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
