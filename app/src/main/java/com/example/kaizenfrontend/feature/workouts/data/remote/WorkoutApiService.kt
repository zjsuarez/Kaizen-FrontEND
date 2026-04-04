package com.example.kaizenfrontend.feature.workouts.data.remote

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.request.WorkoutRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkoutApiService {

    @POST("api/workouts")
    suspend fun saveWorkout(
        @Body request: WorkoutRequest
    ): Response<WorkoutResponseDto>

    @GET("api/workouts")
    suspend fun getWorkouts(): Response<List<WorkoutResponseDto>>

    @GET("api/workouts/{workoutId}")
    suspend fun getWorkoutDetails(
        @Path("workoutId") workoutId: String
    ): Response<WorkoutResponseDto>
}
