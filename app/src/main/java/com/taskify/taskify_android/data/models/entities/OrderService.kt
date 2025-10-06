package com.taskify.taskify_android.data.models.entities

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderService(
    val id: Int,                     // Primary Key
    val userId: Int,                 // Foreign Key -> Customer/User
    val serviceId: Int,              // Foreign Key -> ProviderService
    val startDateTime: LocalDateTime,        // When the service is scheduled to start
    val price: BigDecimal,           // Price agreed for the service
    val createdAt: LocalDateTime     // Timestamp when the order was created
)

enum class ServiceStatus {
    CONFIRMED,
    CANCELLED,
    ARCHIVED,
    PENDING,
    REPROGRAMED
}