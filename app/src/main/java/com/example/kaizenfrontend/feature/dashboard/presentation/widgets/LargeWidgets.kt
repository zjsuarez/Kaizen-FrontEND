package com.example.kaizenfrontend.feature.dashboard.presentation.widgets

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.KaizenWidgetContainer
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PrGold
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import java.util.Calendar

private val routineColorPalette = listOf(
    Color(0xFF5B8DEF),
    Color(0xFF4CAF79),
    Color(0xFFFFB44F),
    Color(0xFFB87FF5),
    Color(0xFFFF7B7B),
)

// ──────────────────────────────────────────────────────────────
// Data model for Recent PRs widget
// ──────────────────────────────────────────────────────────────

data class RecentPrMock(
    val exercise: String,
    val weight: String,
    val weightIncrease: String = "",
    val percentageImprovement: Double? = null,
    val timeAgo: String,
    val workoutId: String? = null
)

// ──────────────────────────────────────────────────────────────
// Next Workout Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun NextWorkoutWidget(
    routineName: String?,
    planName: String? = null,
    scheduledDateLabel: String? = null,
    isStartEnabled: Boolean = true,
    onStartClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isGhost: Boolean = false
) {
    KaizenWidgetContainer(modifier = modifier, onClick = if (isGhost) null else onClick) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_next_workout),
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(com.example.kaizenfrontend.R.string.dashboard_next_workout),
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Routine name + plan name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = routineName ?: if (isGhost) "--"
                        else stringResource(com.example.kaizenfrontend.R.string.dashboard_no_workout_scheduled),
                        color = if (routineName != null) PureWhite else LightGrey,
                        fontSize = if (routineName != null || isGhost) 24.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!planName.isNullOrBlank() && !isGhost) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = planName,
                            color = CrayolaBlue.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Button(
                onClick = onStartClick,
                enabled = !isGhost && isStartEnabled,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrayolaBlue,
                    contentColor = Onyx,
                    disabledContainerColor = ShadowGrey,
                    disabledContentColor = LightGrey.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (isStartEnabled)
                        stringResource(com.example.kaizenfrontend.R.string.dashboard_start_workout)
                    else scheduledDateLabel ?: "Not scheduled",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Recent PRs Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun RecentPrsWidget(
    prs: List<RecentPrMock>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onPrClick: (RecentPrMock) -> Unit = {},
    isGhost: Boolean = false
) {
    KaizenWidgetContainer(modifier = modifier, onClick = if (isGhost) null else onClick) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_recent_prs),
                            tint = PrGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(com.example.kaizenfrontend.R.string.dashboard_recent_prs),
                            color = LightGrey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    }
                    if (!isGhost && onClick != null) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = LightGrey.copy(alpha = 0.35f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (prs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(com.example.kaizenfrontend.R.string.dashboard_no_recent_prs),
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
                            PrRow(pr = pr, onPrClick = onPrClick, isGhost = isGhost)
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
}

@SuppressLint("DefaultLocale")
@Composable
private fun PrRow(pr: RecentPrMock, onPrClick: (RecentPrMock) -> Unit, isGhost: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isGhost) { onPrClick(pr) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pr.exercise,
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Percentage improvement badge
                val pct = pr.percentageImprovement
                if (pct != null) {
                    val pctColor = if (pct >= 0) MalachiteGreen else SubtleRed
                    val pctText = if (pct >= 0) "+${String.format("%.1f", pct)}%" else "${String.format("%.1f", pct)}%"
                    Text(pctText, color = pctColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                } else if (pr.weightIncrease.isNotEmpty()) {
                    Text(pr.weightIncrease, color = MalachiteGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                }
                Text(pr.weight, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(pr.timeAgo, color = LightGrey.copy(alpha = 0.6f), fontSize = 11.sp)
        }

        if (!isGhost) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LightGrey.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Calendar Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun CalendarWidget(
    completedDateMap: Map<String, String?> = emptyMap(),   // "YYYY-MM-DD" → routineName?
    scheduledDateMap: Map<String, String?> = emptyMap(),   // "YYYY-MM-DD" → routineName?
    activePlanName: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDayClick: (Int, Boolean) -> Unit = { _, _ -> },
    isGhost: Boolean = false
) {
    var currentMonthOffset by remember { mutableStateOf(0) }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, currentMonthOffset)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val realCalendar = Calendar.getInstance()
    val isCurrentMonth = realCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
            realCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
    val today = if (isCurrentMonth) realCalendar.get(Calendar.DAY_OF_MONTH) else -1

    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())
        ?.replaceFirstChar { it.uppercase() } ?: ""
    val year = calendar.get(Calendar.YEAR)
    val viewedMonthNum = calendar.get(Calendar.MONTH) + 1
    val dynamicMonthLabel = "$monthName $year"

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val rawDow = calendar.get(Calendar.DAY_OF_WEEK)
    val startOffset = if (rawDow == Calendar.SUNDAY) 6 else rawDow - 2

    // Filter date maps to the currently viewed month for rendering
    val viewedYearMonth = "$year-${viewedMonthNum.toString().padStart(2, '0')}"
    val completedDays: Map<Int, String?> = completedDateMap.entries
        .filter { it.key.startsWith(viewedYearMonth) }
        .associate { it.key.substring(8, 10).toInt() to it.value }
    val scheduledDays: Map<Int, String?> = scheduledDateMap.entries
        .filter { it.key.startsWith(viewedYearMonth) }
        .associate { it.key.substring(8, 10).toInt() to it.value }

    // Build routine→color from ALL data so palette is stable across month navigation
    val uniqueRoutines = (completedDateMap.values + scheduledDateMap.values)
        .filterNotNull().distinct()
    val routineColorMap: Map<String, Color> = uniqueRoutines.mapIndexed { index, name ->
        name to routineColorPalette[index % routineColorPalette.size]
    }.toMap()

    KaizenWidgetContainer(modifier = modifier, onClick = if (isGhost) null else onClick) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = CrayolaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            text = dynamicMonthLabel,
                            color = LightGrey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        if (!activePlanName.isNullOrBlank() && !isGhost) {
                            Text(
                                text = activePlanName,
                                color = CrayolaBlue.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isGhost && onClick != null) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = LightGrey.copy(alpha = 0.35f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = { currentMonthOffset-- }, enabled = !isGhost, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronLeft, null, tint = LightGrey, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { currentMonthOffset++ }, enabled = !isGhost, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, null, tint = LightGrey, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val dayLabels = listOf(
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_mon_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_tue_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_wed_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_thu_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_fri_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_sat_short),
                stringResource(com.example.kaizenfrontend.R.string.statistics_day_sun_short)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                dayLabels.forEach { label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(label, color = LightGrey.copy(alpha = 0.45f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1
                            val isValidDay = dayNumber in 1..daysInMonth
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                if (isValidDay) {
                                    val isCompleted = completedDays.containsKey(dayNumber)
                                    val isScheduled = !isCompleted && scheduledDays.containsKey(dayNumber)
                                    val routineName = completedDays[dayNumber] ?: scheduledDays[dayNumber]
                                    val routineColor = routineName?.let { routineColorMap[it] }
                                        ?: if (isCompleted || isScheduled) Color(0xFF5B8DEF) else null
                                    val isFuture = isCurrentMonth && dayNumber > today
                                    CalendarDayCell(
                                        day = dayNumber,
                                        isCompleted = isCompleted,
                                        isScheduled = isScheduled,
                                        isToday = dayNumber == today,
                                        isFuture = isFuture,
                                        routineColor = routineColor,
                                        isInteractive = !isGhost,
                                        onClick = { onDayClick(dayNumber, isCompleted) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Legend — dynamic per routine, falls back to generic when no names are available
            val legendEntries = routineColorMap.entries.toList()
            if (!isGhost && legendEntries.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    legendEntries.take(5).forEachIndexed { index, (name, color) ->
                        if (index > 0) Spacer(Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (name.length > 12) name.take(10) + "…" else name,
                                color = LightGrey,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            } else if (!isGhost) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CrayolaBlue))
                        Spacer(Modifier.width(4.dp))
                        Text("Trained", color = LightGrey, fontSize = 9.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape)
                                .border(1.dp, LightGrey.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Today", color = LightGrey, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isCompleted: Boolean,
    isScheduled: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    routineColor: Color?,
    isInteractive: Boolean,
    onClick: () -> Unit
) {
    val baseColor = routineColor ?: CrayolaBlue

    // Background: solid for completed, faint fill for scheduled, transparent otherwise
    val bgColor = when {
        isCompleted -> baseColor
        isScheduled -> baseColor.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    // Text: dark on solid completed circles; tinted for scheduled; extremely dim for unknown future
    val textColor = when {
        isCompleted -> Onyx
        isScheduled -> baseColor.copy(alpha = 0.80f)
        isToday -> PureWhite
        isFuture -> LightGrey.copy(alpha = 0.13f)   // almost invisible — not yet happened
        else -> LightGrey.copy(alpha = 0.35f)         // past rest day
    }

    // Border: white ring on today-completed; routine-color ring on scheduled; blue ring on today
    val borderMod = when {
        isCompleted && isToday -> Modifier.border(2.dp, PureWhite, CircleShape)
        isScheduled -> Modifier.border(1.5.dp, baseColor.copy(alpha = 0.60f), CircleShape)
        isToday -> Modifier.border(1.5.dp, CrayolaBlue, CircleShape)
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .then(borderMod)
            .clickable(enabled = isInteractive, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = when {
                isCompleted || isToday -> FontWeight.Bold
                isScheduled -> FontWeight.Medium
                else -> FontWeight.Normal
            },
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
    NextWorkoutWidget(routineName = "Pull Day", planName = "PPL Program", scheduledDateLabel = "Today", isStartEnabled = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 200)
@Composable
private fun NextWorkoutDisabledPreview() {
    NextWorkoutWidget(routineName = "Push Day", planName = "PPL Program", scheduledDateLabel = "Tomorrow", isStartEnabled = false)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 250)
@Composable
private fun RecentPrsWidgetPreview() {
    RecentPrsWidget(
        prs = listOf(
            RecentPrMock("Bench Press", "105 kg", percentageImprovement = 2.4, timeAgo = "2 days ago"),
            RecentPrMock("Squat", "140 kg", percentageImprovement = -1.1, timeAgo = "5 days ago"),
            RecentPrMock("Deadlift", "180 kg", percentageImprovement = 3.7, timeAgo = "1 week ago")
        ),
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 320)
@Composable
private fun CalendarWidgetPreview() {
    CalendarWidget(
        completedDateMap = mapOf(
            "2025-05-01" to "Push", "2025-05-03" to "Pull", "2025-05-06" to "Legs",
            "2025-05-08" to "Push", "2025-05-10" to "Pull", "2025-05-13" to "Legs",
            "2025-05-15" to "Push", "2025-05-17" to "Pull"
        ),
        scheduledDateMap = mapOf(
            "2025-05-22" to "Push", "2025-05-24" to "Pull", "2025-05-27" to "Legs"
        ),
        activePlanName = "PPL Program",
        onClick = {}
    )
}
