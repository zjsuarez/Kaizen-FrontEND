package com.example.kaizenfrontend.feature.dashboard.data.repository

import com.example.kaizenfrontend.feature.dashboard.data.local.dao.DashboardDao
import com.example.kaizenfrontend.feature.dashboard.data.local.entity.DashboardEntity
import com.example.kaizenfrontend.feature.dashboard.data.remote.api.DashboardApiService
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    suspend fun logBodyWeight(weight: Double, bodyFatPercentage: Double? = null): Result<Unit> {
        return try {
            val plainText = "text/plain".toMediaTypeOrNull()
            val weightPart = weight.toString().toRequestBody(plainText)
            val bodyFatPart = bodyFatPercentage?.toString()?.toRequestBody(plainText)
            val response = apiService.logBodyWeight(weightPart, bodyFatPart)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProgressPhoto(
        photoBytes: ByteArray,
        mimeType: String,
        fileName: String
    ): Result<String> {
        return try {
            val photoBody = photoBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("progressPhoto", fileName, photoBody)
            val response = apiService.uploadProgressPhoto(photoPart)
            if (response.isSuccessful) Result.success(response.body()!!.id)
            else Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
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

    suspend fun saveWidgetOrder(widgetOrder: List<String>): Result<Unit> {
        return try {
            val response = apiService.saveWidgetPreferences(widgetOrder)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("PUT /api/preferences/dashboard failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
