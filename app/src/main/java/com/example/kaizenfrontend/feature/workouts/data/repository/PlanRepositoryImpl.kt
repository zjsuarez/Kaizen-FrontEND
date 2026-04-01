package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.TrainingPlanRequest
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlanRepositoryImpl(
    private val api: PlanApiService,
    private val sessionManager: SessionManager
) : PlanRepository {

    override suspend fun createPlan(
        name: String,
        description: String,
        startingDate: String,
        interval: String?,
        cycleLength: Int?,
        isActive: Boolean
    ): Result<TrainingPlan> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"
            
            val request = TrainingPlanRequest(
                name = name,
                description = description,
                startingDate = startingDate,
                interval = interval,
                cycleLength = cycleLength,
                isActive = isActive
            )
            val response = api.createPlan(bearerToken, request)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val plan = TrainingPlan(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        startingDate = dto.startingDate,
                        interval = dto.interval,
                        cycleLength = dto.cycleLength,
                        isActive = dto.isActive
                    )
                    Result.success(plan)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Failed to create plan: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Log.e("PlanRepositoryImpl", "Network error", e) // if logger is available in future
            Result.failure(e)
        }
    }

    override suspend fun getPlans(): Result<List<TrainingPlan>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"
            
            val response = api.getAllPlans(bearerToken)
            if (response.isSuccessful) {
                val plans = response.body()?.map { dto ->
                    TrainingPlan(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        startingDate = dto.startingDate,
                        interval = dto.interval,
                        cycleLength = dto.cycleLength,
                        isActive = dto.isActive
                    )
                } ?: emptyList()
                Result.success(plans)
            } else {
                Result.failure(Exception("Failed to fetch plans: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePlan(planId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"

            val response = api.deletePlan(bearerToken, planId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete plan: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
