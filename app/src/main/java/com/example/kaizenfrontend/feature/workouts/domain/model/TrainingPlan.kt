package com.example.kaizenfrontend.feature.workouts.domain.model

data class TrainingPlan(
    val id: String,
    val name: String,
    val description: String,
    val startingDate: String,
    val isActive: Boolean
)
