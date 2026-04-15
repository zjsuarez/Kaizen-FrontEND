package com.example.kaizenfrontend.feature.statistics.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class TrendPointDto(
    val date: String,
    val value: Double
)

data class BodyWeightTrendResponseDto(
    val unit: String,
    val dataPoints: List<TrendPointDto>
)

data class OneRepMaxTrendResponseDto(
    val exerciseName: String,
    val dataPoints: List<TrendPointDto>
)

interface StatisticsApiService {

    @GET("/api/statistics/body-weight")
    suspend fun getBodyWeightTrend(): Response<BodyWeightTrendResponseDto>

    @GET("/api/statistics/1rm")
    suspend fun getOneRepMaxTrend(
        @Query("exerciseId") exerciseId: String
    ): Response<OneRepMaxTrendResponseDto>
}
