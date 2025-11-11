package com.taskify.taskify_android.logic

import android.util.Patterns

// Function to validate login
fun validateLogin(username: String, password: String): String {
    return when {
        username.isBlank() -> "Username cannot be empty"
        username.length <= 4 -> "Username must be at least 4 characters"
        password.isBlank() -> "Password cannot be empty"
        password.length < 8 -> "Password must be at least 8 characters"
        else -> ""
    }
}

// Function to validate registration form
fun validateRegister(
    fullName: String,
    username: String,
    email: String,
    password: String,
    confirmPassword: String
): String {
    return when {
        fullName.isBlank() -> "Full name cannot be empty"
        username.isBlank() -> "Username cannot be empty"
        username.length <= 4 -> "Username must be at least 4 characters"
        email.isBlank() -> "Email cannot be empty"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
        password.isBlank() -> "Password cannot be empty"
        password.length < 8 -> "Password must be at least 8 characters long"
        confirmPassword.isBlank() -> "Please confirm your password"
        password != confirmPassword -> "Passwords do not match"
        else -> ""
    }
}

// Function to validate company information
fun validateCompanyInfo(companyName: String, cif: String, email: String): String {
    if (companyName.isBlank()) return "Company name cannot be empty."
    if (cif.isBlank()) return "CIF cannot be empty."
    if (!Regex("^[A-Z0-9]{8,9}\$").matches(cif.uppercase())) return "Invalid CIF format."
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid company email."
    return ""
}