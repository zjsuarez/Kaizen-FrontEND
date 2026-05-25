package com.example.kaizenfrontend.feature.dashboard.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.core.ui.components.ActiveWorkoutOverlay
import com.example.kaizenfrontend.feature.workouts.presentation.components.ActiveWorkoutBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.ExerciseCatalogBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.WorkoutDetailBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.WorkoutSummaryBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.ZenModeScreen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.RecentWorkoutSummaryResponse
import com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.StreakDayResponse
import com.example.kaizenfrontend.feature.dashboard.model.WidgetConfig
import com.example.kaizenfrontend.feature.dashboard.model.WidgetSize
import com.example.kaizenfrontend.feature.dashboard.model.WidgetType
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.AvgTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.CalendarWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.LastSessionWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.NextWorkoutWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrMock
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrsWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.StreakWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.WeightTrendWidget
import com.example.kaizenfrontend.feature.statistics.presentation.StatisticsScreen
import com.example.kaizenfrontend.feature.user.presentation.settings.SettingsScreen
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutsScreen
import com.example.kaizenfrontend.core.data.BuiltinExerciseCatalog
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────

sealed class DashboardBottomSheetType {
    data class CalendarDay(val day: Int, val isTrainingDay: Boolean) : DashboardBottomSheetType()
    data class PrDetails(val exerciseName: String) : DashboardBottomSheetType()
    data object LogBodyWeight : DashboardBottomSheetType()
    data class WorkoutDetail(val workoutId: String) : DashboardBottomSheetType()
    data object StreakCalendar : DashboardBottomSheetType()
    data object AvgTimeWorkouts : DashboardBottomSheetType()
}

// ──────────────────────────────────────────────────────────────
// Dashboard Widget Layout
// ──────────────────────────────────────────────────────────────

