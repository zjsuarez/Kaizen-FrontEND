package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.core.ui.components.ActiveWorkoutOverlay
import com.example.kaizenfrontend.feature.workouts.presentation.components.ActiveWorkoutBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.ZenModeScreen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kaizenfrontend.feature.dashboard.model.WidgetConfig
import com.example.kaizenfrontend.feature.dashboard.model.WidgetSize
import com.example.kaizenfrontend.feature.dashboard.model.WidgetType
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.AvgTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.CalendarWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.LastSessionWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.NextWorkoutWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.OneRmWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrMock
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecentPrsWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.RecoveryTimeWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.StreakWidget
import com.example.kaizenfrontend.feature.dashboard.presentation.widgets.WeightTrendWidget
import com.example.kaizenfrontend.feature.statistics.presentation.StatisticsScreen
import com.example.kaizenfrontend.feature.user.presentation.settings.SettingsScreen
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutsScreen
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────

sealed class DashboardBottomSheetType {
    data class CalendarDay(val date: LocalDate, val hasWorkout: Boolean) : DashboardBottomSheetType()
    data class NextWorkoutOptions(val currentRoutineName: String, val currentPlanName: String?) : DashboardBottomSheetType()
    data object PrLedger : DashboardBottomSheetType()
    data class LogBodyWeight(val currentWeight: Double?) : DashboardBottomSheetType()
    data object LastSessionDetails : DashboardBottomSheetType()
    data object RecoveryInfo : DashboardBottomSheetType()
    data class MetricHistory(val metricName: String, val currentValue: String) : DashboardBottomSheetType()
}

// ──────────────────────────────────────────────────────────────
// Dashboard Widget Layout — hardcoded order for the grid engine
// ──────────────────────────────────────────────────────────────

