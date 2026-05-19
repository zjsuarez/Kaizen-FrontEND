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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.*
import com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalConfig
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan
import com.example.kaizenfrontend.feature.workouts.presentation.components.CreatePlanBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.ExerciseCatalogBottomSheet
import com.example.kaizenfrontend.feature.workouts.presentation.components.PlanDetailsSheetContent
import com.example.kaizenfrontend.feature.workouts.presentation.components.RoutineDetailsSheetContent
import com.example.kaizenfrontend.feature.workouts.presentation.components.WorkoutsEmptyState
import com.example.kaizenfrontend.feature.workouts.presentation.components.RoutineWizardScreen
import com.example.kaizenfrontend.feature.workouts.presentation.utils.RoutineScheduleCalculator
import com.example.kaizenfrontend.feature.workouts.domain.ActiveWorkoutManager
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.History
import com.example.kaizenfrontend.feature.workouts.presentation.components.WorkoutHistoryBottomSheet



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    viewModel: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val createRoutineSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val routineDetailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val planDetailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isEditMode by remember { mutableStateOf(false) }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showCreateRoutineWizard by remember { mutableStateOf(false) }
    var selectedRoutineForDetails by remember { mutableStateOf<Routine?>(null) }
    var selectedPlanForDetails by remember { mutableStateOf<TrainingPlan?>(null) }
    var selectedPlanRoutines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var pendingDelete by remember { mutableStateOf<DeleteTarget?>(null) }
    var showRoutineDetailsExerciseCatalog by remember { mutableStateOf(false) }
    var selectedFocusPlanId by rememberSaveable { mutableStateOf<String?>(null) }
    var showHistorySheet by remember { mutableStateOf(false) }
    val historyViewModel: WorkoutHistoryViewModel = viewModel(
        factory = WorkoutHistoryViewModelFactory(LocalContext.current.applicationContext)
    )
    val historyWorkouts by historyViewModel.workouts.collectAsState()
    val historyLoading by historyViewModel.isLoading.collectAsState()
    val historyError by historyViewModel.error.collectAsState()
    val historyPhotoUrls by historyViewModel.photoUrlByMeasurementId.collectAsState()
    val localContext = LocalContext.current
    val effortMetric = remember {
        com.example.kaizenfrontend.core.data.local.SessionManager(localContext)
            .getUserEffortMetric() ?: "RPE"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // EL FIX 1: Añadimos Modifier.weight(1f) a esta Column
                // Esto hace que el título ocupe el espacio disponible, empujando
                // a los botones a la derecha, pero SIN aplastarlos. Si el título
                // es muy largo, se cortará el título, protegiendo los botones.
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.workouts_title),
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(id = R.string.workouts_subtitle_library),
                        color = LightGrey,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp, end = 8.dp) // Un poco de aire por la derecha
                    )
                }

                // Mantenemos este Spacer pequeño por seguridad
                Spacer(modifier = Modifier.width(8.dp))

                if (!isEditMode) {
                    FloatingActionButton(
                        onClick = { isEditMode = true },
                        containerColor = ShadowGrey,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.workouts_enter_edit_mode_cd))
                    }
                } else {
                    Row(
                        modifier = Modifier.wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Single-action FAB: directly opens Create Plan.
                        // Routine creation lives inline at the bottom of the routine list.
                        FloatingActionButton(
                            onClick = { showCreatePlanDialog = true },
                            containerColor = CrayolaBlue,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlaylistAdd,
                                contentDescription = stringResource(id = R.string.workouts_create_plan),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Button(
                            onClick = { isEditMode = false },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ShadowGrey),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(id = R.string.workouts_done),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
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
                        Text(text = stringResource(id = R.string.workouts_error_prefix, state.message), color = Color.Red)
                    }
                }
                is WorkoutsUiState.Success -> {
                    if (state.plans.isEmpty()) {
                        WorkoutsEmptyState(
                            onCreatePlanClick = {
                                isEditMode = true
                                showCreatePlanDialog = true
                            }
                        )
                    } else {
                        val hasAnyRoutine = state.routinesByPlanId.values.any { it.isNotEmpty() } || state.unassignedRoutines.isNotEmpty()

                        if (!isEditMode) {
                            val preferredPlan = state.plans.firstOrNull { it.id == selectedFocusPlanId }
                                ?: state.plans.firstOrNull { it.isActive }
                                ?: state.plans.firstOrNull()

                            if (preferredPlan != null) {
                                LaunchedEffect(state.plans) {
                                    if (state.plans.none { it.id == selectedFocusPlanId }) {
                                        selectedFocusPlanId = preferredPlan.id
                                    }
                                }

                                val selectedPlan = preferredPlan
                                val selectedPlanRoutinesForFocus = state.routinesByPlanId[selectedPlan.id] ?: emptyList()

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    // Micro-header
                                    Text(
                                        text = stringResource(R.string.workouts_training_plans_header),
                                        color = LightGrey,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.8.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Plan tab selector
                                    PlanScrollableTabRow(
                                        plans = state.plans,
                                        selectedPlanId = selectedPlan.id,
                                        onSelectPlan = { selectedFocusPlanId = it }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Context bar (replaces FocusPlanOverviewCard)
                                    PlanContextBar(
                                        plan = selectedPlan,
                                        routineCount = selectedPlanRoutinesForFocus.size,
                                        onEditClick = {
                                            selectedPlanForDetails = selectedPlan
                                            selectedPlanRoutines = selectedPlanRoutinesForFocus
                                        },
                                        onSetActive = {
                                            viewModel.setPlanAsActive(selectedPlan.id)
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))


                                    if (selectedPlanRoutinesForFocus.isEmpty()) {
                                    FocusNoRoutineState(
                                        isFirstRoutineJourney = !hasAnyRoutine,
                                        onCreateRoutineClick = { showCreateRoutineWizard = true }
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 24.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        items(selectedPlanRoutinesForFocus, key = { it.id }) { routine ->
                                            val nextOccurrenceText = remember(routine, selectedPlan) {
                                                RoutineScheduleCalculator.getDisplayStringOrFallback(routine, selectedPlan)
                                            }
                                            RoutineScanCard(
                                                routine = routine,
                                                isEditMode = false,
                                                nextOccurrenceText = nextOccurrenceText,
                                                onClick = { selectedRoutineForDetails = routine },
                                                onDeleteClick = {},
                                                onMoveUpClick = {},
                                                onMoveDownClick = {}
                                            )
                                        }

                                        item(key = "add_routine_inline_${selectedPlan.id}") {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            OutlinedButton(
                                                onClick = { showCreateRoutineWizard = true },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
                                                shape = RoundedCornerShape(14.dp),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = CrayolaBlue.copy(alpha = 0.45f)
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = CrayolaBlue
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(
                                                        R.string.workouts_add_routine_to_plan,
                                                        selectedPlan.name
                                                    ),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                state.plans.forEach { plan ->
                                    item(key = "plan_${plan.id}") {
                                        val isExpanded = state.expandedPlanIds.contains(plan.id)
                                        PlanHeaderItem(
                                            plan = plan,
                                            isExpanded = isExpanded,
                                            isEditMode = isEditMode,
                                            onClick = {
                                                viewModel.togglePlanExpansion(plan.id)
                                            },
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

                                    if (!hasAnyRoutine) {
                                        item(key = "routine_hint_${plan.id}") {
                                            RoutineOnboardingHint(isEditMode = true)
                                        }
                                    }

                                    if (state.expandedPlanIds.contains(plan.id)) {
                                        val routines = state.routinesByPlanId[plan.id] ?: emptyList()
                                        if (routines.isEmpty()) {
                                            item(key = "empty_${plan.id}") {
                                                Text(
                                                    text = stringResource(id = R.string.workouts_no_routines_in_plan_hint),
                                                    color = LightGrey,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
                                                )
                                            }
                                        } else {
                                            items(routines, key = { it.id }) { routine ->
                                                val nextOccurrenceText = remember(routine, plan) {
                                                    RoutineScheduleCalculator.getDisplayStringOrFallback(routine, plan)
                                                }
                                                RoutineScanCard(
                                                    routine = routine,
                                                    isEditMode = true,
                                                    nextOccurrenceText = nextOccurrenceText,
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
                        val wizardState by routineWizardViewModel.uiState.collectAsState()
                        val wizardIsDirty = wizardState.name.isNotBlank() ||
                                wizardState.selectedExercises.isNotEmpty()
                        var showRoutineDiscardDialog by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            val allRoutines = state.routinesByPlanId.values.flatten() +
                                    state.unassignedRoutines
                            routineWizardViewModel.setAvailablePlans(state.plans)
                            routineWizardViewModel.setAvailableRoutines(allRoutines)
                            routineWizardViewModel.resetWizard(
                                preferredPlanId = selectedFocusPlanId
                            )
                        }

                        if (showRoutineDiscardDialog) {
                            AlertDialog(
                                onDismissRequest = { showRoutineDiscardDialog = false },
                                containerColor = ShadowGrey,
                                title = {
                                    Text(stringResource(R.string.workouts_discard_dialog_title),
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                },
                                text = {
                                    Text(stringResource(R.string.workouts_discard_dialog_message),
                                        color = LightGrey, fontSize = 14.sp)
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showRoutineDiscardDialog = false
                                        showCreateRoutineWizard = false
                                    }) {
                                        Text(stringResource(R.string.workouts_discard_confirm),
                                            color = SubtleRed, fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showRoutineDiscardDialog = false }) {
                                        Text(stringResource(R.string.workouts_discard_keep_editing),
                                            color = CrayolaBlue, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            )
                        }

                        ModalBottomSheet(
                            onDismissRequest = {
                                if (wizardIsDirty) showRoutineDiscardDialog = true
                                else showCreateRoutineWizard = false
                            },
                            sheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = true,
                                confirmValueChange = { targetValue ->
                                    if (targetValue == SheetValue.Hidden && wizardIsDirty) {
                                        showRoutineDiscardDialog = true
                                        false
                                    } else { true }
                                }
                            ),
                            containerColor = Onyx,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            RoutineWizardScreen(
                                viewModel = routineWizardViewModel,
                                onCreateRoutine = { planId, name, description,
                                                    schedulingValue, startingDate, selectedExercises ->
                                    viewModel.createRoutine(
                                        planId = planId, name = name,
                                        description = description,
                                        schedulingValue = schedulingValue,
                                        startingDate = startingDate,
                                        routineExercises = selectedExercises
                                    )
                                },
                                onWizardClosed = { showCreateRoutineWizard = false }
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

                    selectedPlanForDetails?.let { selectedPlan ->
                        val planDetailsViewModel: PlanDetailsViewModel = viewModel(
                            key = "plan_details_${selectedPlan.id}",
                            factory = PlanDetailsViewModelFactory(selectedPlan, selectedPlanRoutines)
                        )
                        val planDetailsState by planDetailsViewModel.uiState.collectAsState()

                        ModalBottomSheet(
                            onDismissRequest = {
                                selectedPlanForDetails = null
                            },
                            sheetState = planDetailsSheetState,
                            containerColor = Onyx,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            PlanDetailsSheetContent(
                                state = planDetailsState,
                                onDismiss = { selectedPlanForDetails = null },
                                onEditClick = planDetailsViewModel::toggleEditMode,
                                onDoneClick = {
                                    planDetailsViewModel.saveChanges()
                                    val updatedState = planDetailsViewModel.uiState.value
                                    val updatedPlan = selectedPlan.copy(
                                        name        = updatedState.title,
                                        description = updatedState.description,
                                        isActive    = updatedState.isActive
                                    )
                                    viewModel.savePlanEdits(updatedPlan)
                                },
                                onTitleChange       = planDetailsViewModel::updateTitle,
                                onDescriptionChange = planDetailsViewModel::updateDescription,
                                onRemoveRoutine     = planDetailsViewModel::removeRoutine,
                                onMoveRoutine       = planDetailsViewModel::moveRoutine,
                                onRoutineClick      = { routine ->
                                    selectedRoutineForDetails = routine
                                }
                            )
                        }
                    }

                    selectedRoutineForDetails?.let { selectedRoutine ->
                        val planForRoutine = (uiState as? WorkoutsUiState.Success)?.plans?.find { it.id == selectedRoutine.planId }
                        val routineDetailsViewModel: RoutineDetailsViewModel = viewModel(
                            key = "routine_details_${selectedRoutine.id}",
                            factory = RoutineDetailsViewModelFactory(selectedRoutine, planForRoutine)
                        )
                        val routineDetailsState by routineDetailsViewModel.uiState.collectAsState()

                        ModalBottomSheet(
                            onDismissRequest = {
                                selectedRoutineForDetails = null
                                showRoutineDetailsExerciseCatalog = false
                            },
                            sheetState = routineDetailsSheetState,
                            containerColor = Onyx,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            RoutineDetailsSheetContent(
                                state = routineDetailsState,
                                onEditClick = routineDetailsViewModel::toggleEditMode,
                                onStartClick = {
                                    ActiveWorkoutManager.startWorkout(
                                        routineId = selectedRoutine.id,
                                        routineName = selectedRoutine.name,
                                        exercises = routineDetailsState.exercises.map {
                                            com.example.kaizenfrontend.feature.workouts.domain.ActiveExerciseInit(
                                                id = it.exercise.id,
                                                name = it.exercise.name,
                                                isCustom = it.exercise.isCustom,
                                                targetSets = it.targetSets
                                            )
                                        }
                                    )
                                    selectedRoutineForDetails = null
                                },
                                onDoneClick = {
                                    routineDetailsViewModel.saveChanges()
                                    val updatedState = routineDetailsViewModel.uiState.value
                                    val updatedRoutine = selectedRoutine.copy(
                                        name = updatedState.title,
                                        description = updatedState.description,
                                        exercises = updatedState.exercises,
                                        schedulingValue = updatedState.schedulingValueString
                                    )
                                    viewModel.saveRoutineEdits(updatedRoutine)
                                },
                                onTitleChange = routineDetailsViewModel::updateTitle,
                                onDescriptionChange = routineDetailsViewModel::updateDescription,
                                onRemoveExercise = routineDetailsViewModel::removeExercise,
                                onMoveExercise = routineDetailsViewModel::moveExercise,
                                onAddExerciseClick = { showRoutineDetailsExerciseCatalog = true },
                                onWeekDayToggle = routineDetailsViewModel::toggleWeekDay,
                                onCycleDayToggle = routineDetailsViewModel::toggleCycleDay,
                                onRestDaysChange = routineDetailsViewModel::updateRestDaysBetweenWorkouts
                            )
                        }

                        if (showRoutineDetailsExerciseCatalog) {
                            ExerciseCatalogBottomSheet(
                                onDismissRequest = { showRoutineDetailsExerciseCatalog = false },
                                onExerciseSelected = { exercise ->
                                    routineDetailsViewModel.addExercise(exercise)
                                    showRoutineDetailsExerciseCatalog = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // History FAB — bottom right, above nav bar, only visible when workouts exist
        AnimatedVisibility(
            visible = historyWorkouts.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            FloatingActionButton(
                onClick = { showHistorySheet = true },
                containerColor = ShadowGrey,
                contentColor = CrayolaBlue,
                modifier = Modifier.size(52.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Workout History",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (showHistorySheet) {
            val successState = uiState as? WorkoutsUiState.Success
            WorkoutHistoryBottomSheet(
                workouts = historyWorkouts,
                plans = successState?.plans ?: emptyList(),
                routinesByPlanId = successState?.routinesByPlanId ?: emptyMap(),
                isLoading = historyLoading,
                error = historyError,
                effortMetric = effortMetric,
                photoUrlByMeasurementId = historyPhotoUrls,
                onDismiss = { showHistorySheet = false },
                onRefresh = { historyViewModel.loadWorkouts() }
            )
        }
    }
}

@Composable
private fun RoutineOnboardingHint(isEditMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.workouts_next_step),
            color = LightGrey.copy(alpha = 0.72f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(width = 3.dp, height = 34.dp)
                    .background(CrayolaBlue, RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.workouts_first_routine_prompt),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isEditMode) {
                        stringResource(id = R.string.workouts_hint_tap_plus_create_routine)
                    } else {
                        stringResource(id = R.string.workouts_hint_tap_edit_plus_create_routine)
                    },
                    color = LightGrey,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanScrollableTabRow(
    plans: List<TrainingPlan>,
    selectedPlanId: String,
    onSelectPlan: (String) -> Unit
) {
    val selectedIndex = plans.indexOfFirst { it.id == selectedPlanId }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        edgePadding = 0.dp,
        divider = {
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 1.dp)
        },
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp,
                    color = CrayolaBlue
                )
            }
        }
    ) {
        plans.forEachIndexed { index, plan ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onSelectPlan(plan.id) }
            ) {
                // Custom tab content - name + optional active dot
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = plan.name,
                        color = if (isSelected) PureWhite else LightGrey,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (plan.isActive) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CrayolaBlue, CircleShape)
                        )
                    } else {
                        // Reserve the same 6dp + 4dp spacing to keep tab heights uniform
                        Spacer(modifier = Modifier.size(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanContextBar(
    plan: TrainingPlan,
    routineCount: Int,
    onEditClick: () -> Unit,
    onSetActive: () -> Unit
) {
    val intervalLabel = plan.interval?.takeIf { it.isNotBlank() }?.let {
        PlanIntervalConfig.fromBackend(it, plan.cycleLength).toDisplayLabel()
    } ?: stringResource(R.string.workouts_cycle_weekly)

    val routineLabel = if (routineCount == 1)
        stringResource(R.string.workouts_routine_count_one)
    else
        stringResource(R.string.workouts_routine_count_other, routineCount)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: metadata subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$intervalLabel  ·  $routineLabel",
                color = LightGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Right: action zone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (plan.isActive) {
                // ACTIVE badge
                // Positive confirmation that this is the running mesocycle.
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = CrayolaBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CrayolaBlue.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = stringResource(R.string.workouts_plan_active_badge),
                        color = CrayolaBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            } else {
                // Set Active CTA
                TextButton(onClick = onSetActive) {
                    Text(
                        text = stringResource(R.string.workouts_set_as_active_plan),
                        color = CrayolaBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Edit plan details - always visible
            IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.workouts_edit_plan_cd),
                    tint = LightGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}



@Composable
private fun FocusPlanOverviewCard(
    plan: TrainingPlan,
    routineCount: Int,
    onOpenPlanDetails: () -> Unit,
    onSetActive: () -> Unit
) {
    val intervalLabel = plan.interval?.takeIf { it.isNotBlank() }?.let {
        PlanIntervalConfig.fromBackend(it, plan.cycleLength).toDisplayLabel()
    } ?: stringResource(id = R.string.workouts_cycle_weekly)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ShadowGrey.copy(alpha = 0.72f),
            border = BorderStroke(
                width = if (plan.isActive) 1.dp else 1.dp,
                color = if (plan.isActive) CrayolaBlue.copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.06f)
            ),
            onClick = onOpenPlanDetails
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = plan.name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        // ACTIVE badge - only shown when this is the active plan
                        if (plan.isActive) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = CrayolaBlue.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = stringResource(R.string.workouts_plan_active_badge),
                                    color = CrayolaBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(text = intervalLabel, color = LightGrey, fontSize = 12.sp)
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Onyx,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = if (routineCount == 1)
                            stringResource(R.string.workouts_routine_count_one)
                        else
                            stringResource(R.string.workouts_routine_count_other, routineCount),
                        color = LightGrey,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // "Set as Active Plan" CTA - only shown when this plan is NOT the active one
        if (!plan.isActive) {
            TextButton(
                onClick = onSetActive,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.workouts_set_as_active_plan),
                    color = CrayolaBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FocusNoRoutineState(
    isFirstRoutineJourney: Boolean,
    onCreateRoutineClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val iconAlpha by animateFloatAsState(
        targetValue = if (visible) 0.15f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "no_routine_icon_alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = CrayolaBlue.copy(alpha = iconAlpha)
        )

        Text(
            text = if (isFirstRoutineJourney) {
                stringResource(id = R.string.workouts_focus_no_routine_first_journey)
            } else {
                stringResource(id = R.string.workouts_focus_no_routine)
            },
            color = LightGrey,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Button(
            onClick = onCreateRoutineClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.workouts_no_routines_in_plan_cta),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
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
            stringResource(id = R.string.workouts_delete_plan_title) to
                stringResource(id = R.string.workouts_delete_plan_message, target.planName)
        }

        is DeleteTarget.Routine -> {
            stringResource(id = R.string.workouts_delete_routine_title) to
                stringResource(id = R.string.workouts_delete_routine_message, target.routineName)
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
                Text(text = stringResource(id = R.string.settings_cancel), color = LightGrey)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.workouts_delete), color = SubtleRed, fontWeight = FontWeight.Bold)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = plan.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (plan.isActive) {
                    Text(text = stringResource(id = R.string.workouts_active_suffix), color = CrayolaBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
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
                    contentDescription = stringResource(id = R.string.workouts_delete_plan_cd),
                    tint = SubtleRed
                )
            }
            DragReorderHandle(
                onMoveUp = onMoveUpClick,
                onMoveDown = onMoveDownClick,
                iconTint = LightGrey,
                threshold = 36.dp,
                contentDescription = stringResource(id = R.string.workouts_reorder_plan_cd),
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
private fun RoutineScanCard(
    routine: Routine,
    isEditMode: Boolean,
    nextOccurrenceText: String? = null,
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

    // Derive summary line from exercises list
    val exerciseCount = routine.exercises.size
    val totalSets = routine.exercises.sumOf { it.targetSets }
    val hasSummary = exerciseCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp)
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon
            Icon(
                imageVector = Icons.Outlined.FitnessCenter,
                contentDescription = null,
                tint = CrayolaBlue,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Main content column
            Column(modifier = Modifier.weight(1f)) {
                // Routine title
                Text(
                    text = routine.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Scannable summary: "X exercises · Y sets"
                if (hasSummary) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val summaryText = if (exerciseCount == 1) {
                        stringResource(id = R.string.workouts_routine_one_exercise_sets_summary, totalSets)
                    } else {
                        stringResource(id = R.string.workouts_routine_exercise_sets_summary, exerciseCount, totalSets)
                    }
                    Text(
                        text = summaryText,
                        color = LightGrey,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Schedule badge (only when there's a schedule and not in edit mode)
                if (!isEditMode && !nextOccurrenceText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = CrayolaBlue.copy(alpha = 0.12f),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = nextOccurrenceText,
                            color = CrayolaBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Edit mode controls
            if (isEditMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.workouts_delete_routine_cd),
                        tint = SubtleRed
                    )
                }
                DragReorderHandle(
                    onMoveUp = onMoveUpClick,
                    onMoveDown = onMoveDownClick,
                    iconTint = LightGrey,
                    threshold = 36.dp,
                    contentDescription = stringResource(id = R.string.workouts_reorder_routine_cd),
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
    private val routine: Routine,
    private val plan: TrainingPlan?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineDetailsViewModel::class.java)) {
            return RoutineDetailsViewModel(
                routine = routine,
                plan = plan
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private class PlanDetailsViewModelFactory(
    private val plan: TrainingPlan,
    private val routines: List<Routine>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlanDetailsViewModel::class.java)) {
            return PlanDetailsViewModel(
                plan = plan,
                initialTitle = plan.name,
                initialDescription = plan.description,
                initialRoutines = routines,
                initialIsActive = plan.isActive
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
