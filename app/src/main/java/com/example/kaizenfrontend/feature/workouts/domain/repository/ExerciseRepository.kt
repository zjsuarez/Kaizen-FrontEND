package com.example.kaizenfrontend.feature.workouts.domain.repository

import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.CreateCustomExerciseCommand

interface ExerciseRepository {
    suspend fun getExercises(): Result<List<Exercise>>
    suspend fun createCustomExercise(command: CreateCustomExerciseCommand): Result<Exercise>
}
