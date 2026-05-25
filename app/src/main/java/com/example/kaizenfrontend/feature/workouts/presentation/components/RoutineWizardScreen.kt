package com.example.kaizenfrontend.feature.workouts.presentation.components

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardEvent
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardUiState
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineWizardScreen(
    viewModel: RoutineWizardViewModel,
    onCreateRoutine: (planId: String?, name: String, description: String,
                      schedulingValue: String, startingDate: String,
                      selectedExercises: List<RoutineExercise>) -> Unit = { _, _, _, _, _, _ -> },
    onWizardClosed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInlineErrors by remember { mutableStateOf(false) }
    var showExerciseCatalog by remember { mutableStateOf(false) }
    var tooltipText by remember { mutableStateOf("") }
    var showTooltip by remember { mutableStateOf(false) }
    val scheduleTooltip = stringResource(R.string.workouts_tooltip_routine_schedule)
    val weeklyDaysTooltip = stringResource(R.string.workouts_tooltip_weekly_days)

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is RoutineWizardEvent.Success) onWizardClosed()
        }
    }
    LaunchedEffect(uiState.currentStep) { showInlineErrors = false }

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

    Column(modifier = Modifier.fillMaxWidth().background(Onyx)) {
        RoutineWizardTopBar(
            currentStep = uiState.currentStep,
            canFinish = uiState.selectedExercises.isNotEmpty(),
            onCloseOrBack = {
                if (uiState.currentStep == 1) onWizardClosed() else viewModel.previousStep()
            },
            onFinish = {
                if (isCurrentStepValid(uiState)) {
                    val args = buildRoutineCreationArgs(uiState)
                    onCreateRoutine(args.planId, args.name, args.description,
                        args.schedulingValue, args.startingDate, args.selectedExercises)
                    viewModel.saveRoutine()
                } else { showInlineErrors = true }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    val fwd = targetState > initialState
                    (slideInHorizontally { if (fwd) it else -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { if (fwd) -it else it } + fadeOut())
                },
                label = "WizardStep"
            ) { step ->
                when (step) {
                    1 -> WizardStep1Meta(
                        name = uiState.name, description = uiState.description,
                        plans = uiState.availablePlans, selectedPlanId = uiState.selectedPlanId,
                        onNameChange = viewModel::updateName,
                        onDescriptionChange = viewModel::updateDescription,
                        onPlanSelected = viewModel::selectPlan,
                        showPlanError = showInlineErrors && uiState.availablePlans.isNotEmpty() &&
                                uiState.selectedPlanId == null,
                        showNameError = showInlineErrors && uiState.name.isBlank()
                    )
                    2 -> WizardStep2Schedule(
                        planInterval = uiState.selectedPlanInterval,
                        existingPlanRoutines = uiState.availableRoutines
                            .filter { it.planId == uiState.selectedPlanId },
                        selectedWeekDays = uiState.selectedWeekDays,
                        selectedCycleDays = uiState.selectedCycleDays,
                        restDaysBetweenWorkouts = uiState.restDaysBetweenWorkouts,
                        onWeekDayToggle = viewModel::toggleWeekDay,
                        onCycleDayToggle = viewModel::toggleCycleDay,
                        onRestDaysChange = viewModel::updateRestDaysBetweenWorkouts,
                        showCycleSelectionError = showInlineErrors &&
                                uiState.selectedPlanInterval.type == PlanIntervalType.CYCLE &&
                                ((uiState.selectedPlanInterval.cycleMode == CycleMode.WEEKLY &&
                                        uiState.selectedWeekDays.isEmpty()) ||
                                        (uiState.selectedPlanInterval.cycleMode == CycleMode.CUSTOM &&
                                                uiState.selectedCycleDays.isEmpty())),
                        onScheduleInfoClick = { tooltipText = scheduleTooltip; showTooltip = true },
                        onWeeklyDaysInfoClick = { tooltipText = weeklyDaysTooltip; showTooltip = true }
                    )
                    else -> WizardStep3Exercises(
                        selectedExercises = uiState.selectedExercises,
                        onAddExerciseClick = { showExerciseCatalog = true },
                        onRemoveExercise = viewModel::removeExercise,
                        onUpdateExerciseSets = { id, sets -> viewModel.updateExerciseSets(id, sets) },
                        showEmptyError = showInlineErrors && uiState.selectedExercises.isEmpty()
                    )
                }
            }
            if (uiState.currentStep < 3) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (isCurrentStepValid(uiState)) viewModel.nextStep()
                        else showInlineErrors = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue)
                ) {
                    Text(text = stringResource(R.string.workouts_next),
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showExerciseCatalog) {
        ExerciseCatalogBottomSheet(
            onDismissRequest = { showExerciseCatalog = false },
            onExerciseSelected = { exercise ->
                viewModel.addExercise(exercise); showExerciseCatalog = false
            }
        )
    }
}

@Composable
private fun RoutineWizardTopBar(
    currentStep: Int, canFinish: Boolean,
    onCloseOrBack: () -> Unit, onFinish: () -> Unit
) {
    Surface(color = Onyx) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.CenterStart) {
                IconButton(onClick = onCloseOrBack) {
                    Icon(
                        imageVector = if (currentStep == 1) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = if (currentStep == 1)
                            stringResource(R.string.workouts_wizard_close_cd)
                        else stringResource(R.string.workouts_wizard_prev_cd),
                        tint = Color.White
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.workouts_create_routine),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "$currentStep / 3", color = LightGrey,
                    fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
            Box(modifier = Modifier.width(72.dp), contentAlignment = Alignment.CenterEnd) {
                if (currentStep == 3) {
                    TextButton(onClick = onFinish, enabled = canFinish) {
                        Text(text = stringResource(R.string.workouts_finish),
                            color = if (canFinish) CrayolaBlue else LightGrey,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStep1Meta(
    name: String, description: String, plans: List<TrainingPlan>,
    selectedPlanId: String?, onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit, onPlanSelected: (String) -> Unit,
    showPlanError: Boolean, showNameError: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = name, onValueChange = onNameChange, singleLine = true, isError = showNameError,
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
            colors = wizardTextFieldColors(),
            label = { Text(stringResource(R.string.workouts_name), color = LightGrey) },
            placeholder = { Text(stringResource(R.string.workouts_routine_name_eg),
                color = LightGrey.copy(alpha = 0.5f)) }
        )
        if (showNameError) {
            Text(stringResource(R.string.workouts_name_required), color = SubtleRed, fontSize = 12.sp)
        }
        OutlinedTextField(
            value = description, onValueChange = onDescriptionChange, minLines = 3,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp), colors = wizardTextFieldColors(),
            label = { Text(stringResource(R.string.workouts_description_optional), color = LightGrey) }
        )
        if (plans.isNotEmpty()) {
            PlanDropdownCard(plans = plans, selectedPlanId = selectedPlanId,
                onPlanSelected = onPlanSelected, showError = showPlanError)
        }
    }
}

@Composable
private fun PlanDropdownCard(
    plans: List<TrainingPlan>, selectedPlanId: String?,
    onPlanSelected: (String) -> Unit, showError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedPlan = plans.firstOrNull { it.id == selectedPlanId }
    val borderColor = when { showError -> SubtleRed; expanded -> CrayolaBlue; else -> Color.White.copy(0.08f) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(R.string.workouts_select_plan_label),
            color = if (showError) SubtleRed else LightGrey, fontSize = 12.sp, fontWeight = FontWeight.Medium)

        Surface(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(14.dp), color = ShadowGrey,
            border = BorderStroke(1.dp, borderColor)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedPlan?.name ?: stringResource(R.string.workouts_select_plan_label),
                        color = if (selectedPlan != null) Color.White else LightGrey.copy(0.6f),
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    selectedPlan?.interval?.takeIf { it.isNotBlank() }?.let {
                        Text(text = PlanIntervalConfig.fromBackend(it, selectedPlan.cycleLength)
                            .toDisplayLabel(), color = LightGrey, fontSize = 11.sp)
                    }
                }
                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.workouts_select_plan_cd),
                    tint = LightGrey, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(visible = expanded,
            enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                color = ShadowGrey, border = BorderStroke(1.dp, CrayolaBlue.copy(0.3f))) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    plans.forEach { plan ->
                        val isSelected = plan.id == selectedPlanId
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onPlanSelected(plan.id); expanded = false }
                                .background(if (isSelected) CrayolaBlue.copy(0.10f) else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plan.name, color = Color.White, fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                plan.interval?.takeIf { it.isNotBlank() }?.let {
                                    Text(PlanIntervalConfig.fromBackend(it, plan.cycleLength).toDisplayLabel(),
                                        color = LightGrey, fontSize = 11.sp)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null,
                                    tint = CrayolaBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
        if (showError) {
            Text(stringResource(R.string.workouts_select_plan_error), color = SubtleRed, fontSize = 12.sp)
        }
    }
}

@Composable
fun WizardStep2Schedule(
    planInterval: PlanIntervalConfig, existingPlanRoutines: List<Routine>,
    selectedWeekDays: Set<DayOfWeek>, selectedCycleDays: Set<Int>,
    restDaysBetweenWorkouts: Int, onWeekDayToggle: (DayOfWeek) -> Unit,
    onCycleDayToggle: (Int) -> Unit, onRestDaysChange: (Int) -> Unit,
    showCycleSelectionError: Boolean,
    onScheduleInfoClick: () -> Unit, onWeeklyDaysInfoClick: () -> Unit
) {
    val weeklyAssignments = remember(existingPlanRoutines) { buildWeeklyAssignments(existingPlanRoutines) }
    val cycleAssignments  = remember(existingPlanRoutines, planInterval.cycleLengthDays) {
        buildCycleAssignments(existingPlanRoutines, planInterval.cycleLengthDays)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WizardSectionHeader(title = stringResource(R.string.workouts_step2_schedule_title),
            onInfoClick = onScheduleInfoClick)

        when (planInterval.type) {
            PlanIntervalType.CYCLE -> {
                if (planInterval.cycleMode == CycleMode.WEEKLY) {
                    WeeklyDaysSelector(selectedDays = selectedWeekDays,
                        assignedRoutineNamesByDay = weeklyAssignments, onDayClick = onWeekDayToggle,
                        showError = showCycleSelectionError, onInfoClick = onWeeklyDaysInfoClick)
                } else {
                    CycleDaysSelector(cycleLengthDays = planInterval.cycleLengthDays,
                        selectedCycleDays = selectedCycleDays,
                        assignedRoutineNamesByCycleDay = cycleAssignments,
                        onDayClick = onCycleDayToggle, showError = showCycleSelectionError)
                }
            }
            PlanIntervalType.FREQUENCY -> {
                Text(stringResource(R.string.workouts_choose_rest_days_help),
                    color = LightGrey, fontSize = 12.sp, lineHeight = 17.sp)
                BasicCounterSelector(title = stringResource(R.string.workouts_rest_days),
                    value = restDaysBetweenWorkouts, suffix = stringResource(R.string.workouts_days),
                    onChange = onRestDaysChange)
                FrequencyRoutineAssignments(existingRoutines = existingPlanRoutines)
            }
        }

        RoutineSchedulePreviewCard(planInterval = planInterval, selectedWeekDays = selectedWeekDays,
            selectedCycleDays = selectedCycleDays, restDaysBetweenWorkouts = restDaysBetweenWorkouts)
    }
}

@Composable
private fun WizardSectionHeader(title: String, onInfoClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        IconButton(onClick = onInfoClick, modifier = Modifier.size(22.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = title,
                tint = LightGrey.copy(0.65f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun WeeklyDaysSelector(
    selectedDays: Set<DayOfWeek>, assignedRoutineNamesByDay: Map<DayOfWeek, List<String>>,
    onDayClick: (DayOfWeek) -> Unit, showError: Boolean, onInfoClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val dayItems = listOf(
        DayLabel(DayOfWeek.MONDAY, "L"), DayLabel(DayOfWeek.TUESDAY, "M"),
        DayLabel(DayOfWeek.WEDNESDAY, "X"), DayLabel(DayOfWeek.THURSDAY, "J"),
        DayLabel(DayOfWeek.FRIDAY, "V"), DayLabel(DayOfWeek.SATURDAY, "S"),
        DayLabel(DayOfWeek.SUNDAY, "D")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WizardSectionHeader(title = stringResource(R.string.workouts_select_days_label),
            onInfoClick = onInfoClick)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dayItems.forEach { item ->
                val isSel = selectedDays.contains(item.dayOfWeek)
                val label = formatAssignedRoutineNames(assignedRoutineNamesByDay[item.dayOfWeek].orEmpty())
                Column(modifier = Modifier.width(72.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.width(42.dp).clickable { onDayClick(item.dayOfWeek) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) CrayolaBlue.copy(0.18f) else ShadowGrey,
                        border = BorderStroke(1.dp, if (isSel) CrayolaBlue else Color.Transparent)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center) {
                            Text(item.label, color = if (isSel) CrayolaBlue else LightGrey,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (label.isNotBlank()) Text(label, color = LightGrey, fontSize = 10.sp, lineHeight = 12.sp)
                }
            }
        }
        HorizontalScrollHint(isVisible = scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue)
        if (showError) Text(stringResource(R.string.workouts_select_at_least_one_day),
            color = SubtleRed, fontSize = 12.sp)
    }
}

@Composable
fun CycleDaysSelector(
    cycleLengthDays: Int, selectedCycleDays: Set<Int>,
    assignedRoutineNamesByCycleDay: Map<Int, List<String>>,
    onDayClick: (Int) -> Unit, showError: Boolean
) {
    val scrollState = rememberScrollState()
    val days = (1..cycleLengthDays.coerceAtLeast(1)).toList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.workouts_select_cycle_days_label),
            color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { day ->
                val isSel = day in selectedCycleDays
                val label = formatAssignedRoutineNames(assignedRoutineNamesByCycleDay[day].orEmpty())
                Column(modifier = Modifier.width(72.dp), verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.fillMaxWidth().clickable { onDayClick(day) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSel) CrayolaBlue.copy(0.18f) else ShadowGrey,
                        border = BorderStroke(1.dp, if (isSel) CrayolaBlue else Color.Transparent)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center) {
                            Text("D$day", color = if (isSel) CrayolaBlue else LightGrey,
                                fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                    if (label.isNotBlank()) Text(label, color = LightGrey, fontSize = 10.sp, lineHeight = 12.sp)
                }
            }
        }
        HorizontalScrollHint(isVisible = scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue)
        if (showError) Text(stringResource(R.string.workouts_select_at_least_one_cycle_day),
            color = SubtleRed, fontSize = 12.sp)
    }
}
@Composable
fun BasicCounterSelector(title: String, value: Int, suffix: String, onChange: (Int) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ShadowGrey,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("$value $suffix", color = CrayolaBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) {
                Icon(Icons.Default.Remove, stringResource(R.string.workouts_decrease_cd), tint = LightGrey)
            }
            IconButton(onClick = { onChange(value + 1) }) {
                Icon(Icons.Default.Add, stringResource(R.string.workouts_increase_cd), tint = CrayolaBlue)
            }
        }
    }
}

@Composable
private fun RoutineSchedulePreviewCard(
    planInterval: PlanIntervalConfig, selectedWeekDays: Set<DayOfWeek>,
    selectedCycleDays: Set<Int>, restDaysBetweenWorkouts: Int
) {
    val text = when (planInterval.type) {
        PlanIntervalType.CYCLE -> if (planInterval.cycleMode == CycleMode.WEEKLY) {
            val days = selectedWeekDays.sortedBy { it.value }
            if (days.isEmpty()) "No weekday selected yet."
            else "Runs on: ${days.joinToString(", ") { it.name.lowercase().replaceFirstChar(Char::uppercase) }}."
        } else {
            val days = selectedCycleDays.sorted()
            if (days.isEmpty()) "No cycle day selected yet."
            else "${planInterval.cycleLengthDays}-day cycle · Day ${days.joinToString(", Day ")}."
        }
        PlanIntervalType.FREQUENCY ->
            "Every ${restDaysBetweenWorkouts + 1} day(s) · $restDaysBetweenWorkouts rest day(s)."
    }
    Surface(shape = RoundedCornerShape(16.dp), color = ShadowGrey,
        border = BorderStroke(1.dp, CrayolaBlue.copy(alpha = 0.35f))) {
        Text(text = text, color = LightGrey, fontSize = 12.sp, lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp))
    }
}

@Composable
private fun FrequencyRoutineAssignments(existingRoutines: List<Routine>) {
    AssignmentSectionCard(title = stringResource(R.string.workouts_existing_routine_frequencies)) {
        if (existingRoutines.isEmpty()) {
            AssignmentRow(stringResource(R.string.workouts_plan_label),
                stringResource(R.string.workouts_no_routines_yet))
            return@AssignmentSectionCard
        }
        existingRoutines.forEach { routine ->
            val rest = parseRestDaysFromSchedulingValue(routine.schedulingValue)
            AssignmentRow(routine.name, if (rest != null)
                stringResource(R.string.workouts_frequency_every_n_days_with_rest, rest + 1, rest)
            else stringResource(R.string.workouts_frequency_not_available))
        }
    }
}

@Composable
private fun AssignmentSectionCard(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Surface(shape = RoundedCornerShape(16.dp), color = ShadowGrey,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
        }
    }
}

@Composable
private fun AssignmentRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Text(value, color = LightGrey, fontSize = 12.sp)
    }
}
private fun parseWeekDaysFromSchedulingValue(value: String?): Set<DayOfWeek> {
    if (value.isNullOrBlank()) return emptySet()
    return value.split(',').mapNotNull { runCatching { DayOfWeek.valueOf(it.trim().uppercase()) }.getOrNull() }.toSet()
}

private fun parseCycleDaysFromSchedulingValue(value: String?): Set<Int> {
    if (value.isNullOrBlank()) return emptySet()
    return value.split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it > 0 }.toSet()
}

private fun parseRestDaysFromSchedulingValue(value: String?): Int? =
    value?.trim()?.toIntOrNull()?.takeIf { it >= 1 }

private fun buildWeeklyAssignments(routines: List<Routine>): Map<DayOfWeek, List<String>> =
    DayOfWeek.entries.associateWith { day ->
        routines.filter { day in parseWeekDaysFromSchedulingValue(it.schedulingValue) }.map { it.name }
    }

private fun buildCycleAssignments(routines: List<Routine>, cycleLengthDays: Int): Map<Int, List<String>> =
    (1..cycleLengthDays.coerceAtLeast(1)).associateWith { day ->
        routines.filter { day in parseCycleDaysFromSchedulingValue(it.schedulingValue) }.map { it.name }
    }

private fun formatAssignedRoutineNames(names: List<String>): String {
    if (names.isEmpty()) return ""
    val truncated = names.map { if (it.length <= 7) it else it.take(4) + "..." }
    return if (truncated.size <= 5) truncated.joinToString("\n")
    else truncated.take(5).joinToString("\n") + "\n+${truncated.size - 5}"
}

private fun isCurrentStepValid(state: RoutineWizardUiState): Boolean = when (state.currentStep) {
    1    -> state.name.isNotBlank() && (state.availablePlans.isEmpty() || state.selectedPlanId != null)
    2    -> when (state.selectedPlanInterval.type) {
        PlanIntervalType.CYCLE     -> if (state.selectedPlanInterval.cycleMode == CycleMode.WEEKLY)
            state.selectedWeekDays.isNotEmpty()
        else state.selectedCycleDays.isNotEmpty()
        PlanIntervalType.FREQUENCY -> state.restDaysBetweenWorkouts > 0
    }
    3    -> state.selectedExercises.isNotEmpty()
    else -> true
}

private data class RoutineCreationArgs(
    val planId: String?, val name: String, val description: String,
    val schedulingValue: String, val startingDate: String,
    val selectedExercises: List<RoutineExercise>
)

private fun buildRoutineCreationArgs(state: RoutineWizardUiState): RoutineCreationArgs {
    val today = LocalDate.now().toString()
    return when (state.selectedPlanInterval.type) {
        PlanIntervalType.CYCLE -> if (state.selectedPlanInterval.cycleMode == CycleMode.WEEKLY) {
            RoutineCreationArgs(state.selectedPlanId, state.name.trim(), state.description.trim(),
                state.selectedWeekDays.sortedBy { it.value }.joinToString(",") { it.name }.ifBlank { "MONDAY" },
                today, state.selectedExercises)
        } else {
            RoutineCreationArgs(state.selectedPlanId, state.name.trim(), state.description.trim(),
                state.selectedCycleDays.sorted().joinToString(",").ifBlank { "1" },
                today, state.selectedExercises)
        }
        PlanIntervalType.FREQUENCY -> RoutineCreationArgs(state.selectedPlanId, state.name.trim(),
            state.description.trim(), state.restDaysBetweenWorkouts.toString(), today, state.selectedExercises)
    }
}

@Composable
private fun wizardTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ShadowGrey, unfocusedContainerColor = ShadowGrey,
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = CrayolaBlue, unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = CrayolaBlue, unfocusedLabelColor = LightGrey,
    errorBorderColor = SubtleRed, cursorColor = CrayolaBlue
)

data class DayLabel(val dayOfWeek: DayOfWeek, val label: String)

@Composable
fun HorizontalScrollHint(isVisible: Boolean) {
    if (!isVisible) return
    Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.workouts_swipe_for_more), color = LightGrey, fontSize = 11.sp)
        Icon(Icons.Default.KeyboardArrowRight,
            stringResource(R.string.workouts_more_days_right_cd), tint = LightGrey)
    }
}
