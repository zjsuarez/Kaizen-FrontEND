package com.example.kaizenfrontend.feature.user.domain.model

/**
 * Clean domain model for the authenticated user — no Retrofit or framework deps.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val unitSystem: String,
    val effortMeasurement: String,
    val restTimerDefault: Int,
    val profilePic: String? = null,
    val primaryGoal: String? = null,
    val equipmentAvailable: List<String> = emptyList()
)
