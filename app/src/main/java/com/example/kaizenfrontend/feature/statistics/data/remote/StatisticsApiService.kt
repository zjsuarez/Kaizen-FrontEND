package com.example.kaizenfrontend.feature.statistics.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Strength & Health DTOs

data class TrendPointDto(val date: String, val value: Double)

data class BodyWeightTrendResponseDto(val unit: String, val dataPoints: List<TrendPointDto>)

data class OneRepMaxTrendResponseDto(val exerciseName: String, val dataPoints: List<TrendPointDto>)

// Hypertrophy & Overload DTOs
// All fields are nullable so Gson silently returns null on JSON key mismatch
// rather than NPE-ing at runtime. The repository toDomain() applies safe defaults.

data class VolumeDataPointDto(
        @SerializedName("date", alternate = ["weekLabel", "timestamp", "groupingLabel", "label"]) val date: String?,
        @SerializedName("value", alternate = ["totalTonnage", "tonnage", "volume", "amount"]) val value: Float?
)

data class VolumeTrendResponseDto(
        @SerializedName("dataPoints", alternate = ["points", "data_points", "data"]) val dataPoints: List<VolumeDataPointDto>?,
        @SerializedName("grouping") val grouping: String?
)

data class RepRangeBucketDto(
        @SerializedName("category") val category: String?,
        @SerializedName("range") val range: String?,
        @SerializedName("count") val count: Int?,
        @SerializedName("percentage") val percentage: Double?
)

data class RepRangeResponseDto(
        @SerializedName("buckets") val buckets: List<RepRangeBucketDto>?,
        @SerializedName("totalSets") val totalSets: Int?
)

data class MuscleFrequencyItemDto(
        @SerializedName("muscleGroup", alternate = ["muscle", "muscle_group", "name"])
        val muscleGroup: String?,
        @SerializedName(
                "hitCount",
                alternate = ["count", "hits", "hit_count", "totalHits", "frequency"]
        )
        val hitCount: Int?,
        @SerializedName("percentage", alternate = ["pct", "percent"]) val percentage: Double?
)

data class MuscleFrequencyResponseDto(
        // Accept both "muscles" and common wrapping key variants
        @SerializedName("muscles", alternate = ["data", "muscleFrequencies", "items", "results"])
        val muscles: List<MuscleFrequencyItemDto>?
)

// Efficiency & Fatigue DTOs
data class FatiguePointDto(
        @SerializedName("date") val date: String?,
        @SerializedName("totalVolume") val totalVolume: Double?,
        @SerializedName("averageRpe") val averageRpe: Double?
)

data class FatigueCorrelationResponseDto(
        @SerializedName("dataPoints") val dataPoints: List<FatiguePointDto>?
)

data class SessionEfficiencyPointDto(
        @SerializedName("durationMinutes") val durationMinutes: Long?,
        @SerializedName("totalVolume") val totalVolume: Double?
)

data class SessionEfficiencyResponseDto(
        @SerializedName("totalSessionsAnalyzed") val totalSessionsAnalyzed: Long?,
        @SerializedName("dataPoints") val dataPoints: List<SessionEfficiencyPointDto>?
)

data class RestTimeBucketDto(
        @SerializedName("category") val category: String?,
        @SerializedName("count") val count: Long?,
        @SerializedName("percentage") val percentage: Double?
)

data class RestTimeDistributionResponseDto(
        @SerializedName("totalWorkoutsAnalyzed") val totalWorkoutsAnalyzed: Long?,
        @SerializedName("buckets") val buckets: List<RestTimeBucketDto>?
)

// API interface 

interface StatisticsApiService {

    // Strength & Health
    @GET("/api/statistics/body-weight")
    suspend fun getBodyWeightTrend(): Response<BodyWeightTrendResponseDto>

    @GET("/api/statistics/1rm")
    suspend fun getOneRepMaxTrend(
            @Query("exerciseId") exerciseId: String
    ): Response<OneRepMaxTrendResponseDto>

    // Hypertrophy & Overload
    @GET("/api/statistics/volume")
    suspend fun getVolumeTrend(): Response<VolumeTrendResponseDto>

    @GET("/api/statistics/rep-ranges") suspend fun getRepRanges(): Response<RepRangeResponseDto>

    @GET("/api/statistics/muscle-frequency")
    suspend fun getMuscleFrequency(): Response<MuscleFrequencyResponseDto>

    // Fallback: some Spring Boot controllers return a bare JSON array instead of a wrapper object.
    // We call both and use whichever yields data.
    @GET("/api/statistics/muscle-frequency")
    suspend fun getMuscleFrequencyAsList(): Response<List<MuscleFrequencyItemDto>>

    // Efficiency & Fatigue (The Brain)
    @GET("/api/statistics/fatigue")
    suspend fun getFatigueCorrelation(): Response<FatigueCorrelationResponseDto>

    @GET("/api/statistics/efficiency")
    suspend fun getSessionEfficiency(): Response<SessionEfficiencyResponseDto>

    @GET("/api/statistics/density")
    suspend fun getRestTimeDistribution(): Response<RestTimeDistributionResponseDto>
}
