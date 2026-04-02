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
    val exercises: List<RoutineExerciseResponse>? = null,
    val listRoutineExercises: List<RoutineExerciseResponse>? = null
)

data class RoutineExerciseRequest(
    val targetSets: Int
)

data class RoutineExerciseResponse(
    val id: String?,
    val routineId: String?,
    val exerciseId: String? = null,
    val targetSets: Int?,
    val targetReps: Int? = null,
    val restSeconds: Int? = null,
    val orderIndex: Int?,
    val exerciseName: String? = null,
    val exercise: EmbeddedExerciseResponse? = null
)

data class EmbeddedExerciseResponse(
    val id: String? = null,
    val name: String? = null,
    val muscleTarget: String? = null,
    val type: String? = null,
    val gifUrl: String? = null
)
