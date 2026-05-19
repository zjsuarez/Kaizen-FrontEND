package com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class DashboardResponse(
    @SerializedName("totalSessions") val totalSessions: Int,
    @SerializedName("avgDurationMinutes") val avgDurationMinutes: Int,
    @SerializedName("avgDurationLast14Days") val avgDurationLast14Days: Int? = null,
    @SerializedName("weeklyVolumeKg") val weeklyVolumeKg: Double,
    @SerializedName("prsAchieved") val prsAchieved: Int,
    @SerializedName("lastSession") val lastSession: LastSessionDTO?,
    @SerializedName("nextWorkout") val nextWorkout: NextWorkoutDTO?,
    @SerializedName("workoutStreak") val workoutStreak: Int,
    @SerializedName("trainingDaysThisMonth") val trainingDaysThisMonth: List<String> = emptyList(),
    @SerializedName("trainingDayDetails") val trainingDayDetails: List<TrainingDayDetailResponse> = emptyList(),
    @SerializedName("recentPrs") val recentPrs: List<RecentPrDTO> = emptyList(),
    @SerializedName("currentWeight") val currentWeight: Double? = null,
    @SerializedName("weightDiff") val weightDiff: Double? = null,
    @SerializedName("latestWeightTimestamp") val latestWeightTimestamp: String? = null,
    @SerializedName("streakCalendar") val streakCalendar: List<StreakDayResponse> = emptyList(),
    @SerializedName("recentWorkoutSummaries") val recentWorkoutSummaries: List<RecentWorkoutSummaryResponse> = emptyList()
)

data class LastSessionDTO(
    @SerializedName("workoutId") val workoutId: String,
    @SerializedName("routineName") val routineName: String?,
    @SerializedName("planName") val planName: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int?,
    @SerializedName("completedAt") val completedAt: String
)

data class NextWorkoutDTO(
    @SerializedName("routineId") val routineId: String,
    @SerializedName("routineName") val routineName: String,
    @SerializedName("planName") val planName: String? = null,
    @SerializedName("scheduledDate") val scheduledDate: String? = null
)

data class RecentPrDTO(
    @SerializedName("exerciseName") val exerciseName: String,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("reps") val reps: Int?,
    @SerializedName("achievedAt") val achievedAt: String?,
    @SerializedName("workoutId") val workoutId: String? = null,
    @SerializedName("previousWeight") val previousWeight: Double? = null,
    @SerializedName("percentageImprovement") val percentageImprovement: Double? = null
)

data class TrainingDayDetailResponse(
    @SerializedName("date") val date: String,
    @SerializedName("workoutId") val workoutId: String,
    @SerializedName("routineName") val routineName: String?
)

data class StreakDayResponse(
    @SerializedName("date") val date: String,
    @SerializedName("workoutDone") val workoutDone: Boolean,
    @SerializedName("missedScheduled") val missedScheduled: Boolean
)

data class RecentWorkoutSummaryResponse(
    @SerializedName("workoutId") val workoutId: String,
    @SerializedName("routineName") val routineName: String?,
    @SerializedName("planName") val planName: String?,
    @SerializedName("durationMinutes") val durationMinutes: Int?,
    @SerializedName("completedAt") val completedAt: String
)
