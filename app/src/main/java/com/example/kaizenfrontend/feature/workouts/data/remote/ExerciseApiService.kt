package com.example.kaizenfrontend.feature.workouts.data.remote

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.ExerciseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ExerciseApiService {

    @GET("api/exercises")
    suspend fun getExercises(
        @Header("Authorization") token: String
    ): Response<List<ExerciseResponse>>

    @GET("api/exercises/{exerciseId}")
    suspend fun getExercise(
        @Header("Authorization") token: String,
        @Path("exerciseId") exerciseId: String
    ): Response<ExerciseResponse>

    @POST("api/exercises")
    suspend fun createExercise(
        @Header("Authorization") token: String,
        @Body request: ExerciseRequest
    ): Response<ExerciseResponse>
}
