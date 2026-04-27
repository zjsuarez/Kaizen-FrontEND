package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.data.remote.ExerciseApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.toDomain
import com.example.kaizenfrontend.feature.workouts.domain.model.CreateCustomExerciseCommand
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepositoryImpl(
    private val api: ExerciseApiService,
    private val sessionManager: SessionManager,
    private val fallbackRepository: ExerciseRepository = MockExerciseRepository()
) : ExerciseRepository {

    override suspend fun getExercises(): Result<List<Exercise>> = withContext(Dispatchers.IO) {
        try {
            val fallbackExercises = fallbackRepository.getExercises().getOrDefault(emptyList())

            val token = sessionManager.getToken()
                ?: return@withContext Result.success(fallbackExercises)

            val bearerToken = "Bearer $token"

            val response = api.getExercises(token = bearerToken)

            if (!response.isSuccessful) {
                return@withContext Result.success(fallbackExercises)
            }

            val userExercises = response.body().orEmpty().map { it.toDomain() }

            // Merge: default mock catalog + user's custom exercises from backend
            val combined = (fallbackExercises + userExercises).distinctBy { it.id }
            Result.success(combined)
        } catch (e: Exception) {
            fallbackRepository.getExercises()
        }
    }

    override suspend fun createCustomExercise(command: CreateCustomExerciseCommand): Result<Exercise> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
                ?: return@withContext fallbackRepository.createCustomExercise(command)
            val bearerToken = "Bearer $token"

            val request = ExerciseRequest(
                name = command.name.trim(),
                description = command.description?.trim().takeUnless { it.isNullOrBlank() },
                muscleTarget = command.selectedMuscles
                    .map { it.trim().uppercase() }
                    .filter { it.isNotBlank() }
                    .joinToString(","),
                metrics = command.metrics,
                type = command.equipmentType.name,
                isCustom = true
            )

            val response = api.createExercise(bearerToken, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain())
                }
                    ?: Result.failure(Exception("Empty exercise response"))
            } else {
                Result.failure(Exception("Failed to create custom exercise: ${response.code()}"))
            }
        } catch (e: Exception) {
            fallbackRepository.createCustomExercise(command)
        }
    }
}