private val dashboardWidgets = listOf(
    WidgetConfig(type = WidgetType.NEXT_WORKOUT, size = WidgetSize.FULL_WIDTH, heightDp = 200.dp),
    WidgetConfig(type = WidgetType.WEIGHT_TREND, size = WidgetSize.FULL_WIDTH, heightDp = 80.dp),
    WidgetConfig(type = WidgetType.LAST_SESSION, size = WidgetSize.FULL_WIDTH, heightDp = 80.dp),
    WidgetConfig(type = WidgetType.STREAK, size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    WidgetConfig(type = WidgetType.AVG_TIME, size = WidgetSize.HALF_WIDTH, heightDp = 140.dp),
    WidgetConfig(type = WidgetType.CALENDAR, size = WidgetSize.FULL_WIDTH, heightDp = null),
    WidgetConfig(type = WidgetType.RECENT_PRS, size = WidgetSize.FULL_WIDTH, heightDp = 250.dp),
)

private val widgetConfigByType: Map<WidgetType, WidgetConfig> =
    dashboardWidgets.associateBy { it.type }

private val fallbackWidgetOrder = listOf("NEXT_WORKOUT", "WEIGHT_TREND")

// ──────────────────────────────────────────────────────────────
// Main Screen
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onWorkoutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val widgetOrder by viewModel.widgetOrder.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val showGoogleWelcomeSheet by viewModel.showGoogleWelcomePrompt.collectAsState()
    val weekdaySchedule by viewModel.weekdaySchedule.collectAsState()
    val workoutDetailState by viewModel.workoutDetailState.collectAsState()
    val prHistoryState by viewModel.prHistoryState.collectAsState()
    val workoutLaunchReady by viewModel.workoutLaunchReady.collectAsState()
    val streakMetrics by viewModel.streakMetrics.collectAsState()
    val isLoggingWeight by viewModel.isLoggingWeight.collectAsState()
    val sessionManager = remember { SessionManager(context) }
    val effortMetric = remember { sessionManager.getUserEffortMetric() ?: "RPE" }
    val userName = remember {
        sessionManager.getUserEmail()
            ?.substringBefore("@")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    val greeting = remember {
        val hour = LocalTime.now().hour
        val base = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        if (userName != null) "$base, $userName" else base
    }
    val todayLabel = remember {
        val now = LocalDate.now()
        val dow = now.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
        val month = now.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
        "$dow, ${now.dayOfMonth} $month · ${now.year}"
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showAddWidgetSheet by remember { mutableStateOf(false) }
    var showActiveWorkoutSheet by remember { mutableStateOf(false) }
    var showAddExerciseCatalog by remember { mutableStateOf(false) }
    var zenModeInitialPage by remember { mutableStateOf<Int?>(null) }
    var finishedWorkoutSnapshot by remember {
        mutableStateOf<com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState?>(null)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addWidgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val googleWelcomeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeBottomSheet by remember { mutableStateOf<DashboardBottomSheetType?>(null) }

    val openWorkoutDetail: (String) -> Unit = { workoutId ->
        viewModel.clearWorkoutDetail()
        viewModel.fetchWorkoutDetail(workoutId)
        activeBottomSheet = DashboardBottomSheetType.WorkoutDetail(workoutId)
    }

    // Refresh when the app comes back from the background (Activity ON_RESUME).
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onScreenFocused()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Refresh whenever the user navigates back to the Dashboard tab from another tab.
    // prevTab starts at -1 so the initial composition (tab=0, prev=-1) doesn't trigger a
    // duplicate fetch on top of the ViewModel's init calls.
    val prevTab = remember { mutableIntStateOf(-1) }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 0 && prevTab.intValue != 0) viewModel.onScreenFocused()
        prevTab.intValue = selectedTab
    }

    // Dismiss the body-weight sheet once the save succeeds and data has refreshed.
    LaunchedEffect(Unit) {
        viewModel.weightLoggedEvent.collect { activeBottomSheet = null }
    }

    // Open the active workout sheet as soon as the ViewModel finishes fetching the routine.
    LaunchedEffect(workoutLaunchReady) {
        if (workoutLaunchReady) {
            showActiveWorkoutSheet = true
            viewModel.consumeWorkoutLaunch()
        }
    }

    Scaffold(
        containerColor = Onyx,
        topBar = {},
        bottomBar = {
            KaizenBottomNavigation(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(top = 4.dp)
                        ) {
                            // ── Header ──────────────────────────────────────
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = greeting,
                                        color = PureWhite,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 34.sp
                                    )
                                    Text(
                                        text = todayLabel,
                                        color = LightGrey,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (!isEditing) {
                                    FloatingActionButton(
                                        onClick = { viewModel.toggleEditMode() },
                                        containerColor = ShadowGrey,
                                        contentColor = PureWhite,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.dashboard_edit_mode_enter),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FloatingActionButton(
                                            onClick = { showAddWidgetSheet = true },
                                            containerColor = CrayolaBlue,
                                            contentColor = PureWhite,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = stringResource(id = R.string.dashboard_add_widget),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        FloatingActionButton(
                                            onClick = { viewModel.toggleEditMode() },
                                            containerColor = ShadowGrey,
                                            contentColor = PureWhite,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Done,
                                                contentDescription = stringResource(id = R.string.dashboard_edit_mode_done),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                when (val state = uiState) {
                                    is DashboardUiState.Loading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = CrayolaBlue)
                                        }
                                    }
                                    is DashboardUiState.Success -> {
                                        DashboardWidgetGrid(
                                            successState = state,
                                            widgetOrder = widgetOrder,
                                            weightHistory = weightHistory,
                                            isEditing = isEditing,
                                            weekdaySchedule = weekdaySchedule,
                                            streakMetrics = streakMetrics,
                                            onWorkoutClick = { routineId, routineName ->
                                                viewModel.startNextWorkout(routineId, routineName)
                                            },
                                            onWidgetClick = { sheetType ->
                                                when (sheetType) {
                                                    is DashboardBottomSheetType.WorkoutDetail -> openWorkoutDetail(sheetType.workoutId)
                                                    is DashboardBottomSheetType.PrDetails -> {
                                                        viewModel.clearPrHistory()
                                                        viewModel.fetchPrHistory(sheetType.exerciseName)
                                                        activeBottomSheet = sheetType
                                                    }
                                                    else -> activeBottomSheet = sheetType
                                                }
                                            },
                                            onRemoveWidget = { widgetKey -> viewModel.removeWidget(widgetKey) },
                                            onReorderWidgets = { viewModel.onReorderWidgets(it) },
                                            onAddWidgetClick = { showAddWidgetSheet = true }
                                        )
                                    }
                                    is DashboardUiState.Empty -> { /* no-op */ }
                                    is DashboardUiState.Error -> {
                                        val errorMessage = state.message?.takeIf { it.isNotBlank() }
                                            ?: state.messageResId?.let { stringResource(id = it) }
                                            ?: stringResource(id = R.string.dashboard_error_backend_connection)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .background(ShadowGrey, RoundedCornerShape(16.dp))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = errorMessage, color = Color.Red, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> WorkoutsScreen()
                    2 -> StatisticsScreen()
                    3 -> SettingsScreen(onLogoutClick = onLogoutClick)
                }

                ActiveWorkoutOverlay(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onOpenWorkout = { showActiveWorkoutSheet = true }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = selectedTab == 0 && isEditing,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .background(ShadowGrey.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dashboard_edit_mode_reorder_hint),
                            color = LightGrey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── Generic bottom sheets ──────────────────────────────────
        val dashboardData = (uiState as? DashboardUiState.Success)?.data
        val isWorkoutDetailSheet = activeBottomSheet is DashboardBottomSheetType.WorkoutDetail

        if (activeBottomSheet != null && !isWorkoutDetailSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    if (activeBottomSheet is DashboardBottomSheetType.PrDetails) viewModel.clearPrHistory()
                    activeBottomSheet = null
                },
                sheetState = sheetState,
                containerColor = Onyx,
                dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
            ) {
                BottomSheetContent(
                    sheetType = activeBottomSheet!!,
                    onDismiss = {
                        if (activeBottomSheet is DashboardBottomSheetType.PrDetails) viewModel.clearPrHistory()
                        activeBottomSheet = null
                    },
                    onLogWeight = { weight -> viewModel.logBodyWeight(weight) },
                    isSavingWeight = isLoggingWeight,
                    onWorkoutClick = {
                        onWorkoutClick()
                        activeBottomSheet = null
                    },
                    currentWeight = dashboardData?.currentWeight,
                    weightHistory = weightHistory,
                    prHistoryState = prHistoryState,
                    streakCalendar = dashboardData?.streakCalendar ?: emptyList(),
                    trainingDayDetails = dashboardData?.trainingDayDetails ?: emptyList(),
                    weekdaySchedule = weekdaySchedule,
                    currentStreak = streakMetrics.currentStreak,
                    longestStreak = streakMetrics.longestStreak,
                    recentWorkoutSummaries = dashboardData?.recentWorkoutSummaries ?: emptyList(),
                    effortMetric = effortMetric,
                    onOpenWorkoutDetail = { workoutId ->
                        if (activeBottomSheet is DashboardBottomSheetType.PrDetails) viewModel.clearPrHistory()
                        openWorkoutDetail(workoutId)
                    }
                )
            }
        }

        // ── Workout Detail (reused from WorkoutsScreen) ────────────
        if (isWorkoutDetailSheet) {
            when (val state = workoutDetailState) {
                is WorkoutDetailState.Success -> {
                    WorkoutDetailBottomSheet(
                        workout = state.workout,
                        effortMetric = effortMetric,
                        photoUrlByMeasurementId = weightHistory
                            .filter { !it.progressPhotoUrl.isNullOrBlank() }
                            .associate { it.id to it.progressPhotoUrl!! },
                        onDismiss = {
                            viewModel.clearWorkoutDetail()
                            activeBottomSheet = null
                        }
                    )
                }
                is WorkoutDetailState.Error -> {
                    LaunchedEffect(Unit) {
                        viewModel.clearWorkoutDetail()
                        activeBottomSheet = null
                    }
                }
                else -> {
                    ModalBottomSheet(
                        onDismissRequest = {
                            viewModel.clearWorkoutDetail()
                            activeBottomSheet = null
                        },
                        containerColor = Onyx,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CrayolaBlue)
                        }
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }

        // ── Add Widget sheet ───────────────────────────────────────
        if (showAddWidgetSheet) {
            AddWidgetBottomSheet(
                sheetState = addWidgetSheetState,
                currentWidgetOrder = widgetOrder,
                onDismiss = { showAddWidgetSheet = false },
                onAddWidget = { widgetKey ->
                    viewModel.addWidget(widgetKey)
                    showAddWidgetSheet = false
                }
            )
        }

        // ── Google Welcome sheet ───────────────────────────────────
        if (showGoogleWelcomeSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissGoogleWelcomePrompt() },
                sheetState = googleWelcomeSheetState,
                containerColor = Onyx,
                dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
            ) {
                GoogleWelcomeBottomSheet(
                    onSetPasswordClick = {
                        viewModel.dismissGoogleWelcomePrompt()
                        selectedTab = 3
                    },
                    onNotNowClick = { viewModel.dismissGoogleWelcomePrompt() }
                )
            }
        }

        // ── Active Workout sheet ───────────────────────────────────
        if (showActiveWorkoutSheet) {
            ActiveWorkoutBottomSheet(
                onDismiss = { showActiveWorkoutSheet = false },
                onFinish = {
                    val snapshot = com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager.finishWorkout(effortMetric)
                    snapshot?.let { finishedWorkoutSnapshot = it }
                    showActiveWorkoutSheet = false
                    zenModeInitialPage = null
                },
                onDiscard = {
                    com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager.discardWorkout()
                    showActiveWorkoutSheet = false
                    zenModeInitialPage = null
                },
                onAddExercise = { showAddExerciseCatalog = true },
                onNavigateToZenMode = { page -> zenModeInitialPage = page }
            )
        }

        // ── Add exercise to active workout ─────────────────────────
        if (showAddExerciseCatalog) {
            ExerciseCatalogBottomSheet(
                onDismissRequest = { showAddExerciseCatalog = false },
                onExerciseSelected = { exercise ->
                    com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager.addExercise(exercise)
                    showAddExerciseCatalog = false
                }
            )
        }

        // ── Post-workout summary ───────────────────────────────────
        finishedWorkoutSnapshot?.let { snapshot ->
            val unitSystem = remember { sessionManager.getUserUnitSystem() ?: "METRIC" }
            val weightUnit = if (unitSystem == "IMPERIAL") "lbs" else "kg"
            WorkoutSummaryBottomSheet(
                workoutSnapshot = snapshot,
                saveStatusFlow = viewModel.workoutSaveStatus,
                photoUploadStatusFlow = viewModel.photoUploadStatus,
                weightUnit = weightUnit,
                onDismiss = {
                    finishedWorkoutSnapshot = null
                    viewModel.resetWorkoutSaveStatus()
                    viewModel.resetPhotoUploadStatus()
                },
                onRetry = { viewModel.saveWorkout(snapshot) },
                onSave = { viewModel.saveWorkout(snapshot) },
                onPhotoSelected = { uri -> viewModel.uploadProgressPhoto(uri) }
            )
        }

        // ── Zen Mode ───────────────────────────────────────────────
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
}

// ──────────────────────────────────────────────────────────────
// Widget Grid Engine
// ──────────────────────────────────────────────────────────────

@Composable
fun DashboardWidgetGrid(
    successState: DashboardUiState.Success,
    widgetOrder: List<String>,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>,
    isEditing: Boolean,
    weekdaySchedule: Map<Int, RoutineScheduleItem> = emptyMap(),
    streakMetrics: StreakMetrics = StreakMetrics(0, 0),
    onWorkoutClick: (routineId: String, routineName: String) -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onReorderWidgets: (List<String>) -> Unit,
    onAddWidgetClick: () -> Unit
) {
    val orderedWidgetTypes = widgetOrder.mapNotNull { runCatching { WidgetType.valueOf(it) }.getOrNull() }
    val density = LocalDensity.current
    val gridState = rememberLazyGridState()
    val editWidgetTypes = remember { mutableStateListOf<WidgetType>() }
    val itemHeightPxByKey = remember { mutableStateMapOf<String, Float>() }
    var draggingWidgetKey by remember { mutableStateOf<String?>(null) }
    var dragDistanceY by remember { mutableFloatStateOf(0f) }
    val isDragActive = draggingWidgetKey != null

    val dragPriorityScrollBlocker = remember(isDragActive) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (isDragActive && source == NestedScrollSource.UserInput) available else Offset.Zero
            override suspend fun onPreFling(available: Velocity): Velocity =
                if (isDragActive) available else Velocity.Zero
        }
    }

    LaunchedEffect(orderedWidgetTypes, isEditing) {
        if (isEditing && draggingWidgetKey == null) {
            editWidgetTypes.clear()
            editWidgetTypes.addAll(orderedWidgetTypes)
        }
    }

    if (orderedWidgetTypes.isEmpty()) {
        DashboardEmptyState(onAddWidgetClick = onAddWidgetClick)
    } else if (!isEditing) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = orderedWidgetTypes,
                key = { "widget_${it.name}" },
                span = { widgetType ->
                    GridItemSpan(widgetConfigByType[widgetType]?.size?.span ?: WidgetSize.FULL_WIDTH.span)
                }
            ) { widgetType ->
                val config = widgetConfigByType[widgetType] ?: return@items
                val widgetMod = if (config.heightDp != null) Modifier.fillMaxWidth().height(config.heightDp) else Modifier.fillMaxWidth()
                WidgetContent(widgetType, widgetMod, successState, onWorkoutClick, onWidgetClick, weekdaySchedule, streakMetrics)
            }
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = !isDragActive,
            modifier = Modifier.fillMaxSize().nestedScroll(dragPriorityScrollBlocker)
        ) {
            itemsIndexed(
                items = editWidgetTypes,
                key = { _, wt -> "edit_${wt.name}" },
                span = { _, widgetType ->
                    GridItemSpan(widgetConfigByType[widgetType]?.size?.span ?: WidgetSize.FULL_WIDTH.span)
                }
            ) { _, widgetType ->
                val config = widgetConfigByType[widgetType] ?: return@itemsIndexed
                val isDragging = draggingWidgetKey == widgetType.name
                val animatedDragY by animateFloatAsState(
                    targetValue = if (isDragging) dragDistanceY else 0f,
                    label = "ghost_drag_y"
                )
                val liftedScale by animateFloatAsState(
                    targetValue = if (isDragging) 1.02f else 1f,
                    label = "ghost_drag_scale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isDragging) 0.92f else 1f,
                    label = "ghost_drag_alpha"
                )

                DashboardWidgetGhostPlaceholder(
                    widgetType = widgetType,
                    onRemove = {
                        if (draggingWidgetKey == widgetType.name) {
                            draggingWidgetKey = null
                            dragDistanceY = 0f
                        }
                        onRemoveWidget(widgetType.name)
                    },
                    modifier = (if (config.heightDp != null) Modifier.fillMaxWidth().height(config.heightDp) else Modifier.fillMaxWidth())
                        .zIndex(if (isDragging) 1f else 0f)
                        .onSizeChanged { size -> itemHeightPxByKey[widgetType.name] = size.height.toFloat() }
                        .graphicsLayer {
                            translationY = animatedDragY
                            scaleX = liftedScale
                            scaleY = liftedScale
                            this.alpha = alpha
                        }
                        .pointerInput(widgetType.name) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingWidgetKey = widgetType.name
                                    dragDistanceY = 0f
                                },
                                onDragCancel = { draggingWidgetKey = null; dragDistanceY = 0f },
                                onDragEnd = { draggingWidgetKey = null; dragDistanceY = 0f },
                                onDrag = { change, dragAmount ->
                                    if (draggingWidgetKey != widgetType.name) return@detectDragGesturesAfterLongPress
                                    change.consume()
                                    dragDistanceY += dragAmount.y
                                    var currentIndex = editWidgetTypes.indexOfFirst { it.name == widgetType.name }
                                    if (currentIndex == -1) return@detectDragGesturesAfterLongPress
                                    val baseHeightPx = itemHeightPxByKey[widgetType.name]
                                        ?: with(density) { config.heightDp?.toPx() ?: 320.dp.toPx() }
                                    while (dragDistanceY > baseHeightPx * 0.45f && currentIndex < editWidgetTypes.lastIndex) {
                                        val toIndex = currentIndex + 1
                                        editWidgetTypes.add(toIndex, editWidgetTypes.removeAt(currentIndex))
                                        onReorderWidgets(editWidgetTypes.map { it.name })
                                        dragDistanceY -= baseHeightPx * 0.45f
                                        currentIndex = toIndex
                                    }
                                    while (dragDistanceY < -baseHeightPx * 0.45f && currentIndex > 0) {
                                        val toIndex = currentIndex - 1
                                        editWidgetTypes.add(toIndex, editWidgetTypes.removeAt(currentIndex))
                                        onReorderWidgets(editWidgetTypes.map { it.name })
                                        dragDistanceY += baseHeightPx * 0.45f
                                        currentIndex = toIndex
                                    }
                                }
                            )
                        }
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Widget Content (live data)
// ──────────────────────────────────────────────────────────────

