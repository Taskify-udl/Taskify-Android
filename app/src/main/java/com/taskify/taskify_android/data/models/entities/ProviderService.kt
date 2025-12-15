package com.taskify.taskify_android.data.models.entities

import com.google.gson.annotations.SerializedName

const val API_BASE_URL = "http://10.0.2.2:8000"

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

    // ACTUALITZACIÓ 1: Canvi de Int a String per suportar "12.00"
    val price: String,

    // ACTUALITZACIÓ 2: Nou camp per a les imatges
    val images: List<ServiceImage>? = emptyList(),

    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

// Nou data class per gestionar l'estructura de la imatge
data class ServiceImage(
    val id: Int,
    val image: String
)

