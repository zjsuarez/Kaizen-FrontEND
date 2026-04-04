package com.example.kaizenfrontend.core.network

import com.example.kaizenfrontend.feature.auth.data.remote.AuthApiService
import com.example.kaizenfrontend.feature.user.data.remote.UserApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService
import com.example.kaizenfrontend.feature.workouts.data.remote.WorkoutApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 connects to localhost from the Android emulator
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val userService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val planService: PlanApiService by lazy {
        retrofit.create(PlanApiService::class.java)
    }

    val routineService: RoutineApiService by lazy {
        retrofit.create(RoutineApiService::class.java)
    }

    val workoutService: WorkoutApiService by lazy {
        retrofit.create(WorkoutApiService::class.java)
    }
}
