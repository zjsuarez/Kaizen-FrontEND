package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.Routine

interface RoutineRepository {
    suspend fun createRoutine(
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String,
        startingDate: String
    ): Result<Routine>
    suspend fun getRoutines(planId: String? = null): Result<List<Routine>>
    suspend fun deleteRoutine(routineId: String): Result<Unit>
}
