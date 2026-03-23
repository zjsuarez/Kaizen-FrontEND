package com.example.kaizenfrontend.network

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)