package com.example.kaizenfrontend.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState

// ──────────────────────────────────────────────────────────────
// Active Workout Overlay — "Floating Island" mini-player
// ──────────────────────────────────────────────────────────────

/**
 * A floating card that appears above the bottom navigation whenever
 * a workout is in progress. Tapping it opens the full workout screen.
 *
 * Place this inside a [Box] that wraps the main content area,
 * aligned to [Alignment.BottomCenter].
 */
@Composable
fun ActiveWorkoutOverlay(
    modifier: Modifier = Modifier,
    onOpenWorkout: () -> Unit
) {
    val workoutState by ActiveWorkoutManager.currentWorkout.collectAsState()

    AnimatedVisibility(
        visible = workoutState != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        workoutState?.let { state ->
            WorkoutIsland(
                state = state,
                onClick = onOpenWorkout
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Island Card
// ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutIsland(
    state: ActiveWorkoutState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left: routine name + current exercise ────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = state.routineName,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val currentExercise = state.exercises
                    .firstOrNull { ex -> ex.sets.any { !it.isCompleted } }
                    ?.exerciseName
                    ?: state.exercises.lastOrNull()?.exerciseName
                    ?: ""

                if (currentExercise.isNotBlank()) {
                    Text(
                        text = currentExercise,
                        color = LightGrey,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Right: elapsed time ──────────────────────────────
            Text(
                text = formatElapsed(state.elapsedTimeGlobal),
                color = CrayolaBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Time formatter
// ──────────────────────────────────────────────────────────────

/**
 * Formats milliseconds into MM:SS (or H:MM:SS when ≥ 1 hour).
 */
internal fun formatElapsed(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
