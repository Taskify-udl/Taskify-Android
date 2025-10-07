package com.taskify.taskify_android.data.network

import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.LogoutResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/api-auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api-auth/logout/")
    suspend fun logout(@Header("Authorization") token: String): Response<LogoutResponse>

    @POST("api-auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    // TODO: Adapt according to the actual Django endpoint.

}