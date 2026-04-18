package com.example.kaizenfrontend.feature.statistics.data.repository

import android.util.Log
import com.example.kaizenfrontend.feature.statistics.data.remote.BodyWeightTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.MuscleFrequencyResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.OneRepMaxTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.RepRangeResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.StatisticsApiService
import com.example.kaizenfrontend.feature.statistics.data.remote.VolumeTrendResponseDto
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
        Log.d("StatisticsRepo", "MuscleFrequency mapped ${items.size} items: $items")
        return MuscleFrequency(muscles = items)
    }
}
