package com.example.kaizenfrontend.feature.dashboard.data.remote.api

import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET

interface DashboardApiService {
    @GET("/api/users/me/dashboard")
    suspend fun getDashboardData(): Response<DashboardResponse>
}
