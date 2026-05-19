package com.example.kaizenfrontend.feature.dashboard.presentation.widgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
fun Modifier.StreakWidget(
    streakDays: Int?,
    recordStreak: Int = 12,
    onClick: (() -> Unit)? = null
) {
    val currentStreak = streakDays ?: 0
    val isNewRecord = currentStreak >= recordStreak && currentStreak > 0
    val iconTint = if (isNewRecord) PrGold else CrayolaBlue

    KaizenWidgetContainer(modifier = this, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_streak),
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(com.example.kaizenfrontend.R.string.dashboard_streak),
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = LightGrey.copy(alpha = 0.35f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = streakDays?.toString() ?: "--",
                        color = if (isNewRecord) PrGold else PureWhite,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(com.example.kaizenfrontend.R.string.dashboard_days),
                        color = LightGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Spacer(Modifier.height(2.dp))
                val recordText = if (isNewRecord)
                    stringResource(com.example.kaizenfrontend.R.string.dashboard_new_record)
                else
                    stringResource(com.example.kaizenfrontend.R.string.dashboard_record, recordStreak)
                Text(
                    text = recordText,
                    color = LightGrey.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = if (isNewRecord) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Average Time Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun AvgTimeWidget(
    minutes: Int?,
    trendDiffMinutes: Int = 0,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    KaizenWidgetContainer(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_avg_time),
                        tint = CrayolaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(com.example.kaizenfrontend.R.string.dashboard_avg_time),
                            color = LightGrey,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Last 14 Days",
                            color = LightGrey.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = LightGrey.copy(alpha = 0.35f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = minutes?.toString() ?: "--",
                        color = PureWhite,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 40.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(com.example.kaizenfrontend.R.string.dashboard_min),
                        color = LightGrey,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alignByBaseline()
                    )
                }

                if (trendDiffMinutes != 0) {
                    Spacer(Modifier.height(2.dp))
                    val isUp = trendDiffMinutes > 0
                    val trendColor = if (isUp) SubtleRed else MalachiteGreen
                    val trendIcon = if (isUp) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
                    val trendText = if (isUp)
                        stringResource(com.example.kaizenfrontend.R.string.dashboard_trend_up, trendDiffMinutes)
                    else
                        stringResource(com.example.kaizenfrontend.R.string.dashboard_trend_down, trendDiffMinutes)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(trendIcon, null, tint = trendColor, modifier = Modifier.size(16.dp))
                        Text(trendText, color = trendColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
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
    Modifier.StreakWidget(streakDays = 5, onClick = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun AvgTimeWidgetFasterPreview() {
    AvgTimeWidget(minutes = 62, trendDiffMinutes = -3, onClick = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 170, heightDp = 140)
@Composable
private fun StreakWidgetRecordPreview() {
    Modifier.StreakWidget(streakDays = 15, onClick = {})
}
