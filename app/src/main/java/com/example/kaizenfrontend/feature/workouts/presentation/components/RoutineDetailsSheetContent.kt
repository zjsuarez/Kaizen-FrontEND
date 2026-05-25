package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.model.RoutineExercise
import com.example.kaizenfrontend.feature.workouts.presentation.RoutineDetailsState
import kotlinx.coroutines.launch

@Composable
fun RoutineDetailsSheetContent(
    state: RoutineDetailsState,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit,
    onDoneClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRemoveExercise: (String) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onAddExerciseClick: () -> Unit,
    onWeekDayToggle: (java.time.DayOfWeek) -> Unit,
    onCycleDayToggle: (Int) -> Unit,
    onRestDaysChange: (Int) -> Unit,
    onUpdateExerciseSets: (exerciseId: String, targetSets: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var draggingExerciseId by remember { mutableStateOf<String?>(null) }
    var draggingItemOffsetY by remember { mutableFloatStateOf(0f) }
    var historyTarget by remember { mutableStateOf<ExerciseHistoryTarget?>(null) }

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
            Crossfade(targetState = state.isEditMode, label = "RoutineDetailsMode") { isEditMode ->
                RoutineDetailsHeader(
                    title = state.title,
                    isEditMode = isEditMode,
                    onEditClick = onEditClick,
                    onStartClick = onStartClick,
                    onDoneClick = onDoneClick,
                    onTitleChange = onTitleChange
                )
            }

            RoutineDetailsSummary(
                exerciseCount = state.exercises.size,
                totalSets = state.exercises.sumOf { it.targetSets }
            )

            if (state.planIntervalConfig != null) {
                RoutineDetailsScheduleEditor(
                    state = state,
                    onWeekDayToggle = onWeekDayToggle,
                    onCycleDayToggle = onCycleDayToggle,
                    onRestDaysChange = onRestDaysChange
                )
            }

            val listContainerModifier = if (state.isEditMode) {
                Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = CrayolaBlue,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(modifier = listContainerModifier) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items = state.exercises, key = { _, exercise -> exercise.exercise.id }) { _, exercise ->
                        val isDragging = draggingExerciseId == exercise.exercise.id
                        val dragModifier = if (state.isEditMode) {
                            Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .graphicsLayer {
                                    translationY = if (isDragging) draggingItemOffsetY else 0f
                                }
                        } else {
                            Modifier
                        }

                        Box(modifier = dragModifier) {
                            RoutineExerciseCard(
                                exercise = exercise,
                                isEditMode = state.isEditMode,
                                isDragging = isDragging,
                                onHistoryClick = {
                                    historyTarget = ExerciseHistoryTarget(
                                        exerciseId = exercise.exercise.id,
                                        exerciseName = exercise.exercise.name,
                                        isCustomExercise = exercise.exercise.isCustom
                                    )
                                },
                                onRemoveClick = { onRemoveExercise(exercise.exercise.id) },
                                onSetCountChange = { newSets ->
                                    onUpdateExerciseSets(exercise.exercise.id, newSets)
                                },
                                onDragHandleDragStart = {
                                    draggingExerciseId = exercise.exercise.id
                                    draggingItemOffsetY = 0f
                                },
                                onDragHandleDrag = { dragDelta ->
                                    if (draggingExerciseId != exercise.exercise.id) return@RoutineExerciseCard

                                    draggingItemOffsetY += dragDelta

                                    val currentIndex = state.exercises.indexOfFirst {
                                        it.exercise.id == exercise.exercise.id
                                    }
                                    if (currentIndex == -1) return@RoutineExerciseCard

                                    val currentItemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull {
                                        it.key == exercise.exercise.id
                                    } ?: return@RoutineExerciseCard

                                    val threshold = (currentItemInfo.size * 0.5f).coerceAtLeast(24f)

                                    if (draggingItemOffsetY > threshold && currentIndex < state.exercises.lastIndex) {
                                        onMoveExercise(currentIndex, currentIndex + 1)
                                        draggingItemOffsetY -= threshold
                                    } else if (draggingItemOffsetY < -threshold && currentIndex > 0) {
                                        onMoveExercise(currentIndex, currentIndex - 1)
                                        draggingItemOffsetY += threshold
                                    }

                                    val viewportStart = lazyListState.layoutInfo.viewportStartOffset
                                    val viewportEnd = lazyListState.layoutInfo.viewportEndOffset
                                    val itemTop = currentItemInfo.offset + draggingItemOffsetY
                                    val itemBottom = itemTop + currentItemInfo.size

                                    if (itemBottom > viewportEnd - 72) {
                                        coroutineScope.launch {
                                            val nextIndex = (lazyListState.firstVisibleItemIndex + 1)
                                                .coerceAtMost((lazyListState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))
                                            if (nextIndex != lazyListState.firstVisibleItemIndex) {
                                                lazyListState.animateScrollToItem(nextIndex)
                                            }
                                        }
                                    } else if (itemTop < viewportStart + 72) {
                                        coroutineScope.launch {
                                            val previousIndex = (lazyListState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                            if (previousIndex != lazyListState.firstVisibleItemIndex) {
                                                lazyListState.animateScrollToItem(previousIndex)
                                            }
                                        }
                                    }
                                },
                                onDragHandleDragEnd = {
                                    draggingExerciseId = null
                                    draggingItemOffsetY = 0f
                                }
                            )
                        }
                    }

                    if (state.isEditMode) {
                        item {
                            AddExerciseButton(onClick = onAddExerciseClick)
                        }
                    }
                }
            }

            RoutineDescription(
                description = state.description,
                isEditMode = state.isEditMode,
                onDescriptionChange = onDescriptionChange
            )

            historyTarget?.let { target ->
                ExerciseHistoryBottomSheet(
                    target = target,
                    onDismissRequest = { historyTarget = null }
                )
            }
        }
    }
}

