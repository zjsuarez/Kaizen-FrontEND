package com.example.kaizenfrontend.feature.workouts.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.presentation.components.CreatePlanBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.RoutineDetailsSheetContent
import com.example.kaizenfrontend.feature.workouts.presentation.components.RoutineWizardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showCreateRoutineWizard by remember { mutableStateOf(false) }
    var selectedRoutineForDetails by remember { mutableStateOf<Routine?>(null) }
    var pendingDelete by remember { mutableStateOf<DeleteTarget?>(null) }

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
                if (!isEditMode) {
                    FloatingActionButton(
                        onClick = { isEditMode = true },
                        containerColor = ShadowGrey,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Enter edit mode")
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                        showCreateRoutineWizard = true
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isEditMode = false
                                showFabMenu = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShadowGrey)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Done", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
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
                                    isEditMode = isEditMode,
                                    onClick = { viewModel.togglePlanExpansion(plan.id) },
                                    onDeleteClick = {
                                        pendingDelete = DeleteTarget.Plan(
                                            planId = plan.id,
                                            planName = plan.name
                                        )
                                    },
                                    onMoveUpClick = { viewModel.movePlanUp(plan.id) },
                                    onMoveDownClick = { viewModel.movePlanDown(plan.id) }
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
                                        RoutineCard(
                                            routine = routine,
                                            isEditMode = isEditMode,
                                            onClick = { selectedRoutineForDetails = routine },
                                            onDeleteClick = {
                                                pendingDelete = DeleteTarget.Routine(
                                                    routineId = routine.id,
                                                    routineName = routine.name
                                                )
                                            },
                                            onMoveUpClick = { viewModel.moveRoutineUp(routine.id, plan.id) },
                                            onMoveDownClick = { viewModel.moveRoutineDown(routine.id, plan.id) }
                                        )
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
                                RoutineCard(
                                    routine = routine,
                                    isEditMode = isEditMode,
                                    onClick = { selectedRoutineForDetails = routine },
                                    onDeleteClick = {
                                        pendingDelete = DeleteTarget.Routine(
                                            routineId = routine.id,
                                            routineName = routine.name
                                        )
                                    },
                                    onMoveUpClick = { viewModel.moveRoutineUp(routine.id, null) },
                                    onMoveDownClick = { viewModel.moveRoutineDown(routine.id, null) }
                                )
                            }
                        }
                    }

                    if (showCreatePlanDialog) {
                        CreatePlanBottomSheet(
                            onDismiss = { showCreatePlanDialog = false },
                            onCreate = { name, desc, start, interval, cycleLength ->
                                showCreatePlanDialog = false
                                viewModel.createPlan(name, desc, start, interval, cycleLength)
                            }
                        )
                    }

                    if (showCreateRoutineWizard) {
                        val routineWizardViewModel: RoutineWizardViewModel =
                            androidx.lifecycle.viewmodel.compose.viewModel(key = "routine_wizard_vm")

                        LaunchedEffect(Unit) {
                            val allRoutines = state.routinesByPlanId.values.flatten() + state.unassignedRoutines
                            routineWizardViewModel.setAvailablePlans(state.plans)
                            routineWizardViewModel.setAvailableRoutines(allRoutines)
                            routineWizardViewModel.resetWizard()
                        }

                        ModalBottomSheet(
                            onDismissRequest = { showCreateRoutineWizard = false },
                            containerColor = Onyx,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            RoutineWizardScreen(
                                viewModel = routineWizardViewModel,
                                onCreateRoutine = { planId, name, description, schedulingValue, startingDate, selectedExercises ->
                                    viewModel.createRoutine(
                                        planId = planId,
                                        name = name,
                                        description = description,
                                        schedulingValue = schedulingValue,
                                        startingDate = startingDate,
                                        routineExercises = selectedExercises
                                    )
                                },
                                onWizardClosed = {
                                    showCreateRoutineWizard = false
                                }
                            )
                        }
                    }

                    pendingDelete?.let { target ->
                        ConfirmDeleteDialog(
                            target = target,
                            onDismiss = { pendingDelete = null },
                            onConfirm = {
                                when (target) {
                                    is DeleteTarget.Plan -> viewModel.deletePlan(target.planId)
                                    is DeleteTarget.Routine -> viewModel.deleteRoutine(target.routineId)
                                }
                                pendingDelete = null
                            }
                        )
                    }

                    selectedRoutineForDetails?.let { selectedRoutine ->
                        val routineDetailsViewModel: RoutineDetailsViewModel = viewModel(
                            key = "routine_details_${selectedRoutine.id}",
                            factory = RoutineDetailsViewModelFactory(selectedRoutine)
                        )
                        val routineDetailsState by routineDetailsViewModel.uiState.collectAsState()

                        ModalBottomSheet(
                            onDismissRequest = { selectedRoutineForDetails = null },
                            containerColor = Onyx,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            RoutineDetailsSheetContent(
                                state = routineDetailsState,
                                onEditClick = routineDetailsViewModel::toggleEditMode,
                                onStartClick = { },
                                onDoneClick = routineDetailsViewModel::saveChanges,
                                onTitleChange = routineDetailsViewModel::updateTitle,
                                onDescriptionChange = routineDetailsViewModel::updateDescription,
                                onRemoveExercise = routineDetailsViewModel::removeExercise,
                                onMoveExercise = routineDetailsViewModel::moveExercise,
                                onAddExerciseClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed interface DeleteTarget {
    data class Plan(
        val planId: String,
        val planName: String
    ) : DeleteTarget

    data class Routine(
        val routineId: String,
        val routineName: String
    ) : DeleteTarget
}

@Composable
private fun ConfirmDeleteDialog(
    target: DeleteTarget,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, message) = when (target) {
        is DeleteTarget.Plan -> {
            "Delete Plan" to "Are you sure you want to delete ${target.planName}?"
        }

        is DeleteTarget.Routine -> {
            "Delete Routine" to "Are you sure you want to delete ${target.routineName}?"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShadowGrey,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                color = LightGrey
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = LightGrey)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Delete", color = SubtleRed, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PlanHeaderItem(
    plan: TrainingPlan,
    isExpanded: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    val density = LocalDensity.current
    val maxVisualOffsetPx = remember(density) { with(density) { 56.dp.toPx() } }
    var dragVisualOffset by remember(plan.id) { mutableFloatStateOf(0f) }
    var isDragging by remember(plan.id) { mutableStateOf(false) }

    val animatedDragOffset by animateFloatAsState(
        targetValue = if (isDragging) dragVisualOffset else 0f,
        label = "plan_drag_offset"
    )
    val liftedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.015f else 1f,
        label = "plan_drag_scale"
    )
    val liftedElevation by animateDpAsState(
        targetValue = if (isDragging) 10.dp else 0.dp,
        label = "plan_drag_elevation"
    )
    val dragBackground by animateColorAsState(
        targetValue = if (isDragging) ShadowGrey.copy(alpha = 0.34f) else Color.Transparent,
        label = "plan_drag_background"
    )
    val liftedElevationPx = with(density) { liftedElevation.toPx() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = animatedDragOffset
                scaleX = liftedScale
                scaleY = liftedScale
                shadowElevation = liftedElevationPx
            }
            .clip(RoundedCornerShape(14.dp))
            .background(dragBackground)
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
            plan.interval?.takeIf { it.isNotBlank() }?.let { intervalValue ->
                Text(
                    text = PlanIntervalConfig.fromBackend(intervalValue, plan.cycleLength).toDisplayLabel(),
                    color = LightGrey,
                    fontSize = 12.sp
                )
            }
        }

        if (isEditMode) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete plan",
                    tint = SubtleRed
                )
            }
            DragReorderHandle(
                onMoveUp = onMoveUpClick,
                onMoveDown = onMoveDownClick,
                iconTint = LightGrey,
                threshold = 36.dp,
                contentDescription = "Reorder plan",
                onDragOffsetChange = { offset ->
                    dragVisualOffset = offset.coerceIn(-maxVisualOffsetPx, maxVisualOffsetPx)
                },
                onDragStateChange = { dragging ->
                    isDragging = dragging
                    if (!dragging) {
                        dragVisualOffset = 0f
                    }
                }
            )
        } else {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = LightGrey
            )
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    val density = LocalDensity.current
    val maxVisualOffsetPx = remember(density) { with(density) { 72.dp.toPx() } }
    var dragVisualOffset by remember(routine.id) { mutableFloatStateOf(0f) }
    var isDragging by remember(routine.id) { mutableStateOf(false) }

    val animatedDragOffset by animateFloatAsState(
        targetValue = if (isDragging) dragVisualOffset else 0f,
        label = "routine_drag_offset"
    )
    val liftedScale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        label = "routine_drag_scale"
    )
    val liftedElevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        label = "routine_drag_elevation"
    )
    val cardColor by animateColorAsState(
        targetValue = if (isDragging) ShadowGrey.copy(alpha = 0.9f) else ShadowGrey,
        label = "routine_drag_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDragging) CrayolaBlue.copy(alpha = 0.45f) else Color.Transparent,
        label = "routine_drag_border"
    )
    val liftedElevationPx = with(density) { liftedElevation.toPx() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .graphicsLayer {
                translationY = animatedDragOffset
                scaleX = liftedScale
                scaleY = liftedScale
                shadowElevation = liftedElevationPx
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (isDragging) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                        if (isEditMode) Modifier else Modifier.clickable { onClick() }
                )
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

            if (isEditMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete workout",
                        tint = SubtleRed
                    )
                }
                DragReorderHandle(
                    onMoveUp = onMoveUpClick,
                    onMoveDown = onMoveDownClick,
                    iconTint = LightGrey,
                    threshold = 36.dp,
                    contentDescription = "Reorder workout",
                    onDragOffsetChange = { offset ->
                        dragVisualOffset = offset.coerceIn(-maxVisualOffsetPx, maxVisualOffsetPx)
                    },
                    onDragStateChange = { dragging ->
                        isDragging = dragging
                        if (!dragging) {
                            dragVisualOffset = 0f
                        }
                    }
                )
            }
        }
    }
}

