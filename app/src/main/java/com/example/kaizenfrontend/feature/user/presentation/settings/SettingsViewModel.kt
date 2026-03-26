package com.example.kaizenfrontend.feature.user.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import com.example.kaizenfrontend.feature.user.domain.usecase.GetCurrentUserUseCase
import com.example.kaizenfrontend.feature.user.domain.usecase.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val email: String = "",
    val unitSystem: String = "METRIC",
    val effortMetric: String = "RPE",
    val defaultRest: String = "90 s",
    val savedUnit: String = "METRIC",
    val savedEffort: String = "RPE",
    val isLoading: Boolean = false,
    val isSavingPrefs: Boolean = false,
    val errorMessage: String? = null
) {
    val hasUnsavedChanges: Boolean get() = unitSystem != savedUnit || effortMetric != savedEffort
}

class SettingsViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val repository = UserRepositoryImpl(sessionManager)
    private val getCurrentUserUseCase = GetCurrentUserUseCase(repository)
    private val updateUserUseCase = UpdateUserUseCase(repository)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        // Seed from local cache first for instant display
        val cachedEmail = sessionManager.getUserEmail() ?: ""
        val cachedUnit = sessionManager.getUserUnitSystem() ?: "METRIC"
        val cachedEffort = sessionManager.getUserEffortMetric() ?: "RPE"
        val cachedRest = sessionManager.getUserDefaultRest() ?: "90 s"
        _uiState.update {
            it.copy(
                email = cachedEmail,
                unitSystem = cachedUnit,
                savedUnit = cachedUnit,
                effortMetric = cachedEffort,
                savedEffort = cachedEffort,
                defaultRest = cachedRest
            )
        }

        // Then fetch fresh from API
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getCurrentUserUseCase()
            result.onSuccess { user ->
                val rest = "${user.restTimerDefault} s"
                sessionManager.saveUserPreferences(user.email, user.unitSystem, user.effortMeasurement, rest)
                _uiState.update {
                    it.copy(
                        email = user.email,
                        unitSystem = user.unitSystem,
                        savedUnit = user.unitSystem,
                        effortMetric = user.effortMeasurement,
                        savedEffort = user.effortMeasurement,
                        defaultRest = rest,
                        isLoading = false
                    )
                }
            }
            result.onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun setUnitSystem(unit: String) = _uiState.update { it.copy(unitSystem = unit) }
    fun setEffortMetric(effort: String) = _uiState.update { it.copy(effortMetric = effort) }
    fun setDefaultRest(seconds: Int) {
        val rest = "$seconds s"
        _uiState.update { it.copy(defaultRest = rest) }
        updateProfile(UserUpdateRequest(restTimerDefault = seconds))
    }

    fun savePreferences() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrefs = true) }
            val result = updateUserUseCase(
                UserUpdateRequest(unitSystem = state.unitSystem, effortMeasurement = state.effortMetric)
            )
            result.onSuccess {
                sessionManager.saveUserPreferences(state.email, state.unitSystem, state.effortMetric, state.defaultRest)
                _uiState.update {
                    it.copy(savedUnit = it.unitSystem, savedEffort = it.effortMetric, isSavingPrefs = false)
                }
            }
            result.onFailure { _uiState.update { it.copy(isSavingPrefs = false) } }
        }
    }

    fun changePassword(newPassword: String) {
        updateProfile(UserUpdateRequest(password = newPassword))
    }

    fun logout() {
        sessionManager.clearToken()
        sessionManager.saveCalibrationComplete(false)
    }

    private fun updateProfile(request: UserUpdateRequest) {
        viewModelScope.launch {
            val state = _uiState.value
            updateUserUseCase(request).onSuccess {
                sessionManager.saveUserPreferences(state.email, state.unitSystem, state.effortMetric, state.defaultRest)
            }
        }
    }
}
