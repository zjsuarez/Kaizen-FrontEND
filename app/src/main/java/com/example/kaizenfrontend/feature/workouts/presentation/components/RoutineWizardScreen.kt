package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
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

@Composable
fun RoutineWizardScreen(
    viewModel: RoutineWizardViewModel,
    onCreateRoutine: (
        planId: String?,
        name: String,
        description: String,
        schedulingValue: String,
        startingDate: String,
        selectedExercises: List<RoutineExercise>
    ) -> Unit = { _, _, _, _, _, _ -> },
    onWizardClosed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInlineErrors by remember { mutableStateOf(false) }
    var showExerciseCatalog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is RoutineWizardEvent.Success) {
                onWizardClosed()
            }
        }
    }

    LaunchedEffect(uiState.currentStep) {
        showInlineErrors = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Onyx)
    ) {
        RoutineWizardTopBar(
            currentStep = uiState.currentStep,
            canFinish = uiState.selectedExercises.isNotEmpty(),
            onCloseOrBack = {
                if (uiState.currentStep == 1) {
                    onWizardClosed()
                } else {
                    viewModel.previousStep()
                }
            },
            onFinish = {
                if (isCurrentStepValid(uiState)) {
                    val args = buildRoutineCreationArgs(uiState)
                    onCreateRoutine(
                        args.planId,
                        args.name,
                        args.description,
                        args.schedulingValue,
                        args.startingDate,
                        args.selectedExercises
                    )
                    viewModel.saveRoutine()
                } else {
                    showInlineErrors = true
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    val isForward = targetState > initialState
                    (slideInHorizontally { if (isForward) it else -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (isForward) -it else it } + fadeOut())
                },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> WizardStep1Meta(
                        name = uiState.name,
                        description = uiState.description,
                        plans = uiState.availablePlans,
                        selectedPlanId = uiState.selectedPlanId,
                        onNameChange = viewModel::updateName,
                        onDescriptionChange = viewModel::updateDescription,
                        onPlanSelected = viewModel::selectPlan,
                        showPlanError = showInlineErrors && uiState.availablePlans.isNotEmpty() && uiState.selectedPlanId == null,
                        showNameError = showInlineErrors && uiState.name.isBlank()
                    )

                    2 -> WizardStep2Schedule(
                        planInterval = uiState.selectedPlanInterval,
                        existingPlanRoutines = uiState.availableRoutines.filter { it.planId == uiState.selectedPlanId },
                        selectedWeekDays = uiState.selectedWeekDays,
                        selectedCycleDays = uiState.selectedCycleDays,
                        restDaysBetweenWorkouts = uiState.restDaysBetweenWorkouts,
                        onWeekDayToggle = viewModel::toggleWeekDay,
                        onCycleDayToggle = viewModel::toggleCycleDay,
                        onRestDaysChange = viewModel::updateRestDaysBetweenWorkouts,
                        showCycleSelectionError = showInlineErrors &&
                            uiState.selectedPlanInterval.type == PlanIntervalType.CYCLE &&
                            (
                                (uiState.selectedPlanInterval.cycleMode == CycleMode.WEEKLY && uiState.selectedWeekDays.isEmpty()) ||
                                    (uiState.selectedPlanInterval.cycleMode == CycleMode.CUSTOM && uiState.selectedCycleDays.isEmpty())
                                )
                    )

                    else -> WizardStep3Exercises(
                        selectedExercises = uiState.selectedExercises,
                        onAddExerciseClick = { showExerciseCatalog = true },
                        onRemoveExercise = viewModel::removeExercise,
                        showEmptyError = showInlineErrors && uiState.selectedExercises.isEmpty()
                    )
                }
            }

            if (uiState.currentStep < 3) {
                WizardBottomBar(
                    buttonText = "NEXT",
                    onButtonClick = {
                        if (isCurrentStepValid(uiState)) {
                            viewModel.nextStep()
                        } else {
                            showInlineErrors = true
                        }
                    }
                )
            }
        }
    }

    if (showExerciseCatalog) {
        ExerciseCatalogBottomSheet(
            onDismissRequest = { showExerciseCatalog = false },
            onExerciseSelected = { exercise ->
                viewModel.addExercise(exercise)
                showExerciseCatalog = false
            }
        )
    }
}

