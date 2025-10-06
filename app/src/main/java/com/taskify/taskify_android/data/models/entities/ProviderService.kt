package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

data class ProviderService(
    val id: Int,                        // Primary Key
    val name: String,                   // NOT NULL
    val description: String? = null,    // Optional
    val providerId: Int,                // NOT NULL, Foreign Key
    val type: ServiceType,
    val createdAt: LocalDateTime,       // NOT NULL
    val updatedAt: LocalDateTime        // NOT NULL
)

// Enum representing different categories of services
enum class ServiceType {
    CLEANING,
    PLUMBING,
    ELECTRICITY,
    GARDENING,
    IT_SUPPORT,
    EDUCATION,
    HEALTHCARE,
    DELIVERY,
    OTHER
}