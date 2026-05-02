package com.example.kaizenfrontend.feature.statistics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.navigation.KaizenTabScaffold
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.statistics.presentation.components.BodyWeightTrendWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.Estimated1RmWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.MuscleFrequencyWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.RepRangeWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.VolumeTrendWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.FatigueCorrelationWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.SessionEfficiencyWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.RestTimeDensityWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.ActivityHeatmapWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.PrFrequencyHeatmapWidget
import com.example.kaizenfrontend.feature.statistics.presentation.components.PrPeakTimeWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val bodyWeightProducer = viewModel.bodyWeightModelProducer
    val oneRmProducer = viewModel.estimated1RmModelProducer
    val volumeProducer = viewModel.volumeBarModelProducer
    val fatigueProducer = viewModel.fatigueModelProducer

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatistics()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    KaizenTabScaffold(
        navController = navController,
        title = stringResource(id = R.string.statistics_title)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                TimeRangeSelector(
                    selectedRange = uiState.selectedTimeRange,
                    onRangeSelected = { viewModel.updateTimeRange(it) }
                )
            }

            // Strength & Health 
            item {
                SectionHeader(title = stringResource(id = R.string.statistics_strength_health))
            }

            item {
                BodyWeightTrendWidget(
                    uiState = uiState.bodyWeightChart,
                    modelProducer = bodyWeightProducer
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Estimated1RmWidget(
                    uiState = uiState.estimated1RmChart,
                    modelProducer = oneRmProducer,
                    exercises = uiState.exercises,
                    selectedExerciseId = uiState.selectedExerciseId,
                    onExerciseSelected = viewModel::selectExercise
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Hypertrophy & Overload 
            item {
                SectionHeader(title = stringResource(id = R.string.statistics_hypertrophy_overload))
            }

            item {
                VolumeTrendWidget(
                    uiState = uiState.volumeTrend,
                    modelProducer = volumeProducer
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                RepRangeWidget(uiState = uiState.repRange)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MuscleFrequencyWidget(uiState = uiState.muscleFrequency)
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Efficiency & Fatigue (The Brain)
            item {
                SectionHeader(title = stringResource(id = R.string.statistics_efficiency_fatigue))
            }

            item {
                FatigueCorrelationWidget(
                    uiState = uiState.fatigue,
                    modelProducer = fatigueProducer
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SessionEfficiencyWidget(uiState = uiState.efficiency)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                RestTimeDensityWidget(uiState = uiState.restTime)
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Discipline & Habits
            item {
                SectionHeader(title = stringResource(id = R.string.statistics_discipline_habits))
            }

            item {
                ActivityHeatmapWidget(uiState = uiState.activityHeatmap)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                PrFrequencyHeatmapWidget(uiState = uiState.prHeatmap)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                PrPeakTimeWidget(uiState = uiState.prPeakTime)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = LightGrey,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, top = 4.dp)
    )
}

@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CrayolaBlue.copy(alpha = 0.2f) else ShadowGrey)
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = range.labelResId),
                    color = if (isSelected) CrayolaBlue else LightGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
