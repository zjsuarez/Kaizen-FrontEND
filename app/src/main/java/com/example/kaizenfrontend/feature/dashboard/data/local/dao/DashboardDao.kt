package com.example.kaizenfrontend.feature.dashboard.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaizenfrontend.feature.dashboard.data.local.entity.DashboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {

    @Query("SELECT * FROM dashboard_cache WHERE id = 1")
    fun getDashboard(): Flow<DashboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDashboard(entity: DashboardEntity)
}