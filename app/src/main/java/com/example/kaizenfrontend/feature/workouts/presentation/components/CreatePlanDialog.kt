package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, startingDate: String, interval: String?, cycleLength: Int?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentStep by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showInlineErrors by remember { mutableStateOf(false) }
    var planIntervalType by remember { mutableStateOf(PlanIntervalType.CYCLE) }
    var cycleMode by remember { mutableStateOf(CycleMode.WEEKLY) }
    var customCycleLengthDays by remember { mutableIntStateOf(8) }

    val today = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    var startingDate by remember { mutableStateOf(today) }

    val canContinue = when (currentStep) {
        1 -> name.isNotBlank()
        2 -> startingDate.isNotBlank()
        else -> true
    }
    val canFinish = name.isNotBlank() && startingDate.isNotBlank()

    LaunchedEffect(currentStep) {
        showInlineErrors = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightGrey.copy(alpha = 0.6f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(72.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep -= 1
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentStep > 1) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create plan",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${currentStep}/2",
                        color = LightGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier.width(72.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (currentStep == 2) {
                        TextButton(
                            onClick = {
                                if (canFinish) {
                                    val intervalConfig = when (planIntervalType) {
                                        PlanIntervalType.FREQUENCY -> PlanIntervalConfig.defaultFrequency()
                                        PlanIntervalType.CYCLE -> PlanIntervalConfig(
                                            type = PlanIntervalType.CYCLE,
                                            cycleMode = cycleMode,
                                            cycleLengthDays = if (cycleMode == CycleMode.WEEKLY) 7 else customCycleLengthDays
                                        )
                                    }
                                    onCreate(
                                        name,
                                        description,
                                        startingDate,
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
                                text = "Finish",
                                color = if (canFinish) CrayolaBlue else LightGrey,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val isForward = targetState > initialState
                    (slideInHorizontally { if (isForward) it else -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (isForward) -it else it } + fadeOut())
                },
                label = "CreatePlanStepTransition"
            ) { step ->
                when (step) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Name",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                singleLine = true,
                                isError = showInlineErrors && name.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )

                            if (showInlineErrors && name.isBlank()) {
                                Text(
                                    text = "Name is required",
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "Description",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )
                        }
                    }

                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Starting date",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = startingDate,
                                onValueChange = { startingDate = it },
                                singleLine = true,
                                isError = showInlineErrors && startingDate.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )

                            if (showInlineErrors && startingDate.isBlank()) {
                                Text(
                                    text = "Starting date is required",
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "Plan schedule mode",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IntervalTypeCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Cycle",
                                    subtitle = "Plan defines cycle style",
                                    selected = planIntervalType == PlanIntervalType.CYCLE,
                                    onClick = { planIntervalType = PlanIntervalType.CYCLE }
                                )
                                IntervalTypeCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Frequency",
                                    subtitle = "Rest days set per routine",
                                    selected = planIntervalType == PlanIntervalType.FREQUENCY,
                                    onClick = { planIntervalType = PlanIntervalType.FREQUENCY }
                                )
                            }

                            if (planIntervalType == PlanIntervalType.CYCLE) {
                                Text(
                                    text = "Cycle setup",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CycleModeCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Weekly",
                                        subtitle = "7-day cycle (Mon-Sun)",
                                        selected = cycleMode == CycleMode.WEEKLY,
                                        onClick = { cycleMode = CycleMode.WEEKLY }
                                    )
                                    CycleModeCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Custom",
                                        subtitle = "Set your own cycle length",
                                        selected = cycleMode == CycleMode.CUSTOM,
                                        onClick = { cycleMode = CycleMode.CUSTOM }
                                    )
                                }

                                if (cycleMode == CycleMode.CUSTOM) {
                                    CycleLengthSelector(
                                        value = customCycleLengthDays,
                                        onChange = { customCycleLengthDays = it.coerceAtLeast(1) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            SelectedScheduleInfoText(
                                planIntervalType = planIntervalType,
                                cycleMode = cycleMode,
                                customCycleLengthDays = customCycleLengthDays
                            )
                        }
                    }

                    else -> Unit
                }
            }

            if (currentStep < 2) {
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (canContinue) {
                            currentStep += 1
                        } else {
                            showInlineErrors = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrayolaBlue,
                        disabledContainerColor = ShadowGrey
                    )
                ) {
                    Text("NEXT", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SelectedScheduleInfoText(
    planIntervalType: PlanIntervalType,
    cycleMode: CycleMode,
    customCycleLengthDays: Int
) {
    val selectedModeLabel = when (planIntervalType) {
        PlanIntervalType.CYCLE -> {
            if (cycleMode == CycleMode.WEEKLY) "Selected: Weekly cycle" else "Selected: Custom cycle"
        }
        PlanIntervalType.FREQUENCY -> "Selected: Frequency"
    }

    val detailsLabel = when (planIntervalType) {
        PlanIntervalType.CYCLE -> {
            if (cycleMode == CycleMode.WEEKLY) {
                "Pick weekdays for each routine. Best for stable weekly splits."
            } else {
                "Plan runs in a $customCycleLengthDays-day loop. Pick cycle days for each routine."
            }
        }
        PlanIntervalType.FREQUENCY -> "Each routine defines rest days between sessions. Best for recovery-based training."
    }

    val exampleLabel = when (planIntervalType) {
        PlanIntervalType.CYCLE -> {
            if (cycleMode == CycleMode.WEEKLY) {
                "Example: Mon train, Wed train, Fri train."
            } else {
                val cycleLength = customCycleLengthDays.coerceAtLeast(1)
                val mid = (cycleLength + 1) / 2
                "Example: Day 1, Day $mid and Day $cycleLength in each $cycleLength-day cycle."
            }
        }
        PlanIntervalType.FREQUENCY -> "Example: Train, Rest, Rest, Train, Rest, Rest."
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = selectedModeLabel,
            color = CrayolaBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = detailsLabel,
            color = LightGrey,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Text(
            text = exampleLabel,
            color = LightGrey,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun IntervalTypeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
        border = BorderStroke(1.dp, if (selected) CrayolaBlue else Color.Transparent),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = LightGrey, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CycleModeCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) CrayolaBlue.copy(alpha = 0.18f) else ShadowGrey,
        border = BorderStroke(1.dp, if (selected) CrayolaBlue else Color.Transparent),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = LightGrey, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CycleLengthSelector(
    value: Int,
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
                    text = "Cycle length",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "$value days",
                    color = LightGrey,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { onChange((value - 1).coerceAtLeast(1)) }) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease cycle length",
                    tint = LightGrey
                )
            }

            IconButton(onClick = { onChange(value + 1) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase cycle length",
                    tint = CrayolaBlue
                )
            }
        }
    }
}

@Composable
private fun planFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = CrayolaBlue,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = ShadowGrey,
    unfocusedContainerColor = ShadowGrey,
    cursorColor = CrayolaBlue
)
