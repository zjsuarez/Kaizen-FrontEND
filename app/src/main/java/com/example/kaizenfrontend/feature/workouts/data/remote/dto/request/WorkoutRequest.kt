package com.example.kaizenfrontend.feature.workouts.data.remote.dto.request

data class WorkoutRequest(
    val routineId: String?,
    val startTime: String?,
    val endTime: String?,
    val notes: String?,
    val measurementId: String?,
    val sets: List<WorkoutSetRequest>
)

data class WorkoutSetRequest(
    val customExerciseId: String?,
    val builtinExerciseKey: String?,
    val setNumber: Int,
    val weightKg: Double?,
    val isPR: Boolean,
    val reps: Int?,
    val rpe: Int?,
    val type: String
)
