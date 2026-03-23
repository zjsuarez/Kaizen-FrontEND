package com.example.kaizenfrontend.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "kaizen_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val CALIBRATION_KEY = "calibration_complete"

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
    }

    fun saveCalibrationComplete(context: Context, complete: Boolean) {
        getPreferences(context).edit().putBoolean(CALIBRATION_KEY, complete).apply()
    }

    fun isCalibrationComplete(context: Context): Boolean {
        return getPreferences(context).getBoolean(CALIBRATION_KEY, false)
    }
}
