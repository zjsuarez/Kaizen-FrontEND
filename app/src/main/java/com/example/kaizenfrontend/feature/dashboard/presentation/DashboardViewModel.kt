package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val result = repository.fetchDashboardData()
            
            result.fold(
                onSuccess = { response ->
                    _uiState.value = DashboardUiState.Success(response)
                },
                onFailure = { error ->
                    _uiState.value = DashboardUiState.Error(error.message ?: "An unexpected error occurred")
                }
            )
        }
    }

    fun logBodyWeight(weight: Double) {
        viewModelScope.launch {
            repository.logBodyWeight(weight).fold(
                onSuccess = {
                    // Re-fetch data to reflect the new weight in the trend widget
                    fetchDashboardData()
                },
                onFailure = { error ->
                    // Normally you would expose an effect channel for a Snackbar
                    // For now, if dashboard API fails we could log or show an error
                }
            )
        }
    }
}
