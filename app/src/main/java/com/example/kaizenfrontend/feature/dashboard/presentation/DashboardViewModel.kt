package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
import com.example.kaizenfrontend.feature.workouts.domain.repository.ImgurRepository
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val imgurRepository: ImgurRepository,
    private val workoutRepository: WorkoutRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    sealed interface WorkoutFinishSubmissionState {
        data object Idle : WorkoutFinishSubmissionState
        data object Submitting : WorkoutFinishSubmissionState
        data class Success(val recordsBeaten: Int, val imageUrl: String?) : WorkoutFinishSubmissionState
        data class Error(val message: String) : WorkoutFinishSubmissionState
    }

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

    private val _workoutFinishSubmission =
        MutableStateFlow<WorkoutFinishSubmissionState>(WorkoutFinishSubmissionState.Idle)
    val workoutFinishSubmission: StateFlow<WorkoutFinishSubmissionState> =
        _workoutFinishSubmission.asStateFlow()

    private val _estimatedRecordsBeaten = MutableStateFlow<Int?>(null)
    val estimatedRecordsBeaten: StateFlow<Int?> = _estimatedRecordsBeaten.asStateFlow()

    private val _historicalWorkoutCount = MutableStateFlow(0)
    val historicalWorkoutCount: StateFlow<Int> = _historicalWorkoutCount.asStateFlow()

    fun toggleEditMode() {
        val wasEditing = _isEditing.value
        _isEditing.value = !wasEditing

        // When leaving edit mode, enqueue background sync.
        // Worker reads latest DataStore order and syncs once network is available.
        if (wasEditing) {
            enqueueDashboardSync()
        }
    }

    private fun enqueueDashboardSync() {
        val request =
            OneTimeWorkRequestBuilder<DashboardSyncWorker>()
                .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            DASHBOARD_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
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

    fun resetWorkoutFinishSubmission() {
        _workoutFinishSubmission.value = WorkoutFinishSubmissionState.Idle
    }

    fun estimateRecordsForWorkout(state: ActiveWorkoutState) {
        viewModelScope.launch {
            _estimatedRecordsBeaten.value = null

            val historyResult = workoutRepository.getWorkouts()
            if (historyResult.isFailure) {
                _estimatedRecordsBeaten.value = 0
                return@launch
            }

            val history = historyResult.getOrDefault(emptyList())
            _historicalWorkoutCount.value = history.size
            val bestHistoryVolumeByExercise = mutableMapOf<String, Double>()

            history.forEach { workout ->
                workout.sets.forEach { set ->
                    val key = when {
                        !set.customExerciseId.isNullOrBlank() -> "custom:${set.customExerciseId}"
                        !set.builtinExerciseKey.isNullOrBlank() -> "builtin:${set.builtinExerciseKey}"
                        !set.exerciseName.isNullOrBlank() -> "name:${set.exerciseName.lowercase()}"
                        else -> return@forEach
                    }
                    val volume = (set.weightKg ?: 0.0) * (set.reps ?: 0)
                    val currentMax = bestHistoryVolumeByExercise[key] ?: 0.0
                    if (volume > currentMax) {
                        bestHistoryVolumeByExercise[key] = volume
                    }
                }
            }

            val estimated = state.exercises.sumOf { exercise ->
                val key = if (exercise.isCustom) {
                    "custom:${exercise.id}"
                } else {
                    "builtin:${exercise.id}"
                }
                val previousBest = bestHistoryVolumeByExercise[key] ?: 0.0
                exercise.sets.count { set ->
                    val volume = (set.weight.toDoubleOrNull() ?: 0.0) * (set.reps.toIntOrNull() ?: 0)
                    volume > previousBest && volume > 0.0
                }
            }

            _estimatedRecordsBeaten.value = estimated
        }
    }

    fun submitFinishedWorkout(
        state: ActiveWorkoutState,
        notes: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _workoutFinishSubmission.value = WorkoutFinishSubmissionState.Submitting

            val normalizedState = state.copy(notes = notes.trim())

            val workoutResult = saveWorkoutUseCase(normalizedState)
            if (workoutResult.isFailure) {
                _workoutFinishSubmission.value = WorkoutFinishSubmissionState.Error(
                    workoutResult.exceptionOrNull()?.message ?: "Failed to save workout"
                )
                return@launch
            }

            val recordsBeaten = workoutResult.getOrNull()
                ?.sets
                ?.count { it.isPR }
                ?: 0

            var uploadedImageUrl: String? = null
            if (imageUri != null) {
                val uploadResult = uploadImageToImgur(imageUri)
                if (uploadResult.isSuccess) {
                    uploadedImageUrl = uploadResult.getOrNull()
                } else {
                    android.util.Log.w(
                        "KAIZEN",
                        "Imgur upload failed, continuing workout finish without photo: ${uploadResult.exceptionOrNull()?.message}"
                    )
                }

                if (!uploadedImageUrl.isNullOrBlank()) {
                    val bodyMeasurementResult = repository.createBodyMeasurement(
                        progressPhotoUrl = uploadedImageUrl
                    )
                    if (bodyMeasurementResult.isFailure) {
                        android.util.Log.w(
                            "KAIZEN",
                            "Body measurement photo entry failed, workout already saved: ${bodyMeasurementResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            }

            refreshDashboardData()
            _workoutFinishSubmission.value = WorkoutFinishSubmissionState.Success(
                recordsBeaten = recordsBeaten,
                imageUrl = uploadedImageUrl
            )
        }
    }

    private suspend fun uploadImageToImgur(uri: Uri): Result<String> {
        return runCatching {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "workout_finish.jpg"
            val imageBytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Unable to read selected image")
            imageBytes to fileName
        }.fold(
            onSuccess = { (bytes, fileName) -> imgurRepository.uploadImage(bytes, fileName) },
            onFailure = { Result.failure(it) }
        )
    }

    companion object {
        private const val DASHBOARD_SYNC_WORK_NAME = "dashboard_layout_sync"
    }
}
