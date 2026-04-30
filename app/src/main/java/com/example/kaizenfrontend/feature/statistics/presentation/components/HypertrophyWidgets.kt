package com.example.kaizenfrontend.feature.statistics.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.statistics.presentation.MuscleFrequencyUiItem
import com.example.kaizenfrontend.feature.statistics.presentation.MuscleFrequencyUiState
import com.example.kaizenfrontend.feature.statistics.presentation.RepRangeSegment
import com.example.kaizenfrontend.feature.statistics.presentation.RepRangeUiState
import com.example.kaizenfrontend.feature.statistics.presentation.VolumeTrendUiState
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Volume Trend Widget  

@Composable
fun VolumeTrendWidget(
    uiState: VolumeTrendUiState,
    modelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_weekly_volume_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_weekly_volume_subtitle),
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        modifier = modifier
    ) {
        VolumeBarChart(
            modelProducer = modelProducer,
            weekLabels = uiState.weekLabels
        )
    }
}

@Composable
private fun VolumeBarChart(
    modelProducer: ChartEntryModelProducer,
    weekLabels: List<String>
) {
    val barColor = CrayolaBlue

    val bottomAxisFormatter = remember(weekLabels) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.roundToInt()
            if (index % 2 != 0) "" else weekLabels.getOrNull(index) ?: ""
        }
    }

    val startAxisFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            if (value >= 1000f) "${java.lang.String.format(java.util.Locale.US, "%.1f", value / 1000f)}k" else value.toInt().toString()
        }
    }

    val columnChart = columnChart(
        columns = listOf(
            LineComponent(
                color = barColor.copy(alpha = 0.85f).toArgb(),
                thicknessDp = 12f,
                shape = com.patrykandpatrick.vico.core.component.shape.Shapes.roundedCornerShape(
                    topLeftPercent = 4,
                    topRightPercent = 4
                )
            )
        ),
        mergeMode = ColumnChart.MergeMode.Grouped
    )

    Chart(
        chart = columnChart,
        chartModelProducer = modelProducer,
        modifier = Modifier.fillMaxSize(),
        startAxis = rememberStartAxis(
            valueFormatter = startAxisFormatter,
            guideline = null
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = bottomAxisFormatter,
            labelRotationDegrees = 30f,
            guideline = null
        )
    )
}

// Rep Range Distribution Widget 

@Composable
fun RepRangeWidget(
    uiState: RepRangeUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_rep_range_distribution_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_rep_range_distribution_subtitle),
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        modifier = modifier
    ) {
        RepRangeContent(segments = uiState.segments)
    }
}

@Composable
private fun RepRangeContent(segments: List<RepRangeSegment>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented bar
        SegmentedProgressBar(segments = segments)

        // Percentage breakdown rows
        segments.forEach { segment ->
            RepRangeRow(segment = segment)
        }
    }
}

@Composable
private fun SegmentedProgressBar(segments: List<RepRangeSegment>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        val total = segments.sumOf { it.percentage.toDouble() }.toFloat().coerceAtLeast(1f)
        segments.forEach { segment ->
            val fraction = (segment.percentage / total).coerceIn(0f, 1f)
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .weight(fraction)
                        .fillMaxSize()
                        .background(segment.color)
                )
            }
        }
    }
}

@Composable
private fun RepRangeRow(segment: RepRangeSegment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(segment.color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = segment.label,
            color = LightGrey,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${"%.1f".format(segment.percentage)}%",
            color = PureWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Muscle Frequency Radar Widget 

@Composable
fun MuscleFrequencyWidget(
    uiState: MuscleFrequencyUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_muscle_frequency_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_muscle_frequency_subtitle),
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        modifier = modifier
    ) {
        MuscleFrequencyContent(muscles = uiState.muscles)
    }
}

@Composable
private fun MuscleFrequencyContent(muscles: List<MuscleFrequencyUiItem>) {
    if (muscles.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radar chart on the left (60% width)
        MuscleRadarChart(
            muscles = muscles,
            modifier = Modifier
                .weight(0.55f)
                .fillMaxSize()
        )

        // Legend on the right (40% width)
        MuscleFrequencyLegend(
            muscles = muscles,
            modifier = Modifier
                .weight(0.45f)
                .padding(start = 8.dp)
        )
    }
}

@Composable
private fun MuscleRadarChart(
    muscles: List<MuscleFrequencyUiItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(muscles) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val progress = animProgress.value

    Canvas(modifier = modifier) {
        if (muscles.isEmpty()) return@Canvas

        val count = muscles.size.coerceAtMost(8)
        val displayMuscles = muscles.take(count)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val maxRadius = minOf(cx, cy) * 0.62f
        val levels = 4

        // Draw grid rings
        for (level in 1..levels) {
            val r = maxRadius * (level.toFloat() / levels)
            drawRadarPolygon(
                cx = cx, cy = cy,
                radius = r,
                sides = count,
                color = LightGrey.copy(alpha = 0.12f),
                fill = false,
                strokeWidth = 1f
            )
        }

        // Draw axis spokes
        for (i in 0 until count) {
            val angle = (2 * PI / count * i - PI / 2).toFloat()
            val spokeEnd = Offset(cx + maxRadius * cos(angle), cy + maxRadius * sin(angle))
            drawLine(
                color = LightGrey.copy(alpha = 0.18f),
                start = Offset(cx, cy),
                end = spokeEnd,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw data polygon (animated)
        val maxPct = displayMuscles.maxOfOrNull { it.percentage }?.coerceAtLeast(1f) ?: 1f
        val dataPath = Path()

        displayMuscles.forEachIndexed { index, muscle ->
            val angle = (2 * PI / count * index - PI / 2).toFloat()
            val pctNorm = (muscle.percentage / maxPct).coerceIn(0f, 1f)
            val r = maxRadius * pctNorm * progress
            val px = cx + r * cos(angle)
            val py = cy + r * sin(angle)
            if (index == 0) dataPath.moveTo(px, py) else dataPath.lineTo(px, py)
        }
        dataPath.close()

        // Filled area
        drawPath(
            path = dataPath,
            color = CrayolaBlue.copy(alpha = 0.22f * progress)
        )
        // Stroke
        drawPath(
            path = dataPath,
            color = CrayolaBlue.copy(alpha = progress),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Dot at each vertex + label
        displayMuscles.forEachIndexed { index, muscle ->
            val angle = (2 * PI / count * index - PI / 2).toFloat()
            val pctNorm = (muscle.percentage / maxPct).coerceIn(0f, 1f)
            val r = maxRadius * pctNorm * progress
            val px = cx + r * cos(angle)
            val py = cy + r * sin(angle)

            drawCircle(color = CrayolaBlue, radius = 4.dp.toPx(), center = Offset(px, py))

            // Label at spoke tip
            val labelR = maxRadius + 14.dp.toPx()
            val lx = cx + labelR * cos(angle)
            val ly = cy + labelR * sin(angle)
            drawMuscleLabel(textMeasurer, muscle.muscleGroup.take(3).uppercase(), lx, ly)
        }
    }
}

private fun DrawScope.drawRadarPolygon(
    cx: Float,
    cy: Float,
    radius: Float,
    sides: Int,
    color: Color,
    fill: Boolean,
    strokeWidth: Float = 1f
) {
    val path = Path()
    for (i in 0 until sides) {
        val angle = (2 * PI / sides * i - PI / 2).toFloat()
        val x = cx + radius * cos(angle)
        val y = cy + radius * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    if (fill) {
        drawPath(path, color)
    } else {
        drawPath(path, color, style = Stroke(width = strokeWidth.dp.toPx()))
    }
}

private fun DrawScope.drawMuscleLabel(
    textMeasurer: TextMeasurer,
    text: String,
    cx: Float,
    cy: Float
) {
    val measured = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = LightGrey,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    )
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(cx - measured.size.width / 2f, cy - measured.size.height / 2f)
    )
}

@Composable
private fun MuscleFrequencyLegend(
    muscles: List<MuscleFrequencyUiItem>,
    modifier: Modifier = Modifier
) {
    val sorted = muscles.sortedByDescending { it.hitCount }
    val maxHit = sorted.firstOrNull()?.hitCount?.coerceAtLeast(1) ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        sorted.take(8).forEach { muscle ->
            MuscleFrequencyLegendRow(muscle = muscle, maxHit = maxHit)
        }
    }
}

@Composable
private fun MuscleFrequencyLegendRow(
    muscle: MuscleFrequencyUiItem,
    maxHit: Int
) {
    val barFraction = (muscle.hitCount.toFloat() / maxHit).coerceIn(0f, 1f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = muscle.muscleGroup.replaceFirstChar { it.uppercase() },
                color = LightGrey,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${muscle.hitCount}x",
                color = PureWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ShadowGrey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .fillMaxSize()
                    .background(CrayolaBlue.copy(alpha = 0.8f))
            )
        }
    }
}
