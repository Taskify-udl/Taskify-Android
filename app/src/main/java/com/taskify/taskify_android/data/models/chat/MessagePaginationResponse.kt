package com.taskify.taskify_android.data.models.chat

import com.google.gson.annotations.SerializedName

data class MessagePaginationResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    @SerializedName("messages")
    val results: List<MessageResponse>
)