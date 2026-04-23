package com.example.kaizenfrontend.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaizenfrontend.feature.statistics.data.repository.StatisticsRepository
import com.example.kaizenfrontend.feature.statistics.data.repository.TrendPoint
import com.example.kaizenfrontend.feature.statistics.data.repository.WeeklyVolumePoint
import com.example.kaizenfrontend.feature.statistics.data.repository.FatiguePoint
import com.example.kaizenfrontend.feature.statistics.data.repository.PrPeakTimePoint
import com.example.kaizenfrontend.feature.statistics.data.repository.SessionEfficiencyPoint
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
    ),
    // Hypertrophy & Overload
    val volumeTrend: VolumeTrendUiState = VolumeTrendUiState(),
    val repRange: RepRangeUiState = RepRangeUiState(),
    val muscleFrequency: MuscleFrequencyUiState = MuscleFrequencyUiState(),
    // Efficiency & Fatigue
    val fatigue: FatigueUiState = FatigueUiState(),
    val efficiency: EfficiencyUiState = EfficiencyUiState(),
    val restTime: RestTimeUiState = RestTimeUiState(),
    // Discipline & Habits
    val activityHeatmap: HeatmapUiState = HeatmapUiState(
        isLoading = true,
        isEmpty = true,
        message = "Loading activity heatmap..."
    ),
    val prHeatmap: HeatmapUiState = HeatmapUiState(
        isLoading = true,
        isEmpty = true,
        message = "Loading PR heatmap..."
    ),
    val prPeakTime: PrPeakTimeUiState = PrPeakTimeUiState()
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

// Hypertrophy & Overload UI state models 

data class VolumeTrendUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading volume...",
    val weekLabels: List<String> = emptyList()
)

data class RepRangeSegment(
    val label: String,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

data class RepRangeUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading rep ranges...",
    val segments: List<RepRangeSegment> = emptyList()
)

data class MuscleFrequencyUiItem(
    val muscleGroup: String,
    val hitCount: Int,
    val percentage: Float
)

data class MuscleFrequencyUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading muscle data...",
    val muscles: List<MuscleFrequencyUiItem> = emptyList()
)

// ─── Efficiency & Fatigue UI state models ────────────────────────────

data class FatigueUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading fatigue data...",
    val dates: List<String> = emptyList(),
    val maxVolume: Float = 0f
)

data class EfficiencyPointUi(
    val durationMin: Long,
    val volume: Float
)

data class EfficiencyUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading efficiency data...",
    val points: List<EfficiencyPointUi> = emptyList()
)

data class RestBucketUi(
    val category: String,
    val percentage: Float
)

data class RestTimeUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading rest times...",
    val buckets: List<RestBucketUi> = emptyList()
)

