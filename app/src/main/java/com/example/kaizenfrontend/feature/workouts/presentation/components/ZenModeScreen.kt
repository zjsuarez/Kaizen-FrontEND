package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// Zen Mode Screen (Full Screen Focus)
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenModeScreen(
    initialPage: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()
    val historyViewModel: ZenModeHistoryViewModel = hiltViewModel()
    val historyUiState by historyViewModel.uiState.collectAsState()
    val state = workoutState ?: return // Render nothing if no workout
    val sessionManager = remember(context) { SessionManager(context) }
    val weightUnit = remember(sessionManager) {
        val unit = sessionManager.getUserUnitSystem()?.uppercase(Locale.getDefault())
        if (unit == "LB" || unit == "LBS") WeightUnit.LBS else WeightUnit.KG
    }

    var expandedMenuExerciseId by remember { mutableStateOf<String?>(null) }
    var historyTarget by remember { mutableStateOf<ExerciseHistoryTarget?>(null) }

    // Pager for horizontal swiping between exercises
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { state.exercises.size }
    )

    // Ensure we start on the correct page if instantiated dynamically
    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage) {
            pagerState.scrollToPage(initialPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
            .imePadding() // CRUCIAL: Prevent keyboard from covering inputs
    ) {
        // ── 1. Top Bar ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit Zen Mode",
                    tint = PureWhite
                )
            }

            // Custom Page Indicator
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(state.exercises.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .background(
                                color = if (isSelected) CrayolaBlue else LightGrey.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Dummy spacer to balance the close button
            Spacer(modifier = Modifier.width(48.dp))
        }

        // ── 2. Horizontal Pager ─────────────────────────────────
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val exercise = state.exercises[page]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Hero Section: Exercise Name + options menu
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = exercise.exerciseName,
                        color = PureWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        lineHeight = 38.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(
                            onClick = { expandedMenuExerciseId = exercise.id },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Exercise options",
                                tint = LightGrey
                            )
                        }

                        DropdownMenu(
                            expanded = expandedMenuExerciseId == exercise.id,
                            onDismissRequest = { expandedMenuExerciseId = null },
                            containerColor = ShadowGrey
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "History",
                                        color = PureWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = LightGrey
                                    )
                                },
                                onClick = {
                                    expandedMenuExerciseId = null
                                    historyTarget = ExerciseHistoryTarget(
                                        exerciseId = exercise.id,
                                        exerciseName = exercise.exerciseName,
                                        isCustomExercise = exercise.isCustom
                                    )
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "${exercise.sets.size} sets total",
                    color = LightGrey,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 32.dp)
                )

                // ── Set List ─────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Header Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SET",
                                color = LightGrey.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "WEIGHT",
                                color = LightGrey.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "REPS",
                                color = LightGrey.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "RIR",
                                color = LightGrey.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.width(36.dp))
                        }
                    }

                    // Sets
                    items(exercise.sets) { set ->
                        WorkoutSetRow(
                            set = set,
                            onWeightChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, it, null, null) },
                            onRepsChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, null, it, null) },
                            onRirChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, null, null, it) },
                            onToggleComplete = { ActiveWorkoutManager.toggleSetCompletion(exercise.id, set.id) }
                        )
                    }

                    // Bottom add button
                    item {
                        OutlinedButton(
                            onClick = { ActiveWorkoutManager.addSet(exercise.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(PureWhite.copy(alpha = 0.15f))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LightGrey
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                text = "+ Add Set",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Docked Rest Timer ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadowGrey.copy(alpha = 0.5f))
        ) {
            Column {
                HorizontalDivider(color = PureWhite.copy(alpha = 0.05f), thickness = 1.dp)
                RestTimerBar(
                    restSeconds = state.restTimer,
                    isRunning = state.isRestTimerRunning,
                    onPlayPause = {
                        if (state.isRestTimerRunning) {
                            ActiveWorkoutManager.pauseRestTimer()
                        } else {
                            val seconds = if (state.restTimer > 0) state.restTimer else 90L
                            ActiveWorkoutManager.startRestTimer(seconds)
                        }
                    },
                    onReset = { ActiveWorkoutManager.resetRestTimer() }
                )
            }
        }
    }

    historyTarget?.let { target ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val history = remember(
            historyUiState.workouts,
            target.exerciseId,
            target.exerciseName,
            target.isCustomExercise
        ) {
            historyViewModel.getExerciseHistory(
                exerciseId = target.exerciseId,
                isCustomExercise = target.isCustomExercise,
                exerciseName = target.exerciseName
            )
        }

        ModalBottomSheet(
            onDismissRequest = { historyTarget = null },
            sheetState = sheetState,
            containerColor = Onyx,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            dragHandle = {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    color = LightGrey
                )
            }
        ) {
            ExerciseHistorySheetContent(
                target = target,
                isLoading = historyUiState.isLoading,
                errorMessage = historyUiState.errorMessage,
                history = history,
                weightUnit = weightUnit,
                onRetry = historyViewModel::refresh
            )
        }
    }
}

