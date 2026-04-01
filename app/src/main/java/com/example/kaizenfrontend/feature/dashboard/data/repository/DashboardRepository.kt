package com.example.kaizenfrontend.feature.dashboard.data.repository

import com.example.kaizenfrontend.feature.dashboard.data.remote.api.DashboardApiService
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiService: DashboardApiService
) {
    suspend fun fetchDashboardData(): Result<DashboardResponse> {
        return try {
            val response = apiService.getDashboardData()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
