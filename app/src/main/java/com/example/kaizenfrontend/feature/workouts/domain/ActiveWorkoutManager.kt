package com.example.kaizenfrontend.feature.workouts.domain

import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveExerciseState
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.model.WorkoutSetState
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutInputSanitizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Singleton manager that owns the entire lifecycle of an active workout.
 *
 * Responsibilities:
 * - Holds the single source of truth via [currentWorkout] StateFlow.
 * - Runs a 1-second ticker coroutine to update elapsed time while a workout is active.
 * - Manages an independent rest timer with start / pause / reset.
 * - Provides mutation functions for exercise expansion, set data, and set completion.
 */
data class ActiveExerciseInit(
    val id: String,
    val name: String,
    val isCustom: Boolean,
    val targetSets: Int
)

object ActiveWorkoutManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentWorkout = MutableStateFlow<ActiveWorkoutState?>(null)
    val currentWorkout: StateFlow<ActiveWorkoutState?> = _currentWorkout.asStateFlow()

    // ── Coroutine jobs ──────────────────────────────────────────────────
    private var elapsedTickerJob: Job? = null
    private var restTimerJob: Job? = null

    // ── Workout lifecycle ───────────────────────────────────────────────

    /**
     * Starts a new workout session from the given routine.
     *
     * @param routineId  The routine being performed.
     * @param routineName  Display name for the routine.
     * @param exercises  List of exercises pre-populated from the routine template.
     *                   Each pair is (exerciseId, exerciseName) together with its target sets count.
     */
    fun startWorkout(
        routineId: String,
        routineName: String,
        exercises: List<ActiveExerciseInit>
    ) {
        // Prevent double-starting
        if (_currentWorkout.value != null) return

        val exerciseStates = exercises.map { initData ->
            ActiveExerciseState(
                id = initData.id,
                exerciseName = initData.name,
                isCustom = initData.isCustom,
                sets = List(initData.targetSets.coerceAtLeast(1)) { index ->
                    WorkoutSetState(setNumber = index + 1)
                },
                isExpanded = true
            )
        }

        _currentWorkout.value = ActiveWorkoutState(
            routineId = routineId,
            routineName = routineName,
            startTime = System.currentTimeMillis(),
            exercises = exerciseStates
        )

        startElapsedTicker()
    }

    /**
     * Ends the current workout and cleans up all running coroutines.
     * Returns the final snapshot of the workout so callers can persist it.
     */
    fun finishWorkout(): ActiveWorkoutState? {
        val snapshot = _currentWorkout.value
        stopElapsedTicker()
        stopRestTimer()
        _currentWorkout.value = null
        return snapshot
    }

    // ── Elapsed time ticker ─────────────────────────────────────────────

    private fun startElapsedTicker() {
        elapsedTickerJob?.cancel()
        elapsedTickerJob = scope.launch {
            while (true) {
                delay(1_000L)
                _currentWorkout.value?.let { current ->
                    _currentWorkout.value = current.copy(
                        elapsedTimeGlobal = System.currentTimeMillis() - current.startTime
                    )
                }
            }
        }
    }

    private fun stopElapsedTicker() {
        elapsedTickerJob?.cancel()
        elapsedTickerJob = null
    }

    // ── Rest timer ──────────────────────────────────────────────────────

    /**
     * Starts the rest timer counting down from the given [seconds].
     * If a rest timer is already running it will be replaced.
     */
    fun startRestTimer(seconds: Long) {
        stopRestTimer()
        mutate { it.copy(restTimer = seconds, isRestTimerRunning = true) }

        restTimerJob = scope.launch {
            while (true) {
                delay(1_000L)
                val current = _currentWorkout.value ?: break
                if (!current.isRestTimerRunning) break

                val updated = current.restTimer - 1
                if (updated <= 0L) {
                    mutate { it.copy(restTimer = 0L, isRestTimerRunning = false) }
                    break
                } else {
                    mutate { it.copy(restTimer = updated) }
                }
            }
        }
    }

    /**
     * Pauses the rest timer, keeping its current value.
     */
    fun pauseRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        mutate { it.copy(isRestTimerRunning = false) }
    }

    /**
     * Resets the rest timer to zero and stops it.
     */
    fun resetRestTimer() {
        stopRestTimer()
        mutate { it.copy(restTimer = 0L, isRestTimerRunning = false) }
    }

    private fun stopRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
    }

    // ── Exercise mutations ──────────────────────────────────────────────

    /**
     * Toggles the expanded/collapsed state of an exercise card.
     */
    fun toggleExerciseExpansion(exerciseId: String) {
        mutateExercises { exercises ->
            exercises.map { ex ->
                if (ex.id == exerciseId) ex.copy(isExpanded = !ex.isExpanded) else ex
            }
        }
    }

    // ── Set mutations ───────────────────────────────────────────────────

    /**
     * Updates the weight / reps / rir text values for a specific set.
     */
    fun updateSetData(
        exerciseId: String,
        setId: String,
        weight: String? = null,
        reps: String? = null,
        rpe: String? = null,
        type: com.example.kaizenfrontend.feature.workouts.domain.model.SetType? = null
    ) {
        mutateExercises { exercises ->
            exercises.map { ex ->
                if (ex.id != exerciseId) return@map ex
                ex.copy(
                    sets = ex.sets.map setMap@{ set ->
                        if (set.id != setId) return@setMap set
                        val normalizedWeight = weight?.let(WorkoutInputSanitizer::normalizeSessionNumberInput)
                        val normalizedReps = reps?.let(WorkoutInputSanitizer::normalizeSessionNumberInput)
                        val normalizedRpe = rpe?.let {
                            WorkoutInputSanitizer.normalizeEffortInput(it, set.rpe)
                        }

                        set.copy(
                            weight = normalizedWeight ?: set.weight,
                            reps = normalizedReps ?: set.reps,
                            rpe = normalizedRpe ?: set.rpe,
                            type = type ?: set.type
                        )
                    }
                )
            }
        }
    }

    /**
     * Toggles the completion flag on a set.
     */
    fun toggleSetCompletion(exerciseId: String, setId: String) {
        mutateExercises { exercises ->
            exercises.map { ex ->
                if (ex.id != exerciseId) return@map ex
                ex.copy(
                    sets = ex.sets.map setMap@{ set ->
                        if (set.id != setId) return@setMap set
                        set.copy(isCompleted = !set.isCompleted)
                    }
                )
            }
        }
    }

    // ── Notes ───────────────────────────────────────────────────────────

    /**
     * Updates the workout-level notes field.
     */
    fun updateNotes(notes: String) {
        mutate {
            it.copy(notes = WorkoutInputSanitizer.normalizeNotesInput(notes))
        }
    }

    // ── Add / Remove set helpers ────────────────────────────────────────

    /**
     * Appends a new empty set to the given exercise.
     */
    fun addSet(exerciseId: String) {
        mutateExercises { exercises ->
            exercises.map { ex ->
                if (ex.id != exerciseId) return@map ex
                val nextNumber = (ex.sets.maxOfOrNull { it.setNumber } ?: 0) + 1
                ex.copy(sets = ex.sets + WorkoutSetState(setNumber = nextNumber))
            }
        }
    }

    /**
     * Removes a set from the given exercise by set id.
     */
    fun removeSet(exerciseId: String, setId: String) {
        mutateExercises { exercises ->
            exercises.map { ex ->
                if (ex.id != exerciseId) return@map ex
                ex.copy(
                    sets = ex.sets
                        .filter { it.id != setId }
                        .mapIndexed { index, set -> set.copy(setNumber = index + 1) }
                )
            }
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────

    private inline fun mutate(transform: (ActiveWorkoutState) -> ActiveWorkoutState) {
        _currentWorkout.value?.let { current ->
            _currentWorkout.value = transform(current)
        }
    }

    private inline fun mutateExercises(
        transform: (List<ActiveExerciseState>) -> List<ActiveExerciseState>
    ) {
        mutate { it.copy(exercises = transform(it.exercises)) }
    }
}
