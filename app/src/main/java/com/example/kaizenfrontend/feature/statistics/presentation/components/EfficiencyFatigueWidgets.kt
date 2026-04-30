package com.example.kaizenfrontend.feature.statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.statistics.presentation.EfficiencyUiState
import com.example.kaizenfrontend.feature.statistics.presentation.FatigueUiState
import com.example.kaizenfrontend.feature.statistics.presentation.RestTimeUiState
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.axis.vertical.endAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

@Composable
fun FatigueCorrelationWidget(
    uiState: FatigueUiState,
    modelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_fatigue_correlation_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_fatigue_correlation_subtitle),
        isEmpty = uiState.isEmpty,
        isLoading = uiState.isLoading,
        emptyMessage = uiState.message.resolve(),
        modifier = modifier
    ) {
        val bottomAxisFormatter = remember(uiState.dates) {
            AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                uiState.dates.getOrNull(value.toInt()) ?: ""
            }
        }

        Chart(
            chart = lineChart(
                lines = listOf(
                    lineSpec(lineColor = CrayolaBlue),
                    lineSpec(lineColor = SubtleRed)
                )
            ),
            chartModelProducer = modelProducer,
            startAxis = startAxis(
                valueFormatter = { value, _ ->
                    if (value >= 1000f) "${java.lang.String.format(java.util.Locale.US, "%.1f", value / 1000f)}k" else value.toInt().toString()
                }
            ),
            endAxis = endAxis(
                valueFormatter = { value, _ ->
                    val rpeValue = if (uiState.maxVolume > 0) (value / uiState.maxVolume) * 10f else 0f
                    java.lang.String.format(java.util.Locale.US, "%.1f", rpeValue.coerceIn(0f, 10f))
                }
            ),
            bottomAxis = bottomAxis(valueFormatter = bottomAxisFormatter)
        )
    }
}

@Composable
fun SessionEfficiencyWidget(
    uiState: EfficiencyUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_session_efficiency_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_session_efficiency_subtitle),
        isEmpty = uiState.isEmpty,
        isLoading = uiState.isLoading,
        emptyMessage = uiState.message.resolve(),
        modifier = modifier
    ) {
        if (uiState.points.isEmpty()) return@KaizenChartWidget

        val maxDuration = uiState.points.maxOfOrNull { it.durationMin.toFloat() }?.coerceAtLeast(30f) ?: 60f
        val maxVolume = uiState.points.maxOfOrNull { it.volume }?.coerceAtLeast(100f) ?: 100f

        val formatVolume = { vol: Float ->
            if (vol >= 1000f) "${java.lang.String.format(java.util.Locale.US, "%.1f", vol / 1000f)}k" else vol.toInt().toString()
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            // Y-axis (Volume)
            Column(
                modifier = Modifier.fillMaxHeight().padding(bottom = 20.dp, end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = formatVolume(maxVolume), color = LightGrey, fontSize = 10.sp)
                Text(text = formatVolume(maxVolume / 2f), color = LightGrey, fontSize = 10.sp)
                Text(text = "0", color = LightGrey, fontSize = 10.sp)
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Scatter Plot Canvas
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    val width = size.width
                    val height = size.height

                    // Draw grid
                    drawLine(
                        color = LightGrey.copy(alpha = 0.2f),
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = LightGrey.copy(alpha = 0.2f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, height),
                        strokeWidth = 2f
                    )

                    // Draw points
                    uiState.points.forEach { point ->
                        val x = (point.durationMin / maxDuration) * width
                        val y = height - ((point.volume / maxVolume) * height)
                        
                        drawCircle(
                            color = MalachiteGreen,
                            radius = 12f,
                            center = Offset(x, y),
                            alpha = 0.8f
                        )
                    }
                }

                // X-axis (Duration)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0m", color = LightGrey, fontSize = 10.sp)
                    Text(text = "${(maxDuration / 2f).toInt()}m", color = LightGrey, fontSize = 10.sp)
                    Text(text = "${maxDuration.toInt()}m", color = LightGrey, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun RestTimeDensityWidget(
    uiState: RestTimeUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = stringResource(id = com.example.kaizenfrontend.R.string.statistics_rest_time_density_title),
        subtitle = stringResource(id = com.example.kaizenfrontend.R.string.statistics_rest_time_density_subtitle),
        isEmpty = uiState.isEmpty,
        isLoading = uiState.isLoading,
        emptyMessage = uiState.message.resolve(),
        modifier = modifier
    ) {
        val maxPct = uiState.buckets.maxOfOrNull { it.percentage }?.coerceAtLeast(1f) ?: 100f

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            uiState.buckets.forEach { bucket ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    val heightRatio = bucket.percentage / maxPct
                    
                    Text(
                        text = "${bucket.percentage.toInt()}%",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(heightRatio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(CrayolaBlue)
                    )
                    
                    Text(
                        text = bucket.category,
                        color = LightGrey,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
