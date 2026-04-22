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

// Discipline & Habits DTOs

data class ActivityHeatmapPointDto(
        @SerializedName("date") val date: String?,
        @SerializedName("durationMinutes", alternate = ["duration", "minutes", "workoutDurationMinutes"])
        val durationMinutes: Int?
)

data class TrainingActivityResponseDto(
        @SerializedName("totalActiveDays", alternate = ["totalDays", "total", "count"])
        val totalActiveDays: Int?,
        @SerializedName("dataPoints", alternate = ["points", "data", "items"])
        val dataPoints: List<ActivityHeatmapPointDto>?
)

data class PrFrequencyPointDto(
        @SerializedName("date") val date: String?,
        @SerializedName("count", alternate = ["hits", "total", "prCount"])
        val count: Int?
)

data class PrFrequencyResponseDto(
        @SerializedName("totalPrDays", alternate = ["totalDays", "total", "count"])
        val totalPrDays: Int?,
        @SerializedName("dataPoints", alternate = ["points", "data", "items"])
        val dataPoints: List<PrFrequencyPointDto>?
)

data class PrPeakTimePointDto(
        @SerializedName("date", alternate = ["day", "workoutDate", "prDate", "timestamp"])
        val date: String?,
        @SerializedName("hourOfDay", alternate = ["hour", "hour_of_day"])
        val hourOfDay: Int?,
        @SerializedName("minuteOfHour", alternate = ["minute", "minute_of_hour"])
        val minuteOfHour: Int?
)

data class PrPeakTimeResponseDto(
        @SerializedName("dataPoints", alternate = ["points", "data", "peakTimes", "items"])
        val dataPoints: List<PrPeakTimePointDto>?
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

        // Discipline & Habits
        @GET("/api/statistics/activity-heatmap")
        suspend fun getActivityHeatmap(): Response<TrainingActivityResponseDto>

        @GET("/api/statistics/pr-heatmap")
        suspend fun getPrHeatmap(): Response<PrFrequencyResponseDto>

        @GET("/api/statistics/pr-peak-time")
        suspend fun getPrPeakTime(): Response<PrPeakTimeResponseDto>

        // Fallback for APIs returning a bare array of point objects
        @GET("/api/statistics/pr-peak-time")
        suspend fun getPrPeakTimeAsList(): Response<List<PrPeakTimePointDto>>
}
