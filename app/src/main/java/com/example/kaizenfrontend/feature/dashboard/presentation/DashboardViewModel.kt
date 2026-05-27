package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.RecentPrDTO
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.StreakDayResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.TrainingDayDetailResponse
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.core.WorkoutStalenessFlag
import com.example.kaizenfrontend.core.data.BuiltinExerciseCatalog
import com.example.kaizenfrontend.feature.workouts.domain.ActiveExerciseInit
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import com.example.kaizenfrontend.feature.workouts.presentation.utils.RoutineScheduleCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineScheduleItem(val routineId: String, val routineName: String)

data class StreakMetrics(val currentStreak: Int, val longestStreak: Int)

sealed class WorkoutSaveStatus {
    data object Idle : WorkoutSaveStatus()
    data object Saving : WorkoutSaveStatus()
    data object Success : WorkoutSaveStatus()
    data class Error(val message: String) : WorkoutSaveStatus()
}

sealed class PhotoUploadStatus {
    data object Idle : PhotoUploadStatus()
    data object Uploading : PhotoUploadStatus()
    data object Success : PhotoUploadStatus()
    data class Error(val message: String) : PhotoUploadStatus()
}

sealed class WorkoutDetailState {
    data object Idle : WorkoutDetailState()
    data object Loading : WorkoutDetailState()
    data class Success(val workout: WorkoutResponseDto) : WorkoutDetailState()
    data class Error(val message: String) : WorkoutDetailState()
}

sealed class PrHistoryState {
    data object Idle : PrHistoryState()
    data object Loading : PrHistoryState()
    data class Success(val prs: List<RecentPrDTO>, val exerciseName: String) : PrHistoryState()
    data class Error(val message: String) : PrHistoryState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val dashboardPreferences: DashboardPreferences,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _workoutSaveStatus = MutableStateFlow<WorkoutSaveStatus>(WorkoutSaveStatus.Idle)
    val workoutSaveStatus: StateFlow<WorkoutSaveStatus> = _workoutSaveStatus.asStateFlow()

    private val _photoUploadStatus = MutableStateFlow<PhotoUploadStatus>(PhotoUploadStatus.Idle)
    val photoUploadStatus: StateFlow<PhotoUploadStatus> = _photoUploadStatus.asStateFlow()

    private val _capturedMeasurementId = MutableStateFlow<String?>(null)

    private val _weightHistory = MutableStateFlow<List<
            com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>>(emptyList())
    val weightHistory: StateFlow<List<
            com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>> =
        _weightHistory.asStateFlow()

    private val _showGoogleWelcomePrompt = MutableStateFlow(false)
    val showGoogleWelcomePrompt: StateFlow<Boolean> = _showGoogleWelcomePrompt.asStateFlow()

    private val _workoutDetailState = MutableStateFlow<WorkoutDetailState>(WorkoutDetailState.Idle)
    val workoutDetailState: StateFlow<WorkoutDetailState> = _workoutDetailState.asStateFlow()

    private val _prHistoryState = MutableStateFlow<PrHistoryState>(PrHistoryState.Idle)
    val prHistoryState: StateFlow<PrHistoryState> = _prHistoryState.asStateFlow()

    private val _workoutLaunchReady = MutableStateFlow(false)
    val workoutLaunchReady: StateFlow<Boolean> = _workoutLaunchReady.asStateFlow()

    private val _streakMetrics = MutableStateFlow(StreakMetrics(0, 0))
    val streakMetrics: StateFlow<StreakMetrics> = _streakMetrics.asStateFlow()

    private val _isLoggingWeight = MutableStateFlow(false)
    val isLoggingWeight: StateFlow<Boolean> = _isLoggingWeight.asStateFlow()

    private val _weightLoggedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val weightLoggedEvent: SharedFlow<Unit> = _weightLoggedEvent.asSharedFlow()

    private val sessionManager by lazy { SessionManager(appContext) }
    private val userRepository by lazy {
        UserRepositoryImpl(
            appContext.hiltServiceEntryPoint().userApiService(),
            sessionManager
        )
    }
    private val planApiService by lazy { appContext.hiltServiceEntryPoint().planApiService() }
    private val routineApiService by lazy { appContext.hiltServiceEntryPoint().routineApiService() }

