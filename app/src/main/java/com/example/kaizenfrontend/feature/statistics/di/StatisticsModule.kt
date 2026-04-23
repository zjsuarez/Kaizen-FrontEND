package com.example.kaizenfrontend.feature.statistics.di

import com.example.kaizenfrontend.feature.statistics.data.remote.StatisticsApiService
import com.example.kaizenfrontend.feature.statistics.data.repository.StatisticsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StatisticsModule {

    @Provides
    @Singleton
    fun provideStatisticsApiService(retrofit: Retrofit): StatisticsApiService {
        return retrofit.create(StatisticsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStatisticsRepository(
        apiService: StatisticsApiService
    ): StatisticsRepository {
        return StatisticsRepository(apiService)
    }
}
