package com.example.kaizenfrontend.feature.dashboard.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.KaizenWidgetContainer
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import java.util.Calendar

// ──────────────────────────────────────────────────────────────
// Shared accent colors (same as SmallWidgets)
// ──────────────────────────────────────────────────────────────

private val MalachiteGreen = Color(0xFF00E676)

// ──────────────────────────────────────────────────────────────
// Data class for Recent PRs
// ──────────────────────────────────────────────────────────────

data class RecentPrMock(
    val exercise: String,
    val weight: String,
    val weightIncrease: String = "",
    val timeAgo: String
)

// ──────────────────────────────────────────────────────────────
// Next Workout Widget (~200dp)
// ──────────────────────────────────────────────────────────────

@Composable
fun NextWorkoutWidget(
    routineName: String?,
    onStartClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    KaizenWidgetContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Next Workout",
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEXT WORKOUT",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Body: routine name centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = routineName ?: "No workout scheduled",
                    color = if (routineName != null) PureWhite else LightGrey,
                    fontSize = if (routineName != null) 32.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer: start button
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrayolaBlue,
                    contentColor = Onyx
                )
            ) {
                Text(
                    text = "START WORKOUT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Recent PRs Widget (~250dp)
// ──────────────────────────────────────────────────────────────

@Composable
fun RecentPrsWidget(
    prs: List<RecentPrMock>,
    modifier: Modifier = Modifier
) {
    KaizenWidgetContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Header: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Recent PRs",
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECENT PRs",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: PR list or empty state
            if (prs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent PRs yet.\nTime to lift heavy.",
                        color = LightGrey.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    prs.forEachIndexed { index, pr ->
                        PrRow(pr = pr)
                        if (index < prs.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp,
                                color = LightGrey.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrRow(pr: RecentPrMock) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: exercise name
        Text(
            text = pr.exercise,
            color = PureWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Right: weight + increase + time ago
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (pr.weightIncrease.isNotEmpty()) {
                    Text(
                        text = pr.weightIncrease,
                        color = MalachiteGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = pr.weight,
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = pr.timeAgo,
                color = LightGrey.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Calendar Widget (~250dp)
// ──────────────────────────────────────────────────────────────

@Composable
fun CalendarWidget(
    trainingDays: List<Int>,
    monthLabel: String = "APRIL 2026",
    onMonthPreviousClick: () -> Unit = {},
    onMonthNextClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current month info
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    // First day of month → day-of-week offset (Mon=0 .. Sun=6)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val rawDow = calendar.get(Calendar.DAY_OF_WEEK) // Sun=1, Mon=2 ...
    val startOffset = if (rawDow == Calendar.SUNDAY) 6 else rawDow - 2

    KaizenWidgetContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Header: icon + month name + nav chevrons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: icon + month label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = CrayolaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = monthLabel,
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }

                // Right: month navigation
                Row {
                    IconButton(
                        onClick = onMonthPreviousClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = LightGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMonthNextClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = LightGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day-of-week header row
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = LightGrey.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar grid: rows of 7
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1
                            val isValidDay = dayNumber in 1..daysInMonth

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isValidDay) {
                                    CalendarDayCell(
                                        day = dayNumber,
                                        isTrainingDay = dayNumber in trainingDays,
                                        isToday = dayNumber == today
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CrayolaBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Completed workouts",
                    color = LightGrey.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isTrainingDay: Boolean,
    isToday: Boolean
) {
    val bgColor = when {
        isTrainingDay -> CrayolaBlue
        else -> Color.Transparent
    }
    val borderMod = when {
        isToday && isTrainingDay -> Modifier.border(2.dp, PureWhite, CircleShape)
        isToday -> Modifier.border(1.5.dp, LightGrey, CircleShape)
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .then(borderMod),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = when {
                isTrainingDay -> Onyx
                isToday -> PureWhite
                else -> LightGrey.copy(alpha = 0.5f)
            },
            fontSize = 11.sp,
            fontWeight = if (isTrainingDay || isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 200)
@Composable
private fun NextWorkoutWidgetPreview() {
    NextWorkoutWidget(routineName = "Pull Day")
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 200)
@Composable
private fun NextWorkoutEmptyPreview() {
    NextWorkoutWidget(routineName = null)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 250)
@Composable
private fun RecentPrsWidgetPreview() {
    RecentPrsWidget(
        prs = listOf(
            RecentPrMock("Bench Press", "105 kg", "+2.5 kg", "2 days ago"),
            RecentPrMock("Squat", "140 kg", "+5 kg", "5 days ago"),
            RecentPrMock("Deadlift", "180 kg", "+2.5 kg", "1 week ago")
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 250)
@Composable
private fun RecentPrsEmptyPreview() {
    RecentPrsWidget(prs = emptyList())
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 280)
@Composable
private fun CalendarWidgetPreview() {
    CalendarWidget(
        trainingDays = listOf(1, 3, 5, 8, 10, 12, 15, 17, 19, 22, 24, 26, 29),
        monthLabel = "APRIL 2026"
    )
}
