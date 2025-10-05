package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

open class Customer(
    override val id: Long,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val password: String,
    override val phoneNumber: String,
    override val profilePic: String? = null,
    override val role: Role = Role.CUSTOMER,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    open val address: String,
    open val city: String,
    open val country: String,
    open val zipCode: String
) : User