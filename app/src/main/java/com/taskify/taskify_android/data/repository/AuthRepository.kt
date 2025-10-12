package com.taskify.taskify_android.data.repository

import android.content.Context
import android.util.Log
import com.taskify.taskify_android.data.models.auth.AuthPreferences
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.network.ApiService

class AuthRepository(private val api: ApiService) {
    // ---------- LOGIN ----------
    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            Log.d("AuthRepository", "Login response: ${response.code()} ${response.message()}")
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
    suspend fun register(firstName: String, lastName: String, username: String, email: String, password: String): RegisterResponse? {
        // TODO: implement when the endpoint becomes available
        val request = RegisterRequest(firstName, lastName, username, email, password)
        val response = api.register(request)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}
