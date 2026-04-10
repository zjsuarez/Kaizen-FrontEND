package com.example.kaizenfrontend.feature.workouts.di

import com.example.kaizenfrontend.feature.workouts.data.remote.WorkoutApiService
import com.example.kaizenfrontend.feature.workouts.data.repository.WorkoutRepositoryImpl
import com.example.kaizenfrontend.feature.workouts.domain.repository.WorkoutRepository
import com.example.kaizenfrontend.feature.workouts.domain.usecase.SaveWorkoutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkoutModule {

    @Provides
    @Singleton
    fun provideWorkoutApiService(retrofit: Retrofit): WorkoutApiService {
        return retrofit.create(WorkoutApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        apiService: WorkoutApiService
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideSaveWorkoutUseCase(
        repository: WorkoutRepository
    ): SaveWorkoutUseCase {
        return SaveWorkoutUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePlanApiService(retrofit: Retrofit): com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService {
        return retrofit.create(com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRoutineApiService(retrofit: Retrofit): com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService {
        return retrofit.create(com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlanRepository(
        apiService: com.example.kaizenfrontend.feature.workouts.data.remote.PlanApiService,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository {
        return com.example.kaizenfrontend.feature.workouts.data.repository.PlanRepositoryImpl(
            apiService,
            com.example.kaizenfrontend.core.data.local.SessionManager(context)
        )
    }

    @Provides
    @Singleton
    fun provideRoutineRepository(
        apiService: com.example.kaizenfrontend.feature.workouts.data.remote.RoutineApiService,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository {
        return com.example.kaizenfrontend.feature.workouts.data.repository.RoutineRepositoryImpl(
            apiService,
            com.example.kaizenfrontend.core.data.local.SessionManager(context),
            com.example.kaizenfrontend.feature.workouts.data.repository.MockExerciseRepository()
        )
    }

    @Provides
    @Singleton
    fun provideGetPlansUseCase(repository: com.example.kaizenfrontend.feature.workouts.domain.repository.PlanRepository): com.example.kaizenfrontend.feature.workouts.domain.usecase.GetPlansUseCase {
        return com.example.kaizenfrontend.feature.workouts.domain.usecase.GetPlansUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRoutinesUseCase(repository: com.example.kaizenfrontend.feature.workouts.domain.repository.RoutineRepository): com.example.kaizenfrontend.feature.workouts.domain.usecase.GetRoutinesUseCase {
        return com.example.kaizenfrontend.feature.workouts.domain.usecase.GetRoutinesUseCase(repository)
    }
}
