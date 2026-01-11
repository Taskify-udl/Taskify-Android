package com.taskify.taskify_android.data.models.chat

import com.google.gson.annotations.SerializedName

data class CreateConversationRequest(
    @SerializedName("participant")
    val participantId: Long
)