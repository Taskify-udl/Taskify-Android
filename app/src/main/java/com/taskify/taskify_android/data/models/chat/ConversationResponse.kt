package com.taskify.taskify_android.data.models.chat

import com.google.gson.annotations.SerializedName

// ConversationResponse.kt
data class ConversationResponse(
    val id: Int,
    val participants: ChatUser,

    @SerializedName("last_message")
    val lastMessage: MessageResponse?,

    // El posem opcional (amb valor per defecte 0) perquè no surt al teu JSON de Postman
    @SerializedName("unread_count")
    val unreadCount: Int = 0,

    @SerializedName("created_at")
    val createdAt: String? = null
)