private val dashboardWidgets =
        listOf(
                // LARGE — full-width, tall
                WidgetConfig(
                        type = WidgetType.NEXT_WORKOUT,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 200.dp
                ),
                // THIN — full-width, short
                WidgetConfig(
                        type = WidgetType.WEIGHT_TREND,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 80.dp
                ),
                WidgetConfig(
                        type = WidgetType.RECOVERY_TIME,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 80.dp
                ),
                WidgetConfig(
                        type = WidgetType.LAST_SESSION,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 80.dp
                ),
                // SMALL — half-width, square (side by side)
                WidgetConfig(
                        type = WidgetType.STREAK,
                        size = WidgetSize.HALF_WIDTH,
                        heightDp = 140.dp
                ),
                WidgetConfig(
                        type = WidgetType.AVG_TIME,
                        size = WidgetSize.HALF_WIDTH,
                        heightDp = 140.dp
                ),
                WidgetConfig(
                        type = WidgetType.ONE_RM,
                        size = WidgetSize.HALF_WIDTH,
                        heightDp = 140.dp
                ),
                // Placeholder to keep the grid even (2 cols)
                WidgetConfig(
                        type = WidgetType.ONE_RM,
                        size = WidgetSize.HALF_WIDTH,
                        heightDp = 140.dp
                ),
                // LARGE — full-width, tall
                WidgetConfig(
                        type = WidgetType.CALENDAR,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 320.dp
                ),
                WidgetConfig(
                        type = WidgetType.RECENT_PRS,
                        size = WidgetSize.FULL_WIDTH,
                        heightDp = 250.dp
                ),
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val widgetOrder by viewModel.widgetOrder.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val nextWorkoutExercises by viewModel.nextWorkoutExercises.collectAsState()
    val nextWorkoutDisplay by viewModel.nextWorkoutDisplay.collectAsState()
    val workoutsByDate by viewModel.workoutsByDate.collectAsState()
    val lastSessionDetails by viewModel.lastSessionDetails.collectAsState()
    val isWorkoutHistoryLoading by viewModel.isWorkoutHistoryLoading.collectAsState()
    val recentPrLedger by viewModel.recentPrLedger.collectAsState()
    val muscleReadiness by viewModel.muscleReadiness.collectAsState()
    val isLoggingBodyWeight by viewModel.isLoggingBodyWeight.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val userName = remember {
        SessionManager(context)
            .getUserEmail()
            ?.substringBefore("@")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Athlete"
    }
    val todayLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
    }
    var selectedTab by remember { mutableStateOf(0) }
    var showAddWidgetSheet by remember { mutableStateOf(false) }
    var showActiveWorkoutSheet by remember { mutableStateOf(false) }
    var zenModeInitialPage by remember { mutableStateOf<Int?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addWidgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeBottomSheet by remember { mutableStateOf<DashboardBottomSheetType?>(null) }

    Scaffold(
            containerColor = Onyx,
            topBar = {
                if (selectedTab == 0) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Workouts",
                                    color = PureWhite,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hola, $userName",
                                    color = LightGrey,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = todayLabel,
                                    color = LightGrey.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                            }
                        },
                        actions = {
                            if (!isEditing) {
                                FloatingActionButton(
                                    onClick = { viewModel.toggleEditMode() },
                                    containerColor = ShadowGrey,
                                    contentColor = PureWhite,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Enter edit mode",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = { showAddWidgetSheet = true },
                                        containerColor = CrayolaBlue,
                                        contentColor = PureWhite,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add widget",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.toggleEditMode() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ShadowGrey)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Done,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Done", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Onyx,
                            titleContentColor = PureWhite,
                            actionIconContentColor = PureWhite
                        )
                    )
                }
            },
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
                0 ->
                        when (val state = uiState) {
                            is DashboardUiState.Loading -> {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                            color = CrayolaBlue,
                                            modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            is DashboardUiState.Success -> {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                DashboardWidgetGrid(
                                    successState = state,
                                    widgetOrder = widgetOrder,
                                    weightHistory = weightHistory,
                                    nextWorkoutDisplay = nextWorkoutDisplay,
                                    workoutsByDate = workoutsByDate,
                                    isEditing = isEditing,
                                    onWorkoutClick = onWorkoutClick,
                                    onWidgetClick = { activeBottomSheet = it },
                                    onRemoveWidget = { widgetKey -> viewModel.removeWidget(widgetKey) },
                                    onReorderWidgets = { updatedOrder ->
                                        viewModel.onReorderWidgets(updatedOrder)
                                    },
                                    onAddWidgetClick = { showAddWidgetSheet = true }
                                )
                            }
                            is DashboardUiState.Empty -> {
                                // Normally this handles no-data gracefully
                            }
                            is DashboardUiState.Error -> {
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(100.dp)
                                                        .background(
                                                                ShadowGrey,
                                                                RoundedCornerShape(16.dp)
                                                        )
                                                        .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(text = state.message, color = Color.Red, fontSize = 14.sp)
                                }
                            }
                        }
                1 -> WorkoutsScreen()
                2 -> StatisticsScreen()
                3 -> SettingsScreen(onLogoutClick = onLogoutClick)
            }

            // ── Floating Island: active workout mini-player ─────────
            ActiveWorkoutOverlay(
                modifier = Modifier.align(Alignment.BottomCenter),
                onOpenWorkout = { showActiveWorkoutSheet = true }
            )

            // Pinned helper overlay to avoid content jump when entering edit mode.
            androidx.compose.animation.AnimatedVisibility(
                visible = selectedTab == 0 && isEditing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            color = ShadowGrey.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Mantén pulsado y arrastra para reordenar",
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (activeBottomSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheet = null },
            sheetState = sheetState,
            containerColor = Onyx,
            dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
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
                weightHistory = weightHistory,
                nextWorkoutExercises = nextWorkoutExercises,
                workoutsByDate = workoutsByDate,
                lastSessionDetails = lastSessionDetails,
                isWorkoutHistoryLoading = isWorkoutHistoryLoading,
                recentPrLedger = recentPrLedger,
                muscleReadiness = muscleReadiness,
                isLoggingBodyWeight = isLoggingBodyWeight
            )
        }
    }

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

    // ── Active Workout "Tunnel Mode" bottom sheet ─────────
    if (showActiveWorkoutSheet) {
        ActiveWorkoutBottomSheet(
            onDismiss = { showActiveWorkoutSheet = false },
            onFinish = {
                val snapshot = com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager.finishWorkout()
                snapshot?.let {
                    viewModel.saveWorkout(it)
                }
                showActiveWorkoutSheet = false
                zenModeInitialPage = null // close Zen Mode if open
            },
            onAddExercise = { /* TODO: open exercise catalog — Task 4+ */ },
            onNavigateToZenMode = { page ->
                zenModeInitialPage = page
            }
        )
    }

    // ── Zen Mode Full Screen ─────────
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
            // close inner Box (already closed above, this is formatting fix)
        } // close Column
    }

