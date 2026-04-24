package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.data.remote.ExerciseApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.toDomain
import com.example.kaizenfrontend.feature.workouts.domain.model.CreateCustomExerciseCommand
import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric
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
            val token = sessionManager.getToken()
                ?: return@withContext fallbackRepository.getExercises()
            val bearerToken = "Bearer $token"
            val currentUserId = sessionManager.getUserIdFromToken()

            val response = api.getExercises(
                token = bearerToken,
                createdByUserId = currentUserId
            )
            if (!response.isSuccessful) {
                return@withContext fallbackRepository.getExercises()
            }

            val visibleExercises = response.body().orEmpty()
                .map { it.toDomain() }
                .filter { exercise ->
                    !exercise.isCustom ||
                        exercise.createdByUserId.isNullOrBlank() ||
                        exercise.createdByUserId == currentUserId
                }

            Result.success(visibleExercises)
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
                type = command.metric.toBackendType(),
                isCustom = true
            )

            val response = api.createExercise(bearerToken, request)
            if (response.isSuccessful) {
                val currentUserId = sessionManager.getUserIdFromToken()
                response.body()?.let {
                    val created = it.toDomain().copy(
                        createdByUserId = it.createdByUserId ?: currentUserId
                    )
                    Result.success(created)
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

private fun ExerciseMetric.toBackendType(): String {
    return when (this) {
        ExerciseMetric.SETS -> "Set"
        ExerciseMetric.DURATION -> "Duration"
        ExerciseMetric.DISTANCE -> "Distance"
        ExerciseMetric.SIMPLE_CHECK_OFF -> "SimpleCheckOff"
    }
}
