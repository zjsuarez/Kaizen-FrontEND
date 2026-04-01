package com.example.kaizenfrontend.feature.workouts.presentation

import android.util.Log
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineCreateDTO
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineExerciseTargetDTO
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineScheduleDataDTO
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.example.kaizenfrontend.feature.workouts.data.repository.MockExerciseRepository
import com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.repository.ExerciseRepository
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutineWizardUiState(
    val currentStep: Int = 1,
    val name: String = "",
    val description: String = "",
    val availablePlans: List<TrainingPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val selectedPlanInterval: PlanIntervalConfig = PlanIntervalConfig.defaultCycleWeekly(),
    val selectedWeekDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY),
    val selectedCycleDays: Set<Int> = setOf(1),
    val restDaysBetweenWorkouts: Int = 1,
    val selectedExercises: List<RoutineExercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val isLoadingExercises: Boolean = false,
    val exercisesError: String? = null
)

class RoutineWizardViewModel(
    private val exerciseRepository: ExerciseRepository = MockExerciseRepository()
) : ViewModel() {

    private val gson = Gson()

    private val _uiState = MutableStateFlow(
        RoutineWizardUiState(isLoadingExercises = true)
    )
    val uiState: StateFlow<RoutineWizardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RoutineWizardEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RoutineWizardEvent> = _events.asSharedFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExercises = true, exercisesError = null) }

            val result = exerciseRepository.getExercises()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        availableExercises = result.getOrNull().orEmpty(),
                        isLoadingExercises = false,
                        exercisesError = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingExercises = false,
                        exercisesError = result.exceptionOrNull()?.message ?: "Failed to load exercises"
                    )
                }
            }
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun setAvailablePlans(plans: List<TrainingPlan>) {
        _uiState.update { current ->
            val resolvedPlanId = when {
                plans.isEmpty() -> null
                current.selectedPlanId != null && plans.any { it.id == current.selectedPlanId } -> current.selectedPlanId
                else -> plans.first().id
            }

            val selectedPlan = plans.firstOrNull { it.id == resolvedPlanId }
            val intervalConfig = PlanIntervalConfig.fromBackendValue(selectedPlan?.interval)

            current.copy(
                availablePlans = plans,
                selectedPlanId = resolvedPlanId,
                selectedPlanInterval = intervalConfig,
                selectedCycleDays = normalizedCycleDays(current.selectedCycleDays, intervalConfig.cycleLengthDays)
            )
        }
    }

    fun selectPlan(planId: String) {
        _uiState.update { current ->
            val selectedPlan = current.availablePlans.firstOrNull { it.id == planId }
            val intervalConfig = PlanIntervalConfig.fromBackendValue(selectedPlan?.interval)

            current.copy(
                selectedPlanId = selectedPlan?.id,
                selectedPlanInterval = intervalConfig,
                selectedCycleDays = normalizedCycleDays(current.selectedCycleDays, intervalConfig.cycleLengthDays)
            )
        }
    }

    fun toggleWeekDay(day: DayOfWeek) {
        _uiState.update { current ->
            val nextDays = if (day in current.selectedWeekDays) {
                current.selectedWeekDays - day
            } else {
                current.selectedWeekDays + day
            }

            current.copy(selectedWeekDays = nextDays)
        }
    }

    fun toggleCycleDay(day: Int) {
        _uiState.update { current ->
            val dayValue = day.coerceAtLeast(1).coerceAtMost(current.selectedPlanInterval.cycleLengthDays.coerceAtLeast(1))
            val nextDays = if (dayValue in current.selectedCycleDays) {
                current.selectedCycleDays - dayValue
            } else {
                current.selectedCycleDays + dayValue
            }

            current.copy(selectedCycleDays = nextDays)
        }
    }

    fun updateRestDaysBetweenWorkouts(value: Int) {
        _uiState.update { it.copy(restDaysBetweenWorkouts = value.coerceAtLeast(1)) }
    }

    fun addExercise(exercise: Exercise, targetSets: Int = 3, targetReps: Int = 10) {
        _uiState.update { current ->
            if (current.selectedExercises.any { it.exercise.id == exercise.id }) {
                current
            } else {
                current.copy(
                    selectedExercises = current.selectedExercises + RoutineExercise(
                        exercise = exercise,
                        targetSets = targetSets.coerceAtLeast(1),
                        targetReps = targetReps.coerceAtLeast(1)
                    )
                )
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        _uiState.update { current ->
            current.copy(
                selectedExercises = current.selectedExercises.filterNot { it.exercise.id == exerciseId }
            )
        }
    }

    fun updateExerciseTargets(exerciseId: String, targetSets: Int, targetReps: Int) {
        _uiState.update { current ->
            current.copy(
                selectedExercises = current.selectedExercises.map { routineExercise ->
                    if (routineExercise.exercise.id == exerciseId) {
                        routineExercise.copy(
                            targetSets = targetSets.coerceAtLeast(1),
                            targetReps = targetReps.coerceAtLeast(1)
                        )
                    } else {
                        routineExercise
                    }
                }
            )
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, 3)) }
    }

    fun resetWizard() {
        val cachedExercises = _uiState.value.availableExercises
        val cachedPlans = _uiState.value.availablePlans
        val defaultPlan = cachedPlans.firstOrNull()
        val intervalConfig = PlanIntervalConfig.fromBackendValue(defaultPlan?.interval)
        _uiState.value = RoutineWizardUiState(
            currentStep = 1,
            availablePlans = cachedPlans,
            selectedPlanId = defaultPlan?.id,
            selectedPlanInterval = intervalConfig,
            selectedCycleDays = normalizedCycleDays(setOf(1), intervalConfig.cycleLengthDays),
            availableExercises = cachedExercises,
            isLoadingExercises = false,
            exercisesError = null
        )
    }

    fun saveRoutine() {
        viewModelScope.launch {
            val payload = _uiState.value.toRoutineCreateDTO()
            Log.d(TAG, "Routine payload: ${gson.toJson(payload)}")
            _events.emit(RoutineWizardEvent.Success)
        }
    }

    private fun RoutineWizardUiState.toRoutineCreateDTO(): RoutineCreateDTO {
        val scheduleData = when (selectedPlanInterval.type) {
            PlanIntervalType.FREQUENCY -> RoutineScheduleDataDTO(
                restDays = restDaysBetweenWorkouts
            )

            PlanIntervalType.CYCLE -> {
                if (selectedPlanInterval.cycleMode == CycleMode.WEEKLY) {
                    RoutineScheduleDataDTO(
                        weekDays = selectedWeekDays
                            .sortedBy { it.value }
                            .map { it.name },
                        cycleLength = 7
                    )
                } else {
                    RoutineScheduleDataDTO(
                        cycleLength = selectedPlanInterval.cycleLengthDays,
                        cycleDays = selectedCycleDays
                            .sorted()
                    )
                }
            }
        }

        val exerciseTargets = selectedExercises.map {
            RoutineExerciseTargetDTO(
                exerciseId = it.exercise.id,
                targetSets = it.targetSets,
                targetReps = it.targetReps
            )
        }

        return RoutineCreateDTO(
            planId = selectedPlanId,
            name = name.trim(),
            description = description.trim(),
            scheduleType = when (selectedPlanInterval.type) {
                PlanIntervalType.CYCLE -> "CYCLE"
                PlanIntervalType.FREQUENCY -> "FREQUENCY"
            },
            scheduleData = scheduleData,
            exercises = exerciseTargets
        )
    }

    private fun normalizedCycleDays(days: Set<Int>, cycleLength: Int): Set<Int> {
        val maxDay = cycleLength.coerceAtLeast(1)
        val normalized = days
            .map { it.coerceIn(1, maxDay) }
            .toSet()

        return if (normalized.isEmpty()) {
            setOf(1)
        } else {
            normalized
        }
    }

    companion object {
        private const val TAG = "RoutineWizardViewModel"
    }
}

sealed interface RoutineWizardEvent {
    data object Success : RoutineWizardEvent
}
