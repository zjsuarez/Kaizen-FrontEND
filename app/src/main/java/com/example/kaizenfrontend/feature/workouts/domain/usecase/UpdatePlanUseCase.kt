package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository

class UpdatePlanUseCase(private val planRepository: PlanRepository) {
    suspend operator fun invoke(
        planId: String,
        name: String,
        description: String,
        isActive: Boolean
    ): Result<TrainingPlan> {
        return planRepository.updatePlan(
            planId = planId,
            name = name,
            description = description,
            isActive = isActive
        )
    }
}
