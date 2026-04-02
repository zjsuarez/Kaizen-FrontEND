package com.example.kaizenfrontend.feature.workouts.data.repository

import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineExerciseRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineResponse
import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.MuscleTarget
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
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
        schedulingValue: String,
        startingDate: String,
        routineExercises: List<RoutineExercise>
    ): Result<Routine> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken() ?: return@withContext Result.failure(Exception("No auth token found"))
            val bearerToken = "Bearer $token"
            
            val request = RoutineRequest(planId, name, description, schedulingValue, startingDate)
            val createResponse = api.createRoutine(bearerToken, request)
            if (!createResponse.isSuccessful) {
                Result.failure(Exception("Failed to create routine: ${createResponse.code()}"))
            } else {
                val createdDto = createResponse.body()
                    ?: return@withContext Result.failure(Exception("Response body is null"))

                if (routineExercises.isEmpty()) {
                    Result.success(createdDto.toDomain())
                } else {
                    val exercisesRequest = routineExercises.map { exercise ->
                        RoutineExerciseRequest(
                            targetSets = exercise.targetSets
                        )
                    }

                    val replaceResponse = api.replaceRoutineExercises(
                        token = bearerToken,
                        routineId = createdDto.id,
                        request = exercisesRequest
                    )

                    if (replaceResponse.isSuccessful) {
                        val updatedDto = replaceResponse.body()
                            ?: return@withContext Result.failure(Exception("Updated routine response body is null"))
                        Result.success(updatedDto.toDomain())
                    } else {
                        Result.failure(Exception("Routine created but failed to save exercises: ${replaceResponse.code()}"))
                    }
                }
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
                val routines = response.body()?.map { dto -> dto.toDomain() } ?: emptyList()
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

    private fun RoutineResponse.toDomain(): Routine {
        return Routine(
            id = id,
            planId = planId,
            name = name,
            description = description,
            schedulingValue = schedulingValue,
            startingDate = startingDate,
            exercises = (exercises ?: listRoutineExercises)
                .orEmpty()
                .sortedBy { it.orderIndex ?: Int.MAX_VALUE }
                .mapIndexed { index, exerciseDto ->
                    val embeddedExercise = exerciseDto.exercise
                    val resolvedOrderIndex = exerciseDto.orderIndex ?: index
                    val exerciseName = embeddedExercise?.name
                        ?: exerciseDto.exerciseName
                        ?: "Exercise ${resolvedOrderIndex + 1}"

                    val resolvedExerciseId = embeddedExercise?.id
                        ?: exerciseDto.exerciseId
                        ?: exerciseDto.id
                        ?: "routine_ex_$resolvedOrderIndex"

                    RoutineExercise(
                        exercise = Exercise(
                            id = resolvedExerciseId,
                            name = exerciseName,
                            muscleTarget = parseMuscleTarget(embeddedExercise?.muscleTarget),
                            equipmentType = parseEquipmentType(embeddedExercise?.type),
                            gifUrl = embeddedExercise?.gifUrl
                        ),
                        targetSets = (exerciseDto.targetSets ?: 0).coerceAtLeast(0),
                        targetReps = (exerciseDto.targetReps ?: 0).coerceAtLeast(0)
                    )
                }
        )
    }

    private fun parseMuscleTarget(value: String?): MuscleTarget {
        return runCatching {
            MuscleTarget.valueOf(value?.trim()?.uppercase().orEmpty())
        }.getOrDefault(MuscleTarget.CORE)
    }

    private fun parseEquipmentType(value: String?): EquipmentType {
        val normalized = when (value?.trim()?.uppercase()) {
            "BAND" -> "BANDS"
            else -> value?.trim()?.uppercase().orEmpty()
        }

        return runCatching {
            EquipmentType.valueOf(normalized)
        }.getOrDefault(EquipmentType.BODYWEIGHT)
    }
}