private data class ExerciseHistoryTarget(
    val exerciseId: String,
    val exerciseName: String,
    val isCustomExercise: Boolean
)

private enum class WeightUnit {
    KG,
    LBS
}

@Composable
private fun ExerciseHistorySheetContent(
    target: ExerciseHistoryTarget,
    isLoading: Boolean,
    errorMessage: String?,
    history: List<ExerciseHistoryWorkoutUi>,
    weightUnit: WeightUnit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.68f)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${target.exerciseName} - History",
            color = PureWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Recent workouts first",
            color = LightGrey,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        when {
            isLoading && history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CrayolaBlue)
                }
            }

            errorMessage != null && history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage,
                            color = LightGrey,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrayolaBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history found for this exercise yet.",
                        color = LightGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { dayHistory ->
                        ExerciseHistoryDayCard(
                            item = dayHistory,
                            weightUnit = weightUnit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseHistoryDayCard(
    item: ExerciseHistoryWorkoutUi,
    weightUnit: WeightUnit
) {
    val borderColor = if (item.isMostRecent) CrayolaBlue.copy(alpha = 0.7f) else PureWhite.copy(alpha = 0.08f)
    val containerColor = if (item.isMostRecent) CrayolaBlue.copy(alpha = 0.12f) else ShadowGrey.copy(alpha = 0.75f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.workoutLabel,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${item.formattedDate} • ${item.formattedTime}",
                color = LightGrey,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (item.isMostRecent) {
                Text(
                    text = "Most recent workout - beat these numbers today",
                    color = CrayolaBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            ExerciseHistoryHeaderRow(weightUnit = weightUnit)
            Spacer(modifier = Modifier.height(6.dp))

            item.sets.forEach { set ->
                ExerciseHistorySetRow(
                    set = set,
                    weightUnit = weightUnit,
                    emphasize = item.isMostRecent
                )
            }
        }
    }
}

@Composable
private fun ExerciseHistoryHeaderRow(weightUnit: WeightUnit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set #",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = if (weightUnit == WeightUnit.KG) "Weight (kg)" else "Weight (lbs)",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Reps",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = "RPE",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExerciseHistorySetRow(
    set: ExerciseHistorySetUi,
    weightUnit: WeightUnit,
    emphasize: Boolean
) {
    val valueColor = if (emphasize) PureWhite else PureWhite.copy(alpha = 0.9f)
    val valueWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${set.setNumber}",
            color = LightGrey,
            fontSize = 13.sp,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = set.weightKg.toDisplayWeight(weightUnit),
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = set.reps?.toString() ?: "-",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = set.rpe?.toString() ?: "-",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun Double?.toDisplayWeight(unit: WeightUnit): String {
    if (this == null) return "-"
    val converted = if (unit == WeightUnit.LBS) this * 2.20462 else this
    return String.format(Locale.getDefault(), "%.1f", converted)
}
