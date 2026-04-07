package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Info
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
        viewModel: DashboardViewModel = hiltViewModel(),
        onWorkoutClick: () -> Unit = {},
        onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val widgetOrder by viewModel.widgetOrder.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
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
            // Global edit mode hint (only visible when editing)
            androidx.compose.animation.AnimatedVisibility(visible = isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ShadowGrey)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Arrastra para reordenar los widgets. Usa la papelera para ocultarlos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

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
                                    isEditing = isEditing,
                                    onWorkoutClick = onWorkoutClick,
                                    onWidgetClick = { activeBottomSheet = it },
                                    onRemoveWidget = { widgetKey -> viewModel.removeWidget(widgetKey) },
                                    onMoveWidgetUp = { widgetKey -> viewModel.moveWidgetUp(widgetKey) },
                                    onMoveWidgetDown = { widgetKey -> viewModel.moveWidgetDown(widgetKey) }
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
    isEditing: Boolean,
    onWorkoutClick: () -> Unit,
    onWidgetClick: (DashboardBottomSheetType) -> Unit,
    onRemoveWidget: (String) -> Unit,
    onMoveWidgetUp: (String) -> Unit,
    onMoveWidgetDown: (String) -> Unit
) {
    val orderedWidgetTypes =
        (if (widgetOrder.isEmpty()) fallbackWidgetOrder else widgetOrder)
            .mapNotNull { runCatching { WidgetType.valueOf(it) }.getOrNull() }

    if (!isEditing) {
        // Normal Mode: 2 column grid
        LazyVerticalGrid(
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
        // Edit mode: single-column draggable list
        val density = LocalDensity.current

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Personaliza tu Dashboard",
                    color = LightGrey,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            itemsIndexed(
                items = orderedWidgetTypes,
                key = { _, wt -> "edit_${wt.name}" }
            ) { _, widgetType ->
                DraggableWidgetCard(
                    widgetType = widgetType,
                    onRemove = { onRemoveWidget(widgetType.name) },
                    onMoveUp = { onMoveWidgetUp(widgetType.name) },
                    onMoveDown = { onMoveWidgetDown(widgetType.name) },
                    density = density
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
    when (widgetType) {
        WidgetType.STREAK ->
            widgetModifier.StreakWidget(streakDays = data.workoutStreak)
        WidgetType.AVG_TIME ->
            AvgTimeWidget(minutes = data.avgDurationMinutes, trendDiffMinutes = 0, modifier = widgetModifier)
        WidgetType.ONE_RM ->
            OneRmWidget(
                exercise = "Estimated 1RM",
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
                trendLabel = "${if (isPos) "+" else ""}$diff kg esta semana",
                isPositive = isPos,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LogBodyWeight(data.currentWeight ?: 0.0)) }
            )
        }
        WidgetType.RECOVERY_TIME ->
            RecoveryTimeWidget(hours = data.recoveryTimeHours ?: 0, modifier = widgetModifier)
        WidgetType.LAST_SESSION ->
            LastSessionWidget(
                routineName = data.lastSession?.routineName ?: "Libre",
                timeLabel = data.lastSession?.completedAt?.take(10) ?: "Nunca",
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.LastSessionDetails(data.lastSession?.routineName ?: "Libre")) }
            )
        WidgetType.NEXT_WORKOUT ->
            NextWorkoutWidget(
                routineName = data.nextWorkout?.routineName,
                onStartClick = onWorkoutClick,
                modifier = widgetModifier,
                onClick = { onWidgetClick(DashboardBottomSheetType.NextWorkoutOptions(data.nextWorkout?.routineName ?: "Libre")) }
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
                onClick = { onWidgetClick(DashboardBottomSheetType.PrDetails(data.recentPrs.firstOrNull()?.exerciseName ?: "Overview")) },
                onPrClick = { exercise -> onWidgetClick(DashboardBottomSheetType.PrDetails(exercise)) }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Edit Mode: Draggable widget card (matches Workouts RoutineCard)
// ──────────────────────────────────────────────────────────────

@Composable
private fun DraggableWidgetCard(
    widgetType: WidgetType,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    density: androidx.compose.ui.unit.Density
) {
    val maxVisualOffsetPx = remember(density) { with(density) { 72.dp.toPx() } }
    var dragVisualOffset by remember(widgetType.name) { mutableFloatStateOf(0f) }
    var isDragging by remember(widgetType.name) { mutableStateOf(false) }

    val animatedDragOffset by animateFloatAsState(
        targetValue = if (isDragging) dragVisualOffset else 0f,
        label = "widget_drag_offset"
    )
    val liftedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        label = "widget_drag_scale"
    )
    val liftedElevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        label = "widget_drag_elevation"
    )
    val cardColor by animateColorAsState(
        targetValue = if (isDragging) ShadowGrey.copy(alpha = 0.9f) else ShadowGrey,
        label = "widget_card_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDragging) CrayolaBlue.copy(alpha = 0.45f) else Color.Transparent,
        label = "widget_border_color"
    )
    val liftedElevationPx = with(density) { liftedElevation.toPx() }

    // Human-readable display name
    val displayName = widgetType.name
        .split("_")
        .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = animatedDragOffset
                scaleX = liftedScale
                scaleY = liftedScale
                shadowElevation = liftedElevationPx
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Widget name
            Text(
                modifier = Modifier.weight(1f),
                text = displayName,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            // ── Delete button ──────────────────────────────
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar widget",
                    tint = SubtleRed
                )
            }

            // Drag handle (identical to Workouts)
            DashboardDragHandle(
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                threshold = 36.dp,
                onDragOffsetChange = { offset ->
                    dragVisualOffset = offset.coerceIn(-maxVisualOffsetPx, maxVisualOffsetPx)
                },
                onDragStateChange = { dragging ->
                    isDragging = dragging
                    if (!dragging) dragVisualOffset = 0f
                }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Drag handle
// ──────────────────────────────────────────────────────────────

@Composable
private fun DashboardDragHandle(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    threshold: Dp,
    onDragOffsetChange: (Float) -> Unit = {},
    onDragStateChange: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val thresholdPx = remember(threshold, density) { with(density) { threshold.toPx() } }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val handleBackground by animateColorAsState(
        targetValue = if (isDragging) CrayolaBlue.copy(alpha = 0.2f) else Color.Transparent,
        label = "dash_drag_handle_bg"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(CircleShape)
            .background(handleBackground)
            .pointerInput(onMoveUp, onMoveDown, thresholdPx, onDragOffsetChange, onDragStateChange) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumulator = 0f
                        isDragging = true
                        onDragStateChange(true)
                        onDragOffsetChange(0f)
                    },
                    onDragEnd = {
                        dragAccumulator = 0f
                        isDragging = false
                        onDragOffsetChange(0f)
                        onDragStateChange(false)
                    },
                    onDragCancel = {
                        dragAccumulator = 0f
                        isDragging = false
                        onDragOffsetChange(0f)
                        onDragStateChange(false)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.y
                        var visualOffset = dragAccumulator

                        while (dragAccumulator >= thresholdPx) {
                            onMoveDown()
                            dragAccumulator -= thresholdPx
                            visualOffset -= thresholdPx * 0.65f
                        }
                        while (dragAccumulator <= -thresholdPx) {
                            onMoveUp()
                            dragAccumulator += thresholdPx
                            visualOffset += thresholdPx * 0.65f
                        }

                        onDragOffsetChange(visualOffset)
                    }
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = "Reordenar widget",
            tint = LightGrey
        )
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
fun NextWorkoutSheet(routineName: String, onWorkoutClick: () -> Unit) {
    Text("Día de Tirón (Pull)", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("1. Peso Muerto (4x8)", color = LightGrey, fontSize = 14.sp)
        Text("2. Dominadas (3x10)", color = LightGrey, fontSize = 14.sp)
        Text("3. Remo (3x12)", color = LightGrey, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier.fillMaxWidth().background(Onyx, RoundedCornerShape(12.dp)).border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text("Próximas rutinas en tu plan: Jueves (Pierna), Viernes (Descanso)", color = LightGrey, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = onWorkoutClick, colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue), modifier = Modifier.fillMaxWidth()) {
        Text("Comenzar Entrenamiento", color = Onyx, fontWeight = FontWeight.Bold)
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
            inputWeight.toDoubleOrNull()?.let { onLogWeight(it) } ?: onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
    ) {
        Text("Guardar Registro", color = Onyx, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecoveryInfoSheet(hours: Int?) {
    Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = MalachiteGreen, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.height(8.dp))
    Text("48h Restantes", color = PureWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "Basado en tu alto volumen en el Día de Pierna de ayer, tu Sistema Nervioso Central necesita descanso.",
        color = LightGrey, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp
    )
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(modifier = Modifier.background(Color(0xFF4A1A1A), RoundedCornerShape(8.dp)).border(1.dp, SubtleRed, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("Piernas (Fatiga Alta)", color = SubtleRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.background(Color(0xFF1A3320), RoundedCornerShape(8.dp)).border(1.dp, MalachiteGreen, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text("Pecho (Fresco)", color = MalachiteGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LastSessionSheet(routineName: String) {
    Text("Ticket de Resumen: $routineName", color = PureWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Date", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("12 Abril", color = LightGrey, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Timer, contentDescription = "Duration", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("75 min", color = LightGrey, fontSize = 14.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Volume", tint = LightGrey, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text("6,400 kg", color = LightGrey, fontSize = 14.sp)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Ejercicios Principales:", color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "• Sentadilla Libre\n• Prensa de Piernas\n• Extensiones",
            color = LightGrey, fontSize = 14.sp, lineHeight = 22.sp
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2A2410), RoundedCornerShape(12.dp)).border(1.dp, PrGold, RoundedCornerShape(12.dp)).padding(16.dp)
    ) {
        Text("🏆 Logro destacado: Nuevo PR en Dominadas", color = PrGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CalendarDaySheet(day: Int, isTrainingDay: Boolean) {
    if (isTrainingDay) {
        Text("Día de Tirón (Pull)", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("• Sentadilla: 100kg x 8, 100kg x 7", color = PureWhite, fontSize = 15.sp)
            Text("• Prensa: 200kg x 10, 200kg x 10", color = PureWhite, fontSize = 15.sp)
        }
    } else {
        Text("Día de Descanso", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().background(ShadowGrey, RoundedCornerShape(12.dp)).border(1.dp, LightGrey.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
        ) {
            Text("Recomendación: Caminar 10.000 pasos hoy para recuperación activa.", color = LightGrey, fontSize = 15.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun PrDetailsSheet(exerciseName: String) {
    Text("Historial: $exerciseName", color = PureWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(24.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("105 kg x 1", fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Push Day", color = LightGrey, fontSize = 14.sp)
                Text("Hace 2 semanas", color = LightGrey, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = Onyx)

        // Row 2
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("102.5 kg x 2", fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Push Day", color = LightGrey, fontSize = 14.sp)
                Text("Hace 1 mes", color = LightGrey, fontSize = 12.sp)
            }
        }
        HorizontalDivider(color = Onyx)

        // Row 3
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("100 kg x 3", fontSize = 18.sp, color = PureWhite, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Full Body", color = LightGrey, fontSize = 14.sp)
                Text("Hace 3 meses", color = LightGrey, fontSize = 12.sp)
            }
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
