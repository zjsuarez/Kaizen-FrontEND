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