    // DayOfWeek.value (1=Mon…7=Sun) → schedule item, derived from the active plan's real schedule
    private val _weekdaySchedule = MutableStateFlow<Map<Int, RoutineScheduleItem>>(emptyMap())
    val weekdaySchedule: StateFlow<Map<Int, RoutineScheduleItem>> = _weekdaySchedule.asStateFlow()

    val widgetOrder: StateFlow<List<String>> = dashboardPreferences.widgetOrder.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = listOf("NEXT_WORKOUT", "WEIGHT_TREND")
    )

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDashboardStream().collectLatest { data ->
                if (data != null) {
                    _uiState.value = DashboardUiState.Success(data)
                }
            }
        }
        // Recompute streak metrics whenever dashboard data or schedule changes.
        viewModelScope.launch {
            combine(_uiState, _weekdaySchedule) { state, schedule ->
                if (state is DashboardUiState.Success) {
                    computeStreakMetrics(
                        state.data.streakCalendar,
                        state.data.trainingDayDetails,
                        schedule
                    )
                } else StreakMetrics(0, 0)
            }.collect { _streakMetrics.value = it }
        }
        refreshDashboardData()
        fetchWeightHistory()
        evaluateGoogleWelcomePrompt()
        fetchActivePlanSchedule()
    }

    fun toggleEditMode() {
        val wasEditing = _isEditing.value
        _isEditing.value = !wasEditing
        if (wasEditing) enqueueDashboardSync()
    }

    private fun enqueueDashboardSync() {
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            DASHBOARD_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DashboardSyncWorker>().build()
        )
    }

    fun removeWidget(widgetKey: String) {
        onReorderWidgets(widgetOrder.value.toMutableList().apply { remove(widgetKey) })
    }

    fun addWidget(widgetKey: String) {
        if (widgetKey !in widgetOrder.value) {
            onReorderWidgets(widgetOrder.value + widgetKey)
        }
    }

    fun moveWidgetUp(widgetKey: String) {
        val list = widgetOrder.value.toMutableList()
        val idx = list.indexOf(widgetKey)
        if (idx > 0) { list.add(idx - 1, list.removeAt(idx)); onReorderWidgets(list) }
    }

    fun moveWidgetDown(widgetKey: String) {
        val list = widgetOrder.value.toMutableList()
        val idx = list.indexOf(widgetKey)
        if (idx >= 0 && idx < list.lastIndex) { list.add(idx + 1, list.removeAt(idx)); onReorderWidgets(list) }
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
            repository.getWeightHistory().onSuccess { _weightHistory.value = it }
        }
    }

    fun refreshDashboardData() {
        viewModelScope.launch {
            if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }
            repository.refreshDashboard().fold(
                onSuccess = {},
                onFailure = { error ->
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
        fetchActivePlanSchedule()
    }

    fun logBodyWeight(weight: Double) {
        if (_isLoggingWeight.value) return
        viewModelScope.launch {
            _isLoggingWeight.value = true
            repository.logBodyWeight(weight).fold(
                onSuccess = {
                    val current = _uiState.value
                    if (current is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Success(current.data.copy(currentWeight = weight))
                    }
                    refreshDashboardData()
                    fetchWeightHistory()
                    _isLoggingWeight.value = false
                    _weightLoggedEvent.tryEmit(Unit)
                },
                onFailure = { _isLoggingWeight.value = false }
            )
        }
    }

    fun onReorderWidgets(newOrderedList: List<String>) {
        viewModelScope.launch { dashboardPreferences.saveWidgetOrder(newOrderedList) }
    }

    fun saveWorkout(state: ActiveWorkoutState) {
        _workoutSaveStatus.value = WorkoutSaveStatus.Saving
        viewModelScope.launch {
            val unitSystem = sessionManager.getUserUnitSystem() ?: "METRIC"
            val measurementId = _capturedMeasurementId.value
            val result = saveWorkoutUseCase(state, unitSystem, measurementId)
            if (result.isSuccess) {
                _workoutSaveStatus.value = WorkoutSaveStatus.Success
                WorkoutStalenessFlag.isStatisticsStale = true
                refreshDashboardData()
            } else {
                _workoutSaveStatus.value = WorkoutSaveStatus.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun resetWorkoutSaveStatus() { _workoutSaveStatus.value = WorkoutSaveStatus.Idle }

    fun uploadProgressPhoto(uri: Uri) {
        _photoUploadStatus.value = PhotoUploadStatus.Uploading
        viewModelScope.launch {
            try {
                val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: run {
                        _photoUploadStatus.value = PhotoUploadStatus.Error("Could not read image")
                        return@launch
                    }
                val mimeType = appContext.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = "progress_${System.currentTimeMillis()}.jpg"
                repository.uploadProgressPhoto(bytes, mimeType, fileName).fold(
                    onSuccess = { id ->
                        _capturedMeasurementId.value = id
                        _photoUploadStatus.value = PhotoUploadStatus.Success
                    },
                    onFailure = {
                        _photoUploadStatus.value = PhotoUploadStatus.Error(it.message ?: "Upload failed")
                    }
                )
            } catch (e: Exception) {
                _photoUploadStatus.value = PhotoUploadStatus.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun resetPhotoUploadStatus() {
        _photoUploadStatus.value = PhotoUploadStatus.Idle
        _capturedMeasurementId.value = null
    }

    fun fetchWorkoutDetail(workoutId: String) {
        _workoutDetailState.value = WorkoutDetailState.Loading
        viewModelScope.launch {
            repository.getWorkoutById(workoutId).fold(
                onSuccess = { _workoutDetailState.value = WorkoutDetailState.Success(it) },
                onFailure = { _workoutDetailState.value = WorkoutDetailState.Error(it.message ?: "Error") }
            )
        }
    }

    fun clearWorkoutDetail() {
        _workoutDetailState.value = WorkoutDetailState.Idle
    }

    fun fetchPrHistory(exerciseName: String) {
        _prHistoryState.value = PrHistoryState.Loading
        viewModelScope.launch {
            repository.getPrHistory(exerciseName).fold(
                onSuccess = { _prHistoryState.value = PrHistoryState.Success(it, exerciseName) },
                onFailure = { _prHistoryState.value = PrHistoryState.Error(it.message ?: "Error") }
            )
        }
    }

    fun clearPrHistory() {
        _prHistoryState.value = PrHistoryState.Idle
    }

    fun startNextWorkout(routineId: String, routineName: String) {
        if (ActiveWorkoutManager.currentWorkout.value != null) return
        viewModelScope.launch {
            try {
                val token = "Bearer ${sessionManager.getToken() ?: return@launch}"
                val response = routineApiService.getRoutine(token, routineId)
                if (!response.isSuccessful) return@launch
                val routine = response.body() ?: return@launch

                val exercises = (routine.exercises ?: routine.listRoutineExercises)
                    .orEmpty()
                    .sortedBy { it.orderIndex ?: Int.MAX_VALUE }
                    .mapNotNull { dto ->
                        val builtinKey = dto.builtinExerciseKey
                        val customId = dto.customExerciseId
                        val id = builtinKey ?: customId ?: dto.id ?: return@mapNotNull null
                        val name = builtinKey
                            ?.let { BuiltinExerciseCatalog.resolveExerciseName(it) }
                            ?: customId
                            ?: id
                        ActiveExerciseInit(
                            id = id,
                            name = name,
                            isCustom = customId != null,
                            targetSets = (dto.targetSets ?: 3).coerceAtLeast(1)
                        )
                    }

                ActiveWorkoutManager.startWorkout(
                    routineId = routineId,
                    routineName = routineName,
                    exercises = exercises
                )
                _workoutLaunchReady.value = true
            } catch (_: Exception) { /* silently ignore — user can retry */ }
        }
    }

    fun consumeWorkoutLaunch() {
        _workoutLaunchReady.value = false
    }

    private fun fetchActivePlanSchedule() {
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer ${sessionManager.getToken() ?: return@launch}"

                // 1. Find the active training plan
                val plansResp = planApiService.getAllPlans(bearerToken)
                if (!plansResp.isSuccessful) return@launch
                val activePlan = plansResp.body()?.firstOrNull { it.isActive } ?: return@launch

                // 2. Fetch all routines for that plan
                val routinesResp = routineApiService.getUserRoutines(bearerToken, planId = activePlan.id)
                if (!routinesResp.isSuccessful) return@launch
                val routines = routinesResp.body() ?: return@launch

                // 3. Parse each routine's schedulingValue into the exact days of the week it runs
                val intervalConfig = PlanIntervalConfig.fromBackend(activePlan.interval, activePlan.cycleLength)
                val schedule = mutableMapOf<Int, RoutineScheduleItem>()

                routines.forEach { routine ->
                    val weekdays: Set<DayOfWeek> = when (intervalConfig.type) {
                        PlanIntervalType.FREQUENCY -> {
                            val parsed = RoutineScheduleCalculator.parseWeekDays(routine.schedulingValue)
                            if (parsed.isNotEmpty()) parsed
                            else {
                                RoutineScheduleCalculator.parseCycleDays(routine.schedulingValue)
                                    .mapNotNull { runCatching { DayOfWeek.of(it.coerceIn(1, 7)) }.getOrNull() }
                                    .toSet()
                            }
                        }
                        PlanIntervalType.CYCLE -> {
                            if (intervalConfig.cycleMode == CycleMode.WEEKLY) {
                                val parsed = RoutineScheduleCalculator.parseWeekDays(routine.schedulingValue)
                                if (parsed.isNotEmpty()) parsed
                                else {
                                    RoutineScheduleCalculator.parseCycleDays(routine.schedulingValue)
                                        .mapNotNull { runCatching { DayOfWeek.of(it.coerceIn(1, 7)) }.getOrNull() }
                                        .toSet()
                                }
                            } else {
                                emptySet() // Custom-length cycles can't be projected onto fixed weekdays
                            }
                        }
                    }
                    weekdays.forEach { dow -> schedule[dow.value] = RoutineScheduleItem(routine.id, routine.name) }
                }

                _weekdaySchedule.value = schedule
            } catch (e: Exception) {
                // Fail silently; calendar shows only completed workouts if schedule can't be loaded
            }
        }
    }

    // ── Streak business logic ────────────────────────────────────
    // Rules:
    //  • +1 per completed workout (not per day)
    //  • Streak resets to 0 only after 3 CONSECUTIVE missed SCHEDULED workouts
    //  • Rest days (not in weekdaySchedule) are completely ignored
    private fun computeStreakMetrics(
        streakCalendar: List<StreakDayResponse>,
        trainingDayDetails: List<TrainingDayDetailResponse>,
        weekdaySchedule: Map<Int, RoutineScheduleItem>
    ): StreakMetrics {
        val today = LocalDate.now()

        // Build the set of all dates where a workout was completed.
        val trainedDates = mutableSetOf<String>()
        trainingDayDetails.forEach { trainedDates.add(it.date.take(10)) }
        streakCalendar.forEach { if (it.workoutDone) trainedDates.add(it.date.take(10)) }

        if (trainedDates.isEmpty()) return StreakMetrics(0, 0)

        val earliest = trainedDates
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
            .minOrNull() ?: return StreakMetrics(0, 0)

        val scheduleKnown = weekdaySchedule.isNotEmpty()
        fun isScheduled(d: LocalDate) = !scheduleKnown || weekdaySchedule.containsKey(d.dayOfWeek.value)
        fun isTrained(d: LocalDate) = trainedDates.contains(d.toString())

        // ── Current streak (backward walk) ───────────────────────
        // Count +1 per workout. Stop when 3 consecutive scheduled days are missed.
        val todayTrained = isTrained(today)
        var currentStreak = 0
        var consecutiveMisses = 0
        var checkDate = if (todayTrained) today else today.minusDays(1)

        while (!checkDate.isBefore(earliest)) {
            if (isScheduled(checkDate)) {
                if (isTrained(checkDate)) {
                    currentStreak++
                    consecutiveMisses = 0
                } else {
                    consecutiveMisses++
                    if (consecutiveMisses >= 3) break
                }
            }
            // Rest days: skip silently — no effect on streak or miss counter
            checkDate = checkDate.minusDays(1)
        }

        // ── Longest streak (forward pass through all history) ────
        var longestStreak = currentStreak   // current can't exceed longest
        var running = 0
        var runningMisses = 0
        var walkDate = earliest

        while (!walkDate.isAfter(today)) {
            if (isScheduled(walkDate)) {
                if (isTrained(walkDate)) {
                    running++
                    runningMisses = 0
                    if (running > longestStreak) longestStreak = running
                } else if (walkDate != today) {
                    // Only penalise past missed scheduled days — today may still be done.
                    runningMisses++
                    if (runningMisses >= 3) {
                        running = 0
                        runningMisses = 0
                    }
                }
            }
            walkDate = walkDate.plusDays(1)
        }

        return StreakMetrics(currentStreak, longestStreak)
    }

    companion object {
        private const val DASHBOARD_SYNC_WORK_NAME = "dashboard_layout_sync"
    }
}
