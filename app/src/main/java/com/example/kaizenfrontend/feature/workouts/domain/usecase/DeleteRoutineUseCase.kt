package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository

class DeleteRoutineUseCase(private val repository: RoutineRepository) {
    suspend operator fun invoke(routineId: String): Result<Unit> {
        if (routineId.isBlank()) return Result.failure(Exception("Routine id cannot be blank"))
        return repository.deleteRoutine(routineId)
    }
}
