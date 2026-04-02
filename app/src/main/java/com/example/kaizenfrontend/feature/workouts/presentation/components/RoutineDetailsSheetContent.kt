package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineDetailsState

@Composable
fun RoutineDetailsSheetContent(
    state: RoutineDetailsState,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isEditMode) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(Onyx),
        color = Onyx
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RoutineDetailsHeader(
                title = state.title,
                onEditClick = onEditClick,
                onStartClick = onStartClick
            )

            RoutineDetailsSummary(
                exerciseCount = state.exercises.size,
                totalSets = state.exercises.sumOf { it.targetSets }
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = state.exercises, key = { it.exercise.id }) { exercise ->
                    RoutineExerciseCard(exercise = exercise)
                }

                item {
                    RoutineDescription(description = state.description)
                }
            }
        }
    }
}

@Composable
private fun RoutineDetailsHeader(
    title: String,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PureWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit routine",
                    tint = PureWhite
                )
            }

            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrayolaBlue,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = "Start",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RoutineDetailsSummary(
    exerciseCount: Int,
    totalSets: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$exerciseCount exercises",
            color = LightGrey,
            fontSize = 14.sp
        )
        Text(
            text = "$totalSets sets",
            color = LightGrey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoutineExerciseCard(exercise: RoutineExercise) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ShadowGrey,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = exercise.exercise.name,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${exercise.targetSets} sets x ${exercise.targetReps} reps",
                color = LightGrey,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RoutineDescription(description: String) {
    Text(
        text = description,
        color = Color(0xFFD4D4D8),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
    )
}
