package com.example.kaizenfrontend.feature.user.presentation.calibration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.user.data.remote.dto.UserUpdateRequest
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import com.example.kaizenfrontend.feature.user.domain.usecase.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CalibrationUiState {
    object Idle : CalibrationUiState()
    object Loading : CalibrationUiState()
    object Success : CalibrationUiState()
    data class Error(val message: String) : CalibrationUiState()
}

private fun normalizeCalibrationNumberInput(input: String): String =
    input.filter { it.isDigit() }.take(3)

class CalibrationViewModel(context: Context) : ViewModel() {

    private val sessionManager = SessionManager(context)
    private val userApiService = context.applicationContext.hiltServiceEntryPoint().userApiService()
    private val updateUserUseCase = UpdateUserUseCase(UserRepositoryImpl(userApiService, sessionManager))

    private val _uiState = MutableStateFlow<CalibrationUiState>(CalibrationUiState.Idle)
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    fun submitCalibration(selectedUnit: String, bodyWeight: String, selectedEffort: String) {
        viewModelScope.launch {
            _uiState.value = CalibrationUiState.Loading

            val normalizedBodyWeight = normalizeCalibrationNumberInput(bodyWeight)
            val unitSystemVal = if (selectedUnit == "KG") "METRIC" else "IMPERIAL"
            val parsedWeight = normalizedBodyWeight.toDoubleOrNull()
            val weightKgValue = parsedWeight?.let {
                if (selectedUnit == "LB") it * 0.453592 else it
            }

            val request = UserUpdateRequest(
                unitSystem = unitSystemVal,
                effortMeasurement = selectedEffort,
                weightKg = weightKgValue,
                primaryGoal = "General Fitness"
            )

            val result = updateUserUseCase(request)
            _uiState.value = result.fold(
                onSuccess = {
                    sessionManager.saveCalibrationComplete(true)
                    CalibrationUiState.Success
                },
                onFailure = { CalibrationUiState.Error(it.message ?: "Failed to save settings") }
            )
        }
    }

    fun resetState() {
        _uiState.value = CalibrationUiState.Idle
    }
}
