package com.example.kaizenfrontend.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "kaizen_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val CALIBRATION_KEY = "calibration_complete"
    private const val PREFS_EMAIL = "prefs_email"
    private const val PREFS_UNIT = "prefs_unit"
    private const val PREFS_EFFORT = "prefs_effort"
    private const val PREFS_REST = "prefs_rest"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPreferences(context).edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(TOKEN_KEY, null)
    }

    fun clearToken(context: Context) {
        getPreferences(context).edit().remove(TOKEN_KEY).apply()
        clearUserPreferences(context)
    }

    fun saveCalibrationComplete(context: Context, complete: Boolean) {
        getPreferences(context).edit().putBoolean(CALIBRATION_KEY, complete).apply()
    }

    fun isCalibrationComplete(context: Context): Boolean {
        return getPreferences(context).getBoolean(CALIBRATION_KEY, false)
    }

    fun saveUserPreferences(context: Context, email: String, unitSystem: String, effortMetric: String, defaultRest: String) {
        getPreferences(context).edit()
            .putString(PREFS_EMAIL, email)
            .putString(PREFS_UNIT, unitSystem)
            .putString(PREFS_EFFORT, effortMetric)
            .putString(PREFS_REST, defaultRest)
            .apply()
    }

    fun getUserEmail(context: Context): String? = getPreferences(context).getString(PREFS_EMAIL, null)
    fun getUserUnitSystem(context: Context): String? = getPreferences(context).getString(PREFS_UNIT, null)
    fun getUserEffortMetric(context: Context): String? = getPreferences(context).getString(PREFS_EFFORT, null)
    fun getUserDefaultRest(context: Context): String? = getPreferences(context).getString(PREFS_REST, null)

    private fun clearUserPreferences(context: Context) {
        getPreferences(context).edit()
            .remove(PREFS_EMAIL)
            .remove(PREFS_UNIT)
            .remove(PREFS_EFFORT)
            .remove(PREFS_REST)
            .apply()
    }
}
