package com.example.kaizenfrontend.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.components.ActiveWorkoutOverlay
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.presentation.components.ActiveWorkoutBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.WorkoutSummaryBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.ZenModeScreen

/**
 * App-shell active-workout overlay. Replaces the cluster of state +
 * sheets that used to live inside the legacy DashboardScreen
 * (mini-island, ActiveWorkoutBottomSheet, ZenMode dialog,
 * WorkoutSummaryBottomSheet, finishedWorkoutSnapshot, zenModeInitialPage).
 *
 * Renders inside MainActivity above the NavHost, so the active
 * workout follows the user across every tab transition.
 *
 * Note: ZenMode wiring stays in place for Phase 2 (it's deleted
 * outright in Phase 4). All ZenMode-specific state lives here so
 * Phase 4 only needs to remove this file's relevant lines.
 */
@Composable
fun KaizenAppHost(
    modifier: Modifier = Modifier,
    onWorkoutSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val hostViewModel: ActiveWorkoutHostViewModel = hiltViewModel()
    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()

    var showActiveWorkoutSheet by remember { mutableStateOf(false) }
    var finishedWorkoutSnapshot by remember { mutableStateOf<ActiveWorkoutState?>(null) }
    var zenModeInitialPage by remember { mutableStateOf<Int?>(null) }

    // Forward "saved" events to the parent (Dashboard refreshes its data).
    LaunchedEffect(Unit) {
        hostViewModel.workoutSaved.collect { onWorkoutSaved() }
    }

    // ── Floating mini-island, visible across all tabs ──────────────
    Box(modifier = modifier.fillMaxSize()) {
        ActiveWorkoutOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            onOpenWorkout = { showActiveWorkoutSheet = true }
        )
    }

    // ── Active workout sheet ──────────────────────────────────────
    if (showActiveWorkoutSheet && workoutState != null) {
        ActiveWorkoutBottomSheet(
            onDismiss = { showActiveWorkoutSheet = false },
            onFinish = {
                val snapshot = ActiveWorkoutManager.finishWorkout()
                snapshot?.let {
                    finishedWorkoutSnapshot = it
                    hostViewModel.saveWorkout(it)
                }
                showActiveWorkoutSheet = false
                zenModeInitialPage = null
            },
            onAddExercise = { /* TODO Phase 4 */ },
            onNavigateToZenMode = { page -> zenModeInitialPage = page }
        )
    }

    // ── Workout summary (post-finish) ─────────────────────────────
    finishedWorkoutSnapshot?.let { snapshot ->
        val unitSystem = remember { sessionManager.getUserUnitSystem() ?: "METRIC" }
        val weightUnit = if (unitSystem == "IMPERIAL") "lbs" else "kg"
        WorkoutSummaryBottomSheet(
            workoutSnapshot = snapshot,
            saveStatusFlow = hostViewModel.saveStatus,
            weightUnit = weightUnit,
            onDismiss = {
                finishedWorkoutSnapshot = null
                hostViewModel.resetSaveStatus()
            },
            onRetry = { hostViewModel.saveWorkout(snapshot) }
        )
    }

    // ── Zen Mode (full-screen dialog; deleted in Phase 4) ─────────
    zenModeInitialPage?.let { initialPage ->
        Dialog(
            onDismissRequest = { zenModeInitialPage = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ZenModeScreen(
                initialPage = initialPage,
                onClose = { zenModeInitialPage = null }
            )
        }
    }
}
