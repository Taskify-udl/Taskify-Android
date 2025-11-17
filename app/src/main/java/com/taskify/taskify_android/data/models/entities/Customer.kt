package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

open class Customer(
    override val id: Long,
    override val fullName: String?,
    override val username: String?,
    override val email: String?,
    override val password: String?,
    override val phoneNumber: String?,
    override val profilePic: String? = null,
    override var role: Role? = Role.CUSTOMER,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime,
    open val address: String?,
    open val city: String?,
    open val country: String?,
    open val zipCode: String?
) : User