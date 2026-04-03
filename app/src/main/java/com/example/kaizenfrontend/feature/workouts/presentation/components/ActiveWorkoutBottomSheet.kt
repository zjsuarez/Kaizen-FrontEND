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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.formatElapsed
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveExerciseState
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onAddExercise: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()

    // Guard — nothing to show if workout ended externally
    val state = workoutState ?: return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
    ) {
        ActiveWorkoutSheetContent(
            state = state,
            onFinish = onFinish,
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
            onNotesChange = { ActiveWorkoutManager.updateNotes(it) }
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Sheet Content (stateless composable)
// ──────────────────────────────────────────────────────────────

@Composable
internal fun ActiveWorkoutSheetContent(
    state: ActiveWorkoutState,
    onFinish: () -> Unit,
    onAddExercise: () -> Unit,
    onPlayPauseRest: () -> Unit,
    onResetRest: () -> Unit,
    onNotesChange: (String) -> Unit
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

        // ── 2. Rest Timer ────────────────────────────────────────
        RestTimerBar(
            restSeconds = state.restTimer,
            isRunning = state.isRestTimerRunning,
            onPlayPause = onPlayPauseRest,
            onReset = onResetRest
        )

        HorizontalDivider(
            color = ShadowGrey,
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // ── 3. Exercise list ─────────────────────────────────────────
        ExerciseList(
            exercises = state.exercises,
            onToggleExpansion = { ActiveWorkoutManager.toggleExerciseExpansion(it) },
            onAddSet = { ActiveWorkoutManager.addSet(it) },
            modifier = Modifier.weight(1f)
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
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add exercise",
                    modifier = Modifier.size(20.dp)
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
                    text = "Finish",
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
private fun RestTimerBar(
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
                contentDescription = if (isRunning) "Pause rest" else "Start rest",
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
                contentDescription = "Reset rest timer",
                tint = LightGrey,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Formats rest timer seconds into MM:SS.
 */
private fun formatRestTimer(seconds: Long): String {
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
    onToggleExpansion: (String) -> Unit,
    onAddSet: (String) -> Unit,
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
                onToggleExpand = { onToggleExpansion(exercise.id) },
                onAddSet = { onAddSet(exercise.id) }
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
    onToggleExpand: () -> Unit,
    onAddSet: () -> Unit
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
                contentDescription = "Reorder",
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

            // Animated chevron
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (exercise.isExpanded) "Collapse" else "Expand",
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
                // Set rows slot — filled in Task 5
                // (empty Column for now)

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
                        text = "+ Add Set",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
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
