package com.example.kaizenfrontend.feature.workouts.data.remote

import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineRequest
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.RoutineResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutineApiService {

    @POST("/api/routines")
    suspend fun createRoutine(
        @Header("Authorization") token: String,
        @Body request: RoutineRequest
    ): Response<RoutineResponse>

    @GET("/api/routines")
    suspend fun getUserRoutines(
        @Header("Authorization") token: String,
        @Query("planId") planId: String? = null
    ): Response<List<RoutineResponse>>

    @GET("/api/routines/{routineId}")
    suspend fun getRoutine(
        @Header("Authorization") token: String,
        @Path("routineId") routineId: String
    ): Response<RoutineResponse>

    @PUT("/api/routines/{routineId}")
    suspend fun editRoutine(
        @Header("Authorization") token: String,
        @Path("routineId") routineId: String,
        @Body request: RoutineRequest
    ): Response<RoutineResponse>

    @DELETE("/api/routines/{routineId}")
    suspend fun deleteRoutine(
        @Header("Authorization") token: String,
        @Path("routineId") routineId: String
    ): Response<Unit>
}
