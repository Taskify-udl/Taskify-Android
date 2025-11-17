package com.taskify.taskify_android.data.models.auth

data class CreateServiceRequest(
    val name: String,
    val description: String,
    val provider: Long,
    val category: String,
    val price: Int,
    val createdAt: String,
    val updatedAt: String
)
