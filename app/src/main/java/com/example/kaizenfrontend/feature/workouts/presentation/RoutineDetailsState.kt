package com.example.kaizenfrontend.feature.workouts.presentation

import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise

data class RoutineDetailsState(
    val routineId: String,
    val title: String,
    val description: String,
    val exercises: List<RoutineExercise>,
    val isEditMode: Boolean = false
)
