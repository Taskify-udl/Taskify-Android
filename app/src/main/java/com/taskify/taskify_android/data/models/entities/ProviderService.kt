package com.taskify.taskify_android.data.models.entities

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ProviderService(
    val id: Int,
    @SerializedName("provider")
    val providerId: Long,
    val name: String,
    val description: String?,
    @SerializedName("categories")
    val categoryIds: List<Int>? = emptyList(),
    @SerializedName("category_names")
    val categoryNames: List<String>? = emptyList(),
    val price: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

