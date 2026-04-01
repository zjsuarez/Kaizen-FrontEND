package com.example.kaizenfrontend.feature.workouts.domain.model

/** Local domain model for a single exercise in the workout library. */
data class WorkoutItem(
    val name: String,
    val muscleGroup: String,
    val sets: Int
)
