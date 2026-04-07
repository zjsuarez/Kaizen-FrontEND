package com.example.kaizenfrontend.feature.dashboard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kaizenfrontend.feature.dashboard.data.local.DashboardPreferences
import com.example.kaizenfrontend.feature.dashboard.data.repository.DashboardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DashboardSyncWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dashboardRepository: DashboardRepository,
    private val dashboardPreferences: DashboardPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val latestWidgetOrder = dashboardPreferences.widgetOrder.first()

        return dashboardRepository
            .saveWidgetOrder(latestWidgetOrder)
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() }
            )
    }
}
