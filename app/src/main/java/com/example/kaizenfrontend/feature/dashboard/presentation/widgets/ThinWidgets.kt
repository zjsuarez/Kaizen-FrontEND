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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.OpenInNew
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
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.SubtleRed

// ──────────────────────────────────────────────────────────────
// Last Session Widget
// ──────────────────────────────────────────────────────────────

@Composable
fun LastSessionWidget(
    routineName: String?,
    planName: String?,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_last_session),
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(com.example.kaizenfrontend.R.string.dashboard_last_session),
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                if (onClick != null) {
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = LightGrey.copy(alpha = 0.35f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = routineName ?: "--",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!planName.isNullOrBlank()) {
                    Text(
                        text = planName,
                        color = CrayolaBlue.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
    weightTimestamp: String? = null,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = stringResource(com.example.kaizenfrontend.R.string.dashboard_body_weight),
                    tint = CrayolaBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(com.example.kaizenfrontend.R.string.dashboard_body_weight),
                    color = LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                if (onClick != null) {
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = LightGrey.copy(alpha = 0.35f),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val displayWeight = if (currentWeight != null) {
                        if (currentWeight % 1.0 == 0.0) currentWeight.toInt().toString()
                        else String.format("%.1f", currentWeight)
                    } else "--"
                    Text(
                        text = displayWeight,
                        color = PureWhite,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = stringResource(com.example.kaizenfrontend.R.string.settings_unit_kg),
                        color = LightGrey,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                if (trendLabel != null) {
                    val trendColor = when (isPositive) {
                        true -> SubtleRed.copy(alpha = 0.8f)
                        false -> MalachiteGreen.copy(alpha = 0.8f)
                        null -> LightGrey.copy(alpha = 0.6f)
                    }
                    Text(
                        text = trendLabel,
                        color = trendColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                if (!weightTimestamp.isNullOrBlank()) {
                    Text(
                        text = weightTimestamp,
                        color = LightGrey.copy(alpha = 0.4f),
                        fontSize = 9.sp
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
private fun LastSessionWidgetPreview() {
    LastSessionWidget(
        routineName = "Pull Day",
        planName = "PPL Program",
        timeLabel = "Yesterday",
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun WeightTrendWidgetPreview() {
    WeightTrendWidget(
        currentWeight = 82.5,
        trendLabel = "-0.5 kg",
        weightTimestamp = "18 May",
        isPositive = false,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A0F, widthDp = 360, heightDp = 80)
@Composable
private fun WeightTrendNullPreview() {
    WeightTrendWidget(currentWeight = null, trendLabel = null, isPositive = null)
}