@Composable
private fun RoutineDetailsHeader(
    title: String,
    isEditMode: Boolean,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit,
    onDoneClick: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditMode) {
            EditableTextWithPencil(
                value = title,
                onValueChange = onTitleChange,
                placeholder = stringResource(id = R.string.workouts_routine_title_placeholder),
                textStyle = TextStyle(
                    color = PureWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }

        if (isEditMode) {
            OutlinedButton(
                onClick = onDoneClick,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PureWhite),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PureWhite
                )
            ) {
                Text(text = stringResource(id = R.string.workouts_done), fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.workouts_edit_routine_cd),
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
                        text = stringResource(id = R.string.workouts_start),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableTextWithPencil(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(id = R.string.workouts_editable_field_cd),
            tint = LightGrey
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = LightGrey,
                        fontSize = textStyle.fontSize
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun AddExerciseButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PureWhite),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PureWhite
        )
    ) {
        Text(
            text = stringResource(id = R.string.workouts_add_exercise_button),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
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
            text = if (exerciseCount == 1) {
                stringResource(id = R.string.workouts_exercise_count_one)
            } else {
                stringResource(id = R.string.workouts_exercise_count_other, exerciseCount)
            },
            color = LightGrey,
            fontSize = 14.sp
        )
        Text(
            text = stringResource(id = R.string.workouts_sets_count, totalSets),
            color = LightGrey,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoutineExerciseCard(
    exercise: RoutineExercise,
    isEditMode: Boolean,
    isDragging: Boolean,
    onHistoryClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onSetCountChange: (Int) -> Unit,
    onDragHandleDragStart: () -> Unit,
    onDragHandleDrag: (Float) -> Unit,
    onDragHandleDragEnd: () -> Unit
) {
    var showMenu by remember(exercise.exercise.id) { mutableStateOf(false) }

    val cardColor = if (isDragging) Color(0xFF32313A) else ShadowGrey
    val cardBorderModifier = if (isDragging) {
        Modifier.border(1.dp, CrayolaBlue, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isEditMode) Modifier.clickable { showMenu = true } else Modifier)
            .then(cardBorderModifier),
        color = cardColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exercise.name,
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onRemoveClick) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(id = R.string.workouts_remove_exercise_cd),
                                tint = LightGrey
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pointerInput(exercise.exercise.id, isEditMode) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            onDragHandleDragStart()
                                        },
                                        onDragEnd = {
                                            onDragHandleDragEnd()
                                        },
                                        onDragCancel = {
                                            onDragHandleDragEnd()
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onDragHandleDrag(dragAmount.y)
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = stringResource(id = R.string.workouts_reorder_exercise_cd),
                                tint = LightGrey
                            )
                        }
                    }
                } else {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(id = R.string.workouts_exercise_options_cd),
                                tint = LightGrey
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = ShadowGrey
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.workouts_history),
                                        color = PureWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = LightGrey
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onHistoryClick()
                                }
                            )
                        }
                    }
                }
            }

            if (isEditMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sets:",
                        color = LightGrey,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ShadowGrey, RoundedCornerShape(8.dp))
                            .clickable { onSetCountChange(exercise.targetSets - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = stringResource(id = R.string.workouts_decrease_cd),
                            tint = LightGrey,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${exercise.targetSets}",
                        color = CrayolaBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CrayolaBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { onSetCountChange(exercise.targetSets + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.workouts_increase_cd),
                            tint = CrayolaBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = if (exercise.targetReps > 0) {
                        stringResource(id = R.string.workouts_sets_x_reps, exercise.targetSets, exercise.targetReps)
                    } else {
                        stringResource(id = R.string.workouts_sets_count, exercise.targetSets)
                    },
                    color = LightGrey,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun RoutineDescription(
    description: String,
    isEditMode: Boolean,
    onDescriptionChange: (String) -> Unit
) {
    if (isEditMode) {
        EditableTextWithPencil(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = stringResource(id = R.string.workouts_routine_description_placeholder),
            textStyle = TextStyle(
                color = Color(0xFFD4D4D8),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 6.dp)
        )
    } else {
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
}

@Composable
private fun RoutineDetailsScheduleEditor(
    state: RoutineDetailsState,
    onWeekDayToggle: (java.time.DayOfWeek) -> Unit,
    onCycleDayToggle: (Int) -> Unit,
    onRestDaysChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val intervalType = state.planIntervalConfig?.type
        val cycleMode = state.planIntervalConfig?.cycleMode

        if (!state.isEditMode) {
            // Visualization Mode
            val scheduleText = when (intervalType) {
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.CYCLE -> {
                    if (cycleMode == com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode.WEEKLY) {
                        val days = state.selectedWeekDays.sortedBy { it.value }
                        if (days.isEmpty()) {
                            stringResource(id = R.string.workouts_not_scheduled)
                        } else {
                            stringResource(
                                id = R.string.workouts_schedule_every_days,
                                days.joinToString(", ") { it.name.take(3) }
                            )
                        }
                    } else {
                        val days = state.selectedCycleDays.sorted()
                        if (days.isEmpty()) {
                            stringResource(id = R.string.workouts_not_scheduled)
                        } else {
                            stringResource(
                                id = R.string.workouts_schedule_cycle_days,
                                days.joinToString(", ")
                            )
                        }
                    }
                }
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.FREQUENCY -> {
                    stringResource(id = R.string.workouts_schedule_every_n_days, state.restDaysBetweenWorkouts)
                }
                else -> stringResource(id = R.string.workouts_not_scheduled)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(id = R.string.workouts_schedule_cd),
                    tint = CrayolaBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(id = R.string.workouts_schedule_label, scheduleText),
                    color = LightGrey,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            // Edit Mode
            Text(
                text = stringResource(id = R.string.workouts_edit_schedule),
                color = PureWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            when (intervalType) {
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.CYCLE -> {
                    if (cycleMode == com.example.kaizenfrontend.feature.workouts.domain.model.CycleMode.WEEKLY) {
                        WeeklyDaysSelector(
                            selectedDays = state.selectedWeekDays,
                            assignedRoutineNamesByDay = emptyMap(),
                            onDayClick = onWeekDayToggle,
                            showError = false
                        )
                    } else {
                        CycleDaysSelector(
                            cycleLengthDays = state.planIntervalConfig.cycleLengthDays,
                            selectedCycleDays = state.selectedCycleDays,
                            assignedRoutineNamesByCycleDay = emptyMap(),
                            onDayClick = onCycleDayToggle,
                            showError = false
                        )
                    }
                }
                com.example.kaizenfrontend.feature.workouts.domain.model.PlanIntervalType.FREQUENCY -> {
                    BasicCounterSelector(
                        title = stringResource(id = R.string.workouts_rest_days),
                        value = state.restDaysBetweenWorkouts,
                        suffix = stringResource(id = R.string.workouts_days),
                        onChange = onRestDaysChange
                    )
                }
                null -> {}
            }
        }
    }
}
