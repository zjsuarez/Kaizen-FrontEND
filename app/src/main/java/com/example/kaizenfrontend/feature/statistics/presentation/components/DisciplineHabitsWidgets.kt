package com.example.kaizenfrontend.feature.statistics.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.PrGold
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.feature.statistics.presentation.HeatmapUiState
import com.example.kaizenfrontend.feature.statistics.presentation.PrPeakTimePointUi
import com.example.kaizenfrontend.feature.statistics.presentation.PrPeakTimeUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun ActivityHeatmapWidget(
    uiState: HeatmapUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = "Activity Consistency",
        subtitle = "GitHub-style streak map of workout days",
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        headerContent = {
            HeatmapHeader(
                totalHighlights = uiState.totalHighlights,
                startDate = uiState.startDate,
                endDate = uiState.endDate
            )
        },
        modifier = modifier
    ) {
        ContributionHeatmap(
            dayValues = uiState.dayValues,
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            journeyStartDate = uiState.journeyStartDate,
            activeColor = CrayolaBlue
        )
    }
}

@Composable
fun PrFrequencyHeatmapWidget(
    uiState: HeatmapUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = "PR Frequency",
        subtitle = "Days where personal records were achieved",
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        headerContent = {
            HeatmapHeader(
                totalHighlights = uiState.totalHighlights,
                startDate = uiState.startDate,
                endDate = uiState.endDate
            )
        },
        modifier = modifier
    ) {
        ContributionHeatmap(
            dayValues = uiState.dayValues,
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            journeyStartDate = uiState.journeyStartDate,
            activeColor = PrGold
        )
    }
}

