package com.example.kaizenfrontend.feature.dashboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.kaizenfrontend.core.data.local.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DashboardPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
    )

    private fun getWidgetOrderKey() = stringPreferencesKey("widget_order_${sessionManager.getUserIdFromToken()}")

    val widgetOrder: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[getWidgetOrderKey()]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_WIDGET_ORDER
        }

    suspend fun saveWidgetOrder(order: List<String>) {
        val serializedOrder = order.joinToString(",")
        dataStore.edit { preferences ->
            preferences[getWidgetOrderKey()] = serializedOrder
        }
    }

    companion object {
        private const val DATASTORE_NAME = "dashboard_preferences"
        private val DEFAULT_WIDGET_ORDER = listOf("NEXT_WORKOUT", "WEIGHT_TREND")
    }
}
