package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.feature.workouts.data.remote.WorkoutApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.request.WorkoutRequest
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutRepositoryImpl(
    private val apiService: WorkoutApiService
) : WorkoutRepository {

    override suspend fun saveWorkout(request: WorkoutRequest): Result<WorkoutResponseDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.saveWorkout(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception(response.message() ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getWorkouts(): Result<List<WorkoutResponseDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWorkouts()
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception(response.message() ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
