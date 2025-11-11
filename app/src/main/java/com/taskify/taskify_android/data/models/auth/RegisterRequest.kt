package com.taskify.taskify_android.data.models.auth

data class RegisterRequest(
    // TODO
    val fullname: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)