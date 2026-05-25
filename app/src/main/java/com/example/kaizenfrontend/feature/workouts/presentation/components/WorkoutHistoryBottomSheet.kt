package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutResponseDto
import com.example.kaizenfrontend.feature.workouts.data.remote.dto.WorkoutSetResponseDto
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.core.data.BuiltinExerciseCatalog
import java.text.SimpleDateFormat
import java.util.*

// ──────────────────────────────────────────────────────────────
// Internal navigation state
// ──────────────────────────────────────────────────────────────

private sealed interface HistoryNav {
    data object List : HistoryNav
    data class Detail(val workout: WorkoutResponseDto) : HistoryNav
}

// ──────────────────────────────────────────────────────────────
// Main bottom sheet entry point
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryBottomSheet(
    workouts: List<WorkoutResponseDto>,
    plans: List<TrainingPlan>,
    routinesByPlanId: Map<String, List<Routine>>,
    isLoading: Boolean,
    error: String?,
    effortMetric: String,
    photoUrlByMeasurementId: Map<String, String>,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nav by remember { mutableStateOf<HistoryNav>(HistoryNav.List) }

    ModalBottomSheet(
        onDismissRequest = {
            if (nav is HistoryNav.Detail) nav = HistoryNav.List
            else onDismiss()
        },
        sheetState = sheetState,
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey.copy(alpha = 0.4f)) }
    ) {
        AnimatedContent(
            targetState = nav,
            transitionSpec = {
                if (targetState is HistoryNav.Detail) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "history_nav"
        ) { currentNav ->
            when (currentNav) {
                is HistoryNav.List -> HistoryListContent(
                    workouts = workouts,
                    plans = plans,
                    routinesByPlanId = routinesByPlanId,
                    isLoading = isLoading,
                    error = error,
                    photoUrlByMeasurementId = photoUrlByMeasurementId,
                    onWorkoutClick = { nav = HistoryNav.Detail(it) },
                    onRefresh = onRefresh
                )
                is HistoryNav.Detail -> WorkoutDetailContent(
                    workout = currentNav.workout,
                    effortMetric = effortMetric,
                    photoUrlByMeasurementId = photoUrlByMeasurementId,
                    onBack = { nav = HistoryNav.List }
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// History list
// ──────────────────────────────────────────────────────────────

@Composable
private fun HistoryListContent(
    workouts: List<WorkoutResponseDto>,
    plans: List<TrainingPlan>,
    routinesByPlanId: Map<String, List<Routine>>,
    isLoading: Boolean,
    error: String?,
    photoUrlByMeasurementId: Map<String, String>,
    onWorkoutClick: (WorkoutResponseDto) -> Unit,
    onRefresh: () -> Unit
) {
    // Build routineId → planId map for client-side filter
    val routineIdToPlanId = remember(routinesByPlanId) {
        routinesByPlanId.flatMap { (planId, routines) ->
            routines.map { it.id to planId }
        }.toMap()
    }

    var selectedPlanId by remember { mutableStateOf<String?>(null) }

    val filteredWorkouts = remember(workouts, selectedPlanId) {
        if (selectedPlanId == null) workouts
        else workouts.filter { w ->
            w.routineId != null && routineIdToPlanId[w.routineId] == selectedPlanId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout History",
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isLoading) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LightGrey)
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp),
                    color = CrayolaBlue,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Plan filter chips
        if (plans.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedPlanId == null,
                        onClick = { selectedPlanId = null },
                        label = { Text("All", fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrayolaBlue,
                            selectedLabelColor = PureWhite,
                            containerColor = ShadowGrey,
                            labelColor = LightGrey
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedPlanId == null,
                            selectedBorderColor = Color.Transparent,
                            borderColor = LightGrey.copy(alpha = 0.2f)
                        )
                    )
                }
                items(plans) { plan ->
                    FilterChip(
                        selected = selectedPlanId == plan.id,
                        onClick = { selectedPlanId = if (selectedPlanId == plan.id) null else plan.id },
                        label = {
                            Text(
                                plan.name,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrayolaBlue,
                            selectedLabelColor = PureWhite,
                            containerColor = ShadowGrey,
                            labelColor = LightGrey
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedPlanId == plan.id,
                            selectedBorderColor = Color.Transparent,
                            borderColor = LightGrey.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // Error banner
        if (error != null) {
            Text(
                text = "⚠ $error",
                color = SubtleRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Content area
        when {
            // First open: nothing cached yet — show a prominent centred spinner
            isLoading && workouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CrayolaBlue,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading your workouts…",
                            color = LightGrey,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Empty after load
            filteredWorkouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = LightGrey.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (selectedPlanId != null) "No workouts for this plan" else "No workouts yet",
                            color = LightGrey.copy(alpha = 0.5f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // List — with a slim refresh bar when reloading existing data
            else -> {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = CrayolaBlue,
                        trackColor = ShadowGrey
                    )
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredWorkouts, key = { it.id }) { workout ->
                        val hasPhoto = workout.measurementId != null &&
                            photoUrlByMeasurementId.containsKey(workout.measurementId)
                        WorkoutHistoryCard(
                            workout = workout,
                            hasPhoto = hasPhoto,
                            onClick = { onWorkoutClick(workout) }
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Workout card in list
// ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutResponseDto,
    hasPhoto: Boolean,
    onClick: () -> Unit
) {
    val completedSets = workout.sets.size
    val prCount = workout.sets.count { it.isPR }
    val totalVolumeKg = workout.sets.sumOf { (it.weightKg ?: 0.0) * (it.reps ?: 0) }
    val exerciseCount = workout.sets.map { resolveSetExerciseName(it) }.distinct().count()
    val durationMin = workoutDurationMinutes(workout.startTime, workout.endTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(ShadowGrey.copy(alpha = 0.9f), ShadowGrey.copy(alpha = 0.7f))
                )
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.routineName ?: "Free Workout",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formatWorkoutDate(workout.startTime),
                    color = LightGrey,
                    fontSize = 12.sp
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasPhoto) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrayolaBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.PhotoCamera,
                            contentDescription = "Has progress photo",
                            tint = CrayolaBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                if (prCount > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFD740).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD740),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$prCount PR${if (prCount > 1) "s" else ""}",
                            color = Color(0xFFFFD740),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatPill(icon = Icons.Outlined.Timer, text = if (durationMin != null) "${durationMin}m" else "—")
            StatPill(icon = Icons.Outlined.FitnessCenter, text = formatVolumeShort(totalVolumeKg))
            StatPill(icon = Icons.Outlined.Repeat, text = "$completedSets sets")
            StatPill(icon = Icons.Outlined.Speed, text = "$exerciseCount ex")
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = LightGrey.copy(alpha = 0.6f), modifier = Modifier.size(13.dp))
        Text(text, color = LightGrey, fontSize = 12.sp)
    }
}

// ──────────────────────────────────────────────────────────────
// Standalone reusable: wraps detail in a ModalBottomSheet for use from other screens
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailBottomSheet(
    workout: WorkoutResponseDto,
    effortMetric: String,
    photoUrlByMeasurementId: Map<String, String> = emptyMap(),
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = com.example.kaizenfrontend.core.ui.theme.Onyx,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = com.example.kaizenfrontend.core.ui.theme.LightGrey.copy(alpha = 0.4f)) }
    ) {
        WorkoutDetailContent(
            workout = workout,
            effortMetric = effortMetric,
            photoUrlByMeasurementId = photoUrlByMeasurementId,
            onBack = onDismiss
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Workout detail
// ──────────────────────────────────────────────────────────────

@Composable
internal fun WorkoutDetailContent(
    workout: WorkoutResponseDto,
    effortMetric: String,
    photoUrlByMeasurementId: Map<String, String>,
    onBack: () -> Unit
) {
    val totalVolumeKg = workout.sets.sumOf { (it.weightKg ?: 0.0) * (it.reps ?: 0) }
    val prCount = workout.sets.count { it.isPR }
    val exerciseGroups = workout.sets.groupBy { resolveSetExerciseName(it) }
    val durationMin = workoutDurationMinutes(workout.startTime, workout.endTime)
    // Prefer the URL embedded in the workout DTO (returned by the detail endpoint).
    // Fall back to the measurements map for list-based contexts where progressPhotoUrl is absent.
    val photoUrl = workout.progressPhotoUrl?.takeIf { it.isNotBlank() }
        ?: workout.measurementId?.let { photoUrlByMeasurementId[it] }
    var showPhotoDialog by remember { mutableStateOf(false) }

    if (showPhotoDialog && !photoUrl.isNullOrBlank()) {
        FullScreenPhotoDialog(
            photoUrl = photoUrl,
            onDismiss = { showPhotoDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back + header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightGrey, modifier = Modifier.size(20.dp))
                Text("History", color = LightGrey, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = workout.routineName ?: "Free Workout",
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(formatWorkoutDate(workout.startTime))
                    if (durationMin != null) append(" • ${durationMin}m")
                },
                color = LightGrey,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Stats row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShadowGrey.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailStat(label = "Volume", value = formatVolumeShort(totalVolumeKg))
                DetailStat(label = "Sets", value = "${workout.sets.size}")
                DetailStat(label = "Exercises", value = "${exerciseGroups.size}")
                if (prCount > 0) {
                    DetailStat(
                        label = "PRs",
                        value = "🏆 $prCount",
                        valueColor = Color(0xFFFFD740)
                    )
                }
            }
        }

        // Exercise groups
        item {
            Text(
                text = "EXERCISES",
                color = LightGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }

        exerciseGroups.forEach { (exerciseName, sets) ->
            item(key = "exercise_$exerciseName") {
                ExerciseSetBlock(exerciseName = exerciseName, sets = sets, effortMetric = effortMetric)
            }
        }

        // Progress photo button
        if (!photoUrl.isNullOrBlank()) {
            item {
                OutlinedButton(
                    onClick = { showPhotoDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrayolaBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrayolaBlue.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("View Progress Photo", fontSize = 14.sp)
                }
            }
        }

        // Notes
        if (!workout.notes.isNullOrBlank()) {
            item {
                NotesBlock(notes = workout.notes)
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String, valueColor: Color = PureWhite) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = LightGrey, fontSize = 11.sp)
    }
}

@Composable
private fun ExerciseSetBlock(
    exerciseName: String,
    sets: List<WorkoutSetResponseDto>,
    effortMetric: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShadowGrey.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = exerciseName,
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        HorizontalDivider(color = LightGrey.copy(alpha = 0.1f))
        sets.forEach { set ->
            SetRow(set = set, effortMetric = effortMetric)
        }
    }
}

@Composable
private fun SetRow(set: WorkoutSetResponseDto, effortMetric: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Set number
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ShadowGrey),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                color = LightGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Weight × reps
        Text(
            text = buildString {
                val w = set.weightKg
                val r = set.reps
                if (w != null && w > 0) append("${formatWeight(w)} kg")
                if (w != null && w > 0 && r != null) append(" × ")
                if (r != null) append("$r reps")
                if (w == null && r == null) append("—")
            },
            color = PureWhite,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        )

        // RPE / RIR value
        val showEffort = effortMetric != "NONE" && set.rpe != null
        if (showEffort) {
            Text(
                text = "$effortMetric ${set.rpe}",
                color = LightGrey,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Type badge + PR badge
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (set.type != "NORMAL") {
                Text(
                    text = set.type.replace("_", " "),
                    color = CrayolaBlue.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CrayolaBlue.copy(alpha = 0.12f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            if (set.isPR) {
                Text(
                    text = "PR",
                    color = Color(0xFFFFD740),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFD740).copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FullScreenPhotoDialog(photoUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Progress photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = PureWhite)
            }
        }
    }
}

@Composable
private fun NotesBlock(notes: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShadowGrey.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.Notes, contentDescription = null, tint = LightGrey, modifier = Modifier.size(15.dp))
            Text("Notes", color = LightGrey, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = notes,
            color = PureWhite.copy(alpha = 0.85f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────────

private fun formatWorkoutDate(isoString: String?): String {
    if (isoString == null) return "Unknown date"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoString) ?: return "Unknown date"
        val formatter = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) { "Unknown date" }
}

private fun workoutDurationMinutes(startTime: String?, endTime: String?): Int? {
    if (startTime == null || endTime == null) return null
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val start = parser.parse(startTime) ?: return null
        val end = parser.parse(endTime) ?: return null
        val diffMs = end.time - start.time
        (diffMs / 60_000).toInt().takeIf { it >= 0 }
    } catch (e: Exception) { null }
}

private fun formatVolumeShort(kg: Double): String {
    return if (kg >= 1000) "%.1fk kg".format(kg / 1000) else "%.0f kg".format(kg)
}

private fun formatWeight(kg: Double): String {
    return if (kg == kg.toLong().toDouble()) kg.toLong().toString() else "%.1f".format(kg)
}

private fun resolveSetExerciseName(set: WorkoutSetResponseDto): String {
    if (!set.exerciseName.isNullOrBlank()) return set.exerciseName
    if (!set.builtinExerciseKey.isNullOrBlank()) {
        return BuiltinExerciseCatalog.resolveExerciseName(set.builtinExerciseKey)
    }
    return "Unknown"
}
