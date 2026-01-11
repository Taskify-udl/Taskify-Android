package com.taskify.taskify_android.data.network

import com.taskify.taskify_android.data.models.auth.ContractResponse
import com.taskify.taskify_android.data.models.auth.CreateContractRequest
import com.taskify.taskify_android.data.models.auth.LoginRequest
import com.taskify.taskify_android.data.models.auth.LoginResponse
import com.taskify.taskify_android.data.models.auth.LogoutResponse
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
import com.taskify.taskify_android.data.models.entities.ProviderService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    // Authentication
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<LogoutResponse>

    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // Profile
    @GET("api/profile_detail")
    suspend fun getProfile(): Response<UserResponse>

    @PATCH("api/profile_detail")
    suspend fun updateProfile(
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<UserResponse>

    // Services
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
        @Part("updated_at") updatedAt: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Response<ProviderService>

    @GET("api/service")
    suspend fun getServices(): Response<List<ProviderService>>

    @PATCH("api/service/{id}")
    suspend fun updateService(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ProviderService>

    // Contracts
    @GET("api/contract/mine")
    suspend fun getMyContracts(): Response<List<ContractResponse>>

    @POST("api/contract")
    suspend fun createContract(@Body request: CreateContractRequest): Response<ContractResponse>

    @GET("api/contract/{id}")
    suspend fun getContractDetail(@Path("id") id: Int): Response<ContractResponse>

    @PATCH("api/contract/{id}")
    suspend fun updateContractStatus(
        @Path("id") id: Int,
        @Body request: UpdateContractStatusRequest
    ): Response<ContractResponse>

    @POST("api/contract/{id}/start")
    suspend fun startContract(
        @Path("id") id: Int,
        @Body request: VerifyCodeRequest
    ): Response<ContractResponse>

    @POST("api/contract/{id}/stop")
    suspend fun stopContract(
        @Path("id") id: Int,
        @Body request: VerifyCodeRequest
    ): Response<ContractResponse>

    // Chats
    @GET("api/conversation")
    suspend fun getConversations(): Response<List<ConversationResponse>>

    @GET("api/conversation/{id}/messages")
    suspend fun getChatMessages(@Path("id") id: Int): Response<MessagePaginationResponse>

    @POST("api/conversation")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<ConversationResponse>

    @POST("api/conversation/{id}/messages")
    suspend fun sendMessage(
        @Path("id") id: Int,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>

    @POST("api/conversation/{id}/mark-read")
    suspend fun markAsRead(@Path("id") id: Int): Response<Map<String, Int>>
}