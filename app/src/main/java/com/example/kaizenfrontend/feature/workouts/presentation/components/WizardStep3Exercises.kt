package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import com.example.kaizenfrontend.feature.workouts.data.repository.MockExerciseRepository
import com.example.kaizenfrontend.feature.workouts.data.repository.ExerciseRepositoryImpl
import com.example.kaizenfrontend.feature.workouts.domain.model.Exercise
import com.example.kaizenfrontend.feature.workouts.domain.model.MuscleTarget
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.domain.repository.ExerciseRepository
import kotlinx.coroutines.launch

@Composable
fun WizardStep3Exercises(
    selectedExercises: List<RoutineExercise>,
    onAddExerciseClick: () -> Unit,
    onRemoveExercise: (String) -> Unit,
    showEmptyError: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.workouts_step3_title),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        if (selectedExercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.workouts_no_exercises_yet),
                        color = LightGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedButton(
                        onClick = onAddExerciseClick,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CrayolaBlue)
                    ) {
                        Text(
                            text = stringResource(R.string.workouts_add_first_exercise),
                            color = CrayolaBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showEmptyError) {
                        Text(
                            text = stringResource(R.string.workouts_add_at_least_one_exercise),
                            color = SubtleRed,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = onAddExerciseClick,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrayolaBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrayolaBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CrayolaBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(R.string.workouts_add_exercise))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(selectedExercises, key = { it.exercise.id }) { routineExercise ->
                    SelectedExerciseCard(
                        routineExercise = routineExercise,
                        onRemoveClick = { onRemoveExercise(routineExercise.exercise.id) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExerciseCatalogBottomSheet(
    onDismissRequest: () -> Unit,
    onExerciseSelected: (Exercise) -> Unit,
    exerciseRepository: ExerciseRepository? = null
) {
    val context = LocalContext.current
    val resolvedExerciseRepository = remember(exerciseRepository, context) {
        exerciseRepository ?: ExerciseRepositoryImpl(
            api = context.applicationContext.hiltServiceEntryPoint().exerciseApiService(),
            sessionManager = SessionManager(context),
            fallbackRepository = MockExerciseRepository()
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTarget by remember { mutableStateOf<MuscleTarget?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var showCreateWizard by remember { mutableStateOf(false) }
    var createWizardError by remember { mutableStateOf<String?>(null) }
    var isCreatingExercise by remember { mutableStateOf(false) }

    LaunchedEffect(resolvedExerciseRepository, refreshKey) {
        isLoading = true
        errorMessage = null

        val result = resolvedExerciseRepository.getExercises()
        if (result.isSuccess) {
            exercises = result.getOrNull().orEmpty()
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Unable to load exercises"
        }
        isLoading = false
    }

    val filteredExercises = remember(exercises, selectedTarget) {
        if (selectedTarget == null) {
            exercises
        } else {
            exercises.filter { it.muscleTarget == selectedTarget }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Onyx,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 44.dp, height = 5.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(99.dp),
                    color = LightGrey.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Exercise Catalog",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            OutlinedButton(
                onClick = { showCreateWizard = true },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrayolaBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrayolaBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CrayolaBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Create custom exercise",
                    fontWeight = FontWeight.SemiBold
                )
            }

            MuscleFilterRow(
                selectedTarget = selectedTarget,
                onTargetSelected = { selectedTarget = it }
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CrayolaBlue)
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = LightGrey,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                filteredExercises.isEmpty() -> {
                    Text(
                        text = "No exercises found for this filter",
                        color = LightGrey,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredExercises, key = { it.id }) { exercise ->
                            CatalogExerciseRow(
                                exercise = exercise,
                                onClick = { onExerciseSelected(exercise) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCreateWizard) {
        CreateCustomExerciseWizardOverlay(
            onDismissRequest = {
                if (!isCreatingExercise) {
                    showCreateWizard = false
                    createWizardError = null
                }
            },
            isSubmitting = isCreatingExercise,
            externalError = createWizardError,
            onClearExternalError = { createWizardError = null },
            onFinish = { command ->
                scope.launch {
                    isCreatingExercise = true
                    createWizardError = null
                    val createdResult = resolvedExerciseRepository.createCustomExercise(command)
                    if (createdResult.isSuccess) {
                        val created = createdResult.getOrNull()
                        showCreateWizard = false
                        selectedTarget = null
                        refreshKey += 1
                        createWizardError = null
                        if (created != null) {
                            onExerciseSelected(created)
                        }
                    } else {
                        val message = createdResult.exceptionOrNull()?.message
                            ?: "Unable to create custom exercise"
                        createWizardError = message
                        errorMessage = message
                    }
                    isCreatingExercise = false
                }
            }
        )
    }
}

@Composable
private fun MuscleFilterRow(
    selectedTarget: MuscleTarget?,
    onTargetSelected: (MuscleTarget?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChipItem(
                label = "All",
                isSelected = selectedTarget == null,
                onClick = { onTargetSelected(null) }
            )
        }

        items(MuscleTarget.entries) { target ->
            FilterChipItem(
                label = target.name.lowercase().replaceFirstChar { it.uppercase() },
                isSelected = selectedTarget == target,
                onClick = { onTargetSelected(target) }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CrayolaBlue.copy(alpha = 0.2f) else ShadowGrey,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) CrayolaBlue else Color.Transparent
        )
    ) {
        Text(
            text = label,
            color = if (isSelected) CrayolaBlue else LightGrey,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CatalogExerciseRow(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ShadowGrey,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = exercise.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.muscleTarget.name} • ${exercise.equipmentType.name}",
                color = LightGrey,
                fontSize = 12.sp
            )

            if (exercise.isCustom) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CUSTOM",
                    color = CrayolaBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SelectedExerciseCard(
    routineExercise: RoutineExercise,
    onRemoveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ShadowGrey,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routineExercise.exercise.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.workouts_remove_exercise_cd),
                        tint = LightGrey
                    )
                }
            }

            Text(
                text = "${routineExercise.exercise.muscleTarget.name} • ${routineExercise.exercise.equipmentType.name}",
                color = LightGrey,
                fontSize = 12.sp
            )

            Text(
                text = "${routineExercise.targetSets} sets x ${routineExercise.targetReps} reps",
                color = CrayolaBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
