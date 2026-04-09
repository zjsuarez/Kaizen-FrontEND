package com.example.kaizenfrontend.feature.dashboard.data.repository

import com.example.kaizenfrontend.feature.dashboard.data.local.dao.DashboardDao
import com.example.kaizenfrontend.feature.dashboard.data.local.entity.DashboardEntity
import com.example.kaizenfrontend.feature.dashboard.data.remote.api.DashboardApiService
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.request.RegisterMeasurementMultipartRequest
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.MeasurementUpsertResponse
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
    suspend fun logBodyWeight(weight: Double): Result<Unit> {
        return try {
            val response = apiService.createBodyMeasurement(
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

    suspend fun createBodyMeasurement(
        weightKg: Double? = null,
        bodyFatPercentage: Double? = null,
        progressPhotoUrl: String? = null
    ): Result<Unit> {
        return try {
            val response = apiService.createBodyMeasurement(
                com.example.kaizenfrontend.feature.dashboard.data.remote.api.BodyMeasurementRequest(
                    weightKg = weightKg,
                    bodyFatPercentage = bodyFatPercentage,
                    progressPhotoUrl = progressPhotoUrl
                )
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

    suspend fun registerBodyMeasurement(
        request: RegisterMeasurementMultipartRequest
    ): Result<MeasurementUpsertResponse> {
        return try {
            val weightPart = request.weightKg
                ?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
            val bodyFatPart = request.bodyFatPercentage
                ?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
            val photoRequestBody = request.progressPhotoBytes
                .toRequestBody(request.mimeType.toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                name = "progressPhoto",
                filename = request.fileName,
                body = photoRequestBody
            )

            val response = apiService.registerBodyMeasurement(
                weightKg = weightPart,
                bodyFatPercentage = bodyFatPart,
                progressPhoto = photoPart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorPayload = response.errorBody()?.string().orEmpty()
                val reason = if (errorPayload.isNotBlank()) {
                    "Error: ${response.code()} ${response.message()} - $errorPayload"
                } else {
                    "Error: ${response.code()} ${response.message()} (empty body from /api/measurements)"
                }
                Result.failure(Exception(reason))
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
