package com.taskify.taskify_android.data.models.auth

import com.google.gson.annotations.SerializedName

data class VerifyCodeRequest(
    @SerializedName("code")
    val code: String
)