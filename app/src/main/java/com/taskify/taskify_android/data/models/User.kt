package com.taskify.taskify_android.data.models

import java.time.LocalDateTime

interface User {
    val id: Long
    val firstName: String
    val lastName: String
    val email: String
    val password: String
    val phoneNumber: String
    val profilePic: String?
    val role: Role
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

enum class Role {
    ADMIN, CUSTOMER, PROVIDER
}