package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository

class DeletePlanUseCase(private val repository: PlanRepository) {
    suspend operator fun invoke(planId: String): Result<Unit> {
        if (planId.isBlank()) return Result.failure(Exception("Plan id cannot be blank"))
        return repository.deletePlan(planId)
    }
}
