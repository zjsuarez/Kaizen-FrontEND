package com.example.kaizenfrontend.feature.workouts.domain.usecase

import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository

class GetPlansUseCase(private val repository: PlanRepository) {
    suspend operator fun invoke(): Result<List<TrainingPlan>> {
        return repository.getPlans()
    }
}
