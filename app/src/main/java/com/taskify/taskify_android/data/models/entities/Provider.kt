package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

data class Provider(
    override val id: Long = 0,
    override val fullName: String? = "",
    override val username: String? = "",
    override val email: String? = "",
    override val password: String? = "",
    override val phoneNumber: String? = "",
    override val profilePic: String? = null,
    override var role: Role? = Role.PROVIDER,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val address: String? = "",
    val bio: String? = "",
    val experienceYears: Int = 0,
    val averageRating: Double = 0.0,
    val isVerified: Boolean = false,
    val services: List<ProviderService> = emptyList()
) : Customer(
    id = id,
    fullName = fullName ?: "",
    username = username ?: "",
    email = email ?: "",
    password = password ?: "",
    phoneNumber = phoneNumber ?: "",
    profilePic = profilePic,
    role = role,
    createdAt = createdAt,
    updatedAt = updatedAt,
    address = address ?: "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Provider) return false

        return id == other.id &&
                fullName == other.fullName &&
                username == other.username &&
                email == other.email &&
                password == other.password &&
                phoneNumber == other.phoneNumber &&
                profilePic == other.profilePic &&
                role == other.role &&
                createdAt == other.createdAt &&
                updatedAt == other.updatedAt &&
                address == other.address &&
                bio == other.bio &&
                experienceYears == other.experienceYears &&
                averageRating == other.averageRating &&
                isVerified == other.isVerified &&
                services == other.services
    }

    override fun hashCode(): Int {
        return listOf(
            id,
            fullName,
            username,
            email,
            password,
            phoneNumber,
            profilePic,
            role,
            createdAt,
            updatedAt,
            address,
            bio,
            experienceYears,
            averageRating,
            isVerified,
            services
        ).fold(0) { acc, item -> 31 * acc + (item?.hashCode() ?: 0) }
    }
}
