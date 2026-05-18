package com.example.kaizenfrontend.feature.workouts.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.dashboard.data.remote.api.DashboardApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutHistoryViewModel(
    private val repository: WorkoutRepositoryImpl,
    private val dashboardApiService: DashboardApiService
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<WorkoutResponseDto>>(emptyList())
    val workouts: StateFlow<List<WorkoutResponseDto>> = _workouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // measurementId -> progressPhotoUrl
    private val _photoUrlByMeasurementId = MutableStateFlow<Map<String, String>>(emptyMap())
    val photoUrlByMeasurementId: StateFlow<Map<String, String>> = _photoUrlByMeasurementId.asStateFlow()

    val hasWorkouts: Boolean get() = _workouts.value.isNotEmpty()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val workoutsDeferred = async { repository.getWorkouts() }
            val measurementsDeferred = async {
                try { dashboardApiService.getWeightHistory() } catch (e: Exception) { null }
            }

            workoutsDeferred.await()
                .onSuccess { list -> _workouts.value = list.sortedByDescending { it.startTime } }
                .onFailure { _error.value = it.message }

            measurementsDeferred.await()?.let { response ->
                if (response.isSuccessful) {
                    _photoUrlByMeasurementId.value = response.body()
                        ?.filter { !it.progressPhotoUrl.isNullOrBlank() }
                        ?.associate { it.id to it.progressPhotoUrl!! }
                        ?: emptyMap()
                }
            }

            _isLoading.value = false
        }
    }
}

class WorkoutHistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val ep = context.applicationContext.hiltServiceEntryPoint()
        val repo = WorkoutRepositoryImpl(ep.workoutApiService())
        @Suppress("UNCHECKED_CAST")
        return WorkoutHistoryViewModel(repo, ep.dashboardApiService()) as T
    }
}
