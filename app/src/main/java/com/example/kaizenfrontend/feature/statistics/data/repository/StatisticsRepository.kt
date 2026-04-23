package com.example.kaizenfrontend.feature.statistics.data.repository

import android.util.Log
import com.example.kaizenfrontend.feature.statistics.data.remote.BodyWeightTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.MuscleFrequencyResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.OneRepMaxTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.PrPeakTimePointDto
import com.example.kaizenfrontend.feature.statistics.data.remote.PrPeakTimeResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.PrFrequencyResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.RepRangeResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.StatisticsApiService
import com.example.kaizenfrontend.feature.statistics.data.remote.VolumeTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.FatigueCorrelationResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.SessionEfficiencyResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.RestTimeDistributionResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.TrainingActivityResponseDto
import java.time.LocalDate
import javax.inject.Inject

data class TrendPoint(
    val date: LocalDate,
    val value: Double
)

data class BodyWeightTrend(
    val unit: String,
    val dataPoints: List<TrendPoint>
)

data class OneRepMaxTrend(
    val exerciseName: String,
    val dataPoints: List<TrendPoint>
)

// Hypertrophy & Overload domain models

data class WeeklyVolumePoint(
    val weekLabel: String,
    val totalTonnage: Double
)

data class VolumeTrend(
    val dataPoints: List<WeeklyVolumePoint>
)

data class RepRangeDistribution(
    val strengthPct: Double,
    val hypertrophyPct: Double,
    val endurancePct: Double
)

data class MuscleFrequencyItem(
    val muscleGroup: String,
    val hitCount: Int,
    val percentage: Double
)

data class MuscleFrequency(
    val muscles: List<MuscleFrequencyItem>
)

// Efficiency & Fatigue domain models

data class FatiguePoint(
    val date: LocalDate,
    val totalVolume: Double,
    val averageRpe: Double
)

data class FatigueCorrelation(
    val dataPoints: List<FatiguePoint>
)

data class SessionEfficiencyPoint(
    val durationMinutes: Long,
    val totalVolume: Double
)

data class SessionEfficiency(
    val totalSessionsAnalyzed: Long,
    val dataPoints: List<SessionEfficiencyPoint>
)

data class RestTimeBucket(
    val category: String,
    val count: Long,
    val percentage: Double
)

data class RestTimeDistribution(
    val totalWorkoutsAnalyzed: Long,
    val buckets: List<RestTimeBucket>
)

// Discipline & Habits domain models

data class ActivityHeatmapPoint(
    val date: LocalDate,
    val durationMinutes: Int
)

data class ActivityHeatmap(
    val points: List<ActivityHeatmapPoint>
)

data class PrHeatmapPoint(
    val date: LocalDate,
    val count: Int
)

data class PrHeatmap(
    val points: List<PrHeatmapPoint>
)

data class PrPeakTimePoint(
    val date: LocalDate,
    val hourOfDay: Int,
    val minuteOfHour: Int
)

data class PrPeakTime(
    val dataPoints: List<PrPeakTimePoint>
)

