package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName
import com.taskify.taskify_android.data.models.entities.ContractStatus

/**
 * Model de Resposta (GET)
 * Representa un contracte/reserva que rebem de l'API per mostrar a "My Orders".
 */
data class ContractResponse(
    val id: Int,
    val code: String, // El codi únic (UUID) generat pel backend

    @SerializedName("user")
    val userId: Int, // ID de l'usuari que ha fet la reserva

    @SerializedName("user_username")
    val userName: String, // Nom de l'usuari (útil si ets el proveïdor per saber qui contracta)

    @SerializedName("service")
    val serviceId: Int, // ID del servei contractat

    @SerializedName("service_name")
    val serviceName: String, // Nom del servei (ex: "Asesoría fiscal") per mostrar a la targeta

    @SerializedName("start_date")
    val startDate: String, // Data d'inici (ex: "2026-01-11" o ISO amb hora)

    @SerializedName("start_time")
    val startTime: String? = null, // Hora d'inici

    @SerializedName("status")
    val status: ContractStatus, // Estats possibles: "pending", "accepted", "rejected", "cancelled", "active", "finished",

    @SerializedName("price")
    val price: String, // El preu com a String (ex: "75.00") per evitar problemes de decimals al mostrar

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("start_code_alpha")
    val startCodeAlpha: String? = null,

    @SerializedName("end_code_alpha")
    val endCodeAlpha: String? = null
)