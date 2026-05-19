package com.example.kaizenfrontend.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.dashboard.model.WidgetType

// ──────────────────────────────────────────────────────────────
// Widget metadata - display name & description for each type
// ──────────────────────────────────────────────────────────────

private data class WidgetInfo(
    val type: WidgetType,
    @StringRes val displayNameResId: Int,
    @StringRes val descriptionResId: Int
)

private val allWidgetInfos = listOf(
    WidgetInfo(WidgetType.NEXT_WORKOUT, com.example.kaizenfrontend.R.string.widget_next_workout_title, com.example.kaizenfrontend.R.string.widget_next_workout_desc),
    WidgetInfo(WidgetType.CALENDAR, com.example.kaizenfrontend.R.string.widget_calendar_title, com.example.kaizenfrontend.R.string.widget_calendar_desc),
    WidgetInfo(WidgetType.RECENT_PRS, com.example.kaizenfrontend.R.string.widget_recent_prs_title, com.example.kaizenfrontend.R.string.widget_recent_prs_desc),
    WidgetInfo(WidgetType.WEIGHT_TREND, com.example.kaizenfrontend.R.string.widget_weight_trend_title, com.example.kaizenfrontend.R.string.widget_weight_trend_desc),
    WidgetInfo(WidgetType.LAST_SESSION, com.example.kaizenfrontend.R.string.widget_last_session_title, com.example.kaizenfrontend.R.string.widget_last_session_desc),
    WidgetInfo(WidgetType.STREAK, com.example.kaizenfrontend.R.string.widget_streak_title, com.example.kaizenfrontend.R.string.widget_streak_desc),
    WidgetInfo(WidgetType.AVG_TIME, com.example.kaizenfrontend.R.string.widget_avg_time_title, com.example.kaizenfrontend.R.string.widget_avg_time_desc),
)

// ──────────────────────────────────────────────────────────────
// Bottom Sheet
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWidgetBottomSheet(
    sheetState: SheetState,
    currentWidgetOrder: List<String>,
    onDismiss: () -> Unit,
    onAddWidget: (String) -> Unit
) {
    // Widgets not yet on the dashboard
    val availableWidgets = allWidgetInfos.filter { info ->
        info.type.name !in currentWidgetOrder
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ShadowGrey,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = LightGrey) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = stringResource(id = com.example.kaizenfrontend.R.string.widget_add_title),
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = com.example.kaizenfrontend.R.string.widget_add_subtitle),
                color = LightGrey,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (availableWidgets.isEmpty()) {
                // All widgets already on dashboard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Onyx, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = com.example.kaizenfrontend.R.string.widget_all_added),
                        color = LightGrey,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Widget list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    items(
                        items = availableWidgets,
                        key = { it.type.name }
                    ) { info ->
                        AddWidgetRow(
                            info = info,
                            onAdd = { onAddWidget(info.type.name) }
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// Single Widget Row
// ──────────────────────────────────────────────────────────────

@Composable
private fun AddWidgetRow(
    info: WidgetInfo,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Onyx, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = stringResource(id = info.displayNameResId),
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = info.descriptionResId),
                color = LightGrey,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        FilledIconButton(
            onClick = onAdd,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = CrayolaBlue,
                contentColor = Color.White
            ),
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = com.example.kaizenfrontend.R.string.widget_add_title),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