// ──────────────────────────────────────────────────────────────
// Widget Grid Engine  (dual-mode: grid view / drag-edit)
// ──────────────────────────────────────────────────────────────

@Composable
fun DashboardWidgetGrid(
    successState: DashboardUiState.Success,
    widgetOrder: List<String>,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>,
    nextWorkoutDisplay: NextWorkoutDisplayUi?,
    workoutsByDate: Map<LocalDate, List<CalendarWorkoutUi>>,
    isEditing: Boolean,
    onWorkoutClick: () -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onReorderWidgets: (List<String>) -> Unit,
    onAddWidgetClick: () -> Unit
) {
    val orderedWidgetTypes =
        widgetOrder.mapNotNull { runCatching { WidgetType.valueOf(it) }.getOrNull() }
    val density = LocalDensity.current
    val gridState = rememberLazyGridState()
    val editWidgetTypes = remember { mutableStateListOf<WidgetType>() }
    val itemHeightPxByKey = remember { mutableStateMapOf<String, Float>() }
    var draggingWidgetKey by remember { mutableStateOf<String?>(null) }
    var dragDistanceY by remember { mutableFloatStateOf(0f) }
    val isDragActive = draggingWidgetKey != null

    val dragPriorityScrollBlocker = remember(isDragActive) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (isDragActive && source == NestedScrollSource.UserInput) {
                    available
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return if (isDragActive) available else Velocity.Zero
            }
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
        // Normal Mode: 2 column grid
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val data = successState.data
            items(
                items = orderedWidgetTypes,
                key = { "widget_${it.name}" },
                span = { widgetType ->
                    val config = widgetConfigByType[widgetType]
                    GridItemSpan(config?.size?.span ?: WidgetSize.FULL_WIDTH.span)
                }
            ) { widgetType ->
                val config = widgetConfigByType[widgetType] ?: return@items
                val wMod = Modifier.fillMaxWidth().height(config.heightDp)
                WidgetContent(widgetType, wMod, successState, nextWorkoutDisplay, workoutsByDate, onWorkoutClick, onWidgetClick)
            }
        }
    } else {
        // Edit mode: skeletal ghost grid that keeps each widget footprint.
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = !isDragActive,
            modifier = Modifier.fillMaxSize().nestedScroll(dragPriorityScrollBlocker)
        ) {
            itemsIndexed(
                items = editWidgetTypes,
                key = { _, wt -> "edit_${wt.name}" },
                span = { _, widgetType ->
                    val config = widgetConfigByType[widgetType]
                    GridItemSpan(config?.size?.span ?: WidgetSize.FULL_WIDTH.span)
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
                        val wasDragging = draggingWidgetKey == widgetType.name
                        if (wasDragging) {
                            draggingWidgetKey = null
                            dragDistanceY = 0f
                        }
                        onRemoveWidget(widgetType.name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(config.heightDp)
                        .zIndex(if (isDragging) 1f else 0f)
                        .onSizeChanged { size ->
                            itemHeightPxByKey[widgetType.name] = size.height.toFloat()
                        }
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
                                onDragCancel = {
                                    draggingWidgetKey = null
                                    dragDistanceY = 0f
                                },
                                onDragEnd = {
                                    draggingWidgetKey = null
                                    dragDistanceY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    if (draggingWidgetKey != widgetType.name) return@detectDragGesturesAfterLongPress
                                    change.consume()
                                    dragDistanceY += dragAmount.y

                                    var currentIndex =
                                        editWidgetTypes.indexOfFirst { it.name == widgetType.name }
                                    if (currentIndex == -1) return@detectDragGesturesAfterLongPress

                                    val baseHeightPx =
                                        itemHeightPxByKey[widgetType.name]
                                            ?: with(density) { config.heightDp.toPx() }

                                    while (
                                        dragDistanceY > baseHeightPx * 0.45f &&
                                            currentIndex < editWidgetTypes.lastIndex
                                    ) {
                                        val toIndex = currentIndex + 1
                                        editWidgetTypes.add(
                                            toIndex,
                                            editWidgetTypes.removeAt(currentIndex)
                                        )
                                        onReorderWidgets(editWidgetTypes.map { it.name })
                                        dragDistanceY -= baseHeightPx * 0.45f
                                        currentIndex = toIndex
                                    }

                                    while (
                                        dragDistanceY < -baseHeightPx * 0.45f &&
                                            currentIndex > 0
                                    ) {
                                        val toIndex = currentIndex - 1
                                        editWidgetTypes.add(
                                            toIndex,
                                            editWidgetTypes.removeAt(currentIndex)
                                        )
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
// Reusable: renders the actual widget composable
// ──────────────────────────────────────────────────────────────

@Composable
private fun WidgetContent(
    widgetType: WidgetType,
    widgetModifier: Modifier,
    successState: DashboardUiState.Success,
    nextWorkoutDisplay: NextWorkoutDisplayUi?,
    workoutsByDate: Map<LocalDate, List<CalendarWorkoutUi>>,
    onWorkoutClick: () -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit
) {
    val data = successState.data
    val routineByDate = remember(workoutsByDate) {
        workoutsByDate.mapValues { (_, dayWorkouts) ->
            dayWorkouts
                .groupingBy { it.routineName }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?: "Workout"
        }
    }
    val routineColorByName = remember(routineByDate) { buildRoutineColorMap(routineByDate.values.toSet()) }

    when (widgetType) {
        WidgetType.STREAK -> {
            widgetModifier
                .clickable {
                    onWidgetClick(
                        DashboardBottomSheetType.MetricHistory(
                            metricName = "Workout Streak",
                            currentValue = "${data.workoutStreak} days"
                        )
                    )
                }
                .StreakWidget(streakDays = data.workoutStreak)
        }
        WidgetType.AVG_TIME ->
            AvgTimeWidget(
                minutes = data.avgDurationMinutes,
                trendDiffMinutes = 0,
                modifier = widgetModifier.clickable {
                    onWidgetClick(
                        DashboardBottomSheetType.MetricHistory(
                            metricName = "Average Session Time",
                            currentValue = "${data.avgDurationMinutes} min"
                        )
                    )
                }
            )
        WidgetType.ONE_RM ->
            OneRmWidget(
                exercise = data.recentPrs.maxByOrNull { it.weight }?.exerciseName ?: "Top Lift",
                weight = data.estimated1RM,
                isNewPr = false,
                weightIncrease = 0.0,
                modifier = widgetModifier.clickable {
                    onWidgetClick(
                        DashboardBottomSheetType.MetricHistory(
                            metricName = "Estimated 1RM",
                            currentValue = "${formatVolumeKg(data.estimated1RM)} kg"
                        )
                    )
                }
            )
        WidgetType.WEIGHT_TREND -> {
            val diff = data.weightDiff ?: 0.0
            val isPos = diff >= 0
            WeightTrendWidget(
                currentWeight = data.currentWeight ?: 0.0,
                trendLabel = "${if (isPos) "+" else ""}$diff kg esta semana",
                isPositive = isPos,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LogBodyWeight(data.currentWeight ?: 0.0)) }
            )
        }
        WidgetType.RECOVERY_TIME ->
            RecoveryTimeWidget(
                hours = data.recoveryTimeHours ?: 0,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.RecoveryInfo) }
            )
        WidgetType.LAST_SESSION ->
            LastSessionWidget(
                routineName = data.lastSession?.routineName ?: "Libre",
                timeLabel = data.lastSession?.completedAt?.take(10) ?: "Nunca",
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LastSessionDetails) }
            )
        WidgetType.NEXT_WORKOUT ->
            NextWorkoutWidget(
                routineName = nextWorkoutDisplay?.routineName ?: data.nextWorkout?.routineName,
                planName = nextWorkoutDisplay?.planName,
                scheduleHint = nextWorkoutDisplay?.scheduleHint,
                onStartClick = onWorkoutClick,
                modifier = widgetModifier,
                onClick = {
                    onWidgetClick(
                        DashboardBottomSheetType.NextWorkoutOptions(
                            currentRoutineName = nextWorkoutDisplay?.routineName ?: data.nextWorkout?.routineName ?: "Libre",
                            currentPlanName = nextWorkoutDisplay?.planName
                        )
                    )
                }
            )
        WidgetType.CALENDAR -> {
            val days = data.trainingDaysThisMonth.mapNotNull {
                runCatching { LocalDate.parse(it).dayOfMonth }.getOrNull()
            }
            val today = LocalDate.now()
            CalendarWidget(
                trainingDays = days,
                routineByDate = routineByDate,
                routineColorByName = routineColorByName,
                modifier = widgetModifier,
                onClick = {
                    onWidgetClick(
                        DashboardBottomSheetType.CalendarDay(
                            date = today,
                            hasWorkout = workoutsByDate.contains(today)
                        )
                    )
                },
                onDayClick = { date, isTrainingDay ->
                    onWidgetClick(
                        DashboardBottomSheetType.CalendarDay(
                            date = date,
                            hasWorkout = workoutsByDate.contains(date) || isTrainingDay
                        )
                    )
                }
            )
        }
        WidgetType.RECENT_PRS -> {
            val mapPrs = data.recentPrs.map { pr ->
                RecentPrMock(pr.exerciseName, "${pr.weight} kg", "", pr.achievedAt.take(10))
            }
            RecentPrsWidget(
                prs = mapPrs,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.PrLedger) },
                onPrClick = { _ -> onWidgetClick(DashboardBottomSheetType.PrLedger) }
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
        GhostWidgetContent(
            widgetType = widgetType,
            modifier = Modifier.fillMaxSize()
        )

        // Muted overlay to communicate edit/ghost mode while preserving real widget structure.
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
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Reordenar",
                tint = LightGrey,
                modifier = Modifier.size(16.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar widget",
                    tint = SubtleRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun GhostWidgetContent(
    widgetType: WidgetType,
    modifier: Modifier = Modifier
) {
    when (widgetType) {
        WidgetType.STREAK -> modifier.StreakWidget(streakDays = null)
        WidgetType.AVG_TIME -> AvgTimeWidget(minutes = null, trendDiffMinutes = 0, modifier = modifier)
        WidgetType.ONE_RM ->
            OneRmWidget(
                exercise = "--",
                weight = null,
                isNewPr = false,
                weightIncrease = 0.0,
                modifier = modifier
            )
        WidgetType.WEIGHT_TREND ->
            WeightTrendWidget(
                currentWeight = null,
                trendLabel = "--",
                isPositive = null,
                modifier = modifier,
                onClick = null
            )
        WidgetType.RECOVERY_TIME -> RecoveryTimeWidget(hours = null, modifier = modifier)
        WidgetType.LAST_SESSION ->
            LastSessionWidget(
                routineName = "--",
                timeLabel = "--",
                modifier = modifier,
                onClick = null
            )
        WidgetType.NEXT_WORKOUT ->
            NextWorkoutWidget(
                routineName = "--",
                onStartClick = {},
                modifier = modifier,
                onClick = null,
                isGhost = true
            )
        WidgetType.CALENDAR -> {
            val placeholderTrainingDays = remember { listOf(3, 9, 15, 22, 27) }
            CalendarWidget(
                trainingDays = placeholderTrainingDays,
                modifier = modifier,
                onClick = null,
                onDayClick = { _, _ -> },
                isGhost = true
            )
        }
        WidgetType.RECENT_PRS -> {
            val placeholderPrs =
                remember {
                    listOf(
                        RecentPrMock("Bench Press", "-- kg", "", "--"),
                        RecentPrMock("Squat", "-- kg", "", "--"),
                        RecentPrMock("Deadlift", "-- kg", "", "--")
                    )
                }
            RecentPrsWidget(
                prs = placeholderPrs,
                modifier = modifier,
                onClick = null,
                onPrClick = {},
                isGhost = true
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Bottom Navigation
// ──────────────────────────────────────────────────────────────

@Composable
private fun KaizenBottomNavigation(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val items =
            listOf(
                    Pair("Dashboard", Icons.Default.Home),
                    Pair("Workouts", Icons.Default.FitnessCenter),
                    Pair("Statistics", Icons.Default.BarChart),
                    Pair("Settings", Icons.Default.Settings)
            )

    NavigationBar(containerColor = Onyx, tonalElevation = 0.dp) {
        items.forEachIndexed { index, pair ->
            val selected = selectedTabIndex == index
            NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(imageVector = pair.second, contentDescription = pair.first) },
                    label = { Text(text = pair.first, fontSize = 10.sp) },
                    colors =
                            NavigationBarItemDefaults.colors(
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
// Bottom Sheet Content
// ──────────────────────────────────────────────────────────────

@Composable
private fun BottomSheetContent(
    sheetType: DashboardBottomSheetType,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit = {},
    onWorkoutClick: () -> Unit = {},
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse> = emptyList(),
    nextWorkoutExercises: List<NextWorkoutExerciseUi> = emptyList(),
    workoutsByDate: Map<LocalDate, List<CalendarWorkoutUi>> = emptyMap(),
    lastSessionDetails: LastSessionModalUi? = null,
    isWorkoutHistoryLoading: Boolean = false,
    recentPrLedger: List<PrHistoryEntryUi> = emptyList(),
    muscleReadiness: List<MuscleReadinessUi> = emptyList(),
    isLoggingBodyWeight: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (sheetType) {
            is DashboardBottomSheetType.CalendarDay -> {
                CalendarDaySheet(
                    date = sheetType.date,
                    workouts = workoutsByDate[sheetType.date].orEmpty(),
                    hasTrainingMarker = sheetType.hasWorkout
                )
            }
            is DashboardBottomSheetType.NextWorkoutOptions -> {
                NextWorkoutSheet(
                    routineName = sheetType.currentRoutineName,
                    planName = sheetType.currentPlanName,
                    exercises = nextWorkoutExercises,
                    onWorkoutClick = onWorkoutClick
                )
            }
            is DashboardBottomSheetType.PrLedger -> {
                PrLedgerSheet(
                    history = recentPrLedger,
                    isLoading = isWorkoutHistoryLoading
                )
            }
            is DashboardBottomSheetType.LogBodyWeight -> {
                BodyWeightSheet(
                    currentWeight = sheetType.currentWeight,
                    onDismiss = onDismiss,
                    onLogWeight = onLogWeight,
                    weightHistory = weightHistory,
                    isSaving = isLoggingBodyWeight
                )
            }
            is DashboardBottomSheetType.LastSessionDetails -> {
                LastSessionSheet(
                    session = lastSessionDetails,
                    isLoading = isWorkoutHistoryLoading
                )
            }
            is DashboardBottomSheetType.RecoveryInfo -> {
                RecoveryInfoSheet(muscleReadiness = muscleReadiness)
            }
            is DashboardBottomSheetType.MetricHistory -> {
                Text("${sheetType.metricName} History", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current: ${sheetType.currentValue}", color = CrayolaBlue, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Charts will be available in the Analytics Lab.", color = LightGrey, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun NextWorkoutSheet(
    routineName: String,
    planName: String?,
    exercises: List<NextWorkoutExerciseUi>,
    onWorkoutClick: () -> Unit
) {
    val title = routineName.takeIf { it.isNotBlank() } ?: "Next Workout"
    Text(title, color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    if (!planName.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Plan: $planName",
            color = LightGrey,
            fontSize = 13.sp
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    if (exercises.isEmpty()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(ShadowGrey, RoundedCornerShape(12.dp))
                    .border(1.dp, LightGrey.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
        ) {
            Text(
                "No planned exercises found for this routine yet.",
                color = LightGrey,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            exercises.forEachIndexed { index, exercise ->
                val setsSuffix =
                    exercise.targetSets?.let { targetSets -> " (${targetSets} sets)" }.orEmpty()
                Text(
                    text = "${index + 1}. ${exercise.name}$setsSuffix",
                    color = LightGrey,
                    fontSize = 14.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onWorkoutClick, colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue), modifier = Modifier.fillMaxWidth()) {
        Text("START WORKOUT", color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BodyWeightSheet(
    currentWeight: Double?,
    onDismiss: () -> Unit,
    onLogWeight: (Double) -> Unit,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>,
    isSaving: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (weightHistory.isEmpty()) {
            Text("No hay registros", color = LightGrey, fontSize = 14.sp)
        } else {
            weightHistory.take(3).forEachIndexed { index, measurement ->
                val dateStr = try {
                    val date = java.time.LocalDate.parse(measurement.recordedAt.take(10))
                    val monthNames = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                    "${date.dayOfMonth} ${monthNames[date.monthValue - 1]}"
                } catch (e: Exception) {
                    measurement.recordedAt.take(10)
                }

                val textColor = if (index == 0) PureWhite else LightGrey
                val fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal
                val fontSize = if (index == 0) 16.sp else 14.sp

                Text("$dateStr: ${measurement.weightKg} kg", color = textColor, fontSize = fontSize, fontWeight = fontWeight)
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
        label = { Text("Nuevo peso (kg)", color = LightGrey) },
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
            val parsed = inputWeight.replace(',', '.').toDoubleOrNull()
            if (parsed != null) {
                onLogWeight(parsed)
            }
        },
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
    ) {
        Text(if (isSaving) "Guardando..." else "Guardar Registro", color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecoveryInfoSheet(muscleReadiness: List<MuscleReadinessUi>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = "Muscle readiness", tint = MalachiteGreen, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Muscle Readiness", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(14.dp))

    if (muscleReadiness.isEmpty()) {
        Text("No recent set data available yet.", color = LightGrey, fontSize = 14.sp)
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        muscleReadiness.forEach { muscle ->
            val trackColor = ShadowGrey
            val fillColor =
                when {
                    muscle.recoveredPercent >= 80 -> MalachiteGreen
                    muscle.recoveredPercent >= 50 -> CrayolaBlue
                    else -> SubtleRed
                }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShadowGrey.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(muscle.muscleName, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${muscle.recoveredPercent}%", color = LightGrey, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { muscle.recoveredPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = fillColor,
                    trackColor = trackColor,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = muscle.lastTrainedAt?.let { "${muscle.statusLabel} · Last hit: ${formatDashboardDate(it)}" }
                        ?: "${muscle.statusLabel} · No recent session",
                    color = LightGrey.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun LastSessionSheet(
    session: LastSessionModalUi?,
    isLoading: Boolean
) {
    val title = session?.routineName ?: "Last Session"
    Text("Summary: $title", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))

    if (isLoading) {
        CircularProgressIndicator(color = CrayolaBlue)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading last session details...", color = LightGrey, fontSize = 14.sp)
        return
    }

    if (session == null) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(ShadowGrey, RoundedCornerShape(12.dp))
                    .border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
        ) {
            Text("No completed sessions yet.", color = LightGrey, fontSize = 15.sp)
        }
        return
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                session.completedAt?.let { formatDashboardDate(it) } ?: "--",
                color = LightGrey,
                fontSize = 14.sp
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Timer, contentDescription = "Duration", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                session.durationMinutes?.let { "$it min" } ?: "--",
                color = LightGrey,
                fontSize = 14.sp
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Volume", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${formatVolumeKg(session.totalVolumeKg)} kg",
                color = LightGrey,
                fontSize = 14.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Session Intensity", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadowGrey, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Average RPE: ${session.averageRpe?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "--"}",
                color = CrayolaBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lifts (${session.totalSets} sets):", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        if (session.lifts.isEmpty()) {
            Text("No set details available.", color = LightGrey, fontSize = 14.sp)
        } else {
            session.lifts.take(10).forEach { lift ->
                val topWeightLabel = lift.topWeightKg?.let { "${formatVolumeKg(it)}kg" } ?: "--"
                val avgRepsLabel = lift.averageReps?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "--"
                Text(
                    text = "• ${lift.name}: ${lift.sets} sets x $topWeightLabel (avg reps $avgRepsLabel)",
                    color = LightGrey,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun CalendarDaySheet(
    date: LocalDate,
    workouts: List<CalendarWorkoutUi>,
    hasTrainingMarker: Boolean
) {
    val title = if (workouts.isNotEmpty()) "Training Day" else "Rest Day"
    Text(title, color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault())),
        color = LightGrey,
        fontSize = 13.sp
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (workouts.isEmpty()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(ShadowGrey, RoundedCornerShape(12.dp))
                    .border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
        ) {
            val guidance =
                if (hasTrainingMarker) {
                    "No detailed workout sets were returned for this day. Sync your workout history and try again."
                } else {
                    "No workouts logged for this date."
                }
            Text(guidance, color = LightGrey, fontSize = 15.sp, lineHeight = 22.sp)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        workouts.forEach { workout ->
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .background(ShadowGrey, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = workout.routineName,
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                workout.completedAt?.let { completedAt ->
                    Text(
                        text = formatDashboardTimestamp(completedAt),
                        color = LightGrey.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                workout.exerciseSummaries.take(6).forEach { line ->
                    Text(text = "• $line", color = LightGrey, fontSize = 14.sp)
                }
            }
        }
    }
}

private fun formatDashboardTimestamp(raw: String): String {
    val zone = ZoneId.systemDefault()
    val parsed =
        runCatching { OffsetDateTime.parse(raw).toInstant().atZone(zone) }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(zone) }.getOrNull()

    return parsed?.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault()))
        ?: raw.take(16)
}

@Composable
fun PrLedgerSheet(
    history: List<PrHistoryEntryUi>,
    isLoading: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "PR ledger", tint = PrGold, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("PR Ledger", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(24.dp))

    if (isLoading) {
        CircularProgressIndicator(color = CrayolaBlue)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading PR history...", color = LightGrey, fontSize = 14.sp)
        return
    }

    if (history.isEmpty()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(ShadowGrey, RoundedCornerShape(12.dp))
                    .border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
        ) {
            Text("No PR history found for this exercise.", color = LightGrey, fontSize = 15.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(history) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShadowGrey.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val weightLabel = entry.weightKg?.let { formatVolumeKg(it) } ?: "--"
                    val repsLabel = entry.reps?.toString() ?: "--"
                    Text(entry.exerciseName, fontSize = 13.sp, color = LightGrey)
                    Text(
                        "$weightLabel kg x $repsLabel",
                        fontSize = 18.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(entry.routineName, color = LightGrey, fontSize = 14.sp)
                    Text(
                        entry.achievedAt?.let { formatDashboardDate(it) } ?: "Unknown date",
                        color = LightGrey,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatVolumeKg(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

private fun formatDashboardDate(raw: String): String {
    val zone = ZoneId.systemDefault()
    val parsed =
        runCatching { OffsetDateTime.parse(raw).toInstant().atZone(zone) }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(zone) }.getOrNull()

    return parsed?.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
        ?: raw.take(10)
}

private fun normalizeExerciseLookup(raw: String): String {
    return raw
        .trim()
        .replace('_', ' ')
        .replace('-', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .lowercase(Locale.getDefault())
}

private fun buildRoutineColorMap(routines: Set<String>): Map<String, Color> {
    val palette = listOf(CrayolaBlue, MalachiteGreen, SubtleRed, PrGold, Color(0xFF3AAFA9), Color(0xFFFF9F1C))
    val sorted = routines.filter { it.isNotBlank() }.sortedBy { it.lowercase(Locale.getDefault()) }

    var fallbackIndex = 0
    return sorted.associateWith { routineName ->
        classifyRoutineColor(routineName) ?: palette[fallbackIndex++ % palette.size]
    }
}

private fun classifyRoutineColor(routineName: String): Color? {
    val normalized = routineName.lowercase(Locale.getDefault())
    return when {
        normalized.contains("push") -> CrayolaBlue
        normalized.contains("pull") -> MalachiteGreen
        normalized.contains("leg") -> SubtleRed
        normalized.contains("upper") -> PrGold
        normalized.contains("lower") -> Color(0xFFFF9F1C)
        normalized.contains("full") -> Color(0xFF3AAFA9)
        else -> null
    }
}

// ──────────────────────────────────────────────────────────────
// Empty State
// ──────────────────────────────────────────────────────────────
@Composable
fun DashboardEmptyState(onAddWidgetClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = "Empty Dashboard",
            tint = LightGrey,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu Kaizen Hub está vacío.\nAñade métricas para empezar a trackear tu progreso.",
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
            Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Añadir Widget", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        // Need to provide a mock view model for realistic preview rendering in actual Studio
        // environment
    }
}
