package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.presentation.components.CreatePlanDialog
import com.example.kaizenfrontend.feature.workouts.presentation.components.CreateRoutineDialog

@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    var showFabMenu by remember { mutableStateOf(false) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }

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
                Box {
                    FloatingActionButton(
                        onClick = { showFabMenu = true },
                        containerColor = CrayolaBlue,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Menu")
                    }

                    DropdownMenu(
                        expanded = showFabMenu,
                        onDismissRequest = { showFabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Create Plan") },
                            onClick = {
                                showFabMenu = false
                                showCreatePlanDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Create Workout") },
                            onClick = {
                                showFabMenu = false
                                showCreateRoutineDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is WorkoutsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CrayolaBlue)
                    }
                }
                is WorkoutsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                    }
                }
                is WorkoutsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // Leave room for bottom nav
                    ) {
                        state.plans.forEach { plan ->
                            item(key = "plan_${plan.id}") {
                                val isExpanded = state.expandedPlanIds.contains(plan.id)
                                PlanHeaderItem(
                                    plan = plan,
                                    isExpanded = isExpanded,
                                    onClick = { viewModel.togglePlanExpansion(plan.id) }
                                )
                            }
                            
                            if (state.expandedPlanIds.contains(plan.id)) {
                                val routines = state.routinesByPlanId[plan.id] ?: emptyList()
                                if (routines.isEmpty()) {
                                    item(key = "empty_${plan.id}") {
                                        Text(
                                            text = "No workouts in this plan yet.",
                                            color = LightGrey,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
                                        )
                                    }
                                } else {
                                    items(routines, key = { it.id }) { routine ->
                                        RoutineCard(routine = routine)
                                    }
                                }
                            }
                        }

                        if (state.unassignedRoutines.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Standalone Workouts",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                                )
                            }
                            items(state.unassignedRoutines, key = { "unassigned_${it.id}" }) { routine ->
                                RoutineCard(routine = routine)
                            }
                        }
                    }

                    if (showCreatePlanDialog) {
                        CreatePlanDialog(
                            onDismiss = { showCreatePlanDialog = false },
                            onCreate = { name, desc, start ->
                                showCreatePlanDialog = false
                                viewModel.createPlan(name, desc, start)
                            }
                        )
                    }

                    if (showCreateRoutineDialog) {
                        CreateRoutineDialog(
                            plans = state.plans,
                            onDismiss = { showCreateRoutineDialog = false },
                            onCreate = { planId, name, desc ->
                                showCreateRoutineDialog = false
                                viewModel.createRoutine(planId, name, desc)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanHeaderItem(
    plan: TrainingPlan,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = CrayolaBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = plan.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (plan.description.isNotBlank()) {
                Text(text = plan.description, color = LightGrey, fontSize = 13.sp)
            }
        }
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = LightGrey
        )
    }
}

@Composable
private fun RoutineCard(routine: Routine) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Open Workout Details */ }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = CrayolaBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = routine.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (routine.description.isNotBlank()) {
                    Text(text = routine.description, color = LightGrey, fontSize = 13.sp)
                }
            }
        }
    }
}
