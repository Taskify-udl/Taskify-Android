package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName
import com.taskify.taskify_android.data.models.entities.ContractStatus

data class UpdateContractStatusRequest(
    @SerializedName("service")
    val service: Int,

    @SerializedName("start_date")
    val startDate: String, // Format: "YYYY-MM-DD"

    @SerializedName("start_time")
    val startTime: String, // Format: "HH:mm:ss"

    @SerializedName("price")
    val price: Double,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("status")
    val status: ContractStatus, // L'estat que volem canviar (Enum)
)