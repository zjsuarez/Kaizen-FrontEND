package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
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

    // defaulting to today
    val today = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date())
    }
    var startingDate by remember { mutableStateOf(today) }
    val canContinue = name.isNotBlank()
    val canCreate = name.isNotBlank() && startingDate.isNotBlank()

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
                                if (canCreate) {
                                    onCreate(name, description, startingDate)
                                }
                            },
                            enabled = canCreate
                        ) {
                            Text(
                                text = "Create",
                                color = if (canCreate) CrayolaBlue else LightGrey,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "Build your next training cycle in two quick steps.",
                color = LightGrey,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (currentStep == 1) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ShadowGrey.copy(alpha = 0.56f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = CrayolaBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plan details",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            label = { Text("Plan name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = planFieldColors()
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = planFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { currentStep = 2 },
                    enabled = canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrayolaBlue,
                        disabledContainerColor = ShadowGrey
                    )
                ) {
                    Text("Continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ShadowGrey.copy(alpha = 0.56f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CrayolaBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start date",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedTextField(
                            value = startingDate,
                            onValueChange = { startingDate = it },
                            singleLine = true,
                            label = { Text("Starting date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = CrayolaBlue
                                )
                            },
                            supportingText = {
                                Text(
                                    text = "Use format YYYY-MM-DD",
                                    color = LightGrey,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = planFieldColors()
                        )

                        if (name.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CrayolaBlue.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, CrayolaBlue.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "Plan: ${name.trim()}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (canCreate) {
                            onCreate(name, description, startingDate)
                        }
                    },
                    enabled = canCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrayolaBlue,
                        disabledContainerColor = ShadowGrey
                    )
                ) {
                    Text("Create plan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun planFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = CrayolaBlue,
    unfocusedBorderColor = LightGrey.copy(alpha = 0.42f),
    focusedContainerColor = ShadowGrey.copy(alpha = 0.55f),
    unfocusedContainerColor = ShadowGrey.copy(alpha = 0.55f),
    focusedLabelColor = CrayolaBlue,
    unfocusedLabelColor = LightGrey,
    cursorColor = CrayolaBlue
)