class StatisticsRepository @Inject constructor(
    private val apiService: StatisticsApiService
) {

    suspend fun getBodyWeightTrend(): Result<BodyWeightTrend> {
        return try {
            val response = apiService.getBodyWeightTrend()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("GET /api/statistics/body-weight failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOneRepMaxTrend(exerciseId: String): Result<OneRepMaxTrend> {
        return try {
            val response = apiService.getOneRepMaxTrend(exerciseId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(body.toDomain())
            } else {
                Result.failure(Exception("GET /api/statistics/1rm failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun BodyWeightTrendResponseDto.toDomain(): BodyWeightTrend {
        val points = dataPoints.mapNotNull { point ->
            runCatching { TrendPoint(LocalDate.parse(point.date), point.value) }.getOrNull()
        }
        return BodyWeightTrend(
            unit = unit,
            dataPoints = points
        )
    }

    private fun OneRepMaxTrendResponseDto.toDomain(): OneRepMaxTrend {
        val points = dataPoints.mapNotNull { point ->
            runCatching { TrendPoint(LocalDate.parse(point.date), point.value) }.getOrNull()
        }
        return OneRepMaxTrend(
            exerciseName = exerciseName,
            dataPoints = points
        )
    }

    // Hypertrophy & Overload

    suspend fun getVolumeTrend(): Result<VolumeTrend> {
        return try {
            val response = apiService.getVolumeTrend()
            Log.d("StatisticsRepo", "getVolumeTrend: code=${response.code()} body=${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val msg = "GET /api/statistics/volume-trend failed: ${response.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getVolumeTrend exception", e)
            Result.failure(e)
        }
    }

    suspend fun getRepRanges(): Result<RepRangeDistribution> {
        return try {
            val response = apiService.getRepRanges()
            Log.d("StatisticsRepo", "getRepRanges: code=${response.code()} body=${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val msg = "GET /api/statistics/rep-ranges failed: ${response.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getRepRanges exception", e)
            Result.failure(e)
        }
    }

    suspend fun getMuscleFrequency(): Result<MuscleFrequency> {
        return try {
            // Attempt 1: expect wrapped object { "muscles": [...] } (or alternate keys via @SerializedName)
            val wrappedResponse = apiService.getMuscleFrequency()
            Log.d("StatisticsRepo", "getMuscleFrequency wrapped: code=${wrappedResponse.code()} muscles=${wrappedResponse.body()?.muscles?.size}")
            if (wrappedResponse.isSuccessful && wrappedResponse.body() != null) {
                val domain = wrappedResponse.body()!!.toDomain()
                if (domain.muscles.isNotEmpty()) {
                    Log.d("StatisticsRepo", "getMuscleFrequency: wrapped form succeeded with ${domain.muscles.size} items")
                    return Result.success(domain)
                }
                // muscles came back empty — body parsed but key didn't match. Fall through to raw list.
                Log.w("StatisticsRepo", "getMuscleFrequency: wrapped body was empty muscles — trying raw list fallback")
            }

            // Attempt 2: bare JSON array response  e.g. ResponseEntity<List<MuscleFrequencyProjection>>
            val listResponse = apiService.getMuscleFrequencyAsList()
            Log.d("StatisticsRepo", "getMuscleFrequency list fallback: code=${listResponse.code()} size=${listResponse.body()?.size}")
            if (listResponse.isSuccessful && !listResponse.body().isNullOrEmpty()) {
                val items = listResponse.body()!!.map {
                    MuscleFrequencyItem(
                        muscleGroup = it.muscleGroup ?: "Unknown",
                        hitCount = it.hitCount ?: 0,
                        percentage = it.percentage ?: 0.0
                    )
                }
                Log.d("StatisticsRepo", "getMuscleFrequency: raw list fallback succeeded with ${items.size} items")
                Result.success(MuscleFrequency(muscles = items))
            } else {
                val msg = "GET /api/statistics/muscle-frequency failed: ${listResponse.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getMuscleFrequency exception", e)
            Result.failure(e)
        }
    }

    suspend fun getFatigueCorrelation(): Result<FatigueCorrelation> {
        return try {
            val response = apiService.getFatigueCorrelation()
            Log.d("StatisticsRepo", "getFatigueCorrelation: code=${response.code()} body=${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val msg = "GET /api/statistics/fatigue failed: ${response.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getFatigueCorrelation exception", e)
            Result.failure(e)
        }
    }

    suspend fun getSessionEfficiency(): Result<SessionEfficiency> {
        return try {
            val response = apiService.getSessionEfficiency()
            Log.d("StatisticsRepo", "getSessionEfficiency: code=${response.code()} body=${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val msg = "GET /api/statistics/efficiency failed: ${response.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getSessionEfficiency exception", e)
            Result.failure(e)
        }
    }

    suspend fun getRestTimeDistribution(): Result<RestTimeDistribution> {
        return try {
            val response = apiService.getRestTimeDistribution()
            Log.d("StatisticsRepo", "getRestTimeDistribution: code=${response.code()} body=${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val msg = "GET /api/statistics/density failed: ${response.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getRestTimeDistribution exception", e)
            Result.failure(e)
        }
    }

    suspend fun getActivityHeatmap(): Result<ActivityHeatmap> {
        return try {
            val wrappedResponse = apiService.getActivityHeatmap()
            Log.d(
                "StatisticsRepo",
                "getActivityHeatmap wrapped: code=${wrappedResponse.code()} size=${wrappedResponse.body()?.dataPoints?.size}"
            )
            if (wrappedResponse.isSuccessful && wrappedResponse.body() != null) {
                val domain = wrappedResponse.body()!!.toDomain()
                Result.success(domain)
            } else {
                val msg = "GET /api/statistics/activity-heatmap failed: ${wrappedResponse.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getActivityHeatmap exception", e)
            Result.failure(e)
        }
    }

    suspend fun getPrHeatmap(): Result<PrHeatmap> {
        return try {
            val wrappedResponse = apiService.getPrHeatmap()
            Log.d(
                "StatisticsRepo",
                "getPrHeatmap wrapped: code=${wrappedResponse.code()} size=${wrappedResponse.body()?.dataPoints?.size}"
            )
            if (wrappedResponse.isSuccessful && wrappedResponse.body() != null) {
                val domain = wrappedResponse.body()!!.toDomain()
                Result.success(domain)
            } else {
                val msg = "GET /api/statistics/pr-heatmap failed: ${wrappedResponse.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getPrHeatmap exception", e)
            Result.failure(e)
        }
    }

    suspend fun getPrPeakTime(): Result<PrPeakTime> {
        return try {
            val wrappedResponse = apiService.getPrPeakTime()
            Log.d(
                "StatisticsRepo",
                "getPrPeakTime wrapped: code=${wrappedResponse.code()} size=${wrappedResponse.body()?.dataPoints?.size}"
            )
            if (wrappedResponse.isSuccessful && wrappedResponse.body() != null) {
                val domain = wrappedResponse.body()!!.toDomain()
                if (domain.dataPoints.isNotEmpty()) {
                    return Result.success(domain)
                }
            }

            val listResponse = apiService.getPrPeakTimeAsList()
            Log.d(
                "StatisticsRepo",
                "getPrPeakTime list fallback: code=${listResponse.code()} size=${listResponse.body()?.size}"
            )
            if (listResponse.isSuccessful && !listResponse.body().isNullOrEmpty()) {
                val points = listResponse.body()!!
                    .mapNotNull { it.toDomainPointOrNull() }
                    .sortedBy { it.date }
                Result.success(PrPeakTime(dataPoints = points))
            } else {
                val msg = "GET /api/statistics/pr-peak-time failed: ${listResponse.code()}"
                Log.w("StatisticsRepo", msg)
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e("StatisticsRepo", "getPrPeakTime exception", e)
            Result.failure(e)
        }
    }

    // ─── DTO Converters ────────────────────────────────────────────────────────

    private fun VolumeTrendResponseDto.toDomain(): VolumeTrend {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd")
        val points = (dataPoints ?: emptyList()).map { dto ->
            // Format the backend date string into a UI-friendly label e.g., "Mar 30"
            val label = runCatching { 
                LocalDate.parse(dto.date ?: "").format(formatter)
            }.getOrDefault(dto.date ?: "")
            
            WeeklyVolumePoint(label, dto.value?.toDouble() ?: 0.0)
        }
        Log.d("StatisticsRepo", "VolumeTrend mapped ${points.size} points")
        return VolumeTrend(dataPoints = points)
    }

    private fun RepRangeResponseDto.toDomain(): RepRangeDistribution {
        val strength = buckets?.find { it.category?.equals("Strength", ignoreCase = true) == true }?.percentage ?: 0.0
        val hypertrophy = buckets?.find { it.category?.equals("Hypertrophy", ignoreCase = true) == true }?.percentage ?: 0.0
        val endurance = buckets?.find { it.category?.equals("Endurance", ignoreCase = true) == true }?.percentage ?: 0.0
        
        Log.d("StatisticsRepo", "RepRange mapped: Strength=$strength, Hypertrophy=$hypertrophy, Endurance=$endurance")
        return RepRangeDistribution(
            strengthPct = strength,
            hypertrophyPct = hypertrophy,
            endurancePct = endurance
        )
    }

    private fun MuscleFrequencyResponseDto.toDomain(): MuscleFrequency {
        val items = (muscles ?: emptyList()).map {
            MuscleFrequencyItem(
                muscleGroup = it.muscleGroup ?: "Unknown",
                hitCount = it.hitCount ?: 0,
                percentage = it.percentage ?: 0.0
            )
        }
        val sorted = items.sortedByDescending { it.percentage }
        Log.d("StatisticsRepo", "MuscleFrequency mapped ${sorted.size} items: $sorted")
        return MuscleFrequency(muscles = sorted)
    }

    private fun FatigueCorrelationResponseDto.toDomain(): FatigueCorrelation {
        val points = (dataPoints ?: emptyList()).mapNotNull { dto ->
            val date = runCatching { LocalDate.parse(dto.date ?: "") }.getOrNull() ?: return@mapNotNull null
            FatiguePoint(date, dto.totalVolume ?: 0.0, dto.averageRpe ?: 0.0)
        }
        return FatigueCorrelation(dataPoints = points)
    }

    private fun SessionEfficiencyResponseDto.toDomain(): SessionEfficiency {
        val points = (dataPoints ?: emptyList()).map { dto ->
            SessionEfficiencyPoint(dto.durationMinutes ?: 0L, dto.totalVolume ?: 0.0)
        }
        return SessionEfficiency(totalSessionsAnalyzed ?: 0L, points)
    }

    private fun RestTimeDistributionResponseDto.toDomain(): RestTimeDistribution {
        val bucketsList = (buckets ?: emptyList()).map { dto ->
            RestTimeBucket(dto.category ?: "Unknown", dto.count ?: 0L, dto.percentage ?: 0.0)
        }
        return RestTimeDistribution(totalWorkoutsAnalyzed ?: 0L, bucketsList)
    }

    private fun TrainingActivityResponseDto.toDomain(): ActivityHeatmap {
        val aggregated = (dataPoints ?: emptyList())
            .mapNotNull { point ->
                val parsedDate = parseDateFlexible(point.date.orEmpty()) ?: return@mapNotNull null
                parsedDate to (point.durationMinutes ?: 0).coerceAtLeast(0)
            }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .map { (date, values) ->
                ActivityHeatmapPoint(
                    date = date,
                    durationMinutes = values.sum().coerceAtLeast(0)
                )
            }
            .sortedBy { it.date }
        return ActivityHeatmap(points = aggregated)
    }

    private fun PrFrequencyResponseDto.toDomain(): PrHeatmap {
        val aggregated = (dataPoints ?: emptyList())
            .mapNotNull { point ->
                val parsedDate = parseDateFlexible(point.date.orEmpty()) ?: return@mapNotNull null
                parsedDate to (point.count ?: 0).coerceAtLeast(0)
            }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })
            .map { (date, values) ->
                PrHeatmapPoint(
                    date = date,
                    count = values.sum().coerceAtLeast(0)
                )
            }
            .sortedBy { it.date }
        return PrHeatmap(points = aggregated)
    }

    private fun PrPeakTimeResponseDto.toDomain(): PrPeakTime {
        val points = (dataPoints ?: emptyList())
            .mapNotNull { it.toDomainPointOrNull() }
            .sortedBy { it.date }
        return PrPeakTime(dataPoints = points)
    }

    private fun PrPeakTimePointDto.toDomainPointOrNull(): PrPeakTimePoint? {
        val parsedDate = parseDateFlexible(date ?: return null) ?: return null
        val hour = (hourOfDay ?: return null).coerceIn(0, 23)
        val minute = (minuteOfHour ?: return null).coerceIn(0, 59)
        return PrPeakTimePoint(
            date = parsedDate,
            hourOfDay = hour,
            minuteOfHour = minute
        )
    }

    private fun parseDateFlexible(raw: String): LocalDate? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null

        // Accept plain dates and timestamp strings by truncating to the date portion.
        val normalized = if (trimmed.length >= 10) trimmed.substring(0, 10) else trimmed
        return runCatching { LocalDate.parse(normalized) }.getOrNull()
    }
}
