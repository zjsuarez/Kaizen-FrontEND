package com.example.kaizenfrontend.feature.dashboard.data.remote.api

import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.MeasurementCreatedResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.RecentPrDTO
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface DashboardApiService {
    @GET("/api/users/me/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @GET("/api/users/me/dashboard/pr-history")
    suspend fun getPrHistory(
        @Query("exercise") exercise: String,
        @Query("limit") limit: Int = 10
    ): Response<List<RecentPrDTO>>

    @GET("/api/workouts/{id}")
    suspend fun getWorkoutById(@Path("id") workoutId: String): Response<WorkoutResponseDto>

    @Multipart
    @POST("/api/measurements")
    suspend fun logBodyWeight(
        @Part("weightKg") weightKg: RequestBody,
        @Part("bodyFatPercentage") bodyFatPercentage: RequestBody? = null
    ): Response<Unit>

    @Multipart
    @POST("/api/measurements")
    suspend fun uploadProgressPhoto(
        @Part progressPhoto: MultipartBody.Part
    ): Response<MeasurementCreatedResponse>

    @GET("/api/measurements")
    suspend fun getWeightHistory(): Response<List<BodyMeasurementResponse>>

    @PUT("/api/preferences/dashboard")
    suspend fun saveWidgetPreferences(@Body widgetOrder: List<String>): Response<Unit>
}
