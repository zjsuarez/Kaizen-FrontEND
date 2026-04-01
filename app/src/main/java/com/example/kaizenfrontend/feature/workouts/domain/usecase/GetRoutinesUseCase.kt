package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository

class GetRoutinesUseCase(private val repository: RoutineRepository) {
    suspend operator fun invoke(planId: String? = null): Result<List<Routine>> {
        return repository.getRoutines(planId)
    }
}
