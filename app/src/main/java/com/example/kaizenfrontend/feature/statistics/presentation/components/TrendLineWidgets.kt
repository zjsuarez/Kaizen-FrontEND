package com.example.kaizenfrontend.feature.statistics.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.statistics.presentation.ExerciseOptionUiState
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.feature.statistics.presentation.TrendChartUiState
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BodyWeightTrendWidget(
    uiState: TrendChartUiState,
    modelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier
) {
    val resolvedSubtitle = uiState.subtitle.resolveOrNull()
    val resolvedMessage = uiState.message.resolve()

    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_body_weight_trend_title),
        subtitle = resolvedSubtitle,
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = resolvedMessage,
        modifier = modifier
    ) {
        TrendLineChart(
            modelProducer = modelProducer,
            xLabels = uiState.xLabels,
            minY = uiState.minY,
            maxY = uiState.maxY
        )
    }
}

@Composable
fun Estimated1RmWidget(
    uiState: TrendChartUiState,
    modelProducer: ChartEntryModelProducer,
    exercises: List<ExerciseOptionUiState>,
    selectedExerciseId: String?,
    onExerciseSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedExerciseName = remember(exercises, selectedExerciseId) {
        exercises.firstOrNull { it.id == selectedExerciseId }?.name ?: ""
    }

    val fallbackExerciseName = stringResource(id = com.example.kaizenfrontend.R.string.statistics_select_exercise)

    val resolvedSubtitle = uiState.subtitle.resolveOrNull()
    val resolvedMessage = uiState.message.resolve()

    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_estimated_1rm_title),
        subtitle = resolvedSubtitle,
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = resolvedMessage,
        headerContent = {
            ExerciseDropdown(
                exercises = exercises,
                selectedExerciseName = if (selectedExerciseName.isBlank()) fallbackExerciseName else selectedExerciseName,
                onExerciseSelected = onExerciseSelected
            )
        },
        modifier = modifier
    ) {
        TrendLineChart(
            modelProducer = modelProducer,
            xLabels = uiState.xLabels,
            minY = uiState.minY,
            maxY = uiState.maxY
        )
    }
}

@Composable
private fun TrendLineChart(
    modelProducer: ChartEntryModelProducer,
    xLabels: List<String>,
    minY: Float,
    maxY: Float
) {
    val dateInputFormatter = remember { DateTimeFormatter.ofPattern("dd/MM", Locale.US) }
    val dateOutputFormatter = remember { DateTimeFormatter.ofPattern("MMM dd", Locale.US) }

    val bottomAxisValueFormatter = remember(xLabels) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.roundToInt()
            if (index % 2 != 0) {
                ""
            } else {
                val rawLabel = xLabels.getOrNull(index) ?: return@AxisValueFormatter ""
                try {
                    LocalDate.parse(rawLabel, dateInputFormatter).format(dateOutputFormatter)
                } catch (_: DateTimeParseException) {
                    rawLabel
                }
            }
        }
    }

    val startAxisValueFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            String.format(Locale.US, "%.1f", value)
        }
    }

    val axisOverrider = remember(minY, maxY) {
        AxisValuesOverrider.fixed(minY = minY, maxY = maxY)
    }

    val lineColor = CrayolaBlue
    val lineChart = lineChart(
        lines = listOf(
            lineSpec(
                lineColor = lineColor,
                lineThickness = 3.dp,
                lineBackgroundShader = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ).toDynamicShader(),
                pointConnector = DefaultPointConnector(cubicStrength = 0.5f)
            )
        ),
        axisValuesOverrider = axisOverrider
    )

    Chart(
        chart = lineChart,
        chartModelProducer = modelProducer,
        modifier = Modifier.fillMaxSize(),
        startAxis = rememberStartAxis(
            valueFormatter = startAxisValueFormatter,
            guideline = null
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisValueFormatter,
            labelRotationDegrees = 30f,
            guideline = null
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDropdown(
    exercises: List<ExerciseOptionUiState>,
    selectedExerciseName: String,
    onExerciseSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedExerciseName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(id = com.example.kaizenfrontend.R.string.statistics_exercise_label), color = LightGrey) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = CrayolaBlue
                )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = CrayolaBlue,
                unfocusedBorderColor = LightGrey,
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ShadowGrey
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = com.example.kaizenfrontend.R.string.statistics_none), color = PureWhite) },
                onClick = {
                    onExerciseSelected(null)
                    expanded = false
                }
            )

            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name, color = PureWhite) },
                    onClick = {
                        onExerciseSelected(exercise.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