private class RoutineDetailsViewModelFactory(
    private val routine: Routine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineDetailsViewModel::class.java)) {
            return RoutineDetailsViewModel(
                routineId = routine.id,
                initialTitle = routine.name,
                initialDescription = routine.description,
                initialExercises = routine.exercises
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
private fun DragReorderHandle(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    iconTint: Color,
    threshold: Dp,
    contentDescription: String,
    onDragOffsetChange: (Float) -> Unit = {},
    onDragStateChange: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val thresholdPx = remember(threshold, density) { with(density) { threshold.toPx() } }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    var visualOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val handleBackground by animateColorAsState(
        targetValue = if (isDragging) CrayolaBlue.copy(alpha = 0.2f) else Color.Transparent,
        label = "drag_handle_background"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(CircleShape)
            .background(handleBackground)
            .pointerInput(onMoveUp, onMoveDown, thresholdPx, onDragOffsetChange, onDragStateChange) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumulator = 0f
                        visualOffset = 0f
                        isDragging = true
                        onDragStateChange(true)
                        onDragOffsetChange(0f)
                    },
                    onDragEnd = {
                        dragAccumulator = 0f
                        visualOffset = 0f
                        isDragging = false
                        onDragOffsetChange(0f)
                        onDragStateChange(false)
                    },
                    onDragCancel = {
                        dragAccumulator = 0f
                        visualOffset = 0f
                        isDragging = false
                        onDragOffsetChange(0f)
                        onDragStateChange(false)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.y
                        visualOffset += dragAmount.y

                        while (dragAccumulator >= thresholdPx) {
                            onMoveDown()
                            dragAccumulator -= thresholdPx
                            visualOffset -= thresholdPx * 0.65f
                        }

                        while (dragAccumulator <= -thresholdPx) {
                            onMoveUp()
                            dragAccumulator += thresholdPx
                            visualOffset += thresholdPx * 0.65f
                        }

                        onDragOffsetChange(visualOffset)
                    }
                )
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = contentDescription,
            tint = iconTint
        )
    }
}
