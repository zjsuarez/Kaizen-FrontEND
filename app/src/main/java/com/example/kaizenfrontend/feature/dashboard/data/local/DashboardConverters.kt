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
    fun toDashboardResponse(json: String?): DashboardResponse? {
        if (json == null) return null
        val type = object : TypeToken<DashboardResponse>() {}.type
        return gson.fromJson(json, type)
    }
}
