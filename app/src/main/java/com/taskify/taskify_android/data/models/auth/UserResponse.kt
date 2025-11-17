package com.taskify.taskify_android.data.models.auth

import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.Role
import com.taskify.taskify_android.data.models.entities.User
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val fullName: String,
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val profilePic: String? = null,
    val role: String,   // "CUSTOMER" o "PROVIDER"
    val createdAt: String,
    val updatedAt: String,

    // Camps de Customer
    val address: String?,
    val city: String?,
    val country: String?,
    val zipCode: String?,

    // Camps de Provider
    val bio: String?,
    val experienceYears: Int?,
    val averageRating: Double?,
    val isVerified: Boolean?,
    val services: List<ProviderService>? // substituir Any per ProviderService si tens la classe
) {
    fun toUser(): User {
        return if (role.uppercase() == "PROVIDER") {
            Provider(
                id = id,
                fullName = fullName,
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                profilePic = profilePic,
                role = Role.PROVIDER,
                createdAt = createdAt.toLocalDateTimeOrNow(),
                updatedAt = updatedAt.toLocalDateTimeOrNow(),
                address = address ?: "",
                city = city ?: "",
                country = country ?: "",
                zipCode = zipCode ?: "",
                bio = bio ?: "",
                experienceYears = experienceYears ?: 0,
                averageRating = averageRating ?: 0.0,
                isVerified = isVerified ?: false,
                services = services ?: emptyList()
            )
        } else {
            Customer(
                id = id,
                fullName = fullName,
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                profilePic = profilePic,
                role = Role.CUSTOMER,
                createdAt = createdAt.toLocalDateTimeOrNow(),
                updatedAt = updatedAt.toLocalDateTimeOrNow(),
                address = address ?: "",
                city = city ?: "",
                country = country ?: "",
                zipCode = zipCode ?: ""
            )
        }
    }

    fun String?.toLocalDateTimeOrNow(): LocalDateTime {
        return if (this.isNullOrEmpty()) LocalDateTime.now()
        else LocalDateTime.parse(this)
    }

}

