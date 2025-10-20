package com.taskify.taskify_android.data.models.auth

data class LoginResponse(
    val token: String,
    val user: UserResponse
)