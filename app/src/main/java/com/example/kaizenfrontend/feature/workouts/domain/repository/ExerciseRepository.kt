package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise

interface ExerciseRepository {
    suspend fun getExercises(): Result<List<Exercise>>
}
