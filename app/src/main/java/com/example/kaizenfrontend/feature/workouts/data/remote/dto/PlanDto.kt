package com.example.kaizenfrontend.feature.workouts.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TrainingPlanRequest(
    val name: String,
    val description: String,
    val startingDate: String, // e.g., "2026-06-01"
    @SerializedName("interval")
    val interval: String? = null,
    @SerializedName("cycleLength")
    val cycleLength: Int? = null,
    val isActive: Boolean
)

data class TrainingPlanResponse(
    val id: String,
    val name: String,
    val description: String,
    val startingDate: String,
    @SerializedName(value = "interval", alternate = ["Interval"])
    val interval: String? = null,
    @SerializedName(value = "cycleLength", alternate = ["CycleLength"])
    val cycleLength: Int? = null,
    val isActive: Boolean
)
