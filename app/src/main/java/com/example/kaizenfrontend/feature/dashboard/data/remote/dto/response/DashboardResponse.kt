package com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("totalSessions") val totalSessions: Int,
    @SerializedName("avgDurationMinutes") val avgDurationMinutes: Int,
    @SerializedName("weeklyVolumeKg") val weeklyVolumeKg: Double,
    @SerializedName("prsAchieved") val prsAchieved: Int,
    @SerializedName("estimated1RM") val estimated1RM: Double,
    @SerializedName("lastSession") val lastSession: LastSessionDTO?,
    @SerializedName("nextWorkout") val nextWorkout: NextWorkoutDTO?,
    @SerializedName("recoveryTimeHours") val recoveryTimeHours: Int?,
    @SerializedName("workoutStreak") val workoutStreak: Int,
    @SerializedName("trainingDaysThisMonth") val trainingDaysThisMonth: List<String> = emptyList(),
    @SerializedName("recentPrs") val recentPrs: List<RecentPrDTO> = emptyList()
)

data class LastSessionDTO(
    @SerializedName("workoutId") val workoutId: String,
    @SerializedName("routineName") val routineName: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int?,
    @SerializedName("completedAt") val completedAt: String
)

data class NextWorkoutDTO(
    @SerializedName("routineId") val routineId: String,
    @SerializedName("routineName") val routineName: String
)

data class RecentPrDTO(
    @SerializedName("exerciseName") val exerciseName: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("reps") val reps: Int,
    @SerializedName("achievedAt") val achievedAt: String
)
