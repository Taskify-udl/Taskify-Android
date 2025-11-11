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
)