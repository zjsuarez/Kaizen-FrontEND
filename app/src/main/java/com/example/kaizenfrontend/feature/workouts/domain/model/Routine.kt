package com.example.kaizenfrontend.feature.workouts.domain.model

data class Routine(
    val id: String,
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingType: String?,
    val cycleLength: Int?,
    val schedulingValue: String?,
    val startingDate: String?
)
