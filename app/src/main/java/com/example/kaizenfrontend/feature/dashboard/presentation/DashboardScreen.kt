package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.navigation.KaizenTabScaffold
import com.example.kaizenfrontend.core.ui.theme.*
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// UI State
// ──────────────────────────────────────────────────────────────

sealed class DashboardBottomSheetType {
    data class CalendarDay(val day: Int, val isTrainingDay: Boolean) : DashboardBottomSheetType()
    data class NextWorkoutOptions(val currentRoutineName: String) : DashboardBottomSheetType()
    data class PrDetails(val exerciseName: String) : DashboardBottomSheetType()
    data class LogBodyWeight(val currentWeight: Double?) : DashboardBottomSheetType()
    data class LastSessionDetails(val routineName: String) : DashboardBottomSheetType()
    data class RecoveryInfo(val hours: Int?) : DashboardBottomSheetType()
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
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    onWorkoutClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val widgetOrder by viewModel.widgetOrder.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
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
    var showAddWidgetSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addWidgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val googleWelcomeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeBottomSheet by remember { mutableStateOf<DashboardBottomSheetType?>(null) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onScreenFocused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    KaizenTabScaffold(
        navController = navController,
        title = stringResource(id = R.string.dashboard_title),
        subtitle = "${stringResource(id = R.string.dashboard_hello, userName)}  ·  $todayLabel",
        headerActions = {
            if (!isEditing) {
                IconButton(
                    onClick = { viewModel.toggleEditMode() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.dashboard_edit_mode_enter)
                    )
                }
            } else {
                IconButton(
                    onClick = { showAddWidgetSheet = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.dashboard_add_widget),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleEditMode() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = stringResource(id = R.string.dashboard_edit_mode_done)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardWidgetGrid(
                        successState = state,
                        widgetOrder = widgetOrder,
                        weightHistory = weightHistory,
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
                    // No-data state handled by widget grid's empty branch.
                }
                is DashboardUiState.Error -> {
                    val errorMessage = state.message?.takeIf { it.isNotBlank() }
                        ?: state.messageResId?.let { stringResource(id = it) }
                        ?: stringResource(id = R.string.dashboard_error_backend_connection)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Pinned helper overlay so the reorder hint stays visible above
            // the grid without pushing content down.
            androidx.compose.animation.AnimatedVisibility(
                visible = isEditing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.dashboard_edit_mode_reorder_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

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

    if (showGoogleWelcomeSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.dismissGoogleWelcomePrompt()
            },
            sheetState = googleWelcomeSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            GoogleWelcomeBottomSheet(
                onSetPasswordClick = {
                    viewModel.dismissGoogleWelcomePrompt()
                    navController.navigate(com.example.kaizenfrontend.core.ui.navigation.KaizenDestinations.SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onNotNowClick = {
                    viewModel.dismissGoogleWelcomePrompt()
                }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Widget Grid Engine  (dual-mode: grid view / drag-edit)
// ──────────────────────────────────────────────────────────────

@Composable
fun DashboardWidgetGrid(
    successState: DashboardUiState.Success,
    widgetOrder: List<String>,
    weightHistory: List<com.example.kaizenfrontend.feature.dashboard.data.remote.dto.response.BodyMeasurementResponse>,
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
                WidgetContent(widgetType, wMod, successState, onWorkoutClick, onWidgetClick)
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
    onWorkoutClick: () -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit
) {
    val data = successState.data
    val freeLabel = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_free_label)
    val neverLabel = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_never_label)
    val overviewLabel = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_overview_label)

    when (widgetType) {
        WidgetType.STREAK ->
            widgetModifier.StreakWidget(streakDays = data.workoutStreak)
        WidgetType.AVG_TIME ->
            AvgTimeWidget(minutes = data.avgDurationMinutes, trendDiffMinutes = 0, modifier = widgetModifier)
        WidgetType.ONE_RM ->
            OneRmWidget(
                exercise = stringResource(id = com.example.kaizenfrontend.R.string.statistics_estimated_1rm_title),
                weight = data.estimated1RM,
                isNewPr = false,
                weightIncrease = 0.0,
                modifier = widgetModifier
            )
        WidgetType.WEIGHT_TREND -> {
            val diff = data.weightDiff ?: 0.0
            val isPos = diff >= 0
            WeightTrendWidget(
                currentWeight = data.currentWeight ?: 0.0,
                trendLabel = if (isPos) {
                    stringResource(id = com.example.kaizenfrontend.R.string.dashboard_weight_trend_up_week, diff)
                } else {
                    stringResource(id = com.example.kaizenfrontend.R.string.dashboard_weight_trend_down_week, diff)
                },
                isPositive = isPos,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LogBodyWeight(data.currentWeight ?: 0.0)) }
            )
        }
        WidgetType.RECOVERY_TIME ->
            RecoveryTimeWidget(hours = data.recoveryTimeHours ?: 0, modifier = widgetModifier)
        WidgetType.LAST_SESSION ->
            LastSessionWidget(
                routineName = data.lastSession?.routineName ?: freeLabel,
                timeLabel = data.lastSession?.completedAt?.take(10) ?: neverLabel,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LastSessionDetails(data.lastSession?.routineName ?: freeLabel)) }
            )
        WidgetType.NEXT_WORKOUT ->
            NextWorkoutWidget(
                routineName = data.nextWorkout?.routineName,
                onStartClick = onWorkoutClick,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.NextWorkoutOptions(data.nextWorkout?.routineName ?: freeLabel)) }
            )
        WidgetType.CALENDAR -> {
            val days = data.trainingDaysThisMonth.mapNotNull {
                runCatching { LocalDate.parse(it).dayOfMonth }.getOrNull()
            }
            CalendarWidget(
                trainingDays = days,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.CalendarDay(LocalDate.now().dayOfMonth, days.contains(LocalDate.now().dayOfMonth))) },
                onDayClick = { day, isTrainingDay -> onWidgetClick(DashboardBottomSheetType.CalendarDay(day, isTrainingDay)) }
            )
        }
        WidgetType.RECENT_PRS -> {
            val mapPrs = data.recentPrs.map { pr ->
                RecentPrMock(pr.exerciseName, "${pr.weight} kg", "", pr.achievedAt.take(10))
            }
            RecentPrsWidget(
                prs = mapPrs,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.PrDetails(data.recentPrs.firstOrNull()?.exerciseName ?: overviewLabel)) },
                onPrClick = { exercise -> onWidgetClick(DashboardBottomSheetType.PrDetails(exercise)) }
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
                contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_reorder_content_description),
                tint = LightGrey,
                modifier = Modifier.size(16.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(22.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_remove_widget_content_description),
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
// Bottom Sheet Content
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
            is DashboardBottomSheetType.CalendarDay -> {
                CalendarDaySheet(sheetType.day, sheetType.isTrainingDay)
            }
            is DashboardBottomSheetType.NextWorkoutOptions -> {
                NextWorkoutSheet(sheetType.currentRoutineName, onWorkoutClick)
            }
            is DashboardBottomSheetType.PrDetails -> {
                PrDetailsSheet(sheetType.exerciseName)
            }
            is DashboardBottomSheetType.LogBodyWeight -> {
                BodyWeightSheet(sheetType.currentWeight, onDismiss, onLogWeight, weightHistory)
            }
            is DashboardBottomSheetType.LastSessionDetails -> {
                LastSessionSheet(sheetType.routineName)
            }
            is DashboardBottomSheetType.RecoveryInfo -> {
                RecoveryInfoSheet(sheetType.hours)
            }
            is DashboardBottomSheetType.MetricHistory -> {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_metric_history_title, sheetType.metricName), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_metric_current_value, sheetType.currentValue), color = CrayolaBlue, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_metric_history_hint), color = LightGrey, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun NextWorkoutSheet(routineName: String, onWorkoutClick: () -> Unit) {
    Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_pull_day_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_workout_item_1), color = LightGrey, fontSize = 14.sp)
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_workout_item_2), color = LightGrey, fontSize = 14.sp)
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_workout_item_3), color = LightGrey, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier.fillMaxWidth().background(Onyx, RoundedCornerShape(12.dp)).border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_upcoming_plan), color = LightGrey, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onWorkoutClick, colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue), modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_start_workout_cta), color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BodyWeightSheet(
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
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_no_records), color = LightGrey, fontSize = 14.sp)
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

                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_weight_log_entry, dateStr, measurement.weightKg), color = textColor, fontSize = fontSize, fontWeight = fontWeight)
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
        label = { Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_new_weight_label), color = LightGrey) },
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
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_save_log), color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecoveryInfoSheet(hours: Int?) {
    Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_recovery_battery_content_description), tint = MalachiteGreen, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.height(8.dp))
    Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_recovery_remaining_hours), color = PureWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        stringResource(id = com.example.kaizenfrontend.R.string.dashboard_recovery_description),
        color = LightGrey, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp
    )
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(modifier = Modifier.background(Color(0xFF4A1A1A), RoundedCornerShape(8.dp)).border(1.dp, SubtleRed, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_legs_high_fatigue), color = SubtleRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.background(Color(0xFF1A3320), RoundedCornerShape(8.dp)).border(1.dp, MalachiteGreen, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_chest_fresh), color = MalachiteGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LastSessionSheet(routineName: String) {
    Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_summary_ticket, routineName), color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.statistics_date), tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_session_date_example), color = LightGrey, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Timer, contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_duration), tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_session_duration_example), color = LightGrey, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_volume), tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_session_volume_example), color = LightGrey, fontSize = 14.sp)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_main_exercises), color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(id = com.example.kaizenfrontend.R.string.dashboard_main_exercises_list),
            color = LightGrey, fontSize = 14.sp, lineHeight = 22.sp
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2A2410), RoundedCornerShape(12.dp)).border(1.dp, PrGold, RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_highlight_achievement), color = PrGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CalendarDaySheet(day: Int, isTrainingDay: Boolean) {
    if (isTrainingDay) {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_pull_day_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_training_detail_1), color = PureWhite, fontSize = 15.sp)
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_training_detail_2), color = PureWhite, fontSize = 15.sp)
        }
    } else {
        Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_rest_day_title), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
        ) {
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_rest_day_recommendation), color = LightGrey, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun PrDetailsSheet(exerciseName: String) {
    Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_history_title, exerciseName), color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_pr_entry_1), fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.statistics_push), color = LightGrey, fontSize = 14.sp)
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_two_weeks_ago), color = LightGrey, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = Onyx)

        // Row 2
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_pr_entry_2), fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.statistics_push), color = LightGrey, fontSize = 14.sp)
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_one_month_ago), color = LightGrey, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = Onyx)

        // Row 3
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_pr_entry_3), fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_full_body), color = LightGrey, fontSize = 14.sp)
                Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_three_months_ago), color = LightGrey, fontSize = 12.sp)
            }
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
                painter = painterResource(id = com.example.kaizenfrontend.R.drawable.ic_google),
                contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.settings_google_content_description),
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }

        Text(
            text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_google_welcome_title),
            color = PureWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_google_welcome_message),
            color = LightGrey,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Button(
            onClick = onSetPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
        ) {
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_set_password), color = Onyx, fontWeight = FontWeight.SemiBold)
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
            Text(stringResource(id = com.example.kaizenfrontend.R.string.dashboard_not_now), color = LightGrey, fontWeight = FontWeight.Medium)
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
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_empty_content_description),
            tint = LightGrey,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_empty_state_message),
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
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_add_widget))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_add_widget), fontWeight = FontWeight.SemiBold)
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