data class HeatmapUiState(
    val isLoading: Boolean,
    val isEmpty: Boolean,
    val message: String,
    val dayValues: Map<LocalDate, Int> = emptyMap(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val journeyStartDate: LocalDate? = null,
    val totalHighlights: Int = 0
)

data class PrPeakTimePointUi(
    val date: LocalDate,
    val minutesOfDay: Int
)

data class PrPeakTimeUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = true,
    val message: String = "Loading PR peak-time data...",
    val points: List<PrPeakTimePointUi> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
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
    val volumeBarModelProducer = ChartEntryModelProducer()
    
    // Efficiency & Fatigue
    val fatigueModelProducer = ChartEntryModelProducer()
    val efficiencyModelProducer = ChartEntryModelProducer()

    private var bodyWeightRawData: List<TrendPoint> = emptyList()
    private var oneRepMaxRawData: List<TrendPoint> = emptyList()
    private var volumeRawData: List<WeeklyVolumePoint> = emptyList()
    private var fatigueRawData: List<FatiguePoint> = emptyList()
    private var efficiencyRawData: List<SessionEfficiencyPoint> = emptyList()
    private var activityHeatmapRawData: Map<LocalDate, Int> = emptyMap()
    private var prHeatmapRawData: Map<LocalDate, Int> = emptyMap()
    private var prPeakTimeRawData: List<PrPeakTimePoint> = emptyList()

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
                    ),
                    volumeTrend = it.volumeTrend.copy(isLoading = true, isEmpty = true, message = "Loading volume..."),
                    repRange = it.repRange.copy(isLoading = true, isEmpty = true, message = "Loading rep ranges..."),
                    muscleFrequency = it.muscleFrequency.copy(isLoading = true, isEmpty = true, message = "Loading muscle data..."),
                    activityHeatmap = it.activityHeatmap.copy(
                        isLoading = true,
                        isEmpty = true,
                        message = "Loading activity heatmap..."
                    ),
                    prHeatmap = it.prHeatmap.copy(
                        isLoading = true,
                        isEmpty = true,
                        message = "Loading PR heatmap..."
                    ),
                    prPeakTime = it.prPeakTime.copy(
                        isLoading = true,
                        isEmpty = true,
                        message = "Loading PR peak-time data..."
                    )
                )
            }

            val bodyWeightDeferred = async { repository.getBodyWeightTrend() }
            val oneRmDeferred = async {
                _uiState.value.selectedExerciseId?.let { repository.getOneRepMaxTrend(it) }
            }
            val volumeDeferred = async { repository.getVolumeTrend() }
            val repRangeDeferred = async { repository.getRepRanges() }
            val muscleFreqDeferred = async { repository.getMuscleFrequency() }
            val fatigueDeferred = async { repository.getFatigueCorrelation() }
            val efficiencyDeferred = async { repository.getSessionEfficiency() }
            val densityDeferred = async { repository.getRestTimeDistribution() }
            val activityHeatmapDeferred = async { repository.getActivityHeatmap() }
            val prHeatmapDeferred = async { repository.getPrHeatmap() }
            val prPeakTimeDeferred = async { repository.getPrPeakTime() }

            val bodyWeightResult = bodyWeightDeferred.await()
            val oneRmResult = oneRmDeferred.await()
            val volumeResult = volumeDeferred.await()
            val repRangeResult = repRangeDeferred.await()
            val muscleFreqResult = muscleFreqDeferred.await()
            val fatigueResult = fatigueDeferred.await()
            val efficiencyResult = efficiencyDeferred.await()
            val densityResult = densityDeferred.await()
            val activityHeatmapResult = activityHeatmapDeferred.await()
            val prHeatmapResult = prHeatmapDeferred.await()
            val prPeakTimeResult = prPeakTimeDeferred.await()

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

            // Volume Trend 
            volumeResult
                .onSuccess { trend ->
                    volumeRawData = trend.dataPoints
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            volumeTrend = s.volumeTrend.copy(
                                isLoading = false,
                                isEmpty = true,
                                message = volumeResult.exceptionOrNull()?.message ?: "Unable to load volume trend."
                            )
                        )
                    }
                }

            // Rep Ranges 
            repRangeResult
                .onSuccess { dist ->
                    val segments = listOf(
                        RepRangeSegment("Strength", dist.strengthPct.toFloat(), com.example.kaizenfrontend.core.ui.theme.SubtleRed),
                        RepRangeSegment("Hypertrophy", dist.hypertrophyPct.toFloat(), com.example.kaizenfrontend.core.ui.theme.CrayolaBlue),
                        RepRangeSegment("Endurance", dist.endurancePct.toFloat(), com.example.kaizenfrontend.core.ui.theme.MalachiteGreen)
                    )
                    val hasData = segments.any { it.percentage > 0f }
                    _uiState.update { s ->
                        s.copy(
                            repRange = RepRangeUiState(
                                isLoading = false,
                                isEmpty = !hasData,
                                message = if (hasData) "" else "No session data yet.",
                                segments = segments
                            )
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            repRange = RepRangeUiState(
                                isLoading = false,
                                isEmpty = true,
                                message = repRangeResult.exceptionOrNull()?.message ?: "Unable to load rep ranges."
                            )
                        )
                    }
                }

            // Muscle Frequency 
            muscleFreqResult
                .onSuccess { freq ->
                    val items = freq.muscles.map {
                        MuscleFrequencyUiItem(it.muscleGroup, it.hitCount, it.percentage.toFloat())
                    }
                    _uiState.update { s ->
                        s.copy(
                            muscleFrequency = MuscleFrequencyUiState(
                                isLoading = false,
                                isEmpty = items.isEmpty(),
                                message = if (items.isEmpty()) "No muscle data yet." else "",
                                muscles = items
                            )
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            muscleFrequency = MuscleFrequencyUiState(
                                isLoading = false,
                                isEmpty = true,
                                message = muscleFreqResult.exceptionOrNull()?.message ?: "Unable to load muscle data."
                            )
                        )
                    }
                }

            // Fatigue
            fatigueResult
                .onSuccess { fatigueData ->
                    fatigueRawData = fatigueData.dataPoints
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            fatigue = FatigueUiState(
                                isLoading = false,
                                isEmpty = true,
                                message = fatigueResult.exceptionOrNull()?.message ?: "Unable to load fatigue data."
                            )
                        )
                    }
                }

            // Efficiency
            efficiencyResult
                .onSuccess { efficiencyData ->
                    efficiencyRawData = efficiencyData.dataPoints
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            efficiency = EfficiencyUiState(
                                isLoading = false,
                                isEmpty = true,
                                message = efficiencyResult.exceptionOrNull()?.message ?: "Unable to load efficiency data."
                            )
                        )
                    }
                }

            // Density
            densityResult
                .onSuccess { densityData ->
                    val buckets = densityData.buckets.map {
                        RestBucketUi(it.category, it.percentage.toFloat())
                    }
                    _uiState.update { s ->
                        s.copy(
                            restTime = RestTimeUiState(
                                isLoading = false,
                                isEmpty = buckets.isEmpty(),
                                message = if (buckets.isEmpty()) "No rest data found." else "",
                                buckets = buckets
                            )
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            restTime = RestTimeUiState(
                                isLoading = false,
                                isEmpty = true,
                                message = densityResult.exceptionOrNull()?.message ?: "Unable to load density data."
                            )
                        )
                    }
                }

            // Discipline & Habits
            activityHeatmapResult
                .onSuccess { heatmap ->
                    activityHeatmapRawData = heatmap.points
                        .groupBy(keySelector = { it.date }, valueTransform = { it.durationMinutes })
                        .mapValues { (_, values) -> values.sum().coerceAtLeast(0) }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            activityHeatmap = s.activityHeatmap.copy(
                                isLoading = false,
                                isEmpty = true,
                                message = activityHeatmapResult.exceptionOrNull()?.message
                                    ?: "Unable to load activity heatmap."
                            )
                        )
                    }
                }

            prHeatmapResult
                .onSuccess { heatmap ->
                    prHeatmapRawData = heatmap.points
                        .groupBy(keySelector = { it.date }, valueTransform = { it.count })
                        .mapValues { (_, values) -> values.sum().coerceAtLeast(0) }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            prHeatmap = s.prHeatmap.copy(
                                isLoading = false,
                                isEmpty = true,
                                message = prHeatmapResult.exceptionOrNull()?.message
                                    ?: "Unable to load PR heatmap."
                            )
                        )
                    }
                }

            prPeakTimeResult
                .onSuccess { peakTime ->
                    prPeakTimeRawData = peakTime.dataPoints
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            prPeakTime = s.prPeakTime.copy(
                                isLoading = false,
                                isEmpty = true,
                                message = prPeakTimeResult.exceptionOrNull()?.message
                                    ?: "Unable to load PR peak-time data."
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
        val volumeFiltered = filterVolumeByRange(volumeRawData, range)
        val journeyStartDate = activityHeatmapRawData.keys.minOrNull() ?: prHeatmapRawData.keys.minOrNull()

        val activityWindow = calculateHeatmapWindow(
            latestDate = activityHeatmapRawData.keys.maxOrNull(),
            range = range,
            journeyStartDate = journeyStartDate
        )
        val activityHeatmapFiltered = activityWindow?.let { window ->
            filterHeatmapValuesByWindow(activityHeatmapRawData, window.first, window.second)
        } ?: emptyMap()

        val prWindow = calculateHeatmapWindow(
            latestDate = prHeatmapRawData.keys.maxOrNull() ?: activityHeatmapRawData.keys.maxOrNull(),
            range = range,
            journeyStartDate = journeyStartDate
        )
        val prHeatmapFiltered = prWindow?.let { window ->
            filterHeatmapValuesByWindow(prHeatmapRawData, window.first, window.second)
        } ?: emptyMap()

        val prPeakTimeFiltered = filterPrPeakByRange(prPeakTimeRawData, range)

        val bodyWeightHasTrend = bodyWeightFiltered.size >= 2
        val oneRmHasTrend = oneRmFiltered.size >= 2
        val notEnoughTrendMessage = "Not enough data points in this range to establish a trend."
        val volumeHasData = volumeFiltered.isNotEmpty()

        if (bodyWeightHasTrend) {
            val entries = bodyWeightFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.value.toFloat())
            }
            bodyWeightModelProducer.setEntriesSuspending(listOf(entries)).await()
        } else {
            bodyWeightModelProducer.setEntriesSuspending(listOf(emptyList<FloatEntry>())).await()
        }

        if (oneRmHasTrend) {
            val entries = oneRmFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.value.toFloat())
            }
            estimated1RmModelProducer.setEntriesSuspending(listOf(entries)).await()
        } else {
            estimated1RmModelProducer.setEntriesSuspending(listOf(emptyList<FloatEntry>())).await()
        }

        if (volumeHasData) {
            val entries = volumeFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.totalTonnage.toFloat())
            }
            volumeBarModelProducer.setEntriesSuspending(listOf(entries)).await()
        } else {
            volumeBarModelProducer.setEntriesSuspending(listOf(emptyList<FloatEntry>())).await()
        }

        val fatigueFiltered = filterFatigueByRange(fatigueRawData, range)
        val fatigueHasData = fatigueFiltered.size >= 2
        val maxVolume = fatigueFiltered.maxOfOrNull { it.totalVolume.toFloat() }?.coerceAtLeast(10f) ?: 10f
        
        if (fatigueHasData) {
            val volSeries = fatigueFiltered.mapIndexed { index, point ->
                FloatEntry(x = index.toFloat(), y = point.totalVolume.toFloat())
            }
            val rpeSeries = fatigueFiltered.mapIndexed { index, point ->
                // Normalize RPE (0-10) to visually match the Volume scale so they chart together beautifully
                val normalizedRpe = (point.averageRpe.toFloat() / 10f) * maxVolume
                FloatEntry(x = index.toFloat(), y = normalizedRpe)
            }
            fatigueModelProducer.setEntriesSuspending(listOf(volSeries, rpeSeries)).await()
        } else {
            fatigueModelProducer.setEntriesSuspending(listOf(emptyList<FloatEntry>(), emptyList<FloatEntry>())).await()
        }

        val efficiencyEntries = efficiencyRawData.map { point ->
            // Scatter plots inherently use the actual data values on X and Y, rather than synthetic x indices
            FloatEntry(x = point.durationMinutes.toFloat(), y = point.totalVolume.toFloat())
        }.sortedBy { it.x }
        
        val efficiencyHasData = efficiencyEntries.isNotEmpty()
        if (efficiencyHasData) {
            efficiencyModelProducer.setEntriesSuspending(listOf(efficiencyEntries)).await()
        } else {
            efficiencyModelProducer.setEntriesSuspending(listOf(emptyList<FloatEntry>())).await()
        }

        val bodyWeightLabels = bodyWeightFiltered.map { it.date.format(dateLabelFormatter) }
        val oneRmLabels = oneRmFiltered.map { it.date.format(dateLabelFormatter) }
        val volumeLabels = volumeFiltered.map { it.weekLabel }
        val fatigueLabels = fatigueFiltered.map { it.date.format(dateLabelFormatter) }

        val activityHeatmapStart = activityWindow?.first
        val activityHeatmapEnd = activityWindow?.second
        val prHeatmapStart = prWindow?.first
        val prHeatmapEnd = prWindow?.second
        val prPeakTimeStart = prPeakTimeFiltered.minOfOrNull { it.date }
        val prPeakTimeEnd = prPeakTimeFiltered.maxOfOrNull { it.date }
        val prPeakTimeUiPoints = prPeakTimeFiltered.map {
            PrPeakTimePointUi(
                date = it.date,
                minutesOfDay = (it.hourOfDay * 60 + it.minuteOfHour).coerceIn(0, 1439)
            )
        }

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
                ),
                volumeTrend = it.volumeTrend.copy(
                    isLoading = false,
                    isEmpty = !volumeHasData,
                    message = if (volumeHasData) "" else "No volume data in this range.",
                    weekLabels = volumeLabels
                ),
                fatigue = it.fatigue.copy(
                    isLoading = false,
                    isEmpty = !fatigueHasData,
                    message = if (fatigueHasData) "" else notEnoughTrendMessage,
                    dates = fatigueLabels,
                    maxVolume = maxVolume
                ),
                efficiency = it.efficiency.copy(
                    isLoading = false,
                    isEmpty = !efficiencyHasData,
                    message = if (efficiencyHasData) "" else "No efficiency data available.",
                    points = efficiencyRawData.map { p -> EfficiencyPointUi(p.durationMinutes, p.totalVolume.toFloat()) }
                ),
                activityHeatmap = it.activityHeatmap.copy(
                    isLoading = false,
                    isEmpty = activityHeatmapFiltered.isEmpty(),
                    message = if (activityHeatmapFiltered.isEmpty()) {
                        "No workout activity in this range."
                    } else {
                        ""
                    },
                    dayValues = activityHeatmapFiltered,
                    startDate = activityHeatmapStart,
                    endDate = activityHeatmapEnd,
                    journeyStartDate = journeyStartDate,
                    totalHighlights = activityHeatmapFiltered.count { it.value > 0 }
                ),
                prHeatmap = it.prHeatmap.copy(
                    isLoading = false,
                    isEmpty = prHeatmapFiltered.isEmpty(),
                    message = if (prHeatmapFiltered.isEmpty()) {
                        "No PR activity in this range."
                    } else {
                        ""
                    },
                    dayValues = prHeatmapFiltered,
                    startDate = prHeatmapStart,
                    endDate = prHeatmapEnd,
                    journeyStartDate = journeyStartDate,
                    totalHighlights = prHeatmapFiltered.count { it.value > 0 }
                ),
                prPeakTime = it.prPeakTime.copy(
                    isLoading = false,
                    isEmpty = prPeakTimeUiPoints.isEmpty(),
                    message = if (prPeakTimeUiPoints.isEmpty()) {
                        "No PR timing events in this range."
                    } else {
                        ""
                    },
                    points = prPeakTimeUiPoints,
                    startDate = prPeakTimeStart,
                    endDate = prPeakTimeEnd
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

    private fun filterVolumeByRange(data: List<WeeklyVolumePoint>, range: TimeRange): List<WeeklyVolumePoint> {
        if (data.isEmpty()) return data
        val keep = when (range) {
            TimeRange.ONE_MONTH -> 4
            TimeRange.THREE_MONTHS -> 13
            TimeRange.SIX_MONTHS -> 26
            TimeRange.ONE_YEAR -> 52
            TimeRange.LIFETIME -> data.size
        }
        return data.takeLast(keep)
    }

    private fun filterFatigueByRange(data: List<FatiguePoint>, range: TimeRange): List<FatiguePoint> {
        if (data.isEmpty()) return emptyList()
        val latest = data.maxOf { it.date }
        val startDate = when (range) {
            TimeRange.ONE_MONTH -> latest.minusMonths(1)
            TimeRange.THREE_MONTHS -> latest.minusMonths(3)
            TimeRange.SIX_MONTHS -> latest.minusMonths(6)
            TimeRange.ONE_YEAR -> latest.minusYears(1)
            TimeRange.LIFETIME -> LocalDate.MIN
        }
        return data.filter { it.date >= startDate }.sortedBy { it.date }
    }

    private fun calculateHeatmapWindow(
        latestDate: LocalDate?,
        range: TimeRange,
        journeyStartDate: LocalDate?
    ): Pair<LocalDate, LocalDate>? {
        val endDate = latestDate ?: return null
        val startDate = when (range) {
            TimeRange.ONE_MONTH -> endDate.minusMonths(1)
            TimeRange.THREE_MONTHS -> endDate.minusMonths(3)
            TimeRange.SIX_MONTHS -> endDate.minusMonths(6)
            TimeRange.ONE_YEAR -> endDate.minusYears(1)
            TimeRange.LIFETIME -> (journeyStartDate ?: endDate).withDayOfYear(1)
        }
        return startDate to endDate
    }

    private fun filterHeatmapValuesByWindow(
        data: Map<LocalDate, Int>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Int> {
        if (data.isEmpty()) return emptyMap()
        return data
            .filter { (date, _) -> date >= startDate && date <= endDate }
            .toSortedMap()
    }

    private fun filterPrPeakByRange(data: List<PrPeakTimePoint>, range: TimeRange): List<PrPeakTimePoint> {
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
            .filter { it.date >= startDate }
            .sortedBy { it.date }
    }
}
