package com.example.kaizenfrontend.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.statistics.data.repository.StatisticsRepository
import com.example.kaizenfrontend.feature.statistics.data.repository.TrendPoint
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class TimeRange(val label: String) {
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y"),
    LIFETIME("ALL")
}

data class StatisticsUiState(
    val selectedTimeRange: TimeRange = TimeRange.ONE_MONTH,
    val exercises: List<ExerciseOptionUiState> = emptyList(),
    val selectedExerciseId: String? = null,
    val bodyWeightChart: TrendChartUiState = TrendChartUiState(
        isLoading = true,
        isEmpty = true,
        message = "Loading trend...",
        subtitle = null,
        valueSuffix = ""
    ),
    val estimated1RmChart: TrendChartUiState = TrendChartUiState(
        isLoading = false,
        isEmpty = true,
        message = "No exercise selected for 1RM trend.",
        subtitle = "Strength estimate",
        valueSuffix = " kg"
    )
)

data class TrendChartUiState(
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val message: String,
    val subtitle: String?,
    val valueSuffix: String,
    val minY: Float = 0f,
    val maxY: Float = 0f,
    val xLabels: List<String> = emptyList()
)

data class ExerciseOptionUiState(
    val id: String,
    val name: String
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    val bodyWeightModelProducer = ChartEntryModelProducer()
    val estimated1RmModelProducer = ChartEntryModelProducer()

    private var bodyWeightRawData: List<TrendPoint> = emptyList()
    private var oneRepMaxRawData: List<TrendPoint> = emptyList()

    private var bodyWeightUnit: String = "KG"
    private var oneRepMaxExerciseName: String? = null
    private var rangeUpdateJob: Job? = null

    private val dateLabelFormatter = DateTimeFormatter.ofPattern("dd/MM")

    init {
        loadExerciseOptions()
        refreshStatistics()
    }

    fun selectExercise(exerciseId: String?) {
        oneRepMaxRawData = emptyList()
        oneRepMaxExerciseName = _uiState.value.exercises.firstOrNull { it.id == exerciseId }?.name

        _uiState.update {
            it.copy(
                selectedExerciseId = exerciseId,
                estimated1RmChart = it.estimated1RmChart.copy(
                    isLoading = exerciseId != null,
                    isEmpty = true,
                    message = if (exerciseId == null) {
                        "No exercise selected for 1RM trend."
                    } else {
                        "Loading trend..."
                    }
                )
            )
        }
        refreshStatistics()
    }

    fun updateTimeRange(newRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = newRange) }
        rangeUpdateJob?.cancel()
        rangeUpdateJob = viewModelScope.launch {
            applyRangeAndUpdateModels(newRange)
        }
    }

    fun refreshStatistics() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    bodyWeightChart = it.bodyWeightChart.copy(
                        isLoading = true,
                        isEmpty = true,
                        message = "Loading trend..."
                    ),
                    estimated1RmChart = it.estimated1RmChart.copy(
                        isLoading = it.selectedExerciseId != null,
                        isEmpty = true,
                        message = if (it.selectedExerciseId == null) {
                            "No exercise selected for 1RM trend."
                        } else {
                            "Loading trend..."
                        }
                    )
                )
            }

            val bodyWeightDeferred = async { repository.getBodyWeightTrend() }
            val oneRmDeferred = async {
                _uiState.value.selectedExerciseId?.let { repository.getOneRepMaxTrend(it) }
            }

            val bodyWeightResult = bodyWeightDeferred.await()
            val oneRmResult = oneRmDeferred.await()

            bodyWeightResult
                .onSuccess { trend ->
                    bodyWeightUnit = trend.unit
                    bodyWeightRawData = trend.dataPoints
                }

            if (_uiState.value.selectedExerciseId != null) {
                oneRmResult
                    ?.onSuccess { trend ->
                        oneRepMaxExerciseName = trend.exerciseName
                        oneRepMaxRawData = trend.dataPoints
                    }
            }

            if (bodyWeightResult.isFailure) {
                _uiState.update {
                    it.copy(
                        bodyWeightChart = it.bodyWeightChart.copy(
                            isLoading = false,
                            isEmpty = true,
                            message = bodyWeightResult.exceptionOrNull()?.message
                                ?: "Unable to load body-weight trend.",
                            subtitle = null,
                            valueSuffix = ""
                        )
                    )
                }
            }

            if (_uiState.value.selectedExerciseId != null && (oneRmResult == null || oneRmResult.isFailure)) {
                _uiState.update {
                    it.copy(
                        estimated1RmChart = it.estimated1RmChart.copy(
                            isLoading = false,
                            isEmpty = true,
                            message = oneRmResult?.exceptionOrNull()?.message
                                ?: "Unable to load 1RM trend.",
                            subtitle = oneRepMaxExerciseName?.let { name -> "Exercise: $name" }
                                ?: "Strength estimate",
                            valueSuffix = " kg"
                        )
                    )
                }
            }

            applyRangeAndUpdateModels(_uiState.value.selectedTimeRange)
        }
    }

    private suspend fun applyRangeAndUpdateModels(range: TimeRange) {
        val bodyWeightFiltered = filterByRange(bodyWeightRawData, range)
        val oneRmFiltered = if (_uiState.value.selectedExerciseId == null) {
            emptyList()
        } else {
            filterByRange(oneRepMaxRawData, range)
        }

        val bodyWeightHasTrend = bodyWeightFiltered.size >= 2
        val oneRmHasTrend = oneRmFiltered.size >= 2
        val notEnoughTrendMessage = "Not enough data points in this range to establish a trend."

        if (bodyWeightHasTrend) {
            val entries = bodyWeightFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.value.toFloat())
            }
            bodyWeightModelProducer.setEntriesSuspending(listOf(entries)).await()
        } else {
            bodyWeightModelProducer.setEntriesSuspending(emptyList<List<FloatEntry>>()).await()
        }

        if (oneRmHasTrend) {
            val entries = oneRmFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.value.toFloat())
            }
            estimated1RmModelProducer.setEntriesSuspending(listOf(entries)).await()
        } else {
            estimated1RmModelProducer.setEntriesSuspending(emptyList<List<FloatEntry>>()).await()
        }

        val bodyWeightLabels = bodyWeightFiltered.map { it.date.format(dateLabelFormatter) }
        val oneRmLabels = oneRmFiltered.map { it.date.format(dateLabelFormatter) }

        _uiState.update {
            val bodyWeightMinMax = calculateYAxisRange(bodyWeightFiltered)
            val oneRmMinMax = calculateYAxisRange(oneRmFiltered)

            it.copy(
                bodyWeightChart = it.bodyWeightChart.copy(
                    isLoading = false,
                    isEmpty = !bodyWeightHasTrend,
                    message = if (bodyWeightHasTrend) "" else notEnoughTrendMessage,
                    subtitle = "Unit: $bodyWeightUnit",
                    valueSuffix = " $bodyWeightUnit",
                    minY = bodyWeightMinMax.first,
                    maxY = bodyWeightMinMax.second,
                    xLabels = bodyWeightLabels
                ),
                estimated1RmChart = it.estimated1RmChart.copy(
                    isLoading = false,
                    isEmpty = !oneRmHasTrend,
                    message = when {
                        it.selectedExerciseId == null -> "No exercise selected for 1RM trend."
                        !oneRmHasTrend -> notEnoughTrendMessage
                        else -> ""
                    },
                    subtitle = oneRepMaxExerciseName?.let { name -> "Exercise: $name" }
                        ?: "Strength estimate",
                    valueSuffix = " kg",
                    minY = oneRmMinMax.first,
                    maxY = oneRmMinMax.second,
                    xLabels = oneRmLabels
                )
            )
        }
    }

    private fun calculateYAxisRange(points: List<TrendPoint>): Pair<Float, Float> {
        if (points.isEmpty()) return 0f to 0f
        val minValue = points.minOf { it.value }.toFloat()
        val maxValue = points.maxOf { it.value }.toFloat()
        val spread = (maxValue - minValue).coerceAtLeast(0.1f)
        val padding = (spread * 0.1f).coerceAtLeast(0.5f)
        return (minValue - padding) to (maxValue + padding)
    }

    private fun loadExerciseOptions() {
        viewModelScope.launch {
            val exercises = workoutRepository
                .getWorkouts()
                .getOrDefault(emptyList())
                .flatMap { it.sets }
                .mapNotNull { set ->
                    val id = set.customExerciseId ?: set.builtinExerciseKey ?: return@mapNotNull null
                    val name = set.exerciseName?.takeIf { it.isNotBlank() } ?: id
                    ExerciseOptionUiState(id = id, name = name)
                }
                .distinctBy { it.id }
                .sortedBy { it.name.lowercase() }

            _uiState.update {
                it.copy(exercises = exercises)
            }
        }
    }

    private fun filterByRange(data: List<TrendPoint>, range: TimeRange): List<TrendPoint> {
        if (range == TimeRange.LIFETIME) return data.sortedBy { it.date }
        if (data.isEmpty()) return emptyList()

        val latest = data.maxOf { it.date }
        val startDate = when (range) {
            TimeRange.ONE_MONTH -> latest.minusMonths(1)
            TimeRange.THREE_MONTHS -> latest.minusMonths(3)
            TimeRange.SIX_MONTHS -> latest.minusMonths(6)
            TimeRange.ONE_YEAR -> latest.minusYears(1)
            TimeRange.LIFETIME -> LocalDate.MIN
        }

        return data
            .asSequence()
            .filter { it.date >= startDate }
            .sortedBy { it.date }
            .toList()
    }
}
