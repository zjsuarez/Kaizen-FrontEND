package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState

sealed class WorkoutSaveStatus {
    data object Idle : WorkoutSaveStatus()
    data object Saving : WorkoutSaveStatus()
    data object Success : WorkoutSaveStatus()
    data class Error(val message: String) : WorkoutSaveStatus()
}

@HiltViewModel
class DashboardViewModel
@Inject
constructor(
        private val repository: DashboardRepository,
        private val dashboardPreferences: DashboardPreferences,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _workoutSaveStatus = MutableStateFlow<WorkoutSaveStatus>(WorkoutSaveStatus.Idle)
    val workoutSaveStatus: StateFlow<WorkoutSaveStatus> = _workoutSaveStatus.asStateFlow()

    private val _weightHistory =
            MutableStateFlow<
                    List<
                            com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>>(
                    emptyList()
            )
    val weightHistory:
            StateFlow<
                    List<
                            com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>> =
            _weightHistory.asStateFlow()

        private val _showGoogleWelcomePrompt = MutableStateFlow(false)
        val showGoogleWelcomePrompt: StateFlow<Boolean> = _showGoogleWelcomePrompt.asStateFlow()

        private val sessionManager by lazy { SessionManager(appContext) }
        private val userRepository by lazy {
            UserRepositoryImpl(
                appContext.hiltServiceEntryPoint().userApiService(),
                sessionManager
            )
        }

    val widgetOrder: StateFlow<List<String>> =
            dashboardPreferences.widgetOrder.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = listOf("NEXT_WORKOUT", "WEIGHT_TREND")
            )

    // Edit Mode
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    fun toggleEditMode() {
        val wasEditing = _isEditing.value
        _isEditing.value = !wasEditing

        // When leaving edit mode, enqueue background sync.
        // Worker reads latest DataStore order and syncs once network is available.
        if (wasEditing) {
            enqueueDashboardSync()
        }
    }

    private fun enqueueDashboardSync() {
        val request =
            OneTimeWorkRequestBuilder<DashboardSyncWorker>()
                .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            DASHBOARD_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun removeWidget(widgetKey: String) {
        val updated = widgetOrder.value.toMutableList().apply { remove(widgetKey) }
        onReorderWidgets(updated)
    }

    fun addWidget(widgetKey: String) {
        if (widgetKey !in widgetOrder.value) {
            val updated = widgetOrder.value + widgetKey
            onReorderWidgets(updated)
        }
    }

    fun moveWidgetUp(widgetKey: String) {
        val list = widgetOrder.value.toMutableList()
        val idx = list.indexOf(widgetKey)
        if (idx > 0) {
            list.add(idx - 1, list.removeAt(idx))
            onReorderWidgets(list)
        }
    }

    fun moveWidgetDown(widgetKey: String) {
        val list = widgetOrder.value.toMutableList()
        val idx = list.indexOf(widgetKey)
        if (idx >= 0 && idx < list.lastIndex) {
            list.add(idx + 1, list.removeAt(idx))
            onReorderWidgets(list)
        }
    }

    init {
        viewModelScope.launch {
            repository.getDashboardStream().collectLatest { data ->
                if (data != null) {
                    android.util.Log.d("KAIZEN", "New weight received: ${data.currentWeight}")
                    _uiState.value = DashboardUiState.Success(data)
                }
            }
        }
        refreshDashboardData()
        fetchWeightHistory()
        evaluateGoogleWelcomePrompt()
    }

    private fun evaluateGoogleWelcomePrompt() {
        viewModelScope.launch {
            if (!sessionManager.shouldShowGoogleWelcomePrompt()) {
                _showGoogleWelcomePrompt.value = false
                return@launch
            }

            userRepository.getCurrentUser().onSuccess { user ->
                _showGoogleWelcomePrompt.value = user.authProvider.equals("GOOGLE", ignoreCase = true)
            }.onFailure {
                _showGoogleWelcomePrompt.value = false
            }
        }
    }

    fun dismissGoogleWelcomePrompt() {
        sessionManager.clearGoogleWelcomePrompt()
        _showGoogleWelcomePrompt.value = false
    }

    private fun fetchWeightHistory() {
        viewModelScope.launch {
            repository.getWeightHistory().onSuccess { history -> _weightHistory.value = history }
        }
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            // Only set to loading if we don't already have data
            if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }

            repository
                    .refreshDashboard()
                    .fold(
                            onSuccess = {
                                // Success is handled reactively by getDashboardStream() Flow
                            },
                            onFailure = { error ->
                                // CRITICAL LOGIC: If ALREADY Success (meaning Room cache loaded),
                                // DO NOTHING.
                                // Keep Success state so the user seamlessly sees cached data.
                                // ONLY overwrite with Error if the database was completely empty.
                                if (_uiState.value !is DashboardUiState.Success) {
                                    _uiState.value =
                                            DashboardUiState.Error(
                                                    message = error.message,
                                                    messageResId = R.string.dashboard_error_backend_connection
                                            )
                                }
                            }
                    )
        }
    }

    fun onScreenFocused() {
        refreshDashboardData()
        fetchWeightHistory()
        evaluateGoogleWelcomePrompt()
    }

    fun logBodyWeight(weight: Double) {
        viewModelScope.launch {
            repository
                    .logBodyWeight(weight)
                    .fold(
                            onSuccess = {
                                refreshDashboardData()
                                fetchWeightHistory()
                            },
                            onFailure = { _ ->
                                // Normally expose an effect channel to the UI for a Snackbar.
                                // Ignored for now.
                            }
                    )
        }
    }

    fun onReorderWidgets(newOrderedList: List<String>) {
        viewModelScope.launch { dashboardPreferences.saveWidgetOrder(newOrderedList) }
    }
    fun saveWorkout(state: ActiveWorkoutState) {
        _workoutSaveStatus.value = WorkoutSaveStatus.Saving
        viewModelScope.launch {
            val unitSystem = sessionManager.getUserUnitSystem() ?: "METRIC"
            val result = saveWorkoutUseCase(state, unitSystem)
            if (result.isSuccess) {
                android.util.Log.d("KAIZEN", "Workout saved successfully! Updating dashboard...")
                _workoutSaveStatus.value = WorkoutSaveStatus.Success
                refreshDashboardData()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("KAIZEN", "Failed to save workout: $errorMsg")
                _workoutSaveStatus.value = WorkoutSaveStatus.Error(errorMsg)
            }
        }
    }

    fun resetWorkoutSaveStatus() {
        _workoutSaveStatus.value = WorkoutSaveStatus.Idle
    }

    companion object {
        private const val DASHBOARD_SYNC_WORK_NAME = "dashboard_layout_sync"
    }
}
