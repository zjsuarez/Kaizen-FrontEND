package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
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


@HiltViewModel
class DashboardViewModel
@Inject
constructor(
        private val repository: DashboardRepository,
        private val dashboardPreferences: DashboardPreferences,
        private val saveWorkoutUseCase: SaveWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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

        // When leaving edit mode → sync final order to the backend.
        // Local DataStore is already up-to-date; this is a best-effort remote sync.
        if (wasEditing) {
            saveLayoutToApi(widgetOrder.value)
        }
    }

    // Fire-and-forget PUT to /api/preferences/dashboard
    private fun saveLayoutToApi(order: List<String>) {
        viewModelScope.launch {
            try {
                repository.saveWidgetOrder(order)
                    .onFailure { error ->
                        android.util.Log.w("KAIZEN", "PUT layout sync failed (offline?): ${error.message}")
                    }
            } catch (e: Exception) {
                android.util.Log.e("KAIZEN", "Unexpected error syncing layout: ${e.message}")
            }
        }
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
                                                    error.message ?: "Failed to connect to backend."
                                            )
                                }
                            }
                    )
        }
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
        viewModelScope.launch {
            val result = saveWorkoutUseCase(state)
            if (result.isSuccess) {
                android.util.Log.d("KAIZEN", "Workout saved successfully! Updating dashboard...")
                refreshDashboardData()
            } else {
                android.util.Log.e("KAIZEN", "Failed to save workout: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
