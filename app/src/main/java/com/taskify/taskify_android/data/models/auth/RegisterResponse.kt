package com.taskify.taskify_android.data.models.auth

data class RegisterResponse(
    val token: String,
    val user: UserResponse
)
