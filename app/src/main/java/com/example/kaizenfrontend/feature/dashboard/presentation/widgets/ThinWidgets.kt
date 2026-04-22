package com.example.kaizenfrontend.feature.dashboard.presentation.widgets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.components.KaizenWidgetContainer
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.MalachiteGreen
import com.example.kaizenfrontend.core.ui.theme.PureWhite

// ──────────────────────────────────────────────────────────────
// Recovery Time Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun RecoveryTimeWidget(
    hours: Int?,
    modifier: Modifier = Modifier
) {
    val isRecovered = hours != null && hours <= 0
    val displayHours = if (isRecovered) "0" else hours?.toString() ?: "--"
    val subtext = if (isRecovered) "Recovered" else if (hours != null) "Recovering" else "No Data"
    val accentColor = if (isRecovered) MalachiteGreen else CrayolaBlue

    KaizenWidgetContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Recovery",
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECOVERY",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Right: metric + subtitle
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayHours,
                        color = PureWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "h",
                        color = LightGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Text(
                    text = subtext,
                    color = LightGrey.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Last Session Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun LastSessionWidget(
    routineName: String?,
    timeLabel: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    KaizenWidgetContainer(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Last Session",
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LAST SESSION",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Right: routine name + time label
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = routineName ?: "--",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeLabel ?: "",
                    color = LightGrey.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Body Weight Trend Widget
// ──────────────────────────────────────────────────────────────

@SuppressLint("DefaultLocale")
@Composable
fun WeightTrendWidget(
    currentWeight: Double?,
    trendLabel: String?,
    isPositive: Boolean?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    KaizenWidgetContainer(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = "Body Weight",
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BODY WEIGHT",
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Right: weight + trend
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val displayWeight = if (currentWeight != null) {
                        if (currentWeight % 1.0 == 0.0) {
                            currentWeight.toInt().toString()
                        } else {
                            String.format("%.1f", currentWeight)
                        }
                    } else {
                        "--"
                    }
                    Text(
                        text = displayWeight,
                        color = PureWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "kg",
                        color = LightGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                if (trendLabel != null) {
                    Text(
                        text = trendLabel,
                        color = LightGrey.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun RecoveryTimeWidgetPreview() {
    RecoveryTimeWidget(hours = 48)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun RecoveryTimeReadyPreview() {
    RecoveryTimeWidget(hours = 12)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun LastSessionWidgetPreview() {
    LastSessionWidget(routineName = "Pull Day", timeLabel = "Yesterday")
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun WeightTrendWidgetPreview() {
    WeightTrendWidget(currentWeight = 82.5, trendLabel = "-0.5 kg this week", isPositive = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun WeightTrendNullPreview() {
    WeightTrendWidget(currentWeight = null, trendLabel = null, isPositive = null)
}
