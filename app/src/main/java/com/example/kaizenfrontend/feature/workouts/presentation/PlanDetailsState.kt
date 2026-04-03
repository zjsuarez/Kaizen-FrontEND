package com.example.kaizenfrontend.feature.workouts.presentation

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan

data class PlanDetailsState(
    val planId: String,
    val plan: TrainingPlan?,
    val title: String,
    val description: String,
    val routines: List<Routine>,
    val isActive: Boolean = true,
    val isEditMode: Boolean = false
)
