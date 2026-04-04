package com.example.kaizenfrontend.feature.workouts.domain.model

data class Exercise(
    val id: String,
    val name: String,
    val muscleTarget: MuscleTarget,
    val equipmentType: EquipmentType,
    val gifUrl: String?,
    val isCustom: Boolean = false
)