@SuppressLint("DefaultLocale")
@Composable
private fun WidgetContent(
    widgetType: WidgetType,
    widgetModifier: Modifier,
    successState: DashboardUiState.Success,
    onWorkoutClick: (routineId: String, routineName: String) -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit,
    weekdaySchedule: Map<Int, RoutineScheduleItem> = emptyMap(),
    streakMetrics: StreakMetrics = StreakMetrics(0, 0)
) {
    val data = successState.data

    when (widgetType) {
        WidgetType.STREAK ->
            widgetModifier.StreakWidget(
                streakDays = streakMetrics.currentStreak,
                recordStreak = streakMetrics.longestStreak,
                onClick = { onWidgetClick(DashboardBottomSheetType.StreakCalendar) }
            )

        WidgetType.AVG_TIME ->
            AvgTimeWidget(
                minutes = data.avgDurationLast14Days ?: data.avgDurationMinutes,
                trendDiffMinutes = 0,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.AvgTimeWorkouts) }
            )

        WidgetType.WEIGHT_TREND -> {
            val diff = data.weightDiff
            val weightTimestamp = data.latestWeightTimestamp?.take(10)?.let {
                runCatching {
                    val d = LocalDate.parse(it)
                    d.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
                }.getOrNull()
            }
            WeightTrendWidget(
                currentWeight = data.currentWeight,
                trendLabel = if (diff != null) {
                    if (diff >= 0) stringResource(R.string.dashboard_weight_trend_up_week, diff)
                    else stringResource(R.string.dashboard_weight_trend_down_week, diff)
                } else null,
                weightTimestamp = weightTimestamp,
                isPositive = diff?.let { it >= 0 },
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LogBodyWeight) }
            )
        }

        WidgetType.LAST_SESSION -> {
            val lastSession = data.lastSession
            LastSessionWidget(
                routineName = lastSession?.routineName,
                planName = lastSession?.planName,
                timeLabel = lastSession?.completedAt?.take(10)?.let { dateStr ->
                    runCatching {
                        val d = LocalDate.parse(dateStr)
                        val today = LocalDate.now()
                        when (d) {
                            today -> "Today"
                            today.minusDays(1) -> "Yesterday"
                            else -> d.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
                        }
                    }.getOrElse { dateStr }
                },
                modifier = widgetModifier,
                onClick = lastSession?.workoutId?.let { wid ->
                    { onWidgetClick(DashboardBottomSheetType.WorkoutDetail(wid)) }
                }
            )
        }

        WidgetType.NEXT_WORKOUT -> {
            val nextWorkout = data.nextWorkout
            val today = LocalDate.now()

            // Walk forward from today (skipping today if already trained) to find the next
            // scheduled day. weekdaySchedule is always fresh (re-fetched on every tab return).
            val todayAlreadyDone = data.lastSession?.completedAt?.take(10) == today.toString()
            val nextScheduledDate: LocalDate? = if (weekdaySchedule.isNotEmpty()) {
                val startDay = if (todayAlreadyDone) today.plusDays(1) else today
                (0L..364L).map { startDay.plusDays(it) }
                    .firstOrNull { d -> weekdaySchedule.containsKey(d.dayOfWeek.value) }
            } else {
                // Fall back to the backend-provided scheduledDate
                nextWorkout?.scheduledDate?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
            }

            // Prefer schedule item (always fresh) over stale Room-cached nextWorkout fields.
            val scheduleItem = nextScheduledDate?.let { weekdaySchedule[it.dayOfWeek.value] }

            val isStartEnabled = nextScheduledDate == null || nextScheduledDate == today
            val scheduledDateLabel: String? = when {
                nextScheduledDate == null || nextScheduledDate == today -> null
                nextScheduledDate == today.plusDays(1) -> "Scheduled Tomorrow"
                ChronoUnit.DAYS.between(today, nextScheduledDate) <= 6 ->
                    "Scheduled for ${nextScheduledDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
                        .replaceFirstChar { it.uppercase() }}"
                else -> "Scheduled for ${nextScheduledDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))}"
            }
            NextWorkoutWidget(
                routineName = scheduleItem?.routineName ?: nextWorkout?.routineName,
                planName = nextWorkout?.planName,
                scheduledDateLabel = scheduledDateLabel,
                isStartEnabled = isStartEnabled,
                onStartClick = {
                    val routineId = scheduleItem?.routineId ?: nextWorkout?.routineId ?: return@NextWorkoutWidget
                    val routineName = scheduleItem?.routineName ?: nextWorkout?.routineName ?: return@NextWorkoutWidget
                    onWorkoutClick(routineId, routineName)
                },
                modifier = widgetModifier,
                onClick = null
            )
        }

        WidgetType.CALENDAR -> {
            val trainingDayDetails = data.trainingDayDetails
            val next = data.nextWorkout

            // Completed workouts keyed by "YYYY-MM-DD"
            val completedDateMap: Map<String, String?> = trainingDayDetails.associate { detail ->
                detail.date.take(10) to detail.routineName
            }

            // Project the real plan schedule (from weekdaySchedule) 90 days forward.
            // Only mark a day if its exact day-of-week is in the schedule and it isn't already completed.
            val calToday = LocalDate.now()
            val scheduledDateMap: Map<String, String?> = buildMap {
                var date = calToday.plusDays(1)
                val endDate = calToday.plusDays(90)
                while (!date.isAfter(endDate)) {
                    val routineName = weekdaySchedule[date.dayOfWeek.value]?.routineName
                    val dateStr = date.toString()
                    if (routineName != null && !completedDateMap.containsKey(dateStr)) {
                        put(dateStr, routineName)
                    }
                    date = date.plusDays(1)
                }
            }

            CalendarWidget(
                completedDateMap = completedDateMap,
                scheduledDateMap = scheduledDateMap,
                activePlanName = next?.planName,
                modifier = widgetModifier,
                onClick = null,
                onDayClick = { day, isCompleted ->
                    if (isCompleted) {
                        val detail = trainingDayDetails.firstOrNull {
                            runCatching { LocalDate.parse(it.date).dayOfMonth }.getOrNull() == day
                        }
                        if (detail != null) {
                            onWidgetClick(DashboardBottomSheetType.WorkoutDetail(detail.workoutId))
                        }
                    }
                }
            )
        }

        WidgetType.RECENT_PRS -> {
            val mapPrs = data.recentPrs.map { pr ->
                RecentPrMock(
                    exercise = BuiltinExerciseCatalog.resolveExerciseName(pr.exerciseName),
                    weight = if (pr.weight != null) {
                        if (pr.weight % 1.0 == 0.0) "${pr.weight.toInt()} kg"
                        else "${String.format("%.1f", pr.weight)} kg"
                    } else "--",
                    percentageImprovement = pr.percentageImprovement,
                    timeAgo = pr.achievedAt?.take(10)?.let { dateStr ->
                        runCatching {
                            val d = LocalDate.parse(dateStr)
                            d.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
                        }.getOrElse { dateStr }
                    } ?: "--",
                    workoutId = pr.workoutId
                )
            }
            RecentPrsWidget(
                prs = mapPrs,
                modifier = widgetModifier,
                onClick = {
                    val first = data.recentPrs.firstOrNull()
                    if (first != null) {
                        onWidgetClick(DashboardBottomSheetType.PrDetails(first.exerciseName))
                    }
                },
                onPrClick = { pr ->
                    val originalExercise = data.recentPrs.find {
                        BuiltinExerciseCatalog.resolveExerciseName(it.exerciseName) == pr.exercise
                    }?.exerciseName ?: pr.exercise
                    onWidgetClick(DashboardBottomSheetType.PrDetails(originalExercise))
                }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Edit Mode: Skeletal Ghost Placeholder
// ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardWidgetGhostPlaceholder(
    widgetType: WidgetType,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        GhostWidgetContent(widgetType = widgetType, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Onyx.copy(alpha = 0.22f))
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(ShadowGrey.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = stringResource(id = R.string.dashboard_reorder_content_description),
                tint = LightGrey,
                modifier = Modifier.size(22.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.dashboard_remove_widget_content_description),
                    tint = SubtleRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GhostWidgetContent(widgetType: WidgetType, modifier: Modifier = Modifier) {
    when (widgetType) {
        WidgetType.STREAK -> modifier.StreakWidget(streakDays = null)
        WidgetType.AVG_TIME -> AvgTimeWidget(minutes = null, trendDiffMinutes = 0, modifier = modifier)
        WidgetType.WEIGHT_TREND -> WeightTrendWidget(
            currentWeight = null, trendLabel = "--", isPositive = null, modifier = modifier, onClick = null
        )
        WidgetType.LAST_SESSION -> LastSessionWidget(
            routineName = "--", planName = null, timeLabel = "--", modifier = modifier, onClick = null
        )
        WidgetType.NEXT_WORKOUT -> NextWorkoutWidget(
            routineName = "--", onStartClick = {}, modifier = modifier, onClick = null, isGhost = true
        )
        WidgetType.CALENDAR -> {
            CalendarWidget(
                completedDateMap = emptyMap(),
                scheduledDateMap = emptyMap(),
                modifier = modifier,
                onClick = null,
                onDayClick = { _, _ -> },
                isGhost = true
            )
        }
        WidgetType.RECENT_PRS -> {
            val placeholderPrs = remember {
                listOf(
                    RecentPrMock("Bench Press", "-- kg", timeAgo = "--"),
                    RecentPrMock("Squat", "-- kg", timeAgo = "--"),
                    RecentPrMock("Deadlift", "-- kg", timeAgo = "--")
                )
            }
            RecentPrsWidget(prs = placeholderPrs, modifier = modifier, onClick = null, onPrClick = {}, isGhost = true)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Bottom Navigation
// ──────────────────────────────────────────────────────────────

@Composable
private fun KaizenBottomNavigation(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        Pair(stringResource(id = R.string.dashboard_nav_dashboard), Icons.Default.Home),
        Pair(stringResource(id = R.string.dashboard_nav_workouts), Icons.Default.FitnessCenter),
        Pair(stringResource(id = R.string.dashboard_nav_statistics), Icons.Default.BarChart),
        Pair(stringResource(id = R.string.dashboard_nav_settings), Icons.Default.Settings)
    )

    NavigationBar(containerColor = Onyx, tonalElevation = 0.dp) {
        items.forEachIndexed { index, pair ->
            val selected = selectedTabIndex == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = { Icon(imageVector = pair.second, contentDescription = pair.first) },
                label = {
                    Text(text = pair.first, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Onyx,
                    unselectedIconColor = LightGrey,
                    selectedTextColor = CrayolaBlue,
                    unselectedTextColor = LightGrey,
                    indicatorColor = CrayolaBlue
                )
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Bottom Sheet Content Router
// ──────────────────────────────────────────────────────────────

@Composable
private fun BottomSheetContent(
    sheetType: DashboardBottomSheetType,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit = {},
    isSavingWeight: Boolean = false,
    onWorkoutClick: () -> Unit = {},
    currentWeight: Double? = null,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse> = emptyList(),
    prHistoryState: PrHistoryState = PrHistoryState.Idle,
    streakCalendar: List<StreakDayResponse> = emptyList(),
    trainingDayDetails: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.TrainingDayDetailResponse> = emptyList(),
    weekdaySchedule: Map<Int, RoutineScheduleItem> = emptyMap(),
    currentStreak: Int = 0,
    longestStreak: Int = 0,
    recentWorkoutSummaries: List<RecentWorkoutSummaryResponse> = emptyList(),
    effortMetric: String = "RPE",
    onOpenWorkoutDetail: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (sheetType) {
            is DashboardBottomSheetType.CalendarDay ->
                CalendarDaySheet(sheetType.day, sheetType.isTrainingDay)

            is DashboardBottomSheetType.LogBodyWeight ->
                BodyWeightSheet(currentWeight, onDismiss, onLogWeight, weightHistory, isSavingWeight)

            is DashboardBottomSheetType.PrDetails ->
                PrDetailsSheet(
                    exerciseName = sheetType.exerciseName,
                    prHistoryState = prHistoryState,
                    onOpenWorkoutDetail = onOpenWorkoutDetail
                )

            is DashboardBottomSheetType.StreakCalendar ->
                StreakCalendarSheet(
                    streakCalendar = streakCalendar,
                    trainingDayDetails = trainingDayDetails,
                    weekdaySchedule = weekdaySchedule,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak
                )

            is DashboardBottomSheetType.AvgTimeWorkouts ->
                AvgTimeWorkoutsSheet(
                    recentWorkoutSummaries = recentWorkoutSummaries,
                    onItemClick = onOpenWorkoutDetail
                )

            is DashboardBottomSheetType.WorkoutDetail -> { /* handled externally */ }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet: Body Weight Logger
// ──────────────────────────────────────────────────────────────

@Composable
fun BodyWeightSheet(
    currentWeight: Double?,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>,
    isSaving: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (weightHistory.isEmpty()) {
            Text(stringResource(id = R.string.dashboard_no_records), color = LightGrey, fontSize = 14.sp)
        } else {
            weightHistory.take(3).forEachIndexed { index, measurement ->
                val dateStr = try {
                    val date = LocalDate.parse(measurement.recordedAt.take(10))
                    val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                    "${date.dayOfMonth} ${monthNames[date.monthValue - 1]}"
                } catch (e: Exception) {
                    measurement.recordedAt.take(10)
                }
                val textColor = if (index == 0) PureWhite else LightGrey
                val fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                val fontSize = if (index == 0) 16.sp else 14.sp
                Text(
                    stringResource(id = R.string.dashboard_weight_log_entry, dateStr, measurement.weightKg),
                    color = textColor, fontSize = fontSize, fontWeight = fontWeight
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
        onValueChange = { if (!isSaving && (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$")))) inputWeight = it },
        label = { Text(stringResource(id = R.string.dashboard_new_weight_label), color = LightGrey) },
        enabled = !isSaving,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CrayolaBlue, unfocusedBorderColor = ShadowGrey,
            focusedTextColor = PureWhite, unfocusedTextColor = PureWhite,
            disabledBorderColor = ShadowGrey, disabledTextColor = LightGrey.copy(alpha = 0.5f),
            disabledLabelColor = LightGrey.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    val parsedInput = inputWeight.toDoubleOrNull()
    val isUnchanged = currentWeight != null && parsedInput != null && parsedInput == currentWeight
    val canSave = parsedInput != null && !isUnchanged && !isSaving
    Button(
        onClick = { if (!isSaving) parsedInput?.let { onLogWeight(it) } },
        enabled = canSave,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = CrayolaBlue,
            contentColor = Onyx,
            disabledContainerColor = ShadowGrey,
            disabledContentColor = LightGrey.copy(alpha = 0.5f)
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                color = Onyx,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(stringResource(id = R.string.dashboard_save_log), fontWeight = FontWeight.Bold)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet: Calendar Rest Day
// ──────────────────────────────────────────────────────────────

@Composable
fun CalendarDaySheet(day: Int, isTrainingDay: Boolean) {
    if (isTrainingDay) {
        Text(stringResource(id = R.string.dashboard_pull_day_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(id = R.string.dashboard_training_detail_1), color = PureWhite, fontSize = 15.sp)
            Text(stringResource(id = R.string.dashboard_training_detail_2), color = PureWhite, fontSize = 15.sp)
        }
    } else {
        Text(stringResource(id = R.string.dashboard_rest_day_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(ShadowGrey, RoundedCornerShape(12.dp))
                .border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(stringResource(id = R.string.dashboard_rest_day_recommendation), color = LightGrey, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet: PR History
// ──────────────────────────────────────────────────────────────

@SuppressLint("DefaultLocale")
@Composable
private fun PrDetailsSheet(
    exerciseName: String,
    prHistoryState: PrHistoryState,
    onOpenWorkoutDetail: (String) -> Unit
) {
    val resolvedName = BuiltinExerciseCatalog.resolveExerciseName(exerciseName)

    Text(resolvedName, color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Text("PR History", color = LightGrey, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

    when (val state = prHistoryState) {
        is PrHistoryState.Loading -> {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CrayolaBlue, modifier = Modifier.size(32.dp))
            }
        }
        is PrHistoryState.Success -> {
            if (state.prs.isEmpty()) {
                Text("No PRs recorded for this exercise.", color = LightGrey.copy(alpha = 0.6f), fontSize = 14.sp)
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    state.prs.forEachIndexed { index, pr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!pr.workoutId.isNullOrBlank())
                                        Modifier.clickable { onOpenWorkoutDetail(pr.workoutId) }
                                    else Modifier
                                )
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                if (index == 0) {
                                    Icon(Icons.Default.EmojiEvents, null, tint = PrGold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                }
                                Column {
                                    val weightStr = if (pr.weight != null) {
                                        if (pr.weight % 1.0 == 0.0) "${pr.weight.toInt()} kg"
                                        else "${String.format("%.1f", pr.weight)} kg"
                                    } else "--"
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            weightStr,
                                            color = if (index == 0) PrGold else PureWhite,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (pr.reps != null) {
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "× ${pr.reps}",
                                                color = LightGrey,
                                                fontSize = 14.sp,
                                                modifier = Modifier.alignByBaseline()
                                            )
                                        }
                                    }
                                    if (!pr.achievedAt.isNullOrBlank()) {
                                        Text(
                                            formatShortDate(pr.achievedAt),
                                            color = LightGrey.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val pct = pr.percentageImprovement
                                if (pct != null && pct != 0.0) {
                                    val pctColor = if (pct > 0) MalachiteGreen else SubtleRed
                                    val pctText = if (pct > 0) "+${String.format("%.1f", pct)}%"
                                    else "${String.format("%.1f", pct)}%"
                                    Text(pctText, color = pctColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                if (!pr.workoutId.isNullOrBlank()) {
                                    Icon(
                                        Icons.Default.ChevronRight, null,
                                        tint = LightGrey.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        if (index < state.prs.lastIndex) {
                            HorizontalDivider(color = LightGrey.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        }
        is PrHistoryState.Error -> {
            Text("Couldn't load PR history.", color = SubtleRed, fontSize = 14.sp)
        }
        else -> {} // Idle
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet: Streak Calendar (navigable month view)
// ──────────────────────────────────────────────────────────────

private data class DayMark(val workoutDone: Boolean, val missedScheduled: Boolean)

@Composable
private fun StreakCalendarSheet(
    streakCalendar: List<StreakDayResponse>,
    trainingDayDetails: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.TrainingDayDetailResponse> = emptyList(),
    weekdaySchedule: Map<Int, RoutineScheduleItem> = emptyMap(),
    currentStreak: Int = 0,
    longestStreak: Int = 0
) {
    val today = LocalDate.now()

    // Merge: trainingDayDetails (older history) + streakCalendar (recent, has missed flag).
    // streakCalendar wins on overlap.
    val dateMap: Map<String, DayMark> = remember(streakCalendar, trainingDayDetails) {
        val map = mutableMapOf<String, DayMark>()
        trainingDayDetails.forEach { detail ->
            map[detail.date.take(10)] = DayMark(workoutDone = true, missedScheduled = false)
        }
        streakCalendar.forEach { day ->
            map[day.date.take(10)] = DayMark(day.workoutDone, day.missedScheduled)
        }
        map
    }

    // Flat set of trained dates — used to compute chain connectors cheaply.
    val trainedDateSet: Set<String> = remember(dateMap) {
        dateMap.entries.filter { it.value.workoutDone }.map { it.key }.toSet()
    }

    val earliestMonth = remember(dateMap) {
        dateMap.keys
            .minOrNull()
            ?.let { runCatching { java.time.YearMonth.from(LocalDate.parse(it)) }.getOrNull() }
            ?: java.time.YearMonth.now()
    }

    val currentMonth = java.time.YearMonth.now()
    var viewedMonth by remember { mutableStateOf(currentMonth) }

    val hasTwoConsecutiveMissed = remember(dateMap, weekdaySchedule) {
        val last2 = (1L..2L).map { today.minusDays(it) }
        last2.all { d ->
            val mark = dateMap[d.toString()]
            val wasScheduled = weekdaySchedule.isEmpty() || weekdaySchedule.containsKey(d.dayOfWeek.value)
            mark?.missedScheduled == true && wasScheduled
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Streak Calendar", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // ── Streak metric cards ──────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(ShadowGrey, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("CURRENT", color = LightGrey.copy(alpha = 0.6f), fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Text(
                    text = "$currentStreak",
                    color = if (currentStreak > 0) MalachiteGreen else LightGrey,
                    fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp
                )
                Text("day streak", color = LightGrey.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(ShadowGrey, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.EmojiEvents, null, tint = PrGold, modifier = Modifier.size(13.dp))
                    Text("BEST", color = LightGrey.copy(alpha = 0.6f), fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                }
                Text(
                    text = "$longestStreak",
                    color = PrGold, fontSize = 38.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp
                )
                Text(if (longestStreak == 1) "day all-time" else "days all-time",
                    color = LightGrey.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        if (hasTwoConsecutiveMissed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SubtleRed.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, null, tint = SubtleRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("2 consecutive missed days — get back on track!",
                    color = SubtleRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // ── Month navigation ─────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewedMonth = viewedMonth.minusMonths(1) }, enabled = viewedMonth > earliestMonth) {
                Icon(Icons.Default.ChevronLeft, null,
                    tint = if (viewedMonth > earliestMonth) PureWhite else LightGrey.copy(alpha = 0.3f))
            }
            Text(
                text = viewedMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
                color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { viewedMonth = viewedMonth.plusMonths(1) }, enabled = viewedMonth < currentMonth) {
                Icon(Icons.Default.ChevronRight, null,
                    tint = if (viewedMonth < currentMonth) PureWhite else LightGrey.copy(alpha = 0.3f))
            }
        }

        // Day-of-week headers — matches main CalendarWidget style
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, color = LightGrey.copy(alpha = 0.45f), fontSize = 9.sp,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Calendar grid ────────────────────────────────────────
        val firstOfMonth = viewedMonth.atDay(1)
        val daysInMonth = viewedMonth.lengthOfMonth()
        val startOffset = firstOfMonth.dayOfWeek.value - 1          // Mon=0 … Sun=6
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1

                        // Each cell is tall enough for the circle + chain bar to sit comfortably.
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber in 1..daysInMonth) {
                                val date = viewedMonth.atDay(dayNumber)
                                val isFuture = date.isAfter(today)
                                val isToday = date == today
                                val mark = dateMap[date.toString()]
                                val isTrained = mark?.workoutDone == true

                                // Never flag today as "missed" — the user may still train.
                                val effectiveMissed = !isFuture && !isToday &&
                                    (mark?.missedScheduled == true) &&
                                    (weekdaySchedule.isEmpty() ||
                                        weekdaySchedule.containsKey(date.dayOfWeek.value))

                                val isUpcomingScheduled = isFuture &&
                                    weekdaySchedule.isNotEmpty() &&
                                    weekdaySchedule.containsKey(date.dayOfWeek.value)

                                // Chain connectors: only between consecutive trained past days.
                                val prevTrained = trainedDateSet.contains(date.minusDays(1).toString())
                                val nextTrained = trainedDateSet.contains(date.plusDays(1).toString()) &&
                                    !date.plusDays(1).isAfter(today)
                                val connectLeft = isTrained && prevTrained
                                val connectRight = isTrained && nextTrained

                                // Connector bar drawn FIRST (behind the circle).
                                // It sits at vertical center of the 34dp cell; the 30dp circle
                                // overlaps its middle, leaving the bar visible on each side.
                                if (connectLeft || connectRight) {
                                    Row(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight()
                                                .background(
                                                    if (connectLeft) MalachiteGreen.copy(alpha = 0.45f)
                                                    else Color.Transparent
                                                )
                                        )
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight()
                                                .background(
                                                    if (connectRight) MalachiteGreen.copy(alpha = 0.45f)
                                                    else Color.Transparent
                                                )
                                        )
                                    }
                                }

                                // Day circle drawn ON TOP of the connector bar.
                                StreakDayCell(
                                    day = dayNumber,
                                    isTrained = isTrained,
                                    isMissed = effectiveMissed,
                                    isToday = isToday,
                                    isFuture = isFuture,
                                    isUpcomingScheduled = isUpcomingScheduled
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Legend ───────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MalachiteGreen))
                Spacer(Modifier.width(4.dp))
                Text("Trained", color = LightGrey, fontSize = 9.sp)
            }
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(SubtleRed.copy(alpha = 0.75f)))
                Spacer(Modifier.width(4.dp))
                Text("Missed", color = LightGrey, fontSize = 9.sp)
            }
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).border(2.dp, CrayolaBlue, CircleShape).clip(CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("Today", color = LightGrey, fontSize = 9.sp)
            }
            if (weekdaySchedule.isNotEmpty()) {
                Spacer(Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape)
                        .background(CrayolaBlue.copy(alpha = 0.12f))
                        .border(1.dp, CrayolaBlue.copy(alpha = 0.4f), CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("Planned", color = LightGrey, fontSize = 9.sp)
                }
            }
        }
    }
}

// Single calendar day cell for the streak sheet.
// Matches the visual language of CalendarDayCell in LargeWidgets.kt.
@Composable
private fun StreakDayCell(
    day: Int,
    isTrained: Boolean,
    isMissed: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    isUpcomingScheduled: Boolean
) {
    val bgColor = when {
        isTrained -> MalachiteGreen
        isMissed -> SubtleRed.copy(alpha = 0.75f)
        isToday -> CrayolaBlue.copy(alpha = 0.18f)   // visible but not dominant
        isUpcomingScheduled -> CrayolaBlue.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    // Dark text on solid fills (matches main CalendarWidget's Onyx-on-green convention).
    val textColor = when {
        isTrained -> Onyx
        isMissed -> PureWhite
        isToday -> CrayolaBlue
        isUpcomingScheduled -> CrayolaBlue.copy(alpha = 0.60f)
        isFuture -> LightGrey.copy(alpha = 0.13f)
        else -> LightGrey.copy(alpha = 0.35f)
    }

    // Today always gets a prominent 2dp ring — white when trained/missed, blue otherwise.
    // The ring is applied BEFORE clip so it appears around the circle, matching main widget.
    val borderMod = when {
        isToday && isTrained -> Modifier.border(2.dp, PureWhite, CircleShape)
        isToday -> Modifier.border(2.dp, CrayolaBlue, CircleShape)
        else -> Modifier
    }

    // Today's cell is 2dp wider to make it stand out in the grid at a glance.
    val cellSize = if (isToday) 32.dp else 30.dp

    Box(
        modifier = Modifier
            .size(cellSize)
            .then(borderMod)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (isMissed) {
            // Show day number in micro-text above an "×" — clearly communicates "broken chain".
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.toString(),
                    color = PureWhite.copy(alpha = 0.5f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 7.sp
                )
                Text(
                    text = "×",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp
                )
            }
        } else {
            Text(
                text = day.toString(),
                color = textColor,
                fontSize = if (isToday) 12.sp else 11.sp,
                fontWeight = when {
                    isTrained || isToday -> FontWeight.Bold
                    isUpcomingScheduled -> FontWeight.Medium
                    else -> FontWeight.Normal
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet: Avg Time — Last 14 Days Workouts
// ──────────────────────────────────────────────────────────────

@Composable
private fun AvgTimeWorkoutsSheet(
    recentWorkoutSummaries: List<RecentWorkoutSummaryResponse>,
    onItemClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Last 14 Days", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Tap a session to see full details",
            color = LightGrey,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        if (recentWorkoutSummaries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No sessions in the last 14 days", color = LightGrey.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentWorkoutSummaries.forEach { summary ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ShadowGrey, RoundedCornerShape(12.dp))
                            .clickable { onItemClick(summary.workoutId) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = summary.routineName ?: "Free Workout",
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!summary.planName.isNullOrBlank()) {
                                Text(
                                    text = summary.planName,
                                    color = CrayolaBlue.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = formatShortDate(summary.completedAt),
                                color = LightGrey.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = summary.durationMinutes?.toString() ?: "--",
                                color = PureWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            Text("min", color = LightGrey, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Utility
// ──────────────────────────────────────────────────────────────

private fun formatShortDate(dateStr: String): String = try {
    val date = LocalDate.parse(dateStr.take(10))
    val today = LocalDate.now()
    when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH))
    }
} catch (e: Exception) {
    dateStr.take(10)
}

// ──────────────────────────────────────────────────────────────
// Google Welcome Sheet
// ──────────────────────────────────────────────────────────────

@Composable
private fun GoogleWelcomeBottomSheet(
    onSetPasswordClick: () -> Unit,
    onNotNowClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp).background(CrayolaBlue.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = stringResource(id = R.string.settings_google_content_description),
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(stringResource(id = R.string.dashboard_google_welcome_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(id = R.string.dashboard_google_welcome_message), color = LightGrey, fontSize = 14.sp, lineHeight = 20.sp)
        Button(
            onClick = onSetPasswordClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
        ) {
            Text(stringResource(id = R.string.dashboard_set_password), color = Onyx, fontWeight = FontWeight.SemiBold)
        }
        OutlinedButton(
            onClick = onNotNowClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LightGrey),
            border = androidx.compose.foundation.BorderStroke(1.dp, LightGrey.copy(alpha = 0.35f))
        ) {
            Text(stringResource(id = R.string.dashboard_not_now), color = LightGrey, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// ──────────────────────────────────────────────────────────────
// Empty State
// ──────────────────────────────────────────────────────────────

@Composable
fun DashboardEmptyState(onAddWidgetClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.BarChart, contentDescription = stringResource(id = R.string.dashboard_empty_content_description), tint = LightGrey, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.dashboard_empty_state_message),
            color = PureWhite,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddWidgetClick,
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue, contentColor = PureWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.dashboard_add_widget))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.dashboard_add_widget), fontWeight = FontWeight.SemiBold)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme { /* mock viewmodel needed for realistic preview */ }
}