@Composable
fun WizardStep1Meta(
    name: String,
    description: String,
    plans: List<TrainingPlan>,
    selectedPlanId: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPlanSelected: (String) -> Unit,
    showPlanError: Boolean,
    showNameError: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Name",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            isError = showNameError,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = wizardTextFieldColors()
        )

        if (showNameError) {
            Text(
                text = "Name is required",
                color = SubtleRed,
                fontSize = 12.sp
            )
        }

        Text(
            text = "Description",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            minLines = 4,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = wizardTextFieldColors()
        )

        if (plans.isNotEmpty()) {
            Text(
                text = "Plan",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plans.forEach { plan ->
                    val isSelected = plan.id == selectedPlanId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlanSelected(plan.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CrayolaBlue else Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = plan.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            plan.interval?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = PlanIntervalConfig.fromBackend(it, plan.cycleLength).toDisplayLabel(),
                                    color = LightGrey,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            if (showPlanError) {
                Text(
                    text = "Select a plan",
                    color = SubtleRed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun RoutineWizardTopBar(
    currentStep: Int,
    canFinish: Boolean,
    onCloseOrBack: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(color = Onyx) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(72.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onCloseOrBack) {
                    Icon(
                        imageVector = if (currentStep == 1) Icons.Default.Close else Icons.Default.ArrowBack,
                        contentDescription = if (currentStep == 1) "Close wizard" else "Previous step",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Routine",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "${currentStep}/3",
                    color = LightGrey,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier.width(72.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (currentStep == 3) {
                    TextButton(onClick = onFinish, enabled = canFinish) {
                        Text(
                            text = "Finish",
                            color = if (canFinish) CrayolaBlue else LightGrey,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStep2Schedule(
    planInterval: PlanIntervalConfig,
    existingPlanRoutines: List<Routine>,
    selectedWeekDays: Set<DayOfWeek>,
    selectedCycleDays: Set<Int>,
    restDaysBetweenWorkouts: Int,
    onWeekDayToggle: (DayOfWeek) -> Unit,
    onCycleDayToggle: (Int) -> Unit,
    onRestDaysChange: (Int) -> Unit,
    showCycleSelectionError: Boolean
) {
    val weeklyAssignmentsByDay = remember(existingPlanRoutines) {
        buildWeeklyAssignments(existingPlanRoutines)
    }
    val cycleAssignmentsByDay = remember(existingPlanRoutines, planInterval.cycleLengthDays) {
        buildCycleAssignments(existingPlanRoutines, planInterval.cycleLengthDays)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Step 2: Schedule",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        when (planInterval.type) {
            PlanIntervalType.CYCLE -> {
                if (planInterval.cycleMode == CycleMode.WEEKLY) {
                    WeeklyDaysSelector(
                        selectedDays = selectedWeekDays,
                        assignedRoutineNamesByDay = weeklyAssignmentsByDay,
                        onDayClick = onWeekDayToggle,
                        showError = showCycleSelectionError
                    )
                } else {
                    CycleDaysSelector(
                        cycleLengthDays = planInterval.cycleLengthDays,
                        selectedCycleDays = selectedCycleDays,
                        assignedRoutineNamesByCycleDay = cycleAssignmentsByDay,
                        onDayClick = onCycleDayToggle,
                        showError = showCycleSelectionError
                    )
                }
            }

            PlanIntervalType.FREQUENCY -> {
                Text(
                    text = "Choose rest days between workouts for this routine.",
                    color = LightGrey,
                    fontSize = 12.sp
                )
                BasicCounterSelector(
                    title = "Rest days",
                    value = restDaysBetweenWorkouts,
                    suffix = "days",
                    onChange = onRestDaysChange
                )

                FrequencyRoutineAssignments(existingRoutines = existingPlanRoutines)
            }
        }
    }
}

@Composable
private fun FrequencyRoutineAssignments(existingRoutines: List<Routine>) {
    AssignmentSectionCard(title = "Existing routine frequencies") {
        if (existingRoutines.isEmpty()) {
            AssignmentRow(label = "Plan", value = "No routines yet")
            return@AssignmentSectionCard
        }

        existingRoutines.forEach { routine ->
            val restDays = parseRestDaysFromSchedulingValue(routine.schedulingValue)
            val frequencyLabel = if (restDays != null) {
                "Every ${restDays + 1} day(s) (${restDays} rest day(s))"
            } else {
                "Frequency not available"
            }

            AssignmentRow(
                label = routine.name,
                value = frequencyLabel
            )
        }
    }
}

@Composable
private fun AssignmentSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ShadowGrey,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun AssignmentRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = LightGrey,
            fontSize = 12.sp
        )
    }
}

private fun parseWeekDaysFromSchedulingValue(value: String?): Set<DayOfWeek> {
    if (value.isNullOrBlank()) return emptySet()

    return value
        .split(',')
        .mapNotNull { token ->
            val normalized = token.trim().uppercase()
            runCatching { DayOfWeek.valueOf(normalized) }.getOrNull()
        }
        .toSet()
}

private fun parseCycleDaysFromSchedulingValue(value: String?): Set<Int> {
    if (value.isNullOrBlank()) return emptySet()

    return value
        .split(',')
        .mapNotNull { token -> token.trim().toIntOrNull() }
        .filter { it > 0 }
        .toSet()
}

private fun parseRestDaysFromSchedulingValue(value: String?): Int? {
    return value?.trim()?.toIntOrNull()?.takeIf { it >= 1 }
}

@Composable
private fun WizardBottomBar(
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Surface(color = Onyx) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrayolaBlue,
                    disabledContainerColor = ShadowGrey
                )
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun WeeklyDaysSelector(
    selectedDays: Set<DayOfWeek>,
    assignedRoutineNamesByDay: Map<DayOfWeek, List<String>>,
    onDayClick: (DayOfWeek) -> Unit,
    showError: Boolean
) {
    val dayScrollState = rememberScrollState()
    val dayItems = listOf(
        DayLabel(DayOfWeek.MONDAY, "L"),
        DayLabel(DayOfWeek.TUESDAY, "M"),
        DayLabel(DayOfWeek.WEDNESDAY, "X"),
        DayLabel(DayOfWeek.THURSDAY, "J"),
        DayLabel(DayOfWeek.FRIDAY, "V"),
        DayLabel(DayOfWeek.SATURDAY, "S"),
        DayLabel(DayOfWeek.SUNDAY, "D")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Select days",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dayScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dayItems.forEach { item ->
                val isSelected = selectedDays.contains(item.dayOfWeek)
                val assignedLabel = formatAssignedRoutineNames(
                    assignedRoutineNamesByDay[item.dayOfWeek].orEmpty()
                )

                Column(
                    modifier = Modifier.width(72.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .width(42.dp)
                            .clickable { onDayClick(item.dayOfWeek) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CrayolaBlue else Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.label,
                                color = if (isSelected) CrayolaBlue else LightGrey,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (assignedLabel.isNotBlank()) {
                        Text(
                            text = assignedLabel,
                            color = LightGrey,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        HorizontalScrollHint(
            isVisible = dayScrollState.maxValue > 0 && dayScrollState.value < dayScrollState.maxValue
        )

        if (showError) {
            Text(
                text = "Select at least one day",
                color = SubtleRed,
                fontSize = 12.sp
            )
        }
    }
}

private fun buildWeeklyAssignments(existingRoutines: List<Routine>): Map<DayOfWeek, List<String>> {
    return DayOfWeek.entries.associateWith { dayOfWeek ->
        existingRoutines.filter { routine ->
            dayOfWeek in parseWeekDaysFromSchedulingValue(routine.schedulingValue)
        }.map { it.name }
    }
}

private fun buildCycleAssignments(
    existingRoutines: List<Routine>,
    cycleLengthDays: Int
): Map<Int, List<String>> {
    val totalDays = cycleLengthDays.coerceAtLeast(1)
    return (1..totalDays).associateWith { day ->
        existingRoutines.filter { routine ->
            day in parseCycleDaysFromSchedulingValue(routine.schedulingValue)
        }.map { it.name }
    }
}

private fun formatAssignedRoutineNames(routineNames: List<String>): String {
    if (routineNames.isEmpty()) return ""
    val truncatedNames = routineNames.map(::truncateRoutineName)
    if (truncatedNames.size <= 5) return truncatedNames.joinToString("\n")

    val firstFive = truncatedNames.take(5).joinToString("\n")
    val remaining = truncatedNames.size - 5
    return "$firstFive\n+$remaining"
}

private fun truncateRoutineName(name: String): String {
    val maxChars = 7
    if (name.length <= maxChars) return name
    return name.take(maxChars - 3) + "..."
}

@Composable
fun CycleDaysSelector(
    cycleLengthDays: Int,
    selectedCycleDays: Set<Int>,
    assignedRoutineNamesByCycleDay: Map<Int, List<String>>,
    onDayClick: (Int) -> Unit,
    showError: Boolean
) {
    val dayScrollState = rememberScrollState()
    val days = (1..cycleLengthDays.coerceAtLeast(1)).toList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Select cycle days",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dayScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                val isSelected = day in selectedCycleDays
                val assignedLabel = formatAssignedRoutineNames(
                    assignedRoutineNamesByCycleDay[day].orEmpty()
                )

                Column(
                    modifier = Modifier.width(72.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDayClick(day) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CrayolaBlue else Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "D$day",
                                color = if (isSelected) CrayolaBlue else LightGrey,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (assignedLabel.isNotBlank()) {
                        Text(
                            text = assignedLabel,
                            color = LightGrey,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }

        HorizontalScrollHint(
            isVisible = dayScrollState.maxValue > 0 && dayScrollState.value < dayScrollState.maxValue
        )

        if (showError) {
            Text(
                text = "Select at least one cycle day",
                color = SubtleRed,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BasicCounterSelector(
    title: String,
    value: Int,
    suffix: String,
    onChange: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ShadowGrey,
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
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "$value $suffix",
                    color = LightGrey,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = LightGrey
                )
            }

            IconButton(onClick = { onChange(value + 1) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = CrayolaBlue
                )
            }
        }
    }
}

private fun isCurrentStepValid(state: RoutineWizardUiState): Boolean {
    return when (state.currentStep) {
        1 -> state.name.isNotBlank() && (state.availablePlans.isEmpty() || state.selectedPlanId != null)

        2 -> when (state.selectedPlanInterval.type) {
            PlanIntervalType.CYCLE -> {
                if (state.selectedPlanInterval.cycleMode == CycleMode.WEEKLY) {
                    state.selectedWeekDays.isNotEmpty()
                } else {
                    state.selectedCycleDays.isNotEmpty()
                }
            }

            PlanIntervalType.FREQUENCY -> state.restDaysBetweenWorkouts > 0
        }

        3 -> state.selectedExercises.isNotEmpty()

        else -> true
    }
}

private data class RoutineCreationArgs(
    val planId: String?,
    val name: String,
    val description: String,
    val schedulingValue: String,
    val startingDate: String,
    val selectedExercises: List<RoutineExercise>
)

private fun buildRoutineCreationArgs(state: RoutineWizardUiState): RoutineCreationArgs {
    val today = LocalDate.now().toString()

    return when (state.selectedPlanInterval.type) {
        PlanIntervalType.CYCLE -> {
            if (state.selectedPlanInterval.cycleMode == CycleMode.WEEKLY) {
                val weeklySelection = state.selectedWeekDays
                    .sortedBy { it.value }
                    .joinToString(",") { it.name }
                    .ifBlank { DayOfWeek.MONDAY.name }

                RoutineCreationArgs(
                    planId = state.selectedPlanId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    schedulingValue = weeklySelection,
                    startingDate = today,
                    selectedExercises = state.selectedExercises
                )
            } else {
                val cycleSelection = state.selectedCycleDays
                    .sorted()
                    .joinToString(",")
                    .ifBlank { "1" }

                RoutineCreationArgs(
                    planId = state.selectedPlanId,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    schedulingValue = cycleSelection,
                    startingDate = today,
                    selectedExercises = state.selectedExercises
                )
            }
        }

        PlanIntervalType.FREQUENCY -> RoutineCreationArgs(
            planId = state.selectedPlanId,
            name = state.name.trim(),
            description = state.description.trim(),
            schedulingValue = state.restDaysBetweenWorkouts.toString(),
            startingDate = today,
            selectedExercises = state.selectedExercises
        )
    }
}

@Composable
private fun wizardTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ShadowGrey,
        unfocusedContainerColor = ShadowGrey,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = CrayolaBlue,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = CrayolaBlue,
        unfocusedLabelColor = LightGrey,
        cursorColor = CrayolaBlue
    )
}

data class DayLabel(
    val dayOfWeek: DayOfWeek,
    val label: String
)

@Composable
fun HorizontalScrollHint(isVisible: Boolean) {
    if (!isVisible) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Swipe for more",
            color = LightGrey,
            fontSize = 11.sp
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "More days to the right",
            tint = LightGrey
        )
    }
}
