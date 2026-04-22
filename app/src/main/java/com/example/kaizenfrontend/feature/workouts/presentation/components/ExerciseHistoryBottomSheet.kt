package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import java.util.Locale

data class ExerciseHistoryTarget(
    val exerciseId: String,
    val exerciseName: String,
    val isCustomExercise: Boolean
)

private enum class WeightUnit {
    KG,
    LBS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryBottomSheet(
    target: ExerciseHistoryTarget,
    onDismissRequest: () -> Unit,
    viewModel: ExerciseHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context) }
    val weightUnit = remember(sessionManager) {
        val unit = sessionManager.getUserUnitSystem()?.uppercase(Locale.getDefault())
        if (unit == "LB" || unit == "LBS") WeightUnit.LBS else WeightUnit.KG
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val history = remember(
        uiState.workouts,
        target.exerciseId,
        target.exerciseName,
        target.isCustomExercise
    ) {
        viewModel.getExerciseHistory(
            exerciseId = target.exerciseId,
            isCustomExercise = target.isCustomExercise,
            exerciseName = target.exerciseName
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            androidx.compose.material3.BottomSheetDefaults.DragHandle(
                color = LightGrey
            )
        }
    ) {
        ExerciseHistorySheetContent(
            target = target,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            history = history,
            weightUnit = weightUnit,
            onRetry = viewModel::refresh
        )
    }
}

@Composable
private fun ExerciseHistorySheetContent(
    target: ExerciseHistoryTarget,
    isLoading: Boolean,
    errorMessage: String?,
    history: List<ExerciseHistoryWorkoutUi>,
    weightUnit: WeightUnit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.68f)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${target.exerciseName} - History",
            color = PureWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Recent workouts first",
            color = LightGrey,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        when {
            isLoading && history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CrayolaBlue)
                }
            }

            errorMessage != null && history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = errorMessage,
                            color = LightGrey,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrayolaBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            history.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history found for this exercise yet.",
                        color = LightGrey,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { workoutHistory ->
                        ExerciseHistoryWorkoutCard(
                            item = workoutHistory,
                            weightUnit = weightUnit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseHistoryWorkoutCard(
    item: ExerciseHistoryWorkoutUi,
    weightUnit: WeightUnit
) {
    val borderColor =
        if (item.isMostRecent) CrayolaBlue.copy(alpha = 0.7f) else PureWhite.copy(alpha = 0.08f)
    val containerColor =
        if (item.isMostRecent) CrayolaBlue.copy(alpha = 0.12f) else ShadowGrey.copy(alpha = 0.75f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.workoutLabel,
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${item.formattedDate} • ${item.formattedTime}",
                color = LightGrey,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (item.isMostRecent) {
                Text(
                    text = "Most recent workout",
                    color = CrayolaBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            ExerciseHistoryHeaderRow(weightUnit = weightUnit)
            Spacer(modifier = Modifier.height(6.dp))

            item.sets.forEach { set ->
                ExerciseHistorySetRow(
                    set = set,
                    weightUnit = weightUnit,
                    emphasize = item.isMostRecent
                )
            }
        }
    }
}

@Composable
private fun ExerciseHistoryHeaderRow(weightUnit: WeightUnit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set #",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = if (weightUnit == WeightUnit.KG) "Weight (kg)" else "Weight (lbs)",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Reps",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = "RPE",
            color = LightGrey.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ExerciseHistorySetRow(
    set: ExerciseHistorySetUi,
    weightUnit: WeightUnit,
    emphasize: Boolean
) {
    val valueColor = if (emphasize) PureWhite else PureWhite.copy(alpha = 0.9f)
    val valueWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${set.setNumber}",
            color = LightGrey,
            fontSize = 13.sp,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = set.weightKg.toDisplayWeight(weightUnit),
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = set.reps?.toString() ?: "-",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = set.rpe?.toString() ?: "-",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = valueWeight,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun Double?.toDisplayWeight(unit: WeightUnit): String {
    if (this == null) return "-"
    val converted = if (unit == WeightUnit.LBS) this * 2.20462 else this
    return String.format(Locale.getDefault(), "%.1f", converted)
}
