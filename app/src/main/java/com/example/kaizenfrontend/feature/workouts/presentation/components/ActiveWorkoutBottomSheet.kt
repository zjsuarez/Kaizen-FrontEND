package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.components.formatElapsed
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveExerciseState
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import com.example.kaizenfrontend.feature.workouts.domain.model.WorkoutSetState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun hasIncompleteSets(exercises: List<ActiveExerciseState>): Boolean =
    exercises.any { exercise ->
        exercise.sets.any { set ->
            set.weight.isIncompleteLoggedValue() || set.reps.isIncompleteLoggedValue()
        }
    }

private fun String.isIncompleteLoggedValue(): Boolean =
    isBlank() || toIntOrNull() == 0

// ──────────────────────────────────────────────────────────────
// Active Workout Bottom Sheet — "Tunnel Mode"
// ──────────────────────────────────────────────────────────────

/**
 * Full-screen-style bottom sheet that hosts the entire active
 * workout experience. Opens expanded (skip partial).
 *
 * @param onDismiss Called when the user swipes down or taps scrim.
 * @param onFinish  Called when the user taps "Finish" — callers
 *                  should handle persisting the data.
 * @param onAddExercise Called when the user taps the "+" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutBottomSheet(
    onDismiss: () -> Unit,
    onFinish: () -> Unit,
    onDiscard: () -> Unit,
    onAddExercise: () -> Unit,
    onNavigateToZenMode: (initialPage: Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = androidx.compose.runtime.remember { com.example.kaizenfrontend.core.data.local.SessionManager(context) }
    val effortMetric = androidx.compose.runtime.remember { sessionManager.getUserEffortMetric() ?: "RPE" }
    val unitSystem = androidx.compose.runtime.remember { sessionManager.getUserUnitSystem() ?: "METRIC" }
    val weightUnit = if (unitSystem == "IMPERIAL") "lbs" else "kg"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()

    // Guard — nothing to show if workout ended externally
    val state = workoutState ?: return
    var showIncompleteSetsDialog by remember { mutableStateOf(false) }
    val shouldWarnAboutIncompleteSets = hasIncompleteSets(state.exercises)
    val handleFinishRequest = {
        if (shouldWarnAboutIncompleteSets) {
            showIncompleteSetsDialog = true
        } else {
            onFinish()
        }
    }

    if (showIncompleteSetsDialog) {
        Dialog(onDismissRequest = { showIncompleteSetsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ShadowGrey
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workouts_incomplete_sets_dialog_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(R.string.workouts_incomplete_sets_dialog_message),
                            color = LightGrey,
                            fontSize = 14.sp
                        )
                    }
                    HorizontalDivider(color = LightGrey.copy(alpha = 0.15f))
                    TextButton(
                        onClick = { showIncompleteSetsDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workouts_discard_keep_editing),
                            color = LightGrey,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    HorizontalDivider(color = LightGrey.copy(alpha = 0.15f))
                    TextButton(
                        onClick = {
                            showIncompleteSetsDialog = false
                            onFinish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workouts_incomplete_sets_dialog_confirm),
                            color = CrayolaBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    HorizontalDivider(color = LightGrey.copy(alpha = 0.15f))
                    TextButton(
                        onClick = {
                            showIncompleteSetsDialog = false
                            onDiscard()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workouts_discard_confirm),
                            color = SubtleRed,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
    ) {
        ActiveWorkoutSheetContent(
            state = state,
            effortMetric = effortMetric,
            weightUnit = weightUnit,
            onFinish = handleFinishRequest,
            onAddExercise = onAddExercise,
            onPlayPauseRest = {
                if (state.isRestTimerRunning) {
                    ActiveWorkoutManager.pauseRestTimer()
                } else {
                    // Resume or start a default 90-second timer
                    val seconds = if (state.restTimer > 0) state.restTimer else 90L
                    ActiveWorkoutManager.startRestTimer(seconds)
                }
            },
            onResetRest = { ActiveWorkoutManager.resetRestTimer() },
            onNotesChange = { ActiveWorkoutManager.updateNotes(it) },
            onNavigateToZenMode = onNavigateToZenMode
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet Content (stateless composable)
// ──────────────────────────────────────────────────────────────

@Composable
internal fun ActiveWorkoutSheetContent(
    state: ActiveWorkoutState,
    effortMetric: String,
    weightUnit: String,
    onFinish: () -> Unit,
    onAddExercise: () -> Unit,
    onPlayPauseRest: () -> Unit,
    onResetRest: () -> Unit,
    onNotesChange: (String) -> Unit,
    onNavigateToZenMode: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
    ) {
        // ── 1. Header ────────────────────────────────────────────
        WorkoutHeader(
            routineName = state.routineName,
            startTime = state.startTime,
            elapsedTime = state.elapsedTimeGlobal,
            onAddExercise = onAddExercise,
            onFinish = onFinish
        )

        HorizontalDivider(
            color = ShadowGrey,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // ── 2. Exercise list ─────────────────────────────────────────
        ExerciseList(
            exercises = state.exercises,
            effortMetric = effortMetric,
            weightUnit = weightUnit,
            onToggleExpansion = { ActiveWorkoutManager.toggleExerciseExpansion(it) },
            onAddSet = { ActiveWorkoutManager.addSet(it) },
            onUpdateSetData = { exerciseId, setId, weight, reps, rpe, type ->
                ActiveWorkoutManager.updateSetData(exerciseId, setId, weight, reps, rpe, type)
            },
            onToggleSetCompletion = { exerciseId, setId ->
                ActiveWorkoutManager.toggleSetCompletion(exerciseId, setId)
            },
            onNavigateToZenMode = onNavigateToZenMode,
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider(
            color = ShadowGrey,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // ── 3. Rest Timer (Docked below) ─────────────────────────
        RestTimerBar(
            restSeconds = state.restTimer,
            isRunning = state.isRestTimerRunning,
            onPlayPause = onPlayPauseRest,
            onReset = onResetRest
        )

        // ── 4. Notes footer ──────────────────────────────────────
        NotesFooter(
            notes = state.notes,
            onNotesChange = onNotesChange
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Header
// ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutHeader(
    routineName: String,
    startTime: Long,
    elapsedTime: Long,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Left: routine name + started time + elapsed ──────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = routineName,
                color = PureWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val startedLabel = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(startTime))
            Text(
                text = "Started $startedLabel",
                color = LightGrey,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Elapsed global timer
            Text(
                text = formatElapsed(elapsedTime),
                color = CrayolaBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }

        // ── Right: actions ───────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outlined "+" button
            OutlinedButton(
                onClick = onAddExercise,
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(CrayolaBlue)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CrayolaBlue
                ),
                modifier = Modifier.size(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.workouts_add_exercise_cd),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Primary "Finish" button
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrayolaBlue,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = stringResource(id = R.string.workouts_finish),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Rest Timer Bar
// ──────────────────────────────────────────────────────────────

@Composable
internal fun RestTimerBar(
    restSeconds: Long,
    isRunning: Boolean,
    onPlayPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Play / Pause — minimal icon button
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) {
                    stringResource(id = R.string.workouts_pause_rest_cd)
                } else {
                    stringResource(id = R.string.workouts_start_rest_cd)
                },
                tint = PureWhite,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Giant monospace timer
        val timerColor = if (isRunning) PureWhite else LightGrey
        Text(
            text = formatRestTimer(restSeconds),
            color = timerColor,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Reset — minimal icon button
        IconButton(onClick = onReset) {
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = stringResource(id = R.string.workouts_reset_rest_timer_cd),
                tint = LightGrey,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Formats rest timer seconds into MM:SS.
 */
