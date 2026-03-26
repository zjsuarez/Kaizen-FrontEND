package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.WorkoutItem

// TODO: Replace with real data from ViewModel
private val placeholderWorkouts = listOf(
    WorkoutItem("Bench Press", "Chest", 4),
    WorkoutItem("Pull-ups", "Back", 3),
    WorkoutItem("Overhead Press", "Shoulders", 3),
    WorkoutItem("Barbell Row", "Back", 4),
    WorkoutItem("Tricep Dips", "Arms", 3)
)

@Composable
fun WorkoutsScreen(
    // TODO: Inject real UiState from ViewModel
    onAddWorkoutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Workouts", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Your exercise library",
                        color = LightGrey,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                FloatingActionButton(
                    onClick = onAddWorkoutClick,
                    containerColor = CrayolaBlue,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Workout")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(placeholderWorkouts) { workout ->
                    WorkoutCard(workout = workout)
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(workout: WorkoutItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = CrayolaBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = workout.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = workout.muscleGroup, color = LightGrey, fontSize = 13.sp)
            }
            Text(text = "${workout.sets} sets", color = CrayolaBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenPreview() {
    MaterialTheme { WorkoutsScreen() }
}
