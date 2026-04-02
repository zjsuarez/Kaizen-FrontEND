package com.example.kaizenfrontend.feature.workouts.presentation

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine

data class PlanDetailsState(
    val planId: String,
    val title: String,
    val description: String,
    val routines: List<Routine>,
    val isActive: Boolean = true,
    val isEditMode: Boolean = false
)
