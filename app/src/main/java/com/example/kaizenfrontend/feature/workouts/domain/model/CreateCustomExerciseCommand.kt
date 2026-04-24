package com.example.kaizenfrontend.feature.workouts.domain.model

data class CreateCustomExerciseCommand(
    val name: String,
    val description: String?,
    val selectedMuscles: List<String>,
    val metric: ExerciseMetric
)
