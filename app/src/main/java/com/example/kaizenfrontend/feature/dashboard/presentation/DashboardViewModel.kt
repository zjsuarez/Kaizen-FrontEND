package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDashboardStream().collectLatest { data ->
                if (data != null) {
                    _uiState.value = DashboardUiState.Success(data)
                }
            }
        }
        refreshDashboardData()
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            // Only set to loading if we don't already have data
            if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }
            
            repository.refreshDashboard().fold(
                onSuccess = {
                    // Success is handled reactively by getDashboardStream() Flow
                },
                onFailure = { error ->
                    // CRITICAL LOGIC: If ALREADY Success (meaning Room cache loaded), DO NOTHING.
                    // Keep Success state so the user seamlessly sees cached data.
                    // ONLY overwrite with Error if the database was completely empty.
                    if (_uiState.value !is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Error(error.message ?: "Failed to connect to backend.")
                    }
                }
            )
        }
    }

    fun logBodyWeight(weight: Double) {
        viewModelScope.launch {
            repository.logBodyWeight(weight).fold(
                onSuccess = {
                    // Re-fetch data to reflect the new weight in the trend widget
                    refreshDashboardData()
                },
                onFailure = { _ ->
                    // Normally expose an effect channel to the UI for a Snackbar. Ignored for now.
                }
            )
        }
    }
}