internal fun formatRestTimer(seconds: Long): String {
    val s = seconds.coerceAtLeast(0)
    val min = s / 60
    val sec = s % 60
    return "%02d:%02d".format(min, sec)
}

// ──────────────────────────────────────────────────────────────
// Exercise List
// ──────────────────────────────────────────────────────────────

@Composable
private fun ExerciseList(
    exercises: List<ActiveExerciseState>,
    effortMetric: String,
    weightUnit: String,
    onToggleExpansion: (String) -> Unit,
    onAddSet: (String) -> Unit,
    onUpdateSetData: (exerciseId: String, setId: String, weight: String?, reps: String?, rpe: String?, type: com.example.kaizenfrontend.feature.workouts.domain.model.SetType?) -> Unit,
    onToggleSetCompletion: (exerciseId: String, setId: String) -> Unit,
    onNavigateToZenMode: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = exercises,
            key = { _, ex -> ex.id }
        ) { index, exercise ->
            ActiveExerciseRow(
                exercise = exercise,
                effortMetric = effortMetric,
                weightUnit = weightUnit,
                onToggleExpand = { onToggleExpansion(exercise.id) },
                onAddSet = { onAddSet(exercise.id) },
                onUpdateSetData = { setId, weight, reps, rpe, type ->
                    onUpdateSetData(exercise.id, setId, weight, reps, rpe, type)
                },
                onToggleSetCompletion = { setId ->
                    onToggleSetCompletion(exercise.id, setId)
                },
                onZenModeClick = { onNavigateToZenMode(index) }
            )

            // Ultra-thin divider between exercises (not after last)
            if (index < exercises.lastIndex) {
                HorizontalDivider(
                    color = PureWhite.copy(alpha = 0.06f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Single Exercise Row
// ──────────────────────────────────────────────────────────────

@Composable
private fun ActiveExerciseRow(
    exercise: ActiveExerciseState,
    effortMetric: String,
    weightUnit: String,
    onToggleExpand: () -> Unit,
    onAddSet: () -> Unit,
    onUpdateSetData: (setId: String, weight: String?, reps: String?, rpe: String?, type: com.example.kaizenfrontend.feature.workouts.domain.model.SetType?) -> Unit,
    onToggleSetCompletion: (setId: String) -> Unit,
    onZenModeClick: () -> Unit
) {
    // Animated chevron rotation
    val chevronRotation by animateFloatAsState(
        targetValue = if (exercise.isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Header (clickable) ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(vertical = 14.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subtle drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = stringResource(id = R.string.workouts_reorder_cd),
                tint = PureWhite.copy(alpha = 0.18f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Exercise name + set count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.exerciseName,
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${exercise.sets.size} sets",
                    color = LightGrey,
                    fontSize = 13.sp
                )
            }

            // Zen mode button
            IconButton(
                onClick = onZenModeClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio, // We will need to import AspectRatio or Fullscreen
                    contentDescription = stringResource(id = R.string.workouts_zen_mode_cd),
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Animated chevron
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (exercise.isExpanded) {
                    stringResource(id = R.string.workouts_collapse_cd)
                } else {
                    stringResource(id = R.string.workouts_expand_cd)
                },
                tint = LightGrey,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(chevronRotation)
            )
        }

        // ── Expandable content ───────────────────────────────────
        AnimatedVisibility(
            visible = exercise.isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 4.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ── Set column header ─────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.workouts_set),
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(id = R.string.workouts_weight),
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(id = R.string.workouts_reps),
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = effortMetric,
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    // Spacer for the check button column
                    Spacer(modifier = Modifier.width(36.dp))
                }

                exercise.sets.forEach { set ->
                    WorkoutSetRow(
                        set = set,
                        effortMetric = effortMetric,
                        weightUnit = weightUnit,
                        onWeightChange = { onUpdateSetData(set.id, it, null, null, null) },
                        onRepsChange = { onUpdateSetData(set.id, null, it, null, null) },
                        onRpeChange = { onUpdateSetData(set.id, null, null, it, null) },
                        onTypeChange = { onUpdateSetData(set.id, null, null, null, it) },
                        onToggleComplete = { onToggleSetCompletion(set.id) }
                    )
                }

                // "+ Add Set" button
                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(PureWhite.copy(alpha = 0.15f))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LightGrey
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.workouts_add_set_button),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Workout Set Row
// ──────────────────────────────────────────────────────────────

@Composable
internal fun WorkoutSetRow(
    set: WorkoutSetState,
    effortMetric: String,
    weightUnit: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (String) -> Unit,
    onTypeChange: (com.example.kaizenfrontend.feature.workouts.domain.model.SetType) -> Unit,
    onToggleComplete: () -> Unit
) {
    // Animated alpha for completion fade
    val rowAlpha by animateFloatAsState(
        targetValue = if (set.isCompleted) 0.4f else 1f,
        label = "set_row_alpha"
    )

    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = rowAlpha }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Set number indicator / type selector
        Box(
            modifier = Modifier
                .width(42.dp)
                .clickable { dropdownExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.workouts_change_set_type_cd),
                    tint = LightGrey.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                
                val typeIndicator = when(set.type) {
                    com.example.kaizenfrontend.feature.workouts.domain.model.SetType.NORMAL -> "${set.setNumber}"
                    com.example.kaizenfrontend.feature.workouts.domain.model.SetType.WARMUP -> "W"
                    com.example.kaizenfrontend.feature.workouts.domain.model.SetType.DROP_SET -> "D"
                    com.example.kaizenfrontend.feature.workouts.domain.model.SetType.FAILURE -> "F"
                    com.example.kaizenfrontend.feature.workouts.domain.model.SetType.MYO_REP -> "M"
                }
                Text(
                    text = typeIndicator,
                    color = if (set.type == com.example.kaizenfrontend.feature.workouts.domain.model.SetType.NORMAL) LightGrey else CrayolaBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(18.dp)
                )
            }
            
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                com.example.kaizenfrontend.feature.workouts.domain.model.SetType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(text = type.getDisplayName()) },
                        onClick = {
                            onTypeChange(type)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Weight input
        SetInputField(
            value = set.weight,
            onValueChange = onWeightChange,
            suffix = weightUnit,
            placeholder = stringResource(id = R.string.workouts_em_dash),
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f)
        )

        // Reps input
        SetInputField(
            value = set.reps,
            onValueChange = onRepsChange,
            suffix = stringResource(id = R.string.workouts_reps),
            placeholder = stringResource(id = R.string.workouts_em_dash),
            modifier = Modifier.weight(1f)
        )

        val isRirDisabled = set.type == com.example.kaizenfrontend.feature.workouts.domain.model.SetType.FAILURE ||
                            set.type == com.example.kaizenfrontend.feature.workouts.domain.model.SetType.MYO_REP ||
                            set.type == com.example.kaizenfrontend.feature.workouts.domain.model.SetType.DROP_SET

        // RIR input
        SetInputField(
            value = if (isRirDisabled) "0" else set.rpe,
            onValueChange = onRpeChange,
            suffix = effortMetric,
            placeholder = stringResource(id = R.string.workouts_em_dash),
            enabled = !isRirDisabled,
            modifier = Modifier.weight(1f)
        )

        // Completion toggle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (set.isCompleted) CrayolaBlue else ShadowGrey,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onToggleComplete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (set.isCompleted) {
                    stringResource(id = R.string.workouts_mark_incomplete_cd)
                } else {
                    stringResource(id = R.string.workouts_mark_complete_cd)
                },
                tint = if (set.isCompleted) PureWhite else LightGrey.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Styled Numeric Input Field
// ──────────────────────────────────────────────────────────────

/**
 * Production-quality numeric input with inline suffix.
 * Uses BasicTextField over a ShadowGrey rounded background
 * instead of the clunky default Material TextField.
 */
@Composable
internal fun SetInputField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    placeholder: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Number,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .background(if (enabled) ShadowGrey else LightGrey.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = if (enabled) value else "-",
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = TextStyle(
                    color = if (enabled) PureWhite else LightGrey.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(CrayolaBlue),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (value.isBlank()) {
                            Text(
                                text = placeholder,
                                color = LightGrey.copy(alpha = 0.35f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = suffix,
                color = LightGrey.copy(alpha = 0.55f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Notes Footer
// ──────────────────────────────────────────────────────────────

@Composable
private fun NotesFooter(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp, top = 8.dp)
    ) {
        Text(
            text = "Notes",
            color = LightGrey,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        BasicTextField(
            value = notes,
            onValueChange = onNotesChange,
            textStyle = TextStyle(
                color = PureWhite.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            cursorBrush = SolidColor(CrayolaBlue),
            modifier = Modifier
                .fillMaxWidth()
                .background(ShadowGrey, RoundedCornerShape(12.dp))
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (notes.isBlank()) {
                    Text(
                        text = "How did the workout feel?",
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        )
    }
}
