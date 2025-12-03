package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)