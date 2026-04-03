package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository

class UpdateRoutineUseCase(private val routineRepository: RoutineRepository) {
    suspend operator fun invoke(
        routineId: String,
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String?,
        startingDate: String?,
        exercises: List<RoutineExercise>
    ): Result<Routine> {
        // We first update the metadata
        val metadataResult = routineRepository.updateRoutine(
            routineId = routineId,
            planId = planId,
            name = name,
            description = description,
            schedulingValue = schedulingValue,
            startingDate = startingDate
        )

        if (metadataResult.isFailure) return metadataResult

        // If metadata succeeds, we update the exercises
        return routineRepository.updateRoutineExercises(
            routineId = routineId,
            exercises = exercises
        )
    }
}
