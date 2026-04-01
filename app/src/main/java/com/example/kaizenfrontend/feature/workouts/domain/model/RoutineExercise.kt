package com.example.kaizenfrontend.feature.workouts.domain.model

data class RoutineExercise(
    val exercise: Exercise,
    val targetSets: Int,
    val targetReps: Int
)
