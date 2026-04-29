package com.example.kaizenfrontend.feature.workouts.domain.model

import java.util.UUID

/**
 * Represents a single set within an active workout exercise.
 */
data class WorkoutSetState(
    val id: String = UUID.randomUUID().toString(),
    val setNumber: Int,
    val weight: String = "",
    val reps: String = "",
    val rpe: String = "",
    val isCompleted: Boolean = false,
    val type: SetType = SetType.NORMAL
)

/**
 * Represents an exercise being performed during an active workout.
 */
data class ActiveExerciseState(
    val id: String,
    val exerciseName: String,
    val isCustom: Boolean = false,
    val sets: List<WorkoutSetState>,
    val isExpanded: Boolean = true
)

/**
 * Full state of a workout that is currently in progress.
 * Null value in the manager means no workout is active.
 */
data class ActiveWorkoutState(
    val routineId: String,
    val routineName: String,
    val startTime: Long,
    val elapsedTimeGlobal: Long = 0L,
    val restTimer: Long = 0L,
    val isRestTimerRunning: Boolean = false,
    val exercises: List<ActiveExerciseState>,
    val notes: String = ""
)
