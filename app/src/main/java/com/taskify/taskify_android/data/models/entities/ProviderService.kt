package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

data class ProviderService(
    val id: Int,
    val providerId: Long,
    val name: String,
    val description: String?,
    val category: ServiceType? = ServiceType.OTHER,
    val price: Int = 0,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
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
