package com.taskify.taskify_android.data.models.auth

data class RegisterRequest(
    // TODO
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val password: String
)