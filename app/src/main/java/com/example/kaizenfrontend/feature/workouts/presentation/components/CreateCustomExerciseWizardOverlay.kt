package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.model.CreateCustomExerciseCommand
import com.example.kaizenfrontend.feature.workouts.domain.model.EquipmentType
import com.example.kaizenfrontend.feature.workouts.domain.model.ExerciseMetric

private enum class WizardMetricOption(val label: String, val metric: ExerciseMetric) {
    SETS("Sets", ExerciseMetric.SETS),
    DURATION("Duration", ExerciseMetric.DURATION),
    DISTANCE("Distance", ExerciseMetric.DISTANCE),
    SIMPLE_CHECK_OFF("Simple check off", ExerciseMetric.SIMPLE_CHECK_OFF)
}

private val wizardMuscleOptions = listOf(
    "Lats",
    "Biceps",
    "Chest",
    "Triceps",
    "Shoulders",
    "Forearms",
    "Core",
    "Quads",
    "Hamstrings",
    "Glutes",
    "Calves",
    "Back"
)

private val wizardEquipmentOptions = listOf(
    EquipmentType.BAND,
    EquipmentType.CARDIO,
    EquipmentType.MACHINE,
    EquipmentType.DUMBBELL,
    EquipmentType.BODYWEIGHT,
    EquipmentType.SMITH_MACHINE,
    EquipmentType.CABLE,
    EquipmentType.KETTLEBELL,
    EquipmentType.BARBELL
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCustomExerciseWizardOverlay(
    onDismissRequest: () -> Unit,
    onFinish: (CreateCustomExerciseCommand) -> Unit,
    isSubmitting: Boolean = false,
    externalError: String? = null,
    onClearExternalError: () -> Unit = {}
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMuscles by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedMetric by remember { mutableStateOf<WizardMetricOption?>(null) }
    var selectedEquipment by remember { mutableStateOf<EquipmentType?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var musclesError by remember { mutableStateOf<String?>(null) }
    var metricError by remember { mutableStateOf<String?>(null) }
    var equipmentError by remember { mutableStateOf<String?>(null) }

    fun goBack() {
        if (step == 1) onDismissRequest() else step -= 1
    }

    fun goNext() {
        onClearExternalError()

        if (step == 1) {
            nameError = if (name.trim().isBlank()) "Exercise name is required" else null
            if (nameError == null) step = 2
            return
        }

        if (step == 2) {
            musclesError = if (selectedMuscles.isEmpty()) "Select at least one muscle" else null
            if (musclesError == null) {
                step = 3
            }
            return
        }

        if (step == 3) {
            metricError = if (selectedMetric == null) "Select one metric" else null
            if (metricError == null) {
                step = 4
            }
        }
    }

    fun finish() {
        if (isSubmitting) return
        onClearExternalError()

        equipmentError = if (selectedEquipment == null) "Select one equipment type" else null
        if (equipmentError != null) return

        onFinish(
            CreateCustomExerciseCommand(
                name = name.trim(),
                description = description.trim().ifBlank { null },
                selectedMuscles = selectedMuscles.toList(),
                metrics = selectedMetric!!.metric.toBackendMetrics(),
                equipmentType = selectedEquipment!!
            )
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Onyx.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .align(Alignment.Center),
                color = ShadowGrey,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = ::goBack, enabled = !isSubmitting) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }
                        if (step == 4) {
                            Button(
                                onClick = ::finish,
                                enabled = !isSubmitting,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrayolaBlue,
                                    contentColor = PureWhite
                                )
                            ) {
                                Text(
                                    text = if (isSubmitting) "SAVING..." else "FINISH",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "$step/4",
                                color = LightGrey,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    when (step) {
                        1 -> {
                            Text(
                                text = "Exercise name",
                                color = PureWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Give this custom movement a clear name and optional description.",
                                color = LightGrey,
                                fontSize = 13.sp
                            )

                            WizardTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    if (nameError != null) nameError = null
                                },
                                placeholder = "Unilateral Lat Pulldown"
                            )
                            ValidationSlot(error = nameError)

                            WizardTextField(
                                value = description,
                                onValueChange = {
                                    description = it
                                    onClearExternalError()
                                },
                                placeholder = "Optional notes",
                                minLines = 4
                            )
                            Spacer(modifier = Modifier.weight(1f))

                            WizardActionButton(
                                text = "NEXT",
                                onClick = ::goNext,
                                enabled = !isSubmitting
                            )
                        }

                        2 -> {
                            Text(
                                text = "Muscles trained",
                                color = PureWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select all muscles this exercise targets.",
                                color = LightGrey,
                                fontSize = 13.sp
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                wizardMuscleOptions.forEach { muscle ->
                                    val selected = muscle in selectedMuscles
                                    Surface(
                                        modifier = Modifier.clickable {
                                            selectedMuscles = if (selected) {
                                                selectedMuscles - muscle
                                            } else {
                                                selectedMuscles + muscle
                                            }
                                            musclesError = null
                                            onClearExternalError()
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) CrayolaBlue else ShadowGrey,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) CrayolaBlue else Color.White.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        Text(
                                            text = muscle,
                                            color = PureWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            ValidationSlot(error = musclesError)
                            Spacer(modifier = Modifier.weight(1f))
                            WizardActionButton(
                                text = "NEXT",
                                onClick = ::goNext,
                                enabled = !isSubmitting
                            )
                        }

                        3 -> {
                            Text(
                                text = "Select metrics",
                                color = PureWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose how this exercise will be tracked.",
                                color = LightGrey,
                                fontSize = 13.sp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                WizardMetricOption.entries.forEach { option ->
                                    val selected = selectedMetric == option
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isSubmitting) return@clickable
                                                selectedMetric = option
                                                metricError = null
                                                onClearExternalError()
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) CrayolaBlue.copy(alpha = 0.2f) else Onyx,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) CrayolaBlue else Color.White.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = option.label,
                                                color = PureWhite,
                                                fontSize = 14.sp,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(16.dp)
                                                    .fillMaxWidth(0.05f)
                                            )
                                        }
                                    }
                                }
                            }

                            ValidationSlot(error = metricError)
                            Spacer(modifier = Modifier.weight(1f))

                            WizardActionButton(
                                text = "NEXT",
                                onClick = ::goNext,
                                enabled = !isSubmitting
                            )
                        }

                        else -> {
                            Text(
                                text = "Select equipment",
                                color = PureWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose the equipment type that best fits this exercise.",
                                color = LightGrey,
                                fontSize = 13.sp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                wizardEquipmentOptions.forEach { equipment ->
                                    val selected = selectedEquipment == equipment
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isSubmitting) return@clickable
                                                selectedEquipment = equipment
                                                equipmentError = null
                                                onClearExternalError()
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selected) CrayolaBlue.copy(alpha = 0.2f) else Onyx,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) CrayolaBlue else Color.White.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = equipment.name.replace('_', ' '),
                                                color = PureWhite,
                                                fontSize = 14.sp,
                                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(16.dp)
                                                    .fillMaxWidth(0.05f)
                                            )
                                        }
                                    }
                                }
                            }

                            ValidationSlot(error = equipmentError)
                            ValidationSlot(error = externalError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = PureWhite,
            fontSize = 14.sp
        ),
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        cursorBrush = SolidColor(CrayolaBlue),
        modifier = Modifier
            .fillMaxWidth()
            .background(Onyx, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    color = LightGrey,
                    fontSize = 14.sp
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun ValidationSlot(error: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = Color(0xFFCF6679),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WizardActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CrayolaBlue,
                contentColor = PureWhite
            )
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun ExerciseMetric.toBackendMetrics(): String {
    return when (this) {
        ExerciseMetric.SETS -> "sets"
        ExerciseMetric.DURATION -> "duration"
        ExerciseMetric.DISTANCE -> "distance_km"
        ExerciseMetric.SIMPLE_CHECK_OFF -> "simple_check_off"
    }
}
