package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

data class Provider(
    override val id: Long,
    override val fullName: String,
    override val username: String,
    override val email: String,
    override val password: String,
    override val phoneNumber: String,
    override val profilePic: String? = null,
    override var role: Role? = null,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    override val address: String,
    override val city: String,
    override val country: String,
    override val zipCode: String,
    val bio: String,
    val experienceYears: Int,
    val averageRating: Double,
    val isVerified: Boolean,
    val services: List<ProviderService>
) : Customer(
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
    city,
    country,
    zipCode
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
                city == other.city &&
                country == other.country &&
                zipCode == other.zipCode &&
                bio == other.bio &&
                experienceYears == other.experienceYears &&
                averageRating == other.averageRating &&
                isVerified == other.isVerified &&
                services == other.services
    }

    override fun hashCode(): Int {
        return listOf(
            id, fullName, username, email, password, phoneNumber, profilePic,
            role, createdAt, updatedAt, address, city, country, zipCode,
            bio, experienceYears, averageRating, isVerified, services
        ).fold(0) { acc, item -> 31 * acc + (item?.hashCode() ?: 0) }
    }
}
