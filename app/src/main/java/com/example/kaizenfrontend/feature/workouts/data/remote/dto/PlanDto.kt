package com.example.kaizenfrontend.feature.workouts.data.remote.dto

data class TrainingPlanRequest(
    val name: String,
    val description: String,
    val startingDate: String, // e.g., "2026-06-01"
    val isActive: Boolean
)

data class TrainingPlanResponse(
    val id: String,
    val name: String,
    val description: String,
    val startingDate: String,
    val isActive: Boolean
)
