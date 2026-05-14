package com.example.kaizenfrontend.di

import android.content.Context
import com.example.kaizenfrontend.feature.auth.data.remote.AuthApiService
import com.example.kaizenfrontend.feature.user.data.remote.UserApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.ExerciseApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.WorkoutApiService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltServiceEntryPoint {
    fun authApiService(): AuthApiService
    fun userApiService(): UserApiService
    fun planApiService(): PlanApiService
    fun routineApiService(): RoutineApiService
    fun exerciseApiService(): ExerciseApiService
    fun workoutApiService(): WorkoutApiService
}

fun Context.hiltServiceEntryPoint(): HiltServiceEntryPoint {
    return EntryPointAccessors.fromApplication(this, HiltServiceEntryPoint::class.java)
}
