package com.example.kaizenfrontend.feature.workouts.domain.model

data class Routine(
    val id: String,
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingValue: String?,
    val startingDate: String?,
    val exercises: List<RoutineExercise> = emptyList()
)
