package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.navigation.ActiveWorkoutHostViewModel
import com.example.kaizenfrontend.core.ui.navigation.KaizenDestinations
import com.example.kaizenfrontend.core.ui.navigation.KaizenTabScaffold
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PrGold
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import com.example.kaizenfrontend.core.ui.theme.spacing
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.NextWorkoutWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrMock
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrsWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecoveryTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.StreakWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.WeightTrendWidget
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.ActiveExerciseInit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// Bottom-sheet types — driven by tapping a card on the dashboard.
// `LastSessionDetails` / `CalendarDay` / `MetricHistory` were retired
// from Phase 3; their entry-point widgets no longer live on the home.
// ──────────────────────────────────────────────────────────────

sealed class DashboardBottomSheetType {
    data class NextWorkoutOptions(val currentRoutineName: String) : DashboardBottomSheetType()
    data class PrDetails(val exerciseName: String) : DashboardBottomSheetType()
    data class LogBodyWeight(val currentWeight: Double?) : DashboardBottomSheetType()
    data class RecoveryInfo(val hours: Int?) : DashboardBottomSheetType()
}

// ──────────────────────────────────────────────────────────────
// Main screen — fixed opinionated layout.
// Replaces the legacy customizable widget grid (Phase 3 anti-fatigue
// goal: fewer choices, no edit mode, no per-user reordering).
// ──────────────────────────────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    activeWorkoutHostViewModel: ActiveWorkoutHostViewModel = hiltViewModel(),
    onWorkoutClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val showGoogleWelcomeSheet by viewModel.showGoogleWelcomePrompt.collectAsState()

    val sessionManager = remember { SessionManager(context) }
    val userName = remember {
        sessionManager.getUserEmail()
            ?.substringBefore("@")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Athlete"
    }
    val todayLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val googleWelcomeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeBottomSheet by remember { mutableStateOf<DashboardBottomSheetType?>(null) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenFocused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // After a workout is saved (event fired by ActiveWorkoutHostViewModel),
    // pull fresh dashboard data so PRs / streak / recovery reflect the
    // session that just landed.
    LaunchedEffect(Unit) {
        activeWorkoutHostViewModel.workoutSaved.collect {
            viewModel.refreshDashboardData()
        }
    }

    val freeLabel = stringResource(id = R.string.dashboard_free_label)
    val overviewLabel = stringResource(id = R.string.dashboard_overview_label)

    KaizenTabScaffold(
        navController = navController,
        title = stringResource(id = R.string.dashboard_title),
        subtitle = "${stringResource(id = R.string.dashboard_hello, userName)}  ·  $todayLabel"
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is DashboardUiState.Empty -> Unit
            is DashboardUiState.Error -> {
                DashboardErrorState(
                    state = state,
                    paddingValues = paddingValues
                )
            }
            is DashboardUiState.Success -> {
                DashboardSuccessLayout(
                    state = state,
                    paddingValues = paddingValues,
                    onNextWorkoutClick = {
                        activeBottomSheet = DashboardBottomSheetType.NextWorkoutOptions(
                            state.data.nextWorkout?.routineName ?: freeLabel
                        )
                    },
                    onStartNextWorkout = {
                        startNextWorkoutOrFallback(state, onWorkoutClick)
                    },
                    onWeightClick = {
                        activeBottomSheet = DashboardBottomSheetType.LogBodyWeight(state.data.currentWeight)
                    },
                    onPrsClick = {
                        val firstPr = state.data.recentPrs.firstOrNull()?.exerciseName ?: overviewLabel
                        activeBottomSheet = DashboardBottomSheetType.PrDetails(firstPr)
                    },
                    onPrRowClick = { exercise ->
                        activeBottomSheet = DashboardBottomSheetType.PrDetails(exercise)
                    }
                )
            }
        }
    }

    // Bottom sheets are rendered as siblings to the scaffold so they
    // float above the bottom-nav bar provided by KaizenTabScaffold.

    if (activeBottomSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheet = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            BottomSheetContent(
                sheetType = activeBottomSheet!!,
                onDismiss = { activeBottomSheet = null },
                onLogWeight = { weight ->
                    viewModel.logBodyWeight(weight)
                    activeBottomSheet = null
                },
                onWorkoutClick = {
                    onWorkoutClick()
                    activeBottomSheet = null
                },
                weightHistory = weightHistory
            )
        }
    }

    if (showGoogleWelcomeSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissGoogleWelcomePrompt() },
            sheetState = googleWelcomeSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            GoogleWelcomeBottomSheet(
                onSetPasswordClick = {
                    viewModel.dismissGoogleWelcomePrompt()
                    navController.navigate(KaizenDestinations.SETTINGS) { launchSingleTop = true }
                },
                onNotNowClick = { viewModel.dismissGoogleWelcomePrompt() }
            )
        }
    }
}

