package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

data class Admin(
    override val id: Long,
    override val fullName: String,
    override val username: String,
    override val email: String,
    override val password: String,
    override val phoneNumber: String,
    override val profilePic: String? = null,
    override var role: Role? = Role.ADMIN,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime
) : User