package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository

class CreateRoutineUseCase(private val repository: RoutineRepository) {
    suspend operator fun invoke(
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String = "MONDAY",
        startingDate: String = "2026-03-24", // Defaults that backend expects or user can configure
        routineExercises: List<RoutineExercise> = emptyList()
    ): Result<Routine> {
        if (name.isBlank()) return Result.failure(Exception("Name cannot be blank"))
        return repository.createRoutine(
            planId = planId,
            name = name,
            description = description,
            schedulingValue = schedulingValue,
            startingDate = startingDate,
            routineExercises = routineExercises
        )
    }
}
