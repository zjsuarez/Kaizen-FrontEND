package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineRequest
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepositoryImpl(
    private val api: RoutineApiService,
    private val sessionManager: SessionManager
) : RoutineRepository {

    override suspend fun createRoutine(
        planId: String?,
        name: String,
        description: String,
        schedulingType: String,
        cycleLength: Int?,
        schedulingValue: String,
        startingDate: String
    ): Result<Routine> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"
            
            val request = RoutineRequest(planId, name, description, schedulingType, cycleLength, schedulingValue, startingDate)
            val response = api.createRoutine(bearerToken, request)
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    val routine = Routine(dto.id, dto.planId, dto.name, dto.description, dto.schedulingType, dto.cycleLength, dto.schedulingValue, dto.startingDate)
                    Result.success(routine)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Failed to create routine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutines(planId: String?): Result<List<Routine>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"
            
            val response = api.getUserRoutines(bearerToken, planId)
            if (response.isSuccessful) {
                val routines = response.body()?.map { dto ->
                    Routine(dto.id, dto.planId, dto.name, dto.description, dto.schedulingType, dto.cycleLength, dto.schedulingValue, dto.startingDate)
                } ?: emptyList()
                Result.success(routines)
            } else {
                Result.failure(Exception("Failed to fetch routines: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRoutine(routineId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"

            val response = api.deleteRoutine(bearerToken, routineId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete routine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
