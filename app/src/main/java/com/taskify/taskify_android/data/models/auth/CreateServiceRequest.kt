package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName

data class CreateServiceRequest(
    val name: String,
    val description: String,
    val provider: Long,
    val category: String,
    val price: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
