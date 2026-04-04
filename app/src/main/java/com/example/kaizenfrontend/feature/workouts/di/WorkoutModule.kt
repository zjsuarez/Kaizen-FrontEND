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
}
