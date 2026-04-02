package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.feature.workouts.domain.model.Routine
import com.example.kaizenfrontend.feature.workouts.presentation.PlanDetailsState
import kotlinx.coroutines.launch

@Composable
fun PlanDetailsSheetContent(
    state: PlanDetailsState,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit,
    onToggleActive: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRemoveRoutine: (String) -> Unit,
    onMoveRoutine: (Int, Int) -> Unit,
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Routine) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var draggingRoutineId by remember { mutableStateOf<String?>(null) }
    var draggingItemOffsetY by remember { mutableFloatStateOf(0f) }

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
            Crossfade(targetState = state.isEditMode, label = "PlanDetailsMode") { isEditMode ->
                PlanDetailsHeader(
                    title = state.title,
                    isEditMode = isEditMode,
                    isActive = state.isActive,
                    onEditClick = onEditClick,
                    onDoneClick = onDoneClick,
                    onToggleActive = onToggleActive,
                    onTitleChange = onTitleChange
                )
            }

            PlanDetailsSummary(routineCount = state.routines.size)

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
                    modifier = Modifier.fillMaxWidth(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items = state.routines, key = { _, routine -> routine.id }) { _, routine ->
                        val isDragging = draggingRoutineId == routine.id
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
                            PlanRoutineCard(
                                routine = routine,
                                isEditMode = state.isEditMode,
                                isDragging = isDragging,
                                onClick = { onRoutineClick(routine) },
                                onRemoveClick = { onRemoveRoutine(routine.id) },
                                onDragHandleDragStart = {
                                    draggingRoutineId = routine.id
                                    draggingItemOffsetY = 0f
                                },
                                onDragHandleDrag = { dragDelta ->
                                    if (draggingRoutineId != routine.id) return@PlanRoutineCard

                                    draggingItemOffsetY += dragDelta

                                    val currentIndex = state.routines.indexOfFirst {
                                        it.id == routine.id
                                    }
                                    if (currentIndex == -1) return@PlanRoutineCard

                                    val currentItemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull {
                                        it.key == routine.id
                                    } ?: return@PlanRoutineCard

                                    val threshold = (currentItemInfo.size * 0.5f).coerceAtLeast(24f)

                                    if (draggingItemOffsetY > threshold && currentIndex < state.routines.lastIndex) {
                                        onMoveRoutine(currentIndex, currentIndex + 1)
                                        draggingItemOffsetY -= threshold
                                    } else if (draggingItemOffsetY < -threshold && currentIndex > 0) {
                                        onMoveRoutine(currentIndex, currentIndex - 1)
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
                                    draggingRoutineId = null
                                    draggingItemOffsetY = 0f
                                }
                            )
                        }
                    }

                    if (state.isEditMode) {
                        item {
                            AddRoutineButton(onClick = onAddRoutineClick)
                        }
                    }
                }
            }

            PlanDescription(
                description = state.description,
                isEditMode = state.isEditMode,
                onDescriptionChange = onDescriptionChange
            )
        }
    }
}

@Composable
private fun PlanDetailsHeader(
    title: String,
    isEditMode: Boolean,
    isActive: Boolean,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit,
    onToggleActive: () -> Unit,
    onTitleChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditMode) {
            PlanEditableText(
                value = title,
                onValueChange = onTitleChange,
                placeholder = "Plan name",
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
                Text(text = "Done", fontWeight = FontWeight.SemiBold)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit plan",
                        tint = PureWhite
                    )
                }

                Button(
                    onClick = onToggleActive,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) CrayolaBlue else ShadowGrey,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = if (isActive) "Active" else "Inactive",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanEditableText(
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
            contentDescription = "Editable field",
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
private fun PlanDetailsSummary(routineCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$routineCount routine${if (routineCount != 1) "s" else ""}",
            color = LightGrey,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PlanRoutineCard(
    routine: Routine,
    isEditMode: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDragHandleDragStart: () -> Unit,
    onDragHandleDrag: (Float) -> Unit,
    onDragHandleDragEnd: () -> Unit
) {
    val cardColor = if (isDragging) Color(0xFF32313A) else ShadowGrey
    val cardBorderModifier = if (isDragging) {
        Modifier.border(1.dp, CrayolaBlue, RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardBorderModifier),
        color = cardColor,
        shape = RoundedCornerShape(16.dp),
        onClick = if (!isEditMode) onClick else ({})
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = CrayolaBlue,
                modifier = Modifier.size(22.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = routine.name,
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val exerciseCount = routine.exercises.size
                if (exerciseCount > 0) {
                    Text(
                        text = "$exerciseCount exercise${if (exerciseCount != 1) "s" else ""}",
                        color = LightGrey,
                        fontSize = 13.sp
                    )
                }

                if (routine.description.isNotBlank()) {
                    Text(
                        text = routine.description,
                        color = LightGrey,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            if (isEditMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRemoveClick) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove routine",
                            tint = LightGrey
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .pointerInput(routine.id, isEditMode) {
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
                            contentDescription = "Reorder routine",
                            tint = LightGrey
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRoutineButton(onClick: () -> Unit) {
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
            text = "+ Add Routine",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}

@Composable
private fun PlanDescription(
    description: String,
    isEditMode: Boolean,
    onDescriptionChange: (String) -> Unit
) {
    if (isEditMode) {
        PlanEditableText(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "Plan description",
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
    } else if (description.isNotBlank()) {
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
