package com.taskify.taskify_android.data.models.entities

import java.time.LocalDateTime

interface User {
    val id: Long
    val fullName: String?
    val username: String?
    val email: String?
    val password: String?
    val phoneNumber: String?
    val profilePic: String?
    var role: Role?
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}

enum class Role {
    ADMIN, CUSTOMER, FREELANCER,
    COMPANY_ADMIN, COMPANY_WORKER, PROVIDER
}