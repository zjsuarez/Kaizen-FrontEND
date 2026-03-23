package com.example.kaizenfrontend.network

data class UserUpdateRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val profilePic: String? = null,
    val primaryGoal: String? = null,
    val unitSystem: String? = null,
    val effortMeasurement: String? = null,
    val restTimerDefault: Int? = null,
    val equipmentAvailable: List<String>? = null,
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val progressPhotoUrl: String? = null
)
