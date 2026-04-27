package com.example.kaizenfrontend.feature.dashboard.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.NextWorkoutDTO
import com.example.kaizenfrontend.feature.dashboard.worker.DashboardSyncWorker
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineExerciseResponse
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineResponse
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutSetResponseDto
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import com.example.kaizenfrontend.feature.workouts.presentation.utils.RoutineScheduleCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt
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
    private val planApiService: PlanApiService,
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

    private val _nextWorkoutDisplay = MutableStateFlow<NextWorkoutDisplayUi?>(null)
    val nextWorkoutDisplay: StateFlow<NextWorkoutDisplayUi?> = _nextWorkoutDisplay.asStateFlow()

    private val _workoutsByDate = MutableStateFlow<Map<LocalDate, List<CalendarWorkoutUi>>>(emptyMap())
    val workoutsByDate: StateFlow<Map<LocalDate, List<CalendarWorkoutUi>>> = _workoutsByDate.asStateFlow()

    private val _isWorkoutHistoryLoading = MutableStateFlow(true)
    val isWorkoutHistoryLoading: StateFlow<Boolean> = _isWorkoutHistoryLoading.asStateFlow()

    private val _lastSessionDetails = MutableStateFlow<LastSessionModalUi?>(null)
    val lastSessionDetails: StateFlow<LastSessionModalUi?> = _lastSessionDetails.asStateFlow()

    private val _prHistoryByExercise = MutableStateFlow<Map<String, List<PrHistoryEntryUi>>>(emptyMap())
    val prHistoryByExercise: StateFlow<Map<String, List<PrHistoryEntryUi>>> = _prHistoryByExercise.asStateFlow()

    private val _recentPrLedger = MutableStateFlow<List<PrHistoryEntryUi>>(emptyList())
    val recentPrLedger: StateFlow<List<PrHistoryEntryUi>> = _recentPrLedger.asStateFlow()

    private val _muscleReadiness = MutableStateFlow<List<MuscleReadinessUi>>(emptyList())
    val muscleReadiness: StateFlow<List<MuscleReadinessUi>> = _muscleReadiness.asStateFlow()

    private val _isLoggingBodyWeight = MutableStateFlow(false)
    val isLoggingBodyWeight: StateFlow<Boolean> = _isLoggingBodyWeight.asStateFlow()

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
                    val nextDisplay = resolveNextWorkoutDisplay(data.nextWorkout)
                    _nextWorkoutDisplay.value = nextDisplay
                    loadNextWorkoutExercises(nextDisplay?.routineId)
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
            _isWorkoutHistoryLoading.value = true
            workoutRepository
                .getWorkouts()
                .fold(
                    onSuccess = { workouts ->
                        _workoutsByDate.value = mapWorkoutsByDate(workouts)
                        _lastSessionDetails.value = buildLastSessionDetails(workouts)
                        _prHistoryByExercise.value = buildPrHistoryByExercise(workouts)
                        _recentPrLedger.value = buildRecentPrLedger(workouts)
                        _muscleReadiness.value = buildMuscleReadiness(workouts)
                        _isWorkoutHistoryLoading.value = false
                    },
                    onFailure = {
                        _isWorkoutHistoryLoading.value = false
                    }
                )
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

    private suspend fun resolveNextWorkoutDisplay(dashboardNextWorkout: NextWorkoutDTO?): NextWorkoutDisplayUi? {
        val token = sessionManager.getToken() ?: return null
        val response = runCatching {
            routineApiService.getUserRoutines(token = "Bearer $token", planId = null)
        }.getOrNull()

        val routines = response?.takeIf { it.isSuccessful }?.body().orEmpty()
        val planNamesById = fetchPlanNamesById(token)

        dashboardNextWorkout?.let { nextWorkout ->
            val matchingRoutine = routines.firstOrNull { it.id == nextWorkout.routineId }
            return NextWorkoutDisplayUi(
                routineId = nextWorkout.routineId,
                routineName = nextWorkout.routineName,
                planName = matchingRoutine?.planId?.let { planNamesById[it] },
                scheduleHint = "Today"
            )
        }

        if (routines.isEmpty()) return null

        val today = LocalDate.now()
        val nextCandidate =
            routines
                .mapNotNull { routine ->
                    val nextDate = findNextScheduledDateWithinWeek(today, routine) ?: return@mapNotNull null
                    Triple(routine, nextDate, daysUntil(today, nextDate))
                }
                .sortedWith(compareBy<Triple<RoutineResponse, LocalDate, Int>> { it.third }.thenBy { it.first.name.lowercase(Locale.getDefault()) })
                .firstOrNull()
                ?: return null

        val (routine, date, daysAhead) = nextCandidate
        return NextWorkoutDisplayUi(
            routineId = routine.id,
            routineName = routine.name,
            planName = routine.planId?.let { planNamesById[it] },
            scheduleHint = formatScheduleHint(daysAhead, date)
        )
    }

    private suspend fun fetchPlanNamesById(token: String): Map<String, String> {
        val response = runCatching {
            planApiService.getAllPlans(token = "Bearer $token")
        }.getOrNull()

        return response
            ?.takeIf { it.isSuccessful }
            ?.body()
            .orEmpty()
            .associate { it.id to it.name }
    }

    private fun findNextScheduledDateWithinWeek(today: LocalDate, routine: RoutineResponse): LocalDate? {
        val schedule = routine.schedulingValue
        val weekDays = RoutineScheduleCalculator.parseWeekDays(schedule)
        if (weekDays.isNotEmpty()) {
            return (0..7)
                .asSequence()
                .map { offset -> today.plusDays(offset.toLong()) }
                .firstOrNull { candidate ->
                    candidate.dayOfWeek in weekDays && !isBeforeRoutineStart(candidate, routine.startingDate)
                }
        }

        val cycleDays = RoutineScheduleCalculator.parseCycleDays(schedule)
        if (cycleDays.isEmpty()) return null

        val mappedWeekDays: Set<DayOfWeek> =
            cycleDays
                .mapNotNull { day -> runCatching { DayOfWeek.of(day.coerceIn(1, 7)) }.getOrNull() }
                .toSet()

        if (mappedWeekDays.isEmpty()) return null

        return (0..7)
            .asSequence()
            .map { offset -> today.plusDays(offset.toLong()) }
            .firstOrNull { candidate ->
                candidate.dayOfWeek in mappedWeekDays && !isBeforeRoutineStart(candidate, routine.startingDate)
            }
    }

    private fun isBeforeRoutineStart(candidate: LocalDate, startingDateRaw: String?): Boolean {
        if (startingDateRaw.isNullOrBlank()) return false
        val start = runCatching {
            LocalDate.parse(startingDateRaw.substringBefore("T"))
        }.getOrNull() ?: return false
        return candidate.isBefore(start)
    }

    private fun daysUntil(today: LocalDate, target: LocalDate): Int {
        return kotlin.math.max(0, target.toEpochDay().minus(today.toEpochDay()).toInt())
    }

    private fun formatScheduleHint(daysAhead: Int, date: LocalDate): String {
        return when (daysAhead) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> date.dayOfWeek.name.lowercase(Locale.getDefault()).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
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
            _isLoggingBodyWeight.value = true
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
            _isLoggingBodyWeight.value = false
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

    fun getPrHistoryForExercise(exerciseName: String): List<PrHistoryEntryUi> {
        val key = normalizeExerciseLookup(exerciseName)
        return _prHistoryByExercise.value[key].orEmpty()
    }

    private fun buildLastSessionDetails(workouts: List<WorkoutResponseDto>): LastSessionModalUi? {
        if (workouts.isEmpty()) return null

        val preferredWorkoutId =
            (uiState.value as? DashboardUiState.Success)?.data?.lastSession?.workoutId

        val selectedWorkout =
            workouts.firstOrNull { it.id == preferredWorkoutId }
                ?: workouts.maxByOrNull { parseToEpochMillis(it.endTime ?: it.startTime) }
                ?: return null

        val sessionTimestamp = selectedWorkout.endTime ?: selectedWorkout.startTime
        val durationFromDashboard =
            (uiState.value as? DashboardUiState.Success)
                ?.data
                ?.lastSession
                ?.takeIf { it.workoutId == selectedWorkout.id }
                ?.durationMinutes

        val durationMinutes = durationFromDashboard ?: calculateDurationMinutes(selectedWorkout)
        val totalVolumeKg = selectedWorkout.sets.sumOf { set -> (set.weightKg ?: 0.0) * (set.reps ?: 0) }
        val totalSets = selectedWorkout.sets.size
        val averageRpe = selectedWorkout.sets.mapNotNull { it.rpe }.takeIf { it.isNotEmpty() }?.average()

        return LastSessionModalUi(
            workoutId = selectedWorkout.id,
            routineName = selectedWorkout.routineName?.takeIf { it.isNotBlank() } ?: "Workout",
            completedAt = sessionTimestamp,
            durationMinutes = durationMinutes,
            totalVolumeKg = totalVolumeKg,
            totalSets = totalSets,
            averageRpe = averageRpe,
            lifts = buildSessionLifts(selectedWorkout.sets)
        )
    }

    private fun buildSessionLifts(sets: List<WorkoutSetResponseDto>): List<SessionLiftUi> {
        return sets
            .groupBy { set -> resolveWorkoutSetExerciseName(set) }
            .entries
            .sortedBy { it.key.lowercase(Locale.getDefault()) }
            .map { (exerciseName, groupedSets) ->
                SessionLiftUi(
                    name = exerciseName,
                    sets = groupedSets.size,
                    topWeightKg = groupedSets.mapNotNull { it.weightKg }.maxOrNull(),
                    averageReps = groupedSets.mapNotNull { it.reps }.takeIf { it.isNotEmpty() }?.average()
                )
            }
    }

    private fun buildPrHistoryByExercise(workouts: List<WorkoutResponseDto>): Map<String, List<PrHistoryEntryUi>> {
        val entries =
            workouts.flatMap { workout ->
                workout.sets
                    .filter { it.isPR }
                    .map { set ->
                        PrHistoryEntryUi(
                            exerciseName = resolveWorkoutSetExerciseName(set),
                            weightKg = set.weightKg,
                            reps = set.reps,
                            achievedAt = workout.endTime ?: workout.startTime,
                            routineName = workout.routineName?.takeIf { it.isNotBlank() } ?: "Workout"
                        )
                    }
            }

        return entries
            .groupBy { normalizeExerciseLookup(it.exerciseName) }
            .mapValues { (_, values) ->
                values.sortedByDescending { parseToEpochMillis(it.achievedAt) }
            }
    }

    private fun buildRecentPrLedger(workouts: List<WorkoutResponseDto>): List<PrHistoryEntryUi> {
        return workouts
            .flatMap { workout ->
                workout.sets
                    .filter { it.isPR }
                    .map { set ->
                        PrHistoryEntryUi(
                            exerciseName = resolveWorkoutSetExerciseName(set),
                            weightKg = set.weightKg,
                            reps = set.reps,
                            achievedAt = workout.endTime ?: workout.startTime,
                            routineName = workout.routineName?.takeIf { it.isNotBlank() } ?: "Workout"
                        )
                    }
            }
            .sortedByDescending { parseToEpochMillis(it.achievedAt) }
    }

    private fun buildMuscleReadiness(workouts: List<WorkoutResponseDto>): List<MuscleReadinessUi> {
        val now = Instant.now().atZone(ZoneId.systemDefault())

        val latestByMuscle =
            workouts
                .flatMap { workout ->
                    val timestamp = workout.endTime ?: workout.startTime
                    workout.sets.map { set ->
                        inferMuscleTarget(set) to timestamp
                    }
                }
                .groupBy(keySelector = { it.first }, valueTransform = { parseToEpochMillis(it.second) })
                .mapValues { (_, timestamps) -> timestamps.maxOrNull() ?: 0L }

        val muscleOrder = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core")

        return muscleOrder.mapNotNull { muscle ->
            val lastEpoch = latestByMuscle[muscle]
            if (lastEpoch == null || lastEpoch <= 0L) {
                return@mapNotNull MuscleReadinessUi(
                    muscleName = muscle,
                    recoveredPercent = 100,
                    statusLabel = "Fresh",
                    lastTrainedAt = null
                )
            }

            val lastHit = Instant.ofEpochMilli(lastEpoch).atZone(ZoneId.systemDefault())
            val hoursElapsed = ChronoUnit.HOURS.between(lastHit, now).coerceAtLeast(0)
            val recoveredPercent = ((hoursElapsed.toDouble() / 72.0) * 100.0).roundToInt().coerceIn(0, 100)
            val statusLabel =
                when {
                    recoveredPercent >= 90 -> "Fresh"
                    recoveredPercent >= 60 -> "Ready Soon"
                    recoveredPercent >= 30 -> "Recovering"
                    else -> "Fatigued"
                }

            MuscleReadinessUi(
                muscleName = muscle,
                recoveredPercent = recoveredPercent,
                statusLabel = statusLabel,
                lastTrainedAt = lastHit.toLocalDate().toString()
            )
        }
    }

    private fun inferMuscleTarget(set: WorkoutSetResponseDto): String {
        val raw =
            (set.exerciseName ?: set.builtinExerciseKey ?: set.customExerciseId)
                .orEmpty()
                .lowercase(Locale.getDefault())

        return when {
            raw.contains("bench") || raw.contains("chest") || raw.contains("incline") || raw.contains("press") -> "Chest"
            raw.contains("row") || raw.contains("pull") || raw.contains("lat") || raw.contains("back") || raw.contains("deadlift") -> "Back"
            raw.contains("squat") || raw.contains("leg") || raw.contains("lunge") || raw.contains("ham") || raw.contains("quad") -> "Legs"
            raw.contains("shoulder") || raw.contains("overhead") || raw.contains("lateral") || raw.contains("rear delt") -> "Shoulders"
            raw.contains("curl") || raw.contains("triceps") || raw.contains("biceps") || raw.contains("arm") -> "Arms"
            else -> "Core"
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

    private fun normalizeExerciseLookup(raw: String): String {
        return humanizeIdentifier(raw).lowercase(Locale.getDefault()).trim()
    }

    private fun calculateDurationMinutes(workout: WorkoutResponseDto): Int? {
        val start = parseToEpochMillis(workout.startTime)
        val end = parseToEpochMillis(workout.endTime)
        if (start <= 0L || end <= 0L || end < start) return null
        return ((end - start).toDouble() / 60_000.0).roundToInt().coerceAtLeast(1)
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
