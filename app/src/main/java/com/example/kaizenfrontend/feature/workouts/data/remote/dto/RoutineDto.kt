package com.example.kaizenfrontend.feature.workouts.data.remote.dto

data class RoutineRequest(
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingValue: String, // e.g., "MONDAY"
    val startingDate: String
)

data class RoutineResponse(
    val id: String,
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingValue: String?,
    val startingDate: String?,
    val lastPerformedDate: String?,
    val exercises: List<RoutineExerciseResponse>? = null,
    val listRoutineExercises: List<RoutineExerciseResponse>? = null
)

data class RoutineExerciseRequest(
    val targetSets: Int,
    val customExerciseId: String? = null,
    val builtinExerciseKey: String? = null
)

data class RoutineExerciseResponse(
    val id: String?,
    val orderIndex: Int?,
    val targetSets: Int?,
    val customExerciseId: String? = null,
    val builtinExerciseKey: String? = null
)
