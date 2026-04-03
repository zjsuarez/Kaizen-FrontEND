package com.example.kaizenfrontend.feature.dashboard.data.remote.api

import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

data class BodyMeasurementRequest(
    val weightKg: Double
)

interface DashboardApiService {
    @GET("/api/users/me/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>

    @POST("/api/measurements")
    suspend fun logBodyWeight(@Body request: BodyMeasurementRequest): Response<Unit>

    @GET("/api/measurements")
    suspend fun getWeightHistory(): Response<List<BodyMeasurementResponse>>

    @PUT("/api/preferences/dashboard")
    suspend fun saveWidgetPreferences(@Body widgetOrder: List<String>): Response<Unit>
}
