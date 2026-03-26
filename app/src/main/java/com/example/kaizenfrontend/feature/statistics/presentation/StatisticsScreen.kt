package com.example.kaizenfrontend.feature.statistics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*

@Composable
fun StatisticsScreen(
    // TODO: Inject real UiState from ViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp)
    ) {
        Text(text = "Statistics", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Track your progress",
            color = LightGrey,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.LocalFireDepartment, label = "Calories", value = "—", unit = "kcal")
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Timer, label = "Avg. Session", value = "—", unit = "min")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.DirectionsRun, label = "Workouts", value = "—", unit = "total")
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.BarChart, label = "Best Streak", value = "—", unit = "days")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ShadowGrey)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = CrayolaBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Progress Chart",
                        color = LightGrey,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, label: String, value: String, unit: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = CrayolaBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "$label ($unit)", color = LightGrey, fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    MaterialTheme { StatisticsScreen() }
}
