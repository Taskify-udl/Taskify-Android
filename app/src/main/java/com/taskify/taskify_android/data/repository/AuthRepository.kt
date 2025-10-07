package com.taskify.taskify_android.data.repository

import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.network.ApiService

class AuthRepository(private val api: ApiService) {
    // ---------- LOGIN ----------
    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
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
    suspend fun logout(token: String): Boolean {
        val response = api.logout("Token $token")
        return response.isSuccessful
    }

    // ---------- REGISTER ----------
    suspend fun register(username: String, email: String, password: String): RegisterResponse? {
        // TODO: implement when the endpoint becomes available
        val request = RegisterRequest(username, email, password)
        val response = api.register(request)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}
