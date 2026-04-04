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
import com.example.kaizenfrontend.feature.workouts.domain.repository.ExerciseRepository
import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineRepositoryImpl(
    private val api: RoutineApiService,
    private val sessionManager: SessionManager,
    private val exerciseRepository: ExerciseRepository
) : RoutineRepository {

    // Lazily cached builtin exercise catalog for resolving keys to display names
    private var builtinCatalog: Map<String, Exercise>? = null

    private suspend fun getBuiltinCatalog(): Map<String, Exercise> {
        builtinCatalog?.let { return it }
        val catalog = exerciseRepository.getExercises()
            .getOrDefault(emptyList())
            .associateBy { it.id }
        builtinCatalog = catalog
        return catalog
    }

    override suspend fun createRoutine(
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String,
        startingDate: String,
        routineExercises: List<RoutineExercise>
    ): Result<Routine> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
                ?: return@withContext Result.failure(Exception("No auth token found"))
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
                    val exercisesRequest = routineExercises.map { routineEx ->
                        RoutineExerciseRequest(
                            targetSets = routineEx.targetSets,
                            builtinExerciseKey = routineEx.exercise.id,
                            customExerciseId = null
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

    override suspend fun updateRoutine(
        routineId: String,
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String?,
        startingDate: String?
    ): Result<Routine> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
                ?: return@withContext Result.failure(Exception("No auth token"))
            val bearerToken = "Bearer $token"

            val request = RoutineRequest(
                planId = planId,
                name = name,
                description = description,
                schedulingValue = schedulingValue,
                startingDate = startingDate
            )

            val response = api.editRoutine(bearerToken, routineId, request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain())
                } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed to update routine: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoutineExercises(
        routineId: String,
        exercises: List<RoutineExercise>
    ): Result<Routine> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
                ?: return@withContext Result.failure(Exception("No auth token"))
            val bearerToken = "Bearer $token"

            val exerciseRequests = exercises.map { re ->
                RoutineExerciseRequest(
                    targetSets = re.targetSets,
                    customExerciseId = null,
                    builtinExerciseKey = re.exercise.id
                )
            }

            val response = api.replaceRoutineExercises(bearerToken, routineId, exerciseRequests)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain())
                } ?: Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("Failed to update routine exercises: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoutines(planId: String?): Result<List<Routine>> = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.getToken()
                ?: return@withContext Result.failure(Exception("No auth token found"))
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
            val token = sessionManager.getToken()
                ?: return@withContext Result.failure(Exception("No auth token found"))
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

    private suspend fun RoutineResponse.toDomain(): Routine {
        val catalog = getBuiltinCatalog()

        return Routine(
            id = id,
            planId = planId,
            name = name,
            description = description,
            schedulingValue = schedulingValue,
            startingDate = startingDate,
            lastPerformedDate = lastPerformedDate,
            exercises = (exercises ?: listRoutineExercises)
                .orEmpty()
                .sortedBy { it.orderIndex ?: Int.MAX_VALUE }
                .mapIndexed { index, exerciseDto ->
                    val resolvedOrderIndex = exerciseDto.orderIndex ?: index

                    // Resolve exercise identity from builtin key or custom id
                    val builtinKey = exerciseDto.builtinExerciseKey
                    val customId = exerciseDto.customExerciseId

                    val catalogExercise = builtinKey?.let { catalog[it] }

                    val exerciseName = catalogExercise?.name
                        ?: builtinKey
                        ?: "Custom Exercise"

                    val exerciseId = builtinKey
                        ?: customId
                        ?: exerciseDto.id
                        ?: "routine_ex_$resolvedOrderIndex"

                    RoutineExercise(
                        exercise = Exercise(
                            id = exerciseId,
                            name = exerciseName,
                            muscleTarget = catalogExercise?.muscleTarget ?: MuscleTarget.CORE,
                            equipmentType = catalogExercise?.equipmentType ?: EquipmentType.BODYWEIGHT,
                            gifUrl = catalogExercise?.gifUrl,
                            isCustom = customId != null
                        ),
                        targetSets = (exerciseDto.targetSets ?: 0).coerceAtLeast(0),
                        targetReps = 0
                    )
                }
        )
    }
}
