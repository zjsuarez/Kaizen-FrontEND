package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.lifecycle.ViewModel
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.presentation.utils.RoutineScheduleCalculator
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RoutineDetailsViewModel(
    private val routine: Routine,
    private val plan: TrainingPlan?
) : ViewModel() {

    private val intervalConfig = plan?.let { PlanIntervalConfig.fromBackend(it.interval, it.cycleLength) }


    private val _uiState = MutableStateFlow(
        RoutineDetailsState(
            routineId = routine.id,
            title = routine.name,
            description = routine.description,
            exercises = routine.exercises,
            planIntervalConfig = intervalConfig,
            selectedWeekDays = RoutineScheduleCalculator.parseWeekDays(routine.schedulingValue),
            selectedCycleDays = RoutineScheduleCalculator.parseCycleDays(routine.schedulingValue).toSet(),
            restDaysBetweenWorkouts = RoutineScheduleCalculator.parseRestDays(routine.schedulingValue),
            schedulingValueString = routine.schedulingValue
        )
    )
    val uiState: StateFlow<RoutineDetailsState> = _uiState.asStateFlow()

    fun toggleEditMode() {
        _uiState.update { current ->
            current.copy(isEditMode = !current.isEditMode)
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { current ->
            current.copy(title = newTitle)
        }
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { current ->
            current.copy(description = newDesc)
        }
    }

    fun removeExercise(exerciseId: String) {
        _uiState.update { current ->
            current.copy(
                exercises = current.exercises.filterNot { it.exercise.id == exerciseId }
            )
        }
    }

    fun addExercise(exercise: Exercise, targetSets: Int = 3, targetReps: Int = 10) {
        _uiState.update { current ->
            if (current.exercises.any { it.exercise.id == exercise.id }) {
                current
            } else {
                current.copy(
                    exercises = current.exercises + RoutineExercise(
                        exercise = exercise,
                        targetSets = targetSets.coerceAtLeast(1),
                        targetReps = targetReps.coerceAtLeast(1)
                    )
                )
            }
        }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val list = current.exercises
            if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
                return@update current
            }

            val mutable = list.toMutableList()
            val moved = mutable.removeAt(fromIndex)
            mutable.add(toIndex, moved)
            current.copy(exercises = mutable)
        }
    }

    fun toggleWeekDay(day: DayOfWeek) {
        _uiState.update { current ->
            val nextDays = if (day in current.selectedWeekDays) current.selectedWeekDays - day else current.selectedWeekDays + day
            current.copy(selectedWeekDays = nextDays)
        }
    }

    fun toggleCycleDay(day: Int) {
        _uiState.update { current ->
            val maxDays = current.planIntervalConfig?.cycleLengthDays ?: 7
            val dayValue = day.coerceIn(1, maxDays)
            val nextDays = if (dayValue in current.selectedCycleDays) current.selectedCycleDays - dayValue else current.selectedCycleDays + dayValue
            current.copy(selectedCycleDays = nextDays)
        }
    }

    fun updateRestDaysBetweenWorkouts(value: Int) {
        _uiState.update { current ->
            current.copy(restDaysBetweenWorkouts = value.coerceAtLeast(1))
        }
    }

    fun saveChanges() {
        _uiState.update { current ->
            // Re-serialize the scheduling value string
            val updatedSchedulePart = when (current.planIntervalConfig?.type) {
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.CYCLE -> {
                    if (current.planIntervalConfig.cycleMode == com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode.WEEKLY) {
                        current.selectedWeekDays.sortedBy { it.value }.joinToString(",") { it.name }.ifBlank { DayOfWeek.MONDAY.name }
                    } else {
                        current.selectedCycleDays.sorted().joinToString(",").ifBlank { "1" }
                    }
                }
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.FREQUENCY -> {
                    current.restDaysBetweenWorkouts.toString()
                }
                null -> current.schedulingValueString
            }

            current.copy(
                isEditMode = false,
                schedulingValueString = updatedSchedulePart
            )
        }
    }
}
