package com.example.kaizenfrontend.feature.dashboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.kaizenfrontend.core.data.local.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Per-user DataStore for dashboard preferences.
 *
 * Phase 3 stripped widget-order persistence (the dashboard is now an
 * opinionated fixed layout, not a customizable grid). The class is kept
 * around so future per-user dashboard state has a place to land without
 * a fresh DI wiring step.
 */
class DashboardPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) {

    @Suppress("unused")
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
    )

    companion object {
        private const val DATASTORE_NAME = "dashboard_preferences"
    }
}
