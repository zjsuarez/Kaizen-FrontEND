package com.example.kaizenfrontend.core.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Manages persistent session data (auth token, calibration flag, and cached user preferences)
 * using SharedPreferences. Replaces the old TokenManager singleton.
 */
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit().putString(TOKEN_KEY, token).apply()

    fun getToken(): String? = prefs.getString(TOKEN_KEY, null)

    fun getUserIdFromToken(): String {
        val token = getToken() ?: return "default_anonymous"
        return try {
            val split = token.split(".")
            if (split.size < 2) return "default_anonymous"
            val decodedBytes = android.util.Base64.decode(split[1], android.util.Base64.URL_SAFE)
            val json = String(decodedBytes, Charsets.UTF_8)
            org.json.JSONObject(json).optString("sub", "default_anonymous")
        } catch (e: Exception) {
            "default_anonymous"
        }
    }

    fun clearToken() {
        prefs.edit()
            .remove(TOKEN_KEY)
            .remove(PREFS_EMAIL)
            .remove(PREFS_UNIT)
            .remove(PREFS_EFFORT)
            .remove(PREFS_REST)
            .apply()
    }

    fun saveCalibrationComplete(complete: Boolean) =
        prefs.edit().putBoolean(CALIBRATION_KEY, complete).apply()

    fun isCalibrationComplete(): Boolean = prefs.getBoolean(CALIBRATION_KEY, false)

    fun saveUserPreferences(
        email: String,
        unitSystem: String,
        effortMetric: String,
        defaultRest: String
    ) {
        prefs.edit()
            .putString(PREFS_EMAIL, email)
            .putString(PREFS_UNIT, unitSystem)
            .putString(PREFS_EFFORT, effortMetric)
            .putString(PREFS_REST, defaultRest)
            .apply()
    }

    fun getUserEmail(): String? = prefs.getString(PREFS_EMAIL, null)
    fun getUserUnitSystem(): String? = prefs.getString(PREFS_UNIT, null)
    fun getUserEffortMetric(): String? = prefs.getString(PREFS_EFFORT, null)
    fun getUserDefaultRest(): String? = prefs.getString(PREFS_REST, null)

    companion object {
        private const val PREFS_NAME = "kaizen_prefs"
        private const val TOKEN_KEY = "auth_token"
        private const val CALIBRATION_KEY = "calibration_complete"
        private const val PREFS_EMAIL = "prefs_email"
        private const val PREFS_UNIT = "prefs_unit"
        private const val PREFS_EFFORT = "prefs_effort"
        private const val PREFS_REST = "prefs_rest"
    }
}
