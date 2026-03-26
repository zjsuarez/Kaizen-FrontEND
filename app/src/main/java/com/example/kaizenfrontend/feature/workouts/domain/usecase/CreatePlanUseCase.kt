package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository

class CreatePlanUseCase(private val repository: PlanRepository) {
    suspend operator fun invoke(name: String, description: String, startingDate: String, isActive: Boolean = true): Result<TrainingPlan> {
        if (name.isBlank()) return Result.failure(Exception("Name cannot be blank"))
        // If startingDate is empty, default to current date.
        // Actually the caller should specify, but if not we proceed.
        return repository.createPlan(name, description, startingDate, isActive)
    }
}
