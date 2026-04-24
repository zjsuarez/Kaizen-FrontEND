package com.example.kaizenfrontend.feature.dashboard.presentation

data class NextWorkoutExerciseUi(
    val name: String,
    val targetSets: Int?
)

data class CalendarWorkoutUi(
    val workoutId: String,
    val routineName: String,
    val completedAt: String?,
    val exerciseSummaries: List<String>
)

data class LastSessionModalUi(
    val workoutId: String,
    val routineName: String,
    val completedAt: String?,
    val durationMinutes: Int?,
    val totalVolumeKg: Double,
    val totalSets: Int,
    val averageRpe: Double?,
    val lifts: List<SessionLiftUi>
)

data class SessionLiftUi(
    val name: String,
    val sets: Int,
    val topWeightKg: Double?,
    val averageReps: Double?
)

data class PrHistoryEntryUi(
    val exerciseName: String,
    val weightKg: Double?,
    val reps: Int?,
    val achievedAt: String?,
    val routineName: String
)

data class NextWorkoutDisplayUi(
    val routineId: String?,
    val routineName: String?,
    val planName: String?,
    val scheduleHint: String?
)

data class MuscleReadinessUi(
    val muscleName: String,
    val recoveredPercent: Int,
    val statusLabel: String,
    val lastTrainedAt: String?
)
