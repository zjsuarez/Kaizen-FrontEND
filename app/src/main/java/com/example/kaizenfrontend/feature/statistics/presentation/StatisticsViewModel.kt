package com.example.kaizenfrontend.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class TimeRange(val label: String) {
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    LIFETIME("ALL")
}

data class StatisticsUiState(
    val selectedTimeRange: TimeRange = TimeRange.ONE_MONTH,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    fun updateTimeRange(newRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = newRange) }
        // TODO: Map to actual backend or local fetches in subsequent tasks
    }
}
