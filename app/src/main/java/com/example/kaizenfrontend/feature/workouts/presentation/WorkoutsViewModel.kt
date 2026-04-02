package com.example.kaizenfrontend.feature.workouts.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.network.RetrofitClient
import com.example.kaizenfrontend.feature.workouts.data.repository.PlanRepositoryImpl
import com.example.kaizenfrontend.feature.workouts.data.repository.MockExerciseRepository
import com.example.kaizenfrontend.feature.workouts.data.repository.RoutineRepositoryImpl
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.usecase.CreatePlanUseCase
import com.example.kaizenfrontend.feature.workouts.domain.usecase.CreateRoutineUseCase
import com.example.kaizenfrontend.feature.workouts.domain.usecase.DeletePlanUseCase
import com.example.kaizenfrontend.feature.workouts.domain.usecase.DeleteRoutineUseCase
import com.example.kaizenfrontend.feature.workouts.domain.usecase.GetPlansUseCase
import com.example.kaizenfrontend.feature.workouts.domain.usecase.GetRoutinesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class WorkoutsUiState {
    object Loading : WorkoutsUiState()
    data class Success(
        val plans: List<TrainingPlan>,
        val routinesByPlanId: Map<String, List<Routine>>,
        val unassignedRoutines: List<Routine>, // routines where planId is null
        val expandedPlanIds: Set<String>
    ) : WorkoutsUiState()
    data class Error(val message: String) : WorkoutsUiState()
}

