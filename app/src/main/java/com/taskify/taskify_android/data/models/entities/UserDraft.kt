package com.taskify.taskify_android.data.models.entities

data class UserDraft(
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var phoneNumber: String = "",
    var role: Role? = null,

    // Company data
    var companyName: String = "",
    var cif: String = "",
    var companyEmail: String = ""
)
