package com.example.kaizenfrontend.network

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val profilePic: String?,
    val primaryGoal: String?,
    val unitSystem: String?,
    val effortMeasurement: String?,
    val restTimerDefault: Int?,
    val equipmentAvailable: List<String>?
)
