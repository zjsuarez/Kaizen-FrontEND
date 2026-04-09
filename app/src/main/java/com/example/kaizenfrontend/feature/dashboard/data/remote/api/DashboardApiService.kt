package com.example.kaizenfrontend.feature.dashboard.data.remote.api

import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.MeasurementUpsertResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT

data class BodyMeasurementRequest(
    val weightKg: Double? = null,
    val bodyFatPercentage: Double? = null,
    val progressPhotoUrl: String? = null
)

interface DashboardApiService {
    @GET("/api/users/me/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @POST("/api/users/me/body-measurements")
    suspend fun createBodyMeasurement(@Body request: BodyMeasurementRequest): Response<BodyMeasurementResponse>

    @Multipart
    @POST("/api/measurements")
    suspend fun registerBodyMeasurement(
        @Part("weightKg") weightKg: RequestBody?,
        @Part("bodyFatPercentage") bodyFatPercentage: RequestBody?,
        @Part progressPhoto: MultipartBody.Part
    ): Response<MeasurementUpsertResponse>

    @GET("/api/users/me/body-measurements")
    suspend fun getWeightHistory(): Response<List<BodyMeasurementResponse>>

    @PUT("/api/preferences/dashboard")
    suspend fun saveWidgetPreferences(@Body widgetOrder: List<String>): Response<Unit>
}
