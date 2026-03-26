package com.example.kaizenfrontend.feature.auth.data.remote.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)
