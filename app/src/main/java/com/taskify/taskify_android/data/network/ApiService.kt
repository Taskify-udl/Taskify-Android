package com.taskify.taskify_android.data.network

import com.taskify.taskify_android.data.models.auth.CreateServiceRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.LogoutResponse
import com.taskify.taskify_android.data.models.auth.RegisterRequest
import com.taskify.taskify_android.data.models.auth.RegisterResponse
import com.taskify.taskify_android.data.models.auth.UserResponse
import com.taskify.taskify_android.data.models.entities.ProviderService
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<LogoutResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Multipart
    @POST("api/service")
    suspend fun createService(
        // Tots els camps s'envien com a parts individuals
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("provider") provider: RequestBody,
        @Part("categories") categories: RequestBody,
        @Part("price") price: RequestBody,
        @Part("created_at") createdAt: RequestBody,
        @Part("updated_at") updatedAt: RequestBody
    ): Response<ProviderService>

    @GET("api/service")
    suspend fun getServices(): Response<List<ProviderService>>

    @GET("api/profile_detail")
    suspend fun getProfile(): Response<UserResponse>

    @PATCH("api/profile_detail")
    suspend fun updateProfile(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<UserResponse>

    @PATCH("api/service/{id}")
    suspend fun updateService(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ProviderService>
}