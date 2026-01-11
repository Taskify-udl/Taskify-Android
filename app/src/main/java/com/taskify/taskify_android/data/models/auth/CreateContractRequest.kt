package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName
import com.taskify.taskify_android.data.models.entities.ContractStatus

/**
 * Model de Petició (POST)
 * Dades necessàries per crear una nova sol·licitud de contracte.
 * El backend agafa l'usuari del token automàticament.
 */
data class CreateContractRequest(
    val service: Int, // L'ID del servei que volem contractar

    @SerializedName("start_date")
    val startDate: String, // Format: "YYYY-MM-DD"

    @SerializedName("start_time")
    val startTime: String, // Format: "HH:mm:ss" (NOU CAMP)

    @SerializedName("price")
    val price: Double, // El preu pactat (o el del servei) com a número decimal

    @SerializedName("description")
    val description: String = "",

    @SerializedName("status")
    val status: ContractStatus = ContractStatus.PENDING,
)