package com.example.kaizenfrontend.feature.workouts.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.formatElapsed
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import com.example.kaizenfrontend.feature.dashboard.presentation.PhotoUploadStatus
import com.example.kaizenfrontend.feature.dashboard.presentation.WorkoutSaveStatus
import com.example.kaizenfrontend.feature.workouts.domain.model.ActiveWorkoutState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ──────────────────────────────────────────────────────────────
// Workout Summary Bottom Sheet — shown after finishing a workout
// ──────────────────────────────────────────────────────────────

// Confetti particle colors
private val ConfettiColors = listOf(
    Color(0xFF2979FF),  // CrayolaBlue
    Color(0xFF00E676),  // MalachiteGreen
    Color(0xFFFFD740),  // PrGold
    Color(0xFFFF6D00),  // Vivid Orange
    Color(0xFFAA00FF),  // Purple
    Color(0xFF00B0FF),  // Light Blue
)

/**
 * Data class to hold a single confetti particle's state.
 */
private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val alpha: Float
)

/**
 * Full-screen-style bottom sheet showing a workout summary with
 * celebration animations, stat cards, and save status.
 *
 * @param workoutSnapshot  The frozen state of the just-finished workout.
 * @param saveStatusFlow   Observable save status from the ViewModel.
 * @param weightUnit       User's weight unit string (kg / lbs).
 * @param onDismiss        Called when the user taps "Done".
 * @param onRetry          Called when the user taps "Retry" on failure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryBottomSheet(
    workoutSnapshot: ActiveWorkoutState,
    saveStatusFlow: StateFlow<WorkoutSaveStatus>,
    photoUploadStatusFlow: StateFlow<PhotoUploadStatus>,
    weightUnit: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onPhotoSelected: (Uri) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val saveStatus by saveStatusFlow.collectAsState()
    val photoUploadStatus by photoUploadStatusFlow.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey.copy(alpha = 0.4f)) }
    ) {
        WorkoutSummaryContent(
            snapshot = workoutSnapshot,
            saveStatus = saveStatus,
            photoUploadStatus = photoUploadStatus,
            weightUnit = weightUnit,
            onDismiss = onDismiss,
            onRetry = onRetry,
            onSave = onSave,
            onPhotoSelected = onPhotoSelected
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Content
// ──────────────────────────────────────────────────────────────

@Composable
private fun WorkoutSummaryContent(
    snapshot: ActiveWorkoutState,
    saveStatus: WorkoutSaveStatus,
    photoUploadStatus: PhotoUploadStatus,
    weightUnit: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onPhotoSelected: (Uri) -> Unit
) {
    // ── Compute summary stats ─────────────────────────────────
    val totalSets = snapshot.exercises.sumOf { it.sets.size }
    val completedSets = snapshot.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
    val exerciseCount = snapshot.exercises.size
    val totalVolume = snapshot.exercises.sumOf { ex ->
        ex.sets.filter { it.isCompleted }.sumOf { set ->
            val w = set.weight.toDoubleOrNull() ?: 0.0
            val r = set.reps.toIntOrNull() ?: 0
            (w * r)
        }
    }

    // ── Progress photo state ──────────────────────────────────
    val context = LocalContext.current
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoSizeError by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val fileSize = try {
                    context.contentResolver.openFileDescriptor(it, "r")?.use { fd -> fd.statSize } ?: 0L
                } catch (e: Exception) { 0L }
                if (fileSize > 10 * 1024 * 1024L) {
                    photoSizeError = true
                } else {
                    photoSizeError = false
                    selectedPhotoUri = it
                    onPhotoSelected(it)
                }
            }
        }
    )

    // ── Staggered entrance animations ─────────────────────────
    val heroAlpha = remember { Animatable(0f) }
    val statsAlpha = remember { Animatable(0f) }
    val notesAlpha = remember { Animatable(0f) }
    val ctaAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        delay(100)
        statsAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        delay(100)
        notesAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        delay(100)
        ctaAlpha.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── 1. Hero Section ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .graphicsLayer { alpha = heroAlpha.value },
            contentAlignment = Alignment.Center
        ) {
            // Confetti canvas behind the duration
            ConfettiCanvas(
                modifier = Modifier.fillMaxSize()
            )

            // Central content over confetti
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow circle behind the icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CrayolaBlue.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = CrayolaBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Workout Complete!",
                    color = PureWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = snapshot.routineName,
                    color = LightGrey,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duration hero number
                Text(
                    text = formatElapsed(snapshot.elapsedTimeGlobal),
                    color = CrayolaBlue,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── 2. Stats Grid ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = statsAlpha.value
                    translationY = (1f - statsAlpha.value) * 40f
                },
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    icon = Icons.Outlined.FitnessCenter,
                    label = "Volume",
                    value = formatVolume(totalVolume, weightUnit),
                    accentColor = CrayolaBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    icon = Icons.Outlined.Repeat,
                    label = "Sets",
                    value = "$completedSets / $totalSets",
                    accentColor = MalachiteGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryStatCard(
                    icon = Icons.Default.Timer,
                    label = "Exercises",
                    value = "$exerciseCount",
                    accentColor = Color(0xFFFFD740), // PrGold
                    modifier = Modifier.weight(1f)
                )
                SummaryStatCard(
                    icon = Icons.Outlined.Speed,
                    label = "Avg Volume",
                    value = if (exerciseCount > 0) formatVolume(totalVolume / exerciseCount, weightUnit) else "—",
                    accentColor = Color(0xFFAA00FF), // Purple
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 3. Notes Preview (if any) ────────────────────────────
        if (snapshot.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = notesAlpha.value
                        translationY = (1f - notesAlpha.value) * 30f
                    }
                    .background(
                        ShadowGrey.copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notes,
                        contentDescription = null,
                        tint = LightGrey,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Notes",
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = snapshot.notes,
                    color = PureWhite.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── 4. Progress Photo ────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))

        ProgressPhotoSection(
            photoUri = selectedPhotoUri,
            uploadStatus = photoUploadStatus,
            onPickPhoto = { photoPickerLauncher.launch("image/*") },
            onRemovePhoto = { selectedPhotoUri = null; photoSizeError = false },
            sizeError = photoSizeError,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = ctaAlpha.value
                    translationY = (1f - ctaAlpha.value) * 20f
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── 5. Save Status Indicator ─────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = ctaAlpha.value
                    translationY = (1f - ctaAlpha.value) * 20f
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SaveStatusIndicator(saveStatus = saveStatus)

            Spacer(modifier = Modifier.height(20.dp))

            // ── 6. CTA Button ────────────────────────────────────
            when (saveStatus) {
                is WorkoutSaveStatus.Error -> {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubtleRed,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retry",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Allow dismissing even on error
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShadowGrey,
                            contentColor = LightGrey
                        )
                    ) {
                        Text(
                            text = "Dismiss",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            when (saveStatus) {
                                is WorkoutSaveStatus.Idle -> onSave()
                                is WorkoutSaveStatus.Success -> onDismiss()
                                else -> {}
                            }
                        },
                        enabled = saveStatus !is WorkoutSaveStatus.Saving && photoUploadStatus !is PhotoUploadStatus.Uploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (saveStatus is WorkoutSaveStatus.Success) MalachiteGreen else CrayolaBlue,
                            contentColor = PureWhite,
                            disabledContainerColor = CrayolaBlue.copy(alpha = 0.4f),
                            disabledContentColor = PureWhite.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = when (saveStatus) {
                                is WorkoutSaveStatus.Success -> "Done"
                                else -> "Save Workout"
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Progress Photo Section
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProgressPhotoSection(
    photoUri: Uri?,
    uploadStatus: PhotoUploadStatus,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    sizeError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        bitmap = photoUri?.let { uri ->
            withContext(Dispatchers.IO) {
                try {
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, opts)
                    }
                } catch (e: Exception) { null }
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = LightGrey,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Progress Photo",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "optional",
                color = LightGrey.copy(alpha = 0.4f),
                fontSize = 11.sp
            )
        }

        if (sizeError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Image must be under 10 MB. Please choose a smaller file.",
                color = SubtleRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (photoUri == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ShadowGrey.copy(alpha = 0.5f))
                    .clickable { onPickPhoto() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = LightGrey.copy(alpha = 0.5f),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Tap to add progress photo",
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShadowGrey.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                val bmp = bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Progress photo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShadowGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LightGrey,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Upload status
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    when (uploadStatus) {
                        is PhotoUploadStatus.Uploading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = CrayolaBlue,
                                    strokeWidth = 2.dp
                                )
                                Text("Uploading…", color = CrayolaBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        is PhotoUploadStatus.Success -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MalachiteGreen, modifier = Modifier.size(14.dp))
                                Text("Photo saved!", color = MalachiteGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        is PhotoUploadStatus.Error -> {
                            Text(
                                text = "Upload failed — tap to retry",
                                color = SubtleRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onPickPhoto() }
                            )
                        }
                        else -> {
                            Text("Photo selected", color = LightGrey.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                    }
                }

                // Remove button (hidden while uploading)
                if (uploadStatus !is PhotoUploadStatus.Uploading) {
                    IconButton(
                        onClick = onRemovePhoto,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove photo",
                            tint = LightGrey.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Stat Card — glassmorphic design
// ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ShadowGrey.copy(alpha = 0.85f),
                        ShadowGrey.copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Accent dot + label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Text(
                text = label,
                color = LightGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Icon + value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Save Status Indicator
// ──────────────────────────────────────────────────────────────

@Composable
private fun SaveStatusIndicator(saveStatus: WorkoutSaveStatus) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(
                color = when (saveStatus) {
                    is WorkoutSaveStatus.Saving -> CrayolaBlue.copy(alpha = 0.1f)
                    is WorkoutSaveStatus.Success -> MalachiteGreen.copy(alpha = 0.1f)
                    is WorkoutSaveStatus.Error -> SubtleRed.copy(alpha = 0.1f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        when (saveStatus) {
            is WorkoutSaveStatus.Saving -> {
                CircularProgressIndicator(
                    color = CrayolaBlue,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Saving workout…",
                    color = CrayolaBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is WorkoutSaveStatus.Success -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MalachiteGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Workout saved!",
                    color = MalachiteGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            is WorkoutSaveStatus.Error -> {
                Text(
                    text = "⚠️ Failed to save: ${saveStatus.message}",
                    color = SubtleRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            else -> { /* Idle — nothing to show */ }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Confetti Canvas Animation
