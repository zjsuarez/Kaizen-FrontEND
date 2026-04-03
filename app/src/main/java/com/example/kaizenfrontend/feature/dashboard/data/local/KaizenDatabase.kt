package com.example.kaizenfrontend.feature.dashboard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kaizenfrontend.feature.dashboard.data.local.dao.DashboardDao
import com.example.kaizenfrontend.feature.dashboard.data.local.entity.DashboardEntity

@Database(entities = [DashboardEntity::class], version = 2, exportSchema = false)
@TypeConverters(DashboardConverters::class)
abstract class KaizenDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
}
