package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan

interface PlanRepository {
    suspend fun createPlan(
        name: String,
        description: String,
        startingDate: String,
        interval: String?,
        cycleLength: Int?,
        isActive: Boolean
    ): Result<TrainingPlan>
    suspend fun getPlans(): Result<List<TrainingPlan>>
    suspend fun deletePlan(planId: String): Result<Unit>
}
