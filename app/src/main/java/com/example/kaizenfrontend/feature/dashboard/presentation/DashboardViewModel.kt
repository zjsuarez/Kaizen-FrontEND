package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _weightHistory = MutableStateFlow<
        List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>
    >(emptyList())
    val weightHistory: StateFlow<
        List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>
    > = _weightHistory.asStateFlow()

    private val _showGoogleWelcomePrompt = MutableStateFlow(false)
    val showGoogleWelcomePrompt: StateFlow<Boolean> = _showGoogleWelcomePrompt.asStateFlow()

    private val sessionManager by lazy { SessionManager(appContext) }
    private val userRepository by lazy {
        UserRepositoryImpl(
            appContext.hiltServiceEntryPoint().userApiService(),
            sessionManager
        )
    }

    init {
        viewModelScope.launch {
            repository.getDashboardStream().collectLatest { data ->
                if (data != null) {
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

            repository.refreshDashboard().fold(
                onSuccess = {
                    // Success is handled reactively by getDashboardStream() Flow
                },
                onFailure = { error ->
                    // If ALREADY Success (meaning Room cache loaded), DO NOTHING.
                    // Keep Success state so the user sees cached data seamlessly.
                    // ONLY overwrite with Error if the database was completely empty.
                    if (_uiState.value !is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Error(
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
            repository.logBodyWeight(weight).fold(
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
}
