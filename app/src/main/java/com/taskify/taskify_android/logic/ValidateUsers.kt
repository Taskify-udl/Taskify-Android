package com.taskify.taskify_android.logic

import android.util.Patterns

// Function to validate login
fun validateLogin(email: String, password: String): String {
    return when {
        email.isBlank() -> "Email cannot be empty"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
        password.isBlank() -> "Password cannot be empty"
        password.length < 8 -> "Incorrect password"
        else -> ""
    }
}

// Function to validate registration
fun validateRegister(firstName: String, lastName: String, email: String, password: String): String {
    return when {
        firstName.isBlank() -> "First name cannot be empty"
        lastName.isBlank() -> "Last name cannot be empty"
        email.isBlank() -> "Email cannot be empty"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
        password.isBlank() -> "Password cannot be empty"
        password.length < 8 -> "Password must be at least 8 characters long"
        else -> ""
    }
}
