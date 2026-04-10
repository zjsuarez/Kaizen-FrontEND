package com.example.kaizenfrontend.core.di

import android.content.Context
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDashboardPreferences(
        @ApplicationContext context: Context,
        sessionManager: SessionManager
    ): DashboardPreferences {
        return DashboardPreferences(context, sessionManager)
    }
}
