package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan

interface PlanRepository {
    suspend fun createPlan(name: String, description: String, startingDate: String, isActive: Boolean): Result<TrainingPlan>
    suspend fun getPlans(): Result<List<TrainingPlan>>
}
