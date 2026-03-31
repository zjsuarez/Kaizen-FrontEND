package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import com.example.kaizenfrontend.core.ui.theme.SubtleRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, startingDate: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentStep by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showInlineErrors by remember { mutableStateOf(false) }

    // defaulting to today
    val today = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    var startingDate by remember { mutableStateOf(today) }
    val canContinue = name.isNotBlank()
    val canFinish = name.isNotBlank() && startingDate.isNotBlank()

    LaunchedEffect(currentStep) {
        showInlineErrors = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Onyx,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 48.dp, height = 5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(LightGrey.copy(alpha = 0.6f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(72.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep == 2) {
                                currentStep = 1
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentStep == 2) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create plan",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${currentStep}/2",
                        color = LightGrey,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier.width(72.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (currentStep == 2) {
                        TextButton(
                            onClick = {
                                if (canFinish) {
                                    onCreate(name, description, startingDate)
                                } else {
                                    showInlineErrors = true
                                }
                            },
                            enabled = canFinish
                        ) {
                            Text(
                                text = "Finish",
                                color = if (canFinish) CrayolaBlue else LightGrey,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val isForward = targetState > initialState
                    (slideInHorizontally { if (isForward) it else -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (isForward) -it else it } + fadeOut())
                },
                label = "CreatePlanStepTransition"
            ) { step ->
                when (step) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Name",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                singleLine = true,
                                isError = showInlineErrors && name.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )

                            if (showInlineErrors && name.isBlank()) {
                                Text(
                                    text = "Name is required",
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = "Description",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Starting date",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            OutlinedTextField(
                                value = startingDate,
                                onValueChange = { startingDate = it },
                                singleLine = true,
                                isError = showInlineErrors && startingDate.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = planFieldColors()
                            )

                            if (showInlineErrors && startingDate.isBlank()) {
                                Text(
                                    text = "Starting date is required",
                                    color = SubtleRed,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            if (currentStep == 1) {
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (canContinue) {
                            currentStep = 2
                        } else {
                            showInlineErrors = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrayolaBlue,
                        disabledContainerColor = ShadowGrey
                    )
                ) {
                    Text("NEXT", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun planFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = CrayolaBlue,
    unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = ShadowGrey,
    unfocusedContainerColor = ShadowGrey,
    cursorColor = CrayolaBlue
)
