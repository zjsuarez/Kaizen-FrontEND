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
import com.example.kaizenfrontend.feature.dashboard.domain.model.RegisterBodyMeasurementCommand
import com.example.kaizenfrontend.feature.dashboard.domain.usecase.RegisterBodyMeasurementUseCase
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
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
    private val registerBodyMeasurementUseCase: RegisterBodyMeasurementUseCase,
    private val workoutRepository: WorkoutRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    sealed interface WorkoutFinishSubmissionState {
        data object Idle : WorkoutFinishSubmissionState
        data object Submitting : WorkoutFinishSubmissionState
        data class Success(val recordsBeaten: Int, val imageUrl: String?) : WorkoutFinishSubmissionState
        data class Error(val message: String) : WorkoutFinishSubmissionState
    }

    sealed interface BodyMeasurementRegistrationState {
        data object Idle : BodyMeasurementRegistrationState
        data object Submitting : BodyMeasurementRegistrationState
        data class Success(
            val measurementId: String,
            val progressPhotoUrl: String,
            val recordedAt: String
        ) : BodyMeasurementRegistrationState
        data class Error(val message: String) : BodyMeasurementRegistrationState
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

    private val _bodyMeasurementRegistration =
        MutableStateFlow<BodyMeasurementRegistrationState>(BodyMeasurementRegistrationState.Idle)
    val bodyMeasurementRegistration: StateFlow<BodyMeasurementRegistrationState> =
        _bodyMeasurementRegistration.asStateFlow()

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

    fun resetBodyMeasurementRegistration() {
        _bodyMeasurementRegistration.value = BodyMeasurementRegistrationState.Idle
    }

    fun registerBodyMeasurementExample(
        weightKg: Double,
        bodyFatPercentage: Double,
        progressPhotoUri: Uri
    ) {
        viewModelScope.launch {
            _bodyMeasurementRegistration.value = BodyMeasurementRegistrationState.Submitting

            val imageBytes = appContext.contentResolver.openInputStream(progressPhotoUri)?.use { it.readBytes() }
            if (imageBytes == null || imageBytes.isEmpty()) {
                _bodyMeasurementRegistration.value = BodyMeasurementRegistrationState.Error(
                    "No se pudo leer la imagen seleccionada"
                )
                return@launch
            }

            val fileName = progressPhotoUri.lastPathSegment?.substringAfterLast('/')
                ?: "progress-photo.jpg"

            val result = registerBodyMeasurementUseCase(
                RegisterBodyMeasurementCommand(
                    weightKg = weightKg,
                    bodyFatPercentage = bodyFatPercentage,
                    progressPhotoBytes = imageBytes,
                    fileName = fileName
                )
            )

            _bodyMeasurementRegistration.value = result.fold(
                onSuccess = {
                    BodyMeasurementRegistrationState.Success(
                        measurementId = it.id,
                        progressPhotoUrl = it.progressPhotoUrl,
                        recordedAt = it.recordedAt
                    )
                },
                onFailure = {
                    BodyMeasurementRegistrationState.Error(
                        it.message ?: "Error registrando medicion corporal"
                    )
                }
            )
        }
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
                val imageBytes = appContext.contentResolver
                    .openInputStream(imageUri)
                    ?.use { it.readBytes() }

                if (imageBytes == null || imageBytes.isEmpty()) {
                    android.util.Log.w(
                        "KAIZEN",
                        "Measurement upload skipped: selected image could not be read"
                    )
                } else {
                    val fileName = imageUri.lastPathSegment?.substringAfterLast('/')
                        ?: "progress-photo.jpg"

                    val measurementResult = registerBodyMeasurementUseCase(
                        RegisterBodyMeasurementCommand(
                            weightKg = null,
                            bodyFatPercentage = null,
                            progressPhotoBytes = imageBytes,
                            fileName = fileName
                        )
                    )

                    if (measurementResult.isSuccess) {
                        val response = measurementResult.getOrNull()
                        uploadedImageUrl = response?.progressPhotoUrl
                        android.util.Log.i(
                            "KAIZEN",
                            "Measurement upsert success: id=${response?.id}, recordedAt=${response?.recordedAt}, url=${response?.progressPhotoUrl}"
                        )
                        fetchWeightHistory()
                    } else {
                        android.util.Log.w(
                            "KAIZEN",
                            "Body measurement multipart upload failed, workout already saved: ${measurementResult.exceptionOrNull()?.message}"
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

    companion object {
        private const val DASHBOARD_SYNC_WORK_NAME = "dashboard_layout_sync"
    }
}
