package com.example.kaizenfrontend.feature.dashboard.data.local

import androidx.room.TypeConverter
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.DashboardResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DashboardConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromDashboardResponse(response: DashboardResponse?): String? {
        if (response == null) return null
        return gson.toJson(response)
    }

    @TypeConverter
    @Suppress("SENSELESS_COMPARISON")
    fun toDashboardResponse(json: String?): DashboardResponse? {
        if (json == null) return null
        val type = object : TypeToken<DashboardResponse>() {}.type
        val response: DashboardResponse = gson.fromJson(json, type)
        // Gson skips Kotlin default values, so list fields absent in old cached JSON come back null.
        return response.copy(
            trainingDaysThisMonth = response.trainingDaysThisMonth ?: emptyList(),
            trainingDayDetails = response.trainingDayDetails ?: emptyList(),
            recentPrs = response.recentPrs ?: emptyList(),
            streakCalendar = response.streakCalendar ?: emptyList(),
            recentWorkoutSummaries = response.recentWorkoutSummaries ?: emptyList()
        )
    }
}
