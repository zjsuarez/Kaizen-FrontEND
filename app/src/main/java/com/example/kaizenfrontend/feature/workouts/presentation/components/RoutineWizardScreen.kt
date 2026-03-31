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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextButton
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
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineScheduleType
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardEvent
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardUiState
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineWizardViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek

@Composable
fun RoutineWizardScreen(
    viewModel: RoutineWizardViewModel,
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

    Scaffold(
        containerColor = Onyx,
        topBar = {
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
                        viewModel.saveRoutine()
                    } else {
                        showInlineErrors = true
                    }
                }
            )
        },
        bottomBar = {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Onyx)
                .padding(innerPadding)
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
                        onNameChange = viewModel::updateName,
                        onDescriptionChange = viewModel::updateDescription,
                        showNameError = showInlineErrors && uiState.name.isBlank()
                    )

                    2 -> WizardStep2Schedule(
                        scheduleType = uiState.scheduleType,
                        selectedWeekDays = uiState.selectedWeekDays,
                        intervalDays = uiState.intervalDays,
                        cycleLength = uiState.cycleLength,
                        onScheduleTypeSelected = viewModel::updateScheduleType,
                        onWeekDayToggle = viewModel::toggleWeekDay,
                        onIntervalChange = viewModel::updateIntervalDays,
                        onCycleLengthChange = viewModel::updateCycleLength,
                        showWeeklyError = showInlineErrors &&
                            uiState.scheduleType == RoutineScheduleType.WEEKLY &&
                            uiState.selectedWeekDays.isEmpty()
                    )

                    else -> WizardStep3Exercises(
                        selectedExercises = uiState.selectedExercises,
                        onAddExerciseClick = { showExerciseCatalog = true },
                        onRemoveExercise = viewModel::removeExercise,
                        showEmptyError = showInlineErrors && uiState.selectedExercises.isEmpty()
                    )
                }
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
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
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
    scheduleType: RoutineScheduleType,
    selectedWeekDays: Set<DayOfWeek>,
    intervalDays: Int,
    cycleLength: Int,
    onScheduleTypeSelected: (RoutineScheduleType) -> Unit,
    onWeekDayToggle: (DayOfWeek) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onCycleLengthChange: (Int) -> Unit,
    showWeeklyError: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Step 2: Schedule",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        ScheduleTypeSelector(
            selectedType = scheduleType,
            onTypeSelected = onScheduleTypeSelected
        )

        when (scheduleType) {
            RoutineScheduleType.WEEKLY -> WeeklyDaysSelector(
                selectedDays = selectedWeekDays,
                onDayClick = onWeekDayToggle,
                showError = showWeeklyError
            )

            RoutineScheduleType.INTERVAL -> BasicCounterSelector(
                title = "Every X days",
                value = intervalDays,
                suffix = "days",
                onChange = onIntervalChange
            )

            RoutineScheduleType.CYCLE -> BasicCounterSelector(
                title = "Cycle length",
                value = cycleLength,
                suffix = "weeks",
                onChange = onCycleLengthChange
            )
        }
    }
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
private fun ScheduleTypeSelector(
    selectedType: RoutineScheduleType,
    onTypeSelected: (RoutineScheduleType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RoutineScheduleType.entries.forEach { type ->
            val isSelected = type == selectedType
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(type) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) CrayolaBlue else Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) CrayolaBlue else LightGrey,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyDaysSelector(
    selectedDays: Set<DayOfWeek>,
    onDayClick: (DayOfWeek) -> Unit,
    showError: Boolean
) {
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
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dayItems.forEach { item ->
                val isSelected = selectedDays.contains(item.dayOfWeek)
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
            }
        }

        if (showError) {
            Text(
                text = "Select at least one day",
                color = SubtleRed,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun BasicCounterSelector(
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
        1 -> state.name.isNotBlank()
        2 -> when (state.scheduleType) {
            RoutineScheduleType.WEEKLY -> state.selectedWeekDays.isNotEmpty()
            RoutineScheduleType.INTERVAL -> state.intervalDays > 0
            RoutineScheduleType.CYCLE -> state.cycleLength > 0
        }

        3 -> state.selectedExercises.isNotEmpty()

        else -> true
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

private data class DayLabel(
    val dayOfWeek: DayOfWeek,
    val label: String
)
