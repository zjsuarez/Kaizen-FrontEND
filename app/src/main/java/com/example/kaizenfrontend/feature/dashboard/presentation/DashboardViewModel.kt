package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineExerciseResponse
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutSetResponseDto
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel
@Inject
constructor(
    private val repository: DashboardRepository,
    private val dashboardPreferences: DashboardPreferences,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val workoutRepository: WorkoutRepository,
    private val routineApiService: RoutineApiService,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val sessionManager by lazy { SessionManager(appContext) }

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

    private val _nextWorkoutExercises = MutableStateFlow<List<NextWorkoutExerciseUi>>(emptyList())
    val nextWorkoutExercises: StateFlow<List<NextWorkoutExerciseUi>> = _nextWorkoutExercises.asStateFlow()

    private val _workoutsByDate = MutableStateFlow<Map<LocalDate, List<CalendarWorkoutUi>>>(emptyMap())
    val workoutsByDate: StateFlow<Map<LocalDate, List<CalendarWorkoutUi>>> = _workoutsByDate.asStateFlow()

    val widgetOrder: StateFlow<List<String>> =
            dashboardPreferences.widgetOrder.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = listOf("NEXT_WORKOUT", "WEIGHT_TREND")
            )

    // Edit Mode
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

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
                    loadNextWorkoutExercises(data.nextWorkout?.routineId)
                }
            }
        }
        refreshDashboardData()
        fetchWeightHistory()
        fetchWorkoutHistory()
    }

    private fun fetchWeightHistory() {
        viewModelScope.launch {
            repository.getWeightHistory().onSuccess { history -> _weightHistory.value = history }
        }
    }

    private fun fetchWorkoutHistory() {
        viewModelScope.launch {
            workoutRepository
                .getWorkouts()
                .onSuccess { workouts ->
                    _workoutsByDate.value = mapWorkoutsByDate(workouts)
                }
        }
    }

    private suspend fun loadNextWorkoutExercises(routineId: String?) {
        if (routineId.isNullOrBlank()) {
            _nextWorkoutExercises.value = emptyList()
            return
        }

        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            _nextWorkoutExercises.value = emptyList()
            return
        }

        val response = runCatching {
            routineApiService.getRoutine(token = "Bearer $token", routineId = routineId)
        }.getOrNull()

        val routineDto = response?.takeIf { it.isSuccessful }?.body()
        val routineExercises = (routineDto?.exercises ?: routineDto?.listRoutineExercises).orEmpty()

        _nextWorkoutExercises.value =
            routineExercises
                .sortedBy { it.orderIndex ?: Int.MAX_VALUE }
                .mapIndexed { index, exercise ->
                    NextWorkoutExerciseUi(
                        name = resolveRoutineExerciseName(exercise, index),
                        targetSets = exercise.targetSets?.coerceAtLeast(1)
                    )
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
                                fetchWorkoutHistory()
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
                                fetchWorkoutHistory()
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
                fetchWorkoutHistory()
            } else {
                android.util.Log.e("KAIZEN", "Failed to save workout: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun mapWorkoutsByDate(workouts: List<WorkoutResponseDto>): Map<LocalDate, List<CalendarWorkoutUi>> {
        return workouts
            .mapNotNull { workout ->
                val timestamp = workout.endTime ?: workout.startTime
                val date = parseToLocalDate(timestamp) ?: return@mapNotNull null
                date to
                    CalendarWorkoutUi(
                        workoutId = workout.id,
                        routineName = workout.routineName?.takeIf { it.isNotBlank() } ?: "Workout",
                        completedAt = timestamp,
                        exerciseSummaries = summarizeWorkoutExercises(workout.sets)
                    )
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .mapValues { (_, dayWorkouts) ->
                dayWorkouts.sortedByDescending { parseToEpochMillis(it.completedAt) }
            }
    }

    private fun summarizeWorkoutExercises(sets: List<WorkoutSetResponseDto>): List<String> {
        return sets
            .groupBy { set -> resolveWorkoutSetExerciseName(set) }
            .entries
            .sortedBy { it.key.lowercase(Locale.getDefault()) }
            .map { (exerciseName, groupedSets) ->
                "$exerciseName - ${groupedSets.size} sets"
            }
    }

    private fun resolveWorkoutSetExerciseName(set: WorkoutSetResponseDto): String {
        val raw =
            set.exerciseName?.takeIf { it.isNotBlank() }
                ?: set.builtinExerciseKey?.takeIf { it.isNotBlank() }
                ?: set.customExerciseId?.takeIf { it.isNotBlank() }
                ?: "Exercise"
        return humanizeIdentifier(raw)
    }

    private fun resolveRoutineExerciseName(exercise: RoutineExerciseResponse, index: Int): String {
        val raw =
            exercise.builtinExerciseKey?.takeIf { it.isNotBlank() }
                ?: exercise.customExerciseId?.takeIf { it.isNotBlank() }
                ?: exercise.id?.takeIf { it.isNotBlank() }
                ?: "Exercise ${index + 1}"
        return humanizeIdentifier(raw)
    }

    private fun humanizeIdentifier(raw: String): String {
        val normalized = raw.trim().replace('_', ' ').replace('-', ' ')
        return normalized
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                token
                    .lowercase(Locale.getDefault())
                    .replaceFirstChar {
                        if (it.isLowerCase()) {
                            it.titlecase(Locale.getDefault())
                        } else {
                            it.toString()
                        }
                    }
            }
    }

    private fun parseToLocalDate(rawDate: String?): LocalDate? {
        val value = rawDate?.trim().orEmpty()
        if (value.isBlank()) return null

        return runCatching { OffsetDateTime.parse(value).toLocalDate() }.getOrNull()
            ?: runCatching {
                Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.getOrNull()
            ?: runCatching { LocalDate.parse(value.take(10)) }.getOrNull()
    }

    private fun parseToEpochMillis(rawDate: String?): Long {
        val value = rawDate?.trim().orEmpty()
        if (value.isBlank()) return 0L

        return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
            ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
            ?: 0L
    }

    companion object {
        private const val DASHBOARD_SYNC_WORK_NAME = "dashboard_layout_sync"
    }
}
