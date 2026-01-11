package com.taskify.taskify_android.data.models.chat

import com.google.gson.annotations.SerializedName

data class ChatUser(
    val id: Int,
    val username: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?
)