package com.example.kaizenfrontend.feature.workouts.presentation.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutInputSanitizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SheetValue


// Enum for the 3-card schedule selector

private enum class ScheduleCard { WEEKLY, INTERVAL, CYCLE }

private fun ScheduleCard.toPlanIntervalType() = when (this) {
    ScheduleCard.WEEKLY   -> PlanIntervalType.CYCLE
    ScheduleCard.INTERVAL -> PlanIntervalType.FREQUENCY
    ScheduleCard.CYCLE    -> PlanIntervalType.CYCLE
}

private fun ScheduleCard.toCycleMode() = when (this) {
    ScheduleCard.WEEKLY -> CycleMode.WEEKLY
    ScheduleCard.CYCLE  -> CycleMode.CUSTOM
    else                -> CycleMode.WEEKLY
}

// Root composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, startingDate: String, interval: String?, cycleLength: Int?) -> Unit
) {
    val context               = LocalContext.current
    var currentStep           by remember { mutableIntStateOf(1) }
    var name                  by remember { mutableStateOf("") }
    var description           by remember { mutableStateOf("") }
    var showInlineErrors      by remember { mutableStateOf(false) }
    var scheduleCard          by remember { mutableStateOf(ScheduleCard.WEEKLY) }
    var customCycleLengthDays by remember { mutableIntStateOf(8) }
    var showDiscardDialog     by remember { mutableStateOf(false) }
    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var startingDate by remember { mutableStateOf(today) }
    val startingDateCalendar = remember(startingDate) {
        runCatching {
            Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startingDate) ?: Date()
            }
        }.getOrElse { Calendar.getInstance() }
    }
    val hasDirtyState = name.isNotBlank() || description.isNotBlank() || startingDate != today
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            if (targetValue == SheetValue.Hidden && hasDirtyState) {
                showDiscardDialog = true
                false   // block the swipe
            } else { true }
        }
    )

    // Tooltip state - one sheet, different content keyed by which header was tapped
    var tooltipText by remember { mutableStateOf("") }
    var showTooltip by remember { mutableStateOf(false) }

    val canContinue = when (currentStep) {
        1    -> name.isNotBlank()
        2    -> startingDate.isNotBlank()
        else -> true
    }
    val canFinish = name.isNotBlank() && startingDate.isNotBlank()

    LaunchedEffect(currentStep) { showInlineErrors = false }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = ShadowGrey,
            title = { Text(stringResource(R.string.workouts_discard_dialog_title),
                color = Color.White, fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.workouts_discard_dialog_message),
                color = LightGrey, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onDismiss() }) {
                    Text(stringResource(R.string.workouts_discard_confirm),
                        color = SubtleRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.workouts_discard_keep_editing),
                        color = CrayolaBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showTooltip && tooltipText.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { showTooltip = false },
            containerColor = ShadowGrey,
            shape = RoundedCornerShape(16.dp),
            text = {
                Text(
                    text = tooltipText,
                    color = LightGrey,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showTooltip = false }) {
                    Text(
                        text = stringResource(R.string.workouts_tooltip_got_it),
                        color = CrayolaBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = { if (hasDirtyState) showDiscardDialog = true else onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        dragHandle = {
            Box(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                .size(width = 48.dp, height = 5.dp)
                .clip(RoundedCornerShape(50))
                .background(LightGrey.copy(alpha = 0.6f)))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.CenterStart) {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep -= 1 else onDismiss()
                    }) {
                        Icon(
                            imageVector = if (currentStep > 1) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.workouts_create_plan),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentStep / 2",
                        color = LightGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.CenterEnd) {
                    if (currentStep == 2) {
                        TextButton(
                            onClick = {
                                if (canFinish) {
                                    val intervalConfig = buildIntervalConfig(scheduleCard, customCycleLengthDays)
                                    onCreate(
                                        name, description, startingDate,
                                        intervalConfig.toBackendValue(),
                                        intervalConfig.toBackendCycleLength()
                                    )
                                } else {
                                    showInlineErrors = true
                                }
                            },
                            enabled = canFinish
                        ) {
                            Text(
                                text = stringResource(R.string.workouts_finish),
                                color = if (canFinish) CrayolaBlue else LightGrey,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally { if (forward) it else -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { if (forward) -it else it } + fadeOut())
                },
                label = "CreatePlanStep"
            ) { step ->
                when (step) {

                    // Step 1: Name + Description
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = WorkoutInputSanitizer.normalizeTitleInput(it)
                                },
                                singleLine = true,
                                isError = showInlineErrors && name.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors(),
                                label = {
                                    Text(
                                        text = stringResource(R.string.workouts_name),
                                        color = LightGrey
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.workouts_plan_name_eg),
                                        color = LightGrey.copy(alpha = 0.5f)
                                    )
                                }
                            )

                            if (showInlineErrors && name.isBlank()) {
                                Text(
                                    text = stringResource(R.string.workouts_name_required),
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    description = WorkoutInputSanitizer.normalizeDescriptionInput(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors(),
                                label = {
                                    Text(
                                        text = stringResource(R.string.workouts_description_optional),
                                        color = LightGrey
                                    )
                                }
                            )
                        }
                    }

                    // Step 2: Date + Schedule
                    2 -> {
                        val scheduleTooltip = stringResource(R.string.workouts_tooltip_schedule_mode)
                        val cycleTooltip    = stringResource(R.string.workouts_tooltip_cycle_setup)

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                            OutlinedTextField(
                                value = startingDate,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                isError = showInlineErrors && startingDate.isBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                startingDate = String.format(
                                                    Locale.getDefault(),
                                                    "%04d-%02d-%02d",
                                                    year,
                                                    month + 1,
                                                    dayOfMonth
                                                )
                                            },
                                            startingDateCalendar.get(Calendar.YEAR),
                                            startingDateCalendar.get(Calendar.MONTH),
                                            startingDateCalendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors(),
                                label = {
                                    Text(
                                        text = stringResource(R.string.workouts_starting_date),
                                        color = LightGrey
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = "yyyy-MM-dd",
                                        color = LightGrey.copy(alpha = 0.5f)
                                    )
                                }
                            )

                            if (showInlineErrors && startingDate.isBlank()) {
                                Text(
                                    text = stringResource(R.string.workouts_starting_date_required),
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }

                            // Schedule section header
                            SectionHeaderWithInfo(
                                title = stringResource(R.string.workouts_plan_schedule_mode),
                                onInfoClick = {
                                    tooltipText = scheduleTooltip
                                    showTooltip = true
                                }
                            )

                            // 3-card segmented selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScheduleSegmentCard(
                                    modifier  = Modifier.weight(1f),
                                    title     = stringResource(R.string.workouts_schedule_weekly),
                                    subtitle  = stringResource(R.string.workouts_schedule_weekly_sub),
                                    selected  = scheduleCard == ScheduleCard.WEEKLY,
                                    onClick   = { scheduleCard = ScheduleCard.WEEKLY }
                                )
                                ScheduleSegmentCard(
                                    modifier  = Modifier.weight(1f),
                                    title     = stringResource(R.string.workouts_schedule_interval),
                                    subtitle  = stringResource(R.string.workouts_schedule_interval_sub),
                                    selected  = scheduleCard == ScheduleCard.INTERVAL,
                                    onClick   = { scheduleCard = ScheduleCard.INTERVAL }
                                )
                                ScheduleSegmentCard(
                                    modifier  = Modifier.weight(1f),
                                    title     = stringResource(R.string.workouts_schedule_cycle),
                                    subtitle  = stringResource(R.string.workouts_schedule_cycle_sub),
                                    selected  = scheduleCard == ScheduleCard.CYCLE,
                                    onClick   = { scheduleCard = ScheduleCard.CYCLE }
                                )
                            }

                            // Cycle length stepper (only for CYCLE card)
                            AnimatedVisibility(
                                visible = scheduleCard == ScheduleCard.CYCLE,
                                enter   = expandVertically() + fadeIn(),
                                exit    = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionHeaderWithInfo(
                                        title = stringResource(R.string.workouts_cycle_length),
                                        onInfoClick = {
                                            tooltipText = cycleTooltip
                                            showTooltip = true
                                        }
                                    )
                                    CycleLengthSelector(
                                        value    = customCycleLengthDays,
                                        onChange = { customCycleLengthDays = it.coerceAtLeast(2) }
                                    )
                                }
                            }

                            // Contextual info text (slides on selection)
                            AnimatedContent(
                                targetState = scheduleCard,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "ScheduleInfoText"
                            ) { card ->
                                SelectedScheduleInfoText(
                                    scheduleCard          = card,
                                    customCycleLengthDays = customCycleLengthDays
                                )
                            }
                        }
                    }

                    else -> Unit
                }
            }

            // Next button (step 1 only)
            if (currentStep < 2) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        if (canContinue) currentStep += 1 else showInlineErrors = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor         = CrayolaBlue,
                        disabledContainerColor = ShadowGrey
                    )
                ) {
                    Text(
                        text       = stringResource(R.string.workouts_next),
                        color      = Color.White,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

// Helper: build PlanIntervalConfig from the selected card

private fun buildIntervalConfig(card: ScheduleCard, cycleDays: Int): PlanIntervalConfig =
    when (card) {
        ScheduleCard.WEEKLY    -> PlanIntervalConfig.defaultCycleWeekly()
        ScheduleCard.INTERVAL  -> PlanIntervalConfig.defaultFrequency()
        ScheduleCard.CYCLE     -> PlanIntervalConfig(
            type            = PlanIntervalType.CYCLE,
            cycleMode       = CycleMode.CUSTOM,
            cycleLengthDays = cycleDays.coerceAtLeast(2)
        )
    }

// Section header with inline Info icon

@Composable
private fun SectionHeaderWithInfo(
    title: String,
    onInfoClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text       = title,
            color      = Color.White,
            fontSize   = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            onClick  = onInfoClick,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Info,
                contentDescription = title,
                tint               = LightGrey.copy(alpha = 0.7f),
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

// 3-card segmented schedule selector

@Composable
private fun ScheduleSegmentCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = if (selected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
        border   = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) CrayolaBlue else Color.White.copy(alpha = 0.06f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = title,
                color      = if (selected) CrayolaBlue else Color.White,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center,
                maxLines   = 1
            )
            Text(
                text      = subtitle,
                color     = LightGrey.copy(alpha = if (selected) 0.9f else 0.6f),
                fontSize  = 10.sp,
                textAlign = TextAlign.Center,
                maxLines  = 2,
                lineHeight = 13.sp
            )
        }
    }
}

// Cycle length stepper

@Composable
private fun CycleLengthSelector(value: Int, onChange: (Int) -> Unit) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = ShadowGrey,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = stringResource(R.string.workouts_cycle_length),
                    color      = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp
                )
                Text(
                    text    = stringResource(R.string.workouts_days_count, value),
                    color   = CrayolaBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = { onChange((value - 1).coerceAtLeast(2)) }) {
                Icon(
                    imageVector        = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.workouts_decrease_cycle_length_cd),
                    tint               = LightGrey
                )
            }
            IconButton(onClick = { onChange(value + 1) }) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = stringResource(R.string.workouts_increase_cycle_length_cd),
                    tint               = CrayolaBlue
                )
            }
        }
    }
}

// Contextual info text (replaces the verbose SelectedScheduleInfoText)

@Composable
private fun SelectedScheduleInfoText(
    scheduleCard: ScheduleCard,
    customCycleLengthDays: Int
) {
    val (label, detail, example) = when (scheduleCard) {
        ScheduleCard.WEEKLY -> Triple(
            stringResource(R.string.workouts_selected_weekly_cycle),
            stringResource(R.string.workouts_pick_weekdays_help),
            stringResource(R.string.workouts_example_weekly)
        )
        ScheduleCard.INTERVAL -> Triple(
            stringResource(R.string.workouts_selected_frequency),
            stringResource(R.string.workouts_frequency_help),
            stringResource(R.string.workouts_example_frequency)
        )
        ScheduleCard.CYCLE -> {
            val mid = ((customCycleLengthDays + 1) / 2).coerceAtLeast(1)
            Triple(
                stringResource(R.string.workouts_selected_custom_cycle),
                stringResource(R.string.workouts_custom_cycle_help, customCycleLengthDays),
                stringResource(R.string.workouts_example_custom_cycle, mid, customCycleLengthDays, customCycleLengthDays)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label,   color = CrayolaBlue,  fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = detail,  color = LightGrey,    fontSize = 12.sp, lineHeight = 17.sp)
        Text(text = example, color = LightGrey,    fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// Shared text field colours

@Composable
private fun planFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor      = Color.White,
    unfocusedTextColor    = Color.White,
    focusedBorderColor    = CrayolaBlue,
    unfocusedBorderColor  = Color.Transparent,
    focusedContainerColor = ShadowGrey,
    unfocusedContainerColor = ShadowGrey,
    cursorColor           = CrayolaBlue,
    focusedLabelColor     = CrayolaBlue,
    unfocusedLabelColor   = LightGrey,
    errorBorderColor      = SubtleRed,
    errorLabelColor       = SubtleRed
)
