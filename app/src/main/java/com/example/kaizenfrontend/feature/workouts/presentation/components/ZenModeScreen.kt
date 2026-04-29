package com.example.kaizenfrontend.feature.workouts.presentation.components

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager

// ──────────────────────────────────────────────────────────────
// Zen Mode Screen (Full Screen Focus)
// ──────────────────────────────────────────────────────────────

@Composable
fun ZenModeScreen(
    initialPage: Int,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = androidx.compose.runtime.remember { com.example.kaizenfrontend.core.data.local.SessionManager(context) }
    val effortMetric = androidx.compose.runtime.remember { sessionManager.getUserEffortMetric() ?: "RPE" }

    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()
    val state = workoutState ?: return // Render nothing if no workout

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
                                text = effortMetric,
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
                            effortMetric = effortMetric,
                            onWeightChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, it, null, null, null) },
                            onRepsChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, null, it, null, null) },
                            onRpeChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, null, null, it, null) },
                            onTypeChange = { ActiveWorkoutManager.updateSetData(exercise.id, set.id, null, null, null, it) },
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
        ExerciseHistoryBottomSheet(
            target = target,
            onDismissRequest = { historyTarget = null }
        )
    }
}
