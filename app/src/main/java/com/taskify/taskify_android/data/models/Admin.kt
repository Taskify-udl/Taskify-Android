package com.taskify.taskify_android.data.models

import java.time.LocalDateTime

data class Admin(
    override val id: Long,
    override val firstName: String,
    override val lastName: String,
    override val email: String,
    override val password: String,
    override val phoneNumber: String,
    override val profilePic: String? = null,
    override val role: Role = Role.ADMIN,
    override val createdAt: LocalDateTime,
    override val updatedAt: LocalDateTime
) : User