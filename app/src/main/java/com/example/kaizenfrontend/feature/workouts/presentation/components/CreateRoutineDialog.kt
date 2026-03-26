package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kaizenfrontend.feature.workouts.domain.model.TrainingPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDialog(
    plans: List<TrainingPlan>,
    onDismiss: () -> Unit,
    onCreate: (planId: String?, name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPlanId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val selectedPlanName = plans.find { it.id == selectedPlanId }?.name ?: "No Plan (Standalone)"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedPlanName,
                        onValueChange = {},
                        label = { Text("Parent Plan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Plan (Standalone)") },
                            onClick = {
                                selectedPlanId = null
                                dropdownExpanded = false
                            }
                        )
                        plans.forEach { plan ->
                            DropdownMenuItem(
                                text = { Text(plan.name) },
                                onClick = {
                                    selectedPlanId = plan.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(selectedPlanId, name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
