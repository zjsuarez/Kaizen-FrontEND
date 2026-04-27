package com.example.kaizenfrontend.feature.user.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName(value = "id", alternate = ["ID"])
    val id: String,
    @SerializedName(value = "username", alternate = ["userName", "user_name"])
    val username: String,
    @SerializedName(value = "email", alternate = ["eMail"])
    val email: String,
    @SerializedName(value = "profilePic", alternate = ["profile_pic", "profileImage", "profile_image"])
    val profilePic: String?,
    @SerializedName(value = "primaryGoal", alternate = ["primary_goal", "goalPrimary", "goal_primary"])
    val primaryGoal: String?,
    @SerializedName(value = "unitSystem", alternate = ["unit_system", "units", "unit"])
    val unitSystem: String?,
    @SerializedName(value = "effortMeasurement", alternate = ["effort_measurement", "effortMetric", "effort_metric"])
    val effortMeasurement: String?,
    @SerializedName(value = "restTimerDefault", alternate = ["rest_timer_default", "defaultRestTimer", "default_rest_timer"])
    val restTimerDefault: Int?,
    @SerializedName(value = "equipmentAvailable", alternate = ["equipment_available", "equipment", "available_equipment"])
    val equipmentAvailable: List<String>?,
    @SerializedName(value = "authProvider", alternate = ["auth_provider", "provider", "authType", "auth_type"])
    val authProvider: String?
)
