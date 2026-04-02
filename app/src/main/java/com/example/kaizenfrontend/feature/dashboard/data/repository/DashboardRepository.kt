package com.example.kaizenfrontend.feature.dashboard.data.repository

import com.example.kaizenfrontend.feature.dashboard.data.local.dao.DashboardDao
import com.example.kaizenfrontend.feature.dashboard.data.local.entity.DashboardEntity
import com.example.kaizenfrontend.feature.dashboard.data.remote.api.DashboardApiService
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiService: DashboardApiService,
    private val dashboardDao: DashboardDao
) {

    fun getDashboardStream(): Flow<DashboardResponse?> {
        return dashboardDao.getDashboard().map { it?.dashboardData }
    }

    suspend fun refreshDashboard(): Result<Unit> {
        return try {
            val response = apiService.getDashboardData()
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.IO) {
                    dashboardDao.insertDashboard(DashboardEntity(1, response.body()!!))
                }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun logBodyWeight(weight: Double): Result<Unit> {
        return try {
            val response = apiService.logBodyWeight(
                com.example.kaizenfrontend.feature.dashboard.data.remote.api.BodyMeasurementRequest(weightKg = weight)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeightHistory(): Result<List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>> {
        return try {
            val response = apiService.getWeightHistory()
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
