package com.example.kaizenfrontend.feature.workouts.data.remote.dto

data class RoutineRequest(
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingType: String, // e.g., "WEEKLY"
    val cycleLength: Int?,
    val schedulingValue: String, // e.g., "MONDAY"
    val startingDate: String
)

data class RoutineResponse(
    val id: String,
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingType: String?,
    val cycleLength: Int?,
    val schedulingValue: String?,
    val startingDate: String?,
    val listRoutineExercises: List<RoutineExerciseResponse> = emptyList()
)

data class RoutineExerciseResponse(
    val id: String?,
    val routineId: String?,
    val exerciseId: String,
    val targetSets: Int?,
    val targetReps: Int?,
    val restSeconds: Int?,
    val orderIndex: Int?
)
