package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.lifecycle.ViewModel
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlanDetailsViewModel(
    planId: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialRoutines: List<Routine> = emptyList(),
    initialIsActive: Boolean = true
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlanDetailsState(
            planId = planId,
            title = initialTitle,
            description = initialDescription,
            routines = initialRoutines,
            isActive = initialIsActive
        )
    )
    val uiState: StateFlow<PlanDetailsState> = _uiState.asStateFlow()

    fun toggleEditMode() {
        _uiState.update { current ->
            current.copy(isEditMode = !current.isEditMode)
        }
    }

    fun toggleActive() {
        _uiState.update { current ->
            current.copy(isActive = !current.isActive)
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

    fun removeRoutine(routineId: String) {
        _uiState.update { current ->
            current.copy(
                routines = current.routines.filterNot { it.id == routineId }
            )
        }
    }

    fun moveRoutine(fromIndex: Int, toIndex: Int) {
        _uiState.update { current ->
            val list = current.routines
            if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
                return@update current
            }

            val mutable = list.toMutableList()
            val moved = mutable.removeAt(fromIndex)
            mutable.add(toIndex, moved)
            current.copy(routines = mutable)
        }
    }

    fun saveChanges() {
        _uiState.update { current ->
            current.copy(isEditMode = false)
        }
    }
}
