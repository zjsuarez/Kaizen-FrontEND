package com.example.kaizenfrontend.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName(value = "token", alternate = ["accessToken", "access_token", "jwt"])
    val token: String
)
