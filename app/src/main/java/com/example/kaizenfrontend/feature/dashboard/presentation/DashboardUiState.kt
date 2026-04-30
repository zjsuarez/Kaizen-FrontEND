package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.annotation.StringRes
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardResponse) : DashboardUiState
    data class Error(
        val message: String? = null,
        @StringRes val messageResId: Int? = null
    ) : DashboardUiState
    data object Empty : DashboardUiState
}