/**
 * Eagerly start the routine surfaced by `nextWorkout`. Guarantees the
 * mini-island lights up the moment the user taps the hero CTA without
 * making them traverse Workouts → Plan → Routine → Start.
 *
 * Falls back to the legacy `onWorkoutClick` callback when the dashboard
 * has no routine queued (e.g. user has no active plan yet).
 */
private fun startNextWorkoutOrFallback(
    state: DashboardUiState.Success,
    fallback: () -> Unit
) {
    val next = state.data.nextWorkout
    if (next == null) {
        fallback()
        return
    }
    // The dashboard payload only knows the routine name + id; without the
    // exercise list we can't kick off ActiveWorkoutManager directly. Fall
    // back to the parent callback (which today opens the picker sheet)
    // until Phase 4 wires Start Workout end-to-end through a routine
    // hydration step.
    fallback()
}

// ──────────────────────────────────────────────────────────────
// Fixed Dashboard layout
// ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardSuccessLayout(
    state: DashboardUiState.Success,
    paddingValues: PaddingValues,
    onNextWorkoutClick: () -> Unit,
    onStartNextWorkout: () -> Unit,
    onWeightClick: () -> Unit,
    onPrsClick: () -> Unit,
    onPrRowClick: (String) -> Unit
) {
    val data = state.data
    val recentPrs = data.recentPrs.take(3).map { pr ->
        RecentPrMock(
            exercise = pr.exerciseName,
            weight = "${formatWeight(pr.weight)} kg",
            weightIncrease = "",
            timeAgo = pr.achievedAt.take(10)
        )
    }

    val weightTrendLabel = data.weightDiff?.let { diff ->
        if (diff >= 0) "+${formatWeight(diff)} kg this week"
        else "${formatWeight(diff)} kg this week"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(
            start = MaterialTheme.spacing.md,
            end = MaterialTheme.spacing.md,
            top = MaterialTheme.spacing.xs,
            bottom = MaterialTheme.spacing.lg
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
    ) {
        // Hero — Next Workout
        item {
            NextWorkoutWidget(
                routineName = data.nextWorkout?.routineName,
                onStartClick = onStartNextWorkout,
                onClick = onNextWorkoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        // Streak | Recovery
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
            ) {
                Modifier
                    .weight(1f)
                    .height(140.dp)
                    .StreakWidget(streakDays = data.workoutStreak)

                RecoveryTimeWidget(
                    hours = data.recoveryTimeHours,
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                )
            }
        }

        // Weight trend
        item {
            WeightTrendWidget(
                currentWeight = data.currentWeight,
                trendLabel = weightTrendLabel,
                isPositive = (data.weightDiff ?: 0.0) >= 0.0,
                onClick = onWeightClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }

        // Recent PRs (top 3, real data)
        item {
            RecentPrsWidget(
                prs = recentPrs,
                onClick = onPrsClick,
                onPrClick = onPrRowClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (recentPrs.isEmpty()) 140.dp else 220.dp)
            )
        }
    }
}

@Composable
private fun DashboardErrorState(
    state: DashboardUiState.Error,
    paddingValues: PaddingValues
) {
    val errorMessage = state.message?.takeIf { it.isNotBlank() }
        ?: state.messageResId?.let { stringResource(id = it) }
        ?: stringResource(id = R.string.dashboard_error_backend_connection)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(MaterialTheme.spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatWeight(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)

// ──────────────────────────────────────────────────────────────
// Bottom-sheet content (per-card detail sheets).
// ──────────────────────────────────────────────────────────────

@Composable
private fun BottomSheetContent(
    sheetType: DashboardBottomSheetType,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit = {},
    onWorkoutClick: () -> Unit = {},
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (sheetType) {
            is DashboardBottomSheetType.NextWorkoutOptions -> NextWorkoutSheet(sheetType.currentRoutineName, onWorkoutClick)
            is DashboardBottomSheetType.PrDetails -> PrDetailsSheet(sheetType.exerciseName)
            is DashboardBottomSheetType.LogBodyWeight -> BodyWeightSheet(sheetType.currentWeight, onDismiss, onLogWeight, weightHistory)
            is DashboardBottomSheetType.RecoveryInfo -> RecoveryInfoSheet(sheetType.hours)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun NextWorkoutSheet(routineName: String, onWorkoutClick: () -> Unit) {
    Text(
        text = routineName.ifBlank { stringResource(id = R.string.dashboard_pull_day_title) },
        color = PureWhite,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = R.string.dashboard_workout_item_1), color = LightGrey, style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(id = R.string.dashboard_workout_item_2), color = LightGrey, style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(id = R.string.dashboard_workout_item_3), color = LightGrey, style = MaterialTheme.typography.bodyMedium)
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onWorkoutClick,
        colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            stringResource(id = R.string.dashboard_start_workout_cta),
            color = Onyx,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BodyWeightSheet(
    currentWeight: Double?,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (weightHistory.isEmpty()) {
            Text(stringResource(id = R.string.dashboard_no_records), color = LightGrey, style = MaterialTheme.typography.bodyMedium)
        } else {
            weightHistory.take(3).forEachIndexed { index, measurement ->
                val dateStr = try {
                    val date = LocalDate.parse(measurement.recordedAt.take(10))
                    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    "${date.dayOfMonth} ${monthNames[date.monthValue - 1]}"
                } catch (_: Exception) {
                    measurement.recordedAt.take(10)
                }

                val textColor = if (index == 0) PureWhite else LightGrey
                val style = if (index == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
                Text(
                    text = stringResource(id = R.string.dashboard_weight_log_entry, dateStr, measurement.weightKg),
                    color = textColor,
                    style = style
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    var inputWeight by remember(currentWeight) {
        mutableStateOf(currentWeight?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }

    OutlinedTextField(
        value = inputWeight,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                inputWeight = newValue
            }
        },
        label = { Text(stringResource(id = R.string.dashboard_new_weight_label), color = LightGrey) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CrayolaBlue,
            unfocusedBorderColor = ShadowGrey,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = {
            inputWeight.toDoubleOrNull()?.let { onLogWeight(it) } ?: onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
    ) {
        Text(stringResource(id = R.string.dashboard_save_log), color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecoveryInfoSheet(hours: Int?) {
    Icon(
        imageVector = Icons.Default.BatteryChargingFull,
        contentDescription = stringResource(id = R.string.dashboard_recovery_battery_content_description),
        tint = MalachiteGreen,
        modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(id = R.string.dashboard_recovery_remaining_hours),
        color = PureWhite,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(id = R.string.dashboard_recovery_description),
        color = LightGrey,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(
            modifier = Modifier
                .background(Color(0xFF4A1A1A), RoundedCornerShape(8.dp))
                .border(1.dp, SubtleRed, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                stringResource(id = R.string.dashboard_legs_high_fatigue),
                color = SubtleRed,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .background(Color(0xFF1A3320), RoundedCornerShape(8.dp))
                .border(1.dp, MalachiteGreen, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                stringResource(id = R.string.dashboard_chest_fresh),
                color = MalachiteGreen,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PrDetailsSheet(exerciseName: String) {
    Text(
        text = stringResource(id = R.string.dashboard_history_title, exerciseName),
        color = PureWhite,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(24.dp))

    // Placeholder rows. Real per-exercise PR history is surfaced from
    // the ExerciseHistoryBottomSheet path (Phase 4 makes that one tap
    // away from inside the active workout).
    Column(modifier = Modifier.fillMaxWidth()) {
        listOf(
            Triple(R.string.dashboard_pr_entry_1, R.string.statistics_push, R.string.dashboard_two_weeks_ago),
            Triple(R.string.dashboard_pr_entry_2, R.string.statistics_push, R.string.dashboard_one_month_ago),
            Triple(R.string.dashboard_pr_entry_3, R.string.dashboard_full_body, R.string.dashboard_three_months_ago)
        ).forEachIndexed { index, (entry, group, age) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = entry),
                    color = PureWhite,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(id = group), color = LightGrey, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(id = age), color = LightGrey, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (index < 2) HorizontalDivider(color = Onyx)
        }
    }
}

@Composable
private fun GoogleWelcomeBottomSheet(
    onSetPasswordClick: () -> Unit,
    onNotNowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(CrayolaBlue.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(id = R.string.settings_google_content_description),
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.dashboard_google_welcome_title),
            color = PureWhite,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = R.string.dashboard_google_welcome_message),
            color = LightGrey,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onSetPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
        ) {
            Text(
                stringResource(id = R.string.dashboard_set_password),
                color = Onyx,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onNotNowClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightGrey),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightGrey.copy(alpha = 0.35f))
        ) {
            Text(stringResource(id = R.string.dashboard_not_now), color = LightGrey, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
