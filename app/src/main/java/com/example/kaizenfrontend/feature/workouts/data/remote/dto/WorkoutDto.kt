package com.example.kaizenfrontend.feature.workouts.data.remote.dto

data class WorkoutResponseDto(
    val id: String,
    val routineId: String?,
    val routineName: String?,
    val startTime: String?,
    val endTime: String?,
    val notes: String?,
    val measurementId: String?,
    val progressPhotoUrl: String?,
    val sets: List<WorkoutSetResponseDto>
)

data class WorkoutSetResponseDto(
    val id: String,
    val customExerciseId: String?,
    val builtinExerciseKey: String?,
    val exerciseName: String?,
    val setNumber: Int,
    val weightKg: Double?,
    val isPR: Boolean,
    val reps: Int?,
    val rpe: Int?,
    val type: String
)
