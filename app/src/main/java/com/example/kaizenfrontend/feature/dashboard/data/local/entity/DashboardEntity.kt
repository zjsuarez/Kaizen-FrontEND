package com.example.kaizenfrontend.feature.dashboard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse

@Entity(tableName = "dashboard_cache")
data class DashboardEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "dashboard_data")
    val dashboardData: DashboardResponse
)