class WorkoutsViewModel(
    private val getPlansUseCase: GetPlansUseCase,
    private val getRoutinesUseCase: GetRoutinesUseCase,
    private val createPlanUseCase: CreatePlanUseCase,
    private val createRoutineUseCase: CreateRoutineUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val deleteRoutineUseCase: DeleteRoutineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutsUiState>(WorkoutsUiState.Loading)
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = WorkoutsUiState.Loading
            val plansResult = getPlansUseCase()
            val routinesResult = getRoutinesUseCase()

            if (plansResult.isSuccess && routinesResult.isSuccess) {
                val plans = plansResult.getOrNull() ?: emptyList()
                val routines = routinesResult.getOrNull() ?: emptyList()

                val routinesByPlanId = routines.filter { it.planId != null }.groupBy { it.planId!! }
                val unassignedRoutines = routines.filter { it.planId == null }

                _uiState.value = WorkoutsUiState.Success(
                    plans = plans,
                    routinesByPlanId = routinesByPlanId,
                    unassignedRoutines = unassignedRoutines,
                    expandedPlanIds = plans.map { it.id }.toSet() // Expand all by default
                )
            } else {
                val errorMsg = plansResult.exceptionOrNull()?.message ?: routinesResult.exceptionOrNull()?.message ?: "Unknown error occurred"
                _uiState.value = WorkoutsUiState.Error(errorMsg)
            }
        }
    }

    fun togglePlanExpansion(planId: String) {
        _uiState.update { state ->
            if (state is WorkoutsUiState.Success) {
                val newExpanded = if (state.expandedPlanIds.contains(planId)) {
                    state.expandedPlanIds - planId
                } else {
                    state.expandedPlanIds + planId
                }
                state.copy(expandedPlanIds = newExpanded)
            } else state
        }
    }

    fun createPlan(
        name: String,
        description: String,
        startingDate: String,
        interval: String? = null,
        cycleLength: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = WorkoutsUiState.Loading
            val result = createPlanUseCase(
                name = name,
                description = description,
                startingDate = startingDate,
                interval = interval,
                cycleLength = cycleLength
            )
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.value = WorkoutsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create plan")
            }
        }
    }

    fun createRoutine(
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String = "MONDAY",
        startingDate: String = "2026-03-24",
        routineExercises: List<RoutineExercise> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = WorkoutsUiState.Loading
            val result = createRoutineUseCase(
                planId = planId,
                name = name,
                description = description,
                schedulingValue = schedulingValue,
                startingDate = startingDate,
                routineExercises = routineExercises
            )
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.value = WorkoutsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create routine")
            }
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            _uiState.value = WorkoutsUiState.Loading
            val result = deletePlanUseCase(planId)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.value = WorkoutsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete plan")
            }
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            _uiState.value = WorkoutsUiState.Loading
            val result = deleteRoutineUseCase(routineId)
            if (result.isSuccess) {
                loadData()
            } else {
                _uiState.value = WorkoutsUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete routine")
            }
        }
    }

    fun movePlanDown(planId: String) {
        _uiState.update { state ->
            if (state !is WorkoutsUiState.Success) return@update state
            val fromIndex = state.plans.indexOfFirst { it.id == planId }
            if (fromIndex == -1 || fromIndex == state.plans.lastIndex) return@update state

            val reordered = state.plans.toMutableList().apply {
                add(fromIndex + 1, removeAt(fromIndex))
            }
            state.copy(plans = reordered)
        }
    }

    fun movePlanUp(planId: String) {
        _uiState.update { state ->
            if (state !is WorkoutsUiState.Success) return@update state
            val fromIndex = state.plans.indexOfFirst { it.id == planId }
            if (fromIndex <= 0) return@update state

            val reordered = state.plans.toMutableList().apply {
                add(fromIndex - 1, removeAt(fromIndex))
            }
            state.copy(plans = reordered)
        }
    }

    fun moveRoutineDown(routineId: String, planId: String?) {
        _uiState.update { state ->
            if (state !is WorkoutsUiState.Success) return@update state

            if (planId == null) {
                val fromIndex = state.unassignedRoutines.indexOfFirst { it.id == routineId }
                if (fromIndex == -1 || fromIndex == state.unassignedRoutines.lastIndex) return@update state

                val reorderedUnassigned = state.unassignedRoutines.toMutableList().apply {
                    add(fromIndex + 1, removeAt(fromIndex))
                }
                return@update state.copy(unassignedRoutines = reorderedUnassigned)
            }

            val planRoutines = state.routinesByPlanId[planId] ?: return@update state
            val fromIndex = planRoutines.indexOfFirst { it.id == routineId }
            if (fromIndex == -1 || fromIndex == planRoutines.lastIndex) return@update state

            val reorderedPlanRoutines = planRoutines.toMutableList().apply {
                add(fromIndex + 1, removeAt(fromIndex))
            }

            state.copy(
                routinesByPlanId = state.routinesByPlanId.toMutableMap().apply {
                    put(planId, reorderedPlanRoutines)
                }
            )
        }
    }

    fun moveRoutineUp(routineId: String, planId: String?) {
        _uiState.update { state ->
            if (state !is WorkoutsUiState.Success) return@update state

            if (planId == null) {
                val fromIndex = state.unassignedRoutines.indexOfFirst { it.id == routineId }
                if (fromIndex <= 0) return@update state

                val reorderedUnassigned = state.unassignedRoutines.toMutableList().apply {
                    add(fromIndex - 1, removeAt(fromIndex))
                }
                return@update state.copy(unassignedRoutines = reorderedUnassigned)
            }

            val planRoutines = state.routinesByPlanId[planId] ?: return@update state
            val fromIndex = planRoutines.indexOfFirst { it.id == routineId }
            if (fromIndex <= 0) return@update state

            val reorderedPlanRoutines = planRoutines.toMutableList().apply {
                add(fromIndex - 1, removeAt(fromIndex))
            }

            state.copy(
                routinesByPlanId = state.routinesByPlanId.toMutableMap().apply {
                    put(planId, reorderedPlanRoutines)
                }
            )
        }
    }
}

class WorkoutsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutsViewModel::class.java)) {
            val sessionManager = SessionManager(context)
            val planRepo = PlanRepositoryImpl(RetrofitClient.planService, sessionManager)
            val exerciseRepo = MockExerciseRepository()
            val routineRepo = RoutineRepositoryImpl(RetrofitClient.routineService, sessionManager, exerciseRepo)
            @Suppress("UNCHECKED_CAST")
            return WorkoutsViewModel(
                GetPlansUseCase(planRepo),
                GetRoutinesUseCase(routineRepo),
                CreatePlanUseCase(planRepo),
                CreateRoutineUseCase(routineRepo),
                DeletePlanUseCase(planRepo),
                DeleteRoutineUseCase(routineRepo)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
