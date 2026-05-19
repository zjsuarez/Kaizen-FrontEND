package com.example.kaizenfrontend.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.PureWhite
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

// Dark navy background: distinct from ShadowGrey widgets and Onyx app background.
private val IslandBackground = Color(0xFF111827)

@Composable
private fun WorkoutIsland(
    state: ActiveWorkoutState,
    onClick: () -> Unit
) {
    // Pulsing live-indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    val currentExercise = state.exercises
        .firstOrNull { ex -> ex.sets.any { !it.isCompleted } }
        ?.exerciseName
        ?: state.exercises.lastOrNull()?.exerciseName
        ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.5.dp, CrayolaBlue.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IslandBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left: live dot + routine name + current exercise ──
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MalachiteGreen.copy(alpha = dotAlpha))
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = state.routineName,
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