// ──────────────────────────────────────────────────────────────

@Composable
private fun ConfettiCanvas(modifier: Modifier = Modifier) {
    // Generate particles once and animate them via an infinite transition
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f, // Start above the canvas
                size = Random.nextFloat() * 6f + 3f,
                color = ConfettiColors.random(),
                velocityX = (Random.nextFloat() - 0.5f) * 0.003f,
                velocityY = Random.nextFloat() * 0.004f + 0.002f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 4f,
                alpha = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            // Animate position based on progress
            val currentY = ((p.y + progress * (1f + p.velocityY * 600f)) % 1.4f)
            val currentX = p.x + sin(progress * 6.28f + p.rotation) * 0.04f

            // Only draw when particle is within visible area
            if (currentY in -0.1f..1.1f) {
                val px = currentX * w
                val py = currentY * h
                val fadeAlpha = p.alpha * (1f - (currentY.coerceIn(0f, 1f) * 0.5f))

                drawConfettiPiece(
                    center = Offset(px, py),
                    size = p.size,
                    color = p.color.copy(alpha = fadeAlpha),
                    rotation = p.rotation + progress * p.rotationSpeed * 360f
                )
            }
        }
    }
}

private fun DrawScope.drawConfettiPiece(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    val rad = Math.toRadians(rotation.toDouble())
    val halfW = size * 1.5f
    val halfH = size * 0.6f

    // Draw a simple rotated rectangle using lines for performance
    val cos = cos(rad).toFloat()
    val sin = sin(rad).toFloat()

    val p1 = Offset(center.x - halfW * cos, center.y - halfW * sin)
    val p2 = Offset(center.x + halfW * cos, center.y + halfW * sin)

    drawLine(
        color = color,
        start = p1,
        end = p2,
        strokeWidth = halfH * 2f,
        cap = StrokeCap.Round
    )
}

// ──────────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────────

private fun formatVolume(volume: Double, unit: String): String {
    return if (volume >= 1000) {
        val k = volume / 1000.0
        "%.1fk %s".format(k, unit)
    } else {
        "%.0f %s".format(volume, unit)
    }
}
