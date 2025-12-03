package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName
import com.taskify.taskify_android.data.models.entities.Customer
import com.taskify.taskify_android.data.models.entities.Provider
import com.taskify.taskify_android.data.models.entities.ProviderService
import com.taskify.taskify_android.data.models.entities.Role
import com.taskify.taskify_android.data.models.entities.User
import java.time.LocalDateTime

data class UserResponse(
    val id: Long,

    @SerializedName("first_name")
    val firstName: String?, // Fent que sigui nullable

    @SerializedName("last_name")
    val lastName: String?,  // Fent que sigui nullable

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String?,

    @SerializedName("phone")
    val phoneNumber: String?,

    val profilePic: String? = null,
    val role: String,   // "CUSTOMER" o "PROVIDER"
    val createdAt: String?, // Fent que sigui nullable
    val updatedAt: String?, // Fent que sigui nullable

    // Camps de Customer
    @SerializedName("location")
    val address: String?,

    // Camps de Provider
    @SerializedName("bio")
    val bio: String?,

    val isVerified: Boolean?,
    val services: List<ProviderService>?
) {
    fun toUser(): User {
        // 1. Valors segurs: evitem "null null" i errors amb dates
        val safeFirstName = firstName ?: ""
        val safeLastName = lastName ?: ""
        val safeCreatedAt = createdAt.toLocalDateTimeOrNow()
        val safeUpdatedAt = updatedAt.toLocalDateTimeOrNow()

        // Concatenem i eliminem espais extra si un nom és buit (p. ex., si només hi ha cognom)
        val fullName = "$safeFirstName $safeLastName".trim()

        // 2. Determinació del rol (mateixa lògica de comprovació)
        val actualRole = try {
            Role.valueOf(role.uppercase())
        } catch (e: IllegalArgumentException) {
            Role.CUSTOMER
        }

        val isProviderRole = when (actualRole) {
            Role.FREELANCER,
            Role.COMPANY_ADMIN,
            Role.COMPANY_WORKER,
            Role.PROVIDER -> true

            else -> false
        }

        // 3. Creació de l'usuari amb valors segurs
        return if (isProviderRole) {
            Provider(
                id = id,
                fullName = fullName,
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                profilePic = profilePic,
                role = actualRole,
                createdAt = safeCreatedAt,
                updatedAt = safeUpdatedAt,
                address = address ?: "",
                bio = bio ?: "",
                isVerified = isVerified ?: false,
                services = services ?: emptyList()
            )
        } else {
            Customer(
                id = id,
                fullName = fullName,
                username = username,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                profilePic = profilePic,
                role = actualRole,
                createdAt = safeCreatedAt,
                updatedAt = safeUpdatedAt,
                address = address ?: "",
            )
        }
    }

    // Funció toLocalDateTimeOrNow millorada per tractar strings "null" de l'API
    fun String?.toLocalDateTimeOrNow(): LocalDateTime {
        // Verifica si la cadena és null, buida o si conté la paraula "null"
        return if (this.isNullOrEmpty() || this.equals("null", ignoreCase = true)) {
            LocalDateTime.now()
        } else {
            LocalDateTime.parse(this)
        }
    }
}