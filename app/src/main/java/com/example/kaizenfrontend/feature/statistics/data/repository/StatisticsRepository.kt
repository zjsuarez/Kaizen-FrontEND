package com.example.kaizenfrontend.feature.statistics.data.repository

import com.example.kaizenfrontend.feature.statistics.data.remote.BodyWeightTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.OneRepMaxTrendResponseDto
import com.example.kaizenfrontend.feature.statistics.data.remote.StatisticsApiService
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
}