@Composable
fun PrPeakTimeWidget(
    uiState: PrPeakTimeUiState,
    modifier: Modifier = Modifier
) {
    KaizenChartWidget(
        title = "PR Peak Time",
        subtitle = "When in the day your PRs usually happen",
        isLoading = uiState.isLoading,
        isEmpty = uiState.isEmpty,
        emptyMessage = uiState.message,
        headerContent = {
            if (!uiState.isLoading && !uiState.isEmpty) {
                val formatter = remember { DateTimeFormatter.ofPattern("MMM dd") }
                val start = uiState.startDate?.format(formatter).orEmpty()
                val end = uiState.endDate?.format(formatter).orEmpty()
                val rangeLabel = if (start.isNotBlank() && end.isNotBlank()) "$start to $end" else ""
                Text(
                    text = "${uiState.points.size} PR events${if (rangeLabel.isNotBlank()) "  |  $rangeLabel" else ""}",
                    color = LightGrey,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        },
        modifier = modifier
    ) {
        PrPeakTimeScatter(
            points = uiState.points,
            startDate = uiState.startDate,
            endDate = uiState.endDate
        )
    }
}

@Composable
private fun HeatmapHeader(
    totalHighlights: Int,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd") }
    val start = startDate?.format(formatter).orEmpty()
    val end = endDate?.format(formatter).orEmpty()
    val rangeLabel = if (start.isNotBlank() && end.isNotBlank()) "$start to $end" else ""

    Text(
        text = "$totalHighlights active days${if (rangeLabel.isNotBlank()) "  |  $rangeLabel" else ""}",
        color = LightGrey,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun ContributionHeatmap(
    dayValues: Map<LocalDate, Int>,
    startDate: LocalDate?,
    endDate: LocalDate?,
    journeyStartDate: LocalDate?,
    activeColor: Color
) {
    val start = startDate ?: return
    val end = endDate ?: return

    val alignedStart = remember(start) {
        start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
    val alignedEnd = remember(end) {
        end.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    }

    val totalDays = remember(alignedStart, alignedEnd) {
        (alignedEnd.toEpochDay() - alignedStart.toEpochDay() + 1L).coerceAtLeast(0L)
    }
    val weekCount = remember(totalDays) {
        ((totalDays + 6L) / 7L).toInt().coerceAtLeast(1)
    }

    val inactiveColor = LightGrey.copy(alpha = 0.14f)
    val outOfRangeColor = LightGrey.copy(alpha = 0.04f)
    val preJourneyColor = LightGrey.copy(alpha = 0.055f)
    val maxValue = remember(dayValues) {
        dayValues.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    }
    val rowSpacing = 3.dp
    val columnSpacing = 3.dp

    Column(modifier = Modifier.fillMaxSize()) {
        HeatmapLegend(activeColor = activeColor)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val availableHeight = maxHeight
                val availableWidth = maxWidth
                val axisAndGapWidth = 16.dp
                val gridAvailableWidth = (availableWidth - axisAndGapWidth).coerceAtLeast(48.dp)

                val cellHeight = ((availableHeight - (rowSpacing * 6)) / 7)
                    .coerceIn(6.dp, 28.dp)

                val targetCellWidth = ((gridAvailableWidth - (columnSpacing * (weekCount - 1))) / weekCount)
                val cellWidth = targetCellWidth.coerceAtLeast(4.dp)
                val needsScroll = cellWidth > targetCellWidth

                val gridModifier = if (needsScroll) {
                    Modifier.horizontalScroll(rememberScrollState())
                } else {
                    Modifier
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    DayAxisLabels(cellHeight = cellHeight, rowSpacing = rowSpacing)
                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        modifier = gridModifier,
                        horizontalArrangement = Arrangement.spacedBy(columnSpacing)
                    ) {
                        repeat(weekCount) { weekIndex ->
                            Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
                                repeat(7) { dayIndex ->
                                    val date = alignedStart.plusDays((weekIndex * 7L) + dayIndex.toLong())
                                    val inRange = date >= start && date <= end
                                    val dayValue = dayValues[date] ?: 0
                                    val intensity = (dayValue.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)

                                    val cellColor = when {
                                        !inRange -> outOfRangeColor
                                        journeyStartDate != null && date < journeyStartDate -> preJourneyColor
                                        dayValue > 0 -> activeColor.copy(alpha = heatmapAlpha(intensity))
                                        else -> inactiveColor
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(width = cellWidth, height = cellHeight)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(cellColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun heatmapAlpha(intensity: Float): Float {
    // Keep low values visible on dark surfaces while preserving strong visual separation at the top.
    return (0.28f + (intensity * 0.67f)).coerceIn(0.28f, 0.95f)
}

@Composable
private fun DayAxisLabels(
    cellHeight: androidx.compose.ui.unit.Dp,
    rowSpacing: androidx.compose.ui.unit.Dp
) {
    val labels = listOf("M", "", "W", "", "F", "", "")
    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.size(width = 10.dp, height = cellHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    color = LightGrey.copy(alpha = 0.85f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun HeatmapLegend(activeColor: Color) {
    val levels = listOf(0f, 0.33f, 0.66f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Less", color = LightGrey, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        levels.forEach { level ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(activeColor.copy(alpha = heatmapAlpha(level)))
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "More", color = LightGrey, fontSize = 10.sp)
    }
}

@Composable
private fun PrPeakTimeScatter(
    points: List<PrPeakTimePointUi>,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    val start = startDate ?: return
    val end = endDate ?: return

    val startEpoch = remember(start) { start.toEpochDay() }
    val daySpan = remember(start, end) { (end.toEpochDay() - start.toEpochDay()).coerceAtLeast(1L).toFloat() }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd") }
    val middleDate = remember(start, end) {
        start.plusDays(((end.toEpochDay() - start.toEpochDay()) / 2L).coerceAtLeast(0L))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf("24:00", "18:00", "12:00", "06:00", "00:00").forEach { label ->
                    Text(text = label, color = LightGrey, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(bottom = 18.dp)
            ) {
                val width = size.width
                val height = size.height

                for (index in 0..4) {
                    val y = height * (index / 4f)
                    drawLine(
                        color = LightGrey.copy(alpha = 0.18f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                for (index in 0..4) {
                    val x = width * (index / 4f)
                    drawLine(
                        color = LightGrey.copy(alpha = 0.12f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                }

                drawRect(
                    color = LightGrey.copy(alpha = 0.22f),
                    style = Stroke(width = 1.2f)
                )

                points.forEach { point ->
                    val x = ((point.date.toEpochDay() - startEpoch) / daySpan) * width
                    val y = height - ((point.minutesOfDay.toFloat() / 1440f) * height)

                    drawCircle(
                        color = PrGold,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y),
                        alpha = 0.95f
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = start.format(dateFormatter), color = LightGrey, fontSize = 10.sp)
            Text(text = middleDate.format(dateFormatter), color = LightGrey, fontSize = 10.sp)
            Text(text = end.format(dateFormatter), color = LightGrey, fontSize = 10.sp)
        }

        Text(
            text = "Date",
            color = PureWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
        )
    }
}
