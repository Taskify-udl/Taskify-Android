package com.taskify.taskify_android.data.models.chat

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    val id: Int,
    val conversation: Int,
    val sender: Int,
    @SerializedName("sender_username") val senderUsername: String,
    val content: String,
    val timestamp: String,
    @SerializedName("is_read") val isRead: Boolean
)