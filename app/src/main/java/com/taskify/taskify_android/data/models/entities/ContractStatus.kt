package com.taskify.taskify_android.data.models.entities

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

enum class ContractStatus(val apiValue: String) {
    @SerializedName("pending")
    PENDING("pending"),

    @SerializedName("accepted")
    ACCEPTED("accepted"),

    @SerializedName("active")
    ACTIVE("active"),

    @SerializedName("finished")
    FINISHED("finished"),

    @SerializedName("rejected")
    REJECTED("rejected"),

    @SerializedName("cancelled")
    CANCELLED("cancelled"),

    // Per si l'API retorna un estat que no coneixem (fallback)
    UNKNOWN("unknown");

    companion object {
        // Funció per convertir de String (API) a Enum de forma segura
        fun fromApiValue(value: String): ContractStatus {
            return entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }

    // Pots afegir propietats visuals aquí mateix
    fun getDisplayColor(): Color {
        return when (this) {
            PENDING -> Color(0xFFFF9800) // Taronja
            ACCEPTED -> Color(0xFF2196F3) // Blau
            ACTIVE -> Color(0xFF4CAF50)   // Verd
            FINISHED -> Color(0xFF607D8B) // Gris Blavós
            REJECTED -> Color(0xFFF44336) // Vermell
            CANCELLED -> Color(0xFF9E9E9E)// Gris
            UNKNOWN -> Color.Black
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "Pending"
            ACCEPTED -> "Accepted"
            ACTIVE -> "Active"
            FINISHED -> "Finished"
            REJECTED -> "Rejected"
            CANCELLED -> "Cancelled"
            UNKNOWN -> "Unknown"
        }
    }
}