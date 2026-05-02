package com.example.kaizenfrontend.core.ui.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutSaveStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the post-finish workout-save logic, hoisted out of
 * `DashboardViewModel`. Lives at the app-shell level so the
 * "Workout Summary" sheet can be presented from any tab the user
 * happens to be on when they tap Finish — not only from Dashboard.
 *
 * Emits a one-shot [workoutSaved] event that the dashboard observes
 * to trigger a fresh data fetch (PRs, streak, recovery may all change
 * after a save).
 */
@HiltViewModel
class ActiveWorkoutHostViewModel @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val sessionManager by lazy { SessionManager(appContext) }

    private val _saveStatus = MutableStateFlow<WorkoutSaveStatus>(WorkoutSaveStatus.Idle)
    val saveStatus: StateFlow<WorkoutSaveStatus> = _saveStatus.asStateFlow()

    private val _workoutSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val workoutSaved: SharedFlow<Unit> = _workoutSaved.asSharedFlow()

    fun saveWorkout(state: ActiveWorkoutState) {
        _saveStatus.value = WorkoutSaveStatus.Saving
        viewModelScope.launch {
            val unitSystem = sessionManager.getUserUnitSystem() ?: "METRIC"
            val result = saveWorkoutUseCase(state, unitSystem)
            if (result.isSuccess) {
                _saveStatus.value = WorkoutSaveStatus.Success
                _workoutSaved.tryEmit(Unit)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                _saveStatus.value = WorkoutSaveStatus.Error(errorMsg)
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = WorkoutSaveStatus.Idle
    }
}
