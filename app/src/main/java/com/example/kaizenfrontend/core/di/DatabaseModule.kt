package com.example.kaizenfrontend.core.di

import android.content.Context
import androidx.room.Room
import com.example.kaizenfrontend.feature.dashboard.data.local.KaizenDatabase
import com.example.kaizenfrontend.feature.dashboard.data.local.dao.DashboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKaizenDatabase(@ApplicationContext context: Context): KaizenDatabase {
        return Room.databaseBuilder(
            context,
            KaizenDatabase::class.java,
            "kaizen_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDashboardDao(database: KaizenDatabase): DashboardDao {
        return database.dashboardDao()
    }
}
