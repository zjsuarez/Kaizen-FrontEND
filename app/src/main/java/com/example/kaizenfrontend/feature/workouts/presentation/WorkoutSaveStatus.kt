package com.example.kaizenfrontend.feature.workouts.presentation

/**
 * Status of the post-finish persistence step shown in
 * `WorkoutSummaryBottomSheet`. Lives in `feature/workouts` because
 * both the active-workout host and the summary sheet consume it;
 * neither feature should depend on `feature/dashboard`.
 */
sealed class WorkoutSaveStatus {
    data object Idle : WorkoutSaveStatus()
    data object Saving : WorkoutSaveStatus()
    data object Success : WorkoutSaveStatus()
    data class Error(val message: String) : WorkoutSaveStatus()
}
