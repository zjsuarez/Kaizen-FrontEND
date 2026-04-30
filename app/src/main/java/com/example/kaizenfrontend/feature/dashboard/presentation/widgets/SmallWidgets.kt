package com.example.kaizenfrontend.feature.dashboard.presentation.widgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.KaizenWidgetContainer
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.PrGold
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.SubtleRed


// ──────────────────────────────────────────────────────────────
// Streak Widget
// ──────────────────────────────────────────────────────────────
@Composable
fun Modifier.StreakWidget(streakDays: Int?, recordStreak: Int = 12) {
    val currentStreak = streakDays ?: 0
    val isNewRecord = currentStreak >= recordStreak && currentStreak > 0
    val iconTint = if (isNewRecord) PrGold else CrayolaBlue

    KaizenWidgetContainer(modifier = this) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Top: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_streak),
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                        text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_streak),
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                )
            }

            // Bottom: big number + subtitle + record
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                            text = streakDays?.toString() ?: "--",
                            color = PureWhite,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_days),
                            color = LightGrey,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier =
                                    Modifier.alignByBaseline()
                            )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val recordText = if (isNewRecord) {
                    stringResource(id = com.example.kaizenfrontend.R.string.dashboard_new_record)
                } else {
                    stringResource(id = com.example.kaizenfrontend.R.string.dashboard_record, recordStreak)
                }
                val recordWeight = if (isNewRecord) FontWeight.Bold else FontWeight.Normal
                Text(
                        text = recordText,
                        color = LightGrey.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = recordWeight
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Average Time Widget
// ──────────────────────────────────────────────────────────────
@Composable
fun AvgTimeWidget(minutes: Int?, trendDiffMinutes: Int = 0, modifier: Modifier = Modifier) {
    KaizenWidgetContainer(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Top: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_avg_time),
                        tint = CrayolaBlue,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                        text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_avg_time),
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                )
            }

            // Bottom: big number + unit + trend
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                            text = minutes?.toString() ?: "--",
                            color = PureWhite,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = stringResource(id = com.example.kaizenfrontend.R.string.dashboard_min),
                            color = LightGrey,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.alignByBaseline()
                    )
                }

                // Trend indicator
                if (trendDiffMinutes != 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val isUp = trendDiffMinutes > 0
                    // Up = slower (bad) → subtle red, Down = faster (good) → green
                    val trendColor = if (isUp) SubtleRed else MalachiteGreen
                    val trendIcon =
                            if (isUp) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
                    val trendText =
                            if (isUp) {
                                stringResource(id = com.example.kaizenfrontend.R.string.dashboard_trend_up, trendDiffMinutes)
                            } else {
                                stringResource(id = com.example.kaizenfrontend.R.string.dashboard_trend_down, trendDiffMinutes)
                            }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = trendIcon,
                                contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.statistics_trend),
                                tint = trendColor,
                                modifier = Modifier.size(16.dp)
                        )
                        Text(
                                text = trendText,
                                color = trendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Highlighted 1RM Widget
// ──────────────────────────────────────────────────────────────
@SuppressLint("DefaultLocale")
@Composable
fun OneRmWidget(
    exercise: String,
    weight: Double?,
    isNewPr: Boolean = false,
    weightIncrease: Double = 0.0,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val iconTint = if (isNewPr) PrGold else CrayolaBlue

    KaizenWidgetContainer(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Top: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.statistics_estimated_1rm_title),
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                        text = stringResource(id = com.example.kaizenfrontend.R.string.statistics_estimated_1rm_title),
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                )
            }

            // Bottom: weight + PR badge + exercise name
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Format: strip ".0" for clean integers
                    val displayWeight =
                            when {
                                weight == null -> "--"
                                weight % 1.0 == 0.0 -> weight.toInt().toString()
                                else -> String.format("%.1f", weight)
                            }
                    Text(
                            text = displayWeight,
                            color = PureWhite,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = stringResource(id = com.example.kaizenfrontend.R.string.settings_unit_kg),
                            color = LightGrey,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                    )

                    // PR increase badge
                    if (isNewPr && weightIncrease > 0.0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val increaseText =
                                if (weightIncrease % 1.0 == 0.0) {
                                    "+${weightIncrease.toInt()}"
                                } else {
                                    "+${String.format("%.1f", weightIncrease)}"
                                }
                        Text(
                                text = increaseText,
                                color = MalachiteGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                        text = exercise,
                        color = LightGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun StreakWidgetPreview() {
    Modifier.StreakWidget(streakDays = 5)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun AvgTimeWidgetFasterPreview() {
    AvgTimeWidget(minutes = 62, trendDiffMinutes = -3)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun AvgTimeWidgetSlowerPreview() {
    AvgTimeWidget(minutes = 68, trendDiffMinutes = 5)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun OneRmWidgetPrPreview() {
    OneRmWidget(exercise = "Bench Press", weight = 105.0, isNewPr = true, weightIncrease = 2.5)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun OneRmWidgetNoPrPreview() {
    OneRmWidget(exercise = "Squat", weight = 140.0, isNewPr = false)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun StreakWidgetRecordPreview() {
    Modifier.StreakWidget(streakDays = 15)
}
