package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.request.WorkoutRequest

interface WorkoutRepository {
    suspend fun saveWorkout(request: WorkoutRequest): Result<WorkoutResponseDto>
    suspend fun getWorkouts(): Result<List<WorkoutResponseDto>>
}
