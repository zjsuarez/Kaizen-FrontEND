package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.lifecycle.ViewModel
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RoutineDetailsViewModel(
    routineId: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialExercises: List<RoutineExercise> = emptyList()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RoutineDetailsState(
            routineId = routineId,
            title = initialTitle,
            description = initialDescription,
            exercises = initialExercises
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

    fun saveChanges() {
        _uiState.update { current ->
            current.copy(isEditMode = false)
        }
    }
}
