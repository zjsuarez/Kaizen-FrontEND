package com.example.kaizenfrontend.feature.workouts.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutHistoryViewModel(
    private val repository: WorkoutRepositoryImpl
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<WorkoutResponseDto>>(emptyList())
    val workouts: StateFlow<List<WorkoutResponseDto>> = _workouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val hasWorkouts: Boolean get() = _workouts.value.isNotEmpty()

    init {
        loadWorkouts()
    }

    fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getWorkouts()
                .onSuccess { list ->
                    _workouts.value = list.sortedByDescending { it.startTime }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}

class WorkoutHistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val ep = context.applicationContext.hiltServiceEntryPoint()
        val repo = WorkoutRepositoryImpl(ep.workoutApiService())
        @Suppress("UNCHECKED_CAST")
        return WorkoutHistoryViewModel(repo) as T
    }
}
