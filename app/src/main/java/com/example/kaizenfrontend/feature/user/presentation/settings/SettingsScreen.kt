package com.example.kaizenfrontend.feature.user.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import com.example.kaizenfrontend.core.ui.theme.*

@Composable
fun SettingsScreen(
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onManageApiClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshUserProfile()
    }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRestDialog by remember { mutableStateOf(false) }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { newPassword ->
                viewModel.changePassword(newPassword)
                showChangePasswordDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = ShadowGrey,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Log Out", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?", color = LightGrey) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logout()
                    showLogoutDialog = false
                    onLogoutClick()
                }) {
                    Text("Log Out", color = SubtleRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = LightGrey)
                }
            }
        )
    }

    if (showRestDialog) {
        RestTimerDialog(
            currentSeconds = uiState.defaultRest.removeSuffix(" s").toIntOrNull() ?: 90,
            onDismiss = { showRestDialog = false },
            onConfirm = { seconds ->
                viewModel.setDefaultRest(seconds)
                showRestDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Settings",
            color = PureWhite,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )

        SettingsAccountSection(
            email = uiState.email,
            authProvider = uiState.authProvider,
            onChangePasswordClick = { showChangePasswordDialog = true },
            onLogoutClick = { showLogoutDialog = true },
            onDeleteAccountClick = onDeleteAccountClick
        )

        SettingsPreferencesSection(
            unitSystem = uiState.unitSystem,
            effortMetric = uiState.effortMetric,
            defaultRest = uiState.defaultRest,
            onUnitToggle = { viewModel.setUnitSystem(it) },
            onEffortToggle = { viewModel.setEffortMetric(it) },
            onRestClick = { showRestDialog = true },
            showSaveButton = uiState.hasUnsavedChanges,
            isSaving = uiState.isSavingPrefs,
            onSaveClick = { viewModel.savePreferences() }
        )

        SettingsDataPrivacySection(
            onExportClick = onExportClick,
            onManageApiClick = onManageApiClick
        )
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShadowGrey,
        shape = RoundedCornerShape(16.dp),
        title = { Text(text = "Change Password", color = PureWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Password", color = LightGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = CrayolaBlue,
                        unfocusedBorderColor = LightGrey,
                        cursorColor = CrayolaBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm Password", color = LightGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = CrayolaBlue,
                        unfocusedBorderColor = LightGrey,
                        cursorColor = CrayolaBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(text = error!!, color = SubtleRed, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    newPassword.length < 6 -> error = "Password must be at least 6 characters."
                    newPassword != confirmPassword -> error = "Passwords do not match."
                    else -> onConfirm(newPassword)
                }
            }) { Text("Save", color = CrayolaBlue, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = LightGrey) }
        }
    )
}

@Composable
private fun RestTimerDialog(currentSeconds: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var input by remember { mutableStateOf(currentSeconds.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ShadowGrey,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Default Rest Timer", color = PureWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Set rest time in seconds", color = LightGrey, fontSize = 13.sp)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; error = null },
                    label = { Text("Seconds", color = LightGrey) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = CrayolaBlue,
                        unfocusedBorderColor = LightGrey,
                        cursorColor = CrayolaBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(text = error!!, color = SubtleRed, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val seconds = input.toIntOrNull()
                when {
                    seconds == null || seconds <= 0 -> error = "Enter a valid number of seconds."
                    seconds > 600 -> error = "Maximum rest is 600 seconds."
                    else -> onConfirm(seconds)
                }
            }) { Text("Save", color = CrayolaBlue, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = LightGrey) }
        }
    )
}

@Composable
private fun SettingsAccountSection(
    email: String,
    authProvider: String?,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    SettingsSectionCard(label = "ACCOUNT") {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = "Email", color = LightGrey, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = email, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            // ── Auth provider badge ────────────────────────────────────────
            when (authProvider) {
                "GOOGLE" -> {
                    AuthProviderBadge(
                        showGoogle = true,
                        showLock = false,
                        label = "Linked via Google"
                    )
                }
                "BOTH" -> {
                    AuthProviderBadge(
                        showGoogle = true,
                        showLock = true,
                        label = "Google & Local Password"
                    )
                }
                // LOCAL or null → no badge needed
                else -> {}
            }
        }
        SettingsDivider()
        // Password action: GOOGLE-only accounts must CREATE a password; all others CHANGE it.
        val passwordText = if (authProvider == "GOOGLE") "Create Local Password" else "Change Password"
        SettingsTextButton(text = passwordText, textColor = PureWhite, onClick = onChangePasswordClick)
        SettingsDivider()
        SettingsTextButton(text = "Log Out", textColor = SubtleRed, onClick = onLogoutClick)
        SettingsDivider()
        SettingsTextButton(text = "Delete Account", textColor = SubtleRed, onClick = onDeleteAccountClick)
    }
}

@Composable
private fun AuthProviderBadge(
    showGoogle: Boolean,
    showLock: Boolean,
    label: String
) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showGoogle) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.kaizenfrontend.R.drawable.ic_google),
                contentDescription = "Google",
                tint = Color.Unspecified,
                modifier = Modifier.size(13.dp)
            )
        }
        if (showLock) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Local password",
                tint = CrayolaBlue,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = label,
            color = LightGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SettingsPreferencesSection(
    unitSystem: String,
    effortMetric: String,
    defaultRest: String,
    onUnitToggle: (String) -> Unit,
    onEffortToggle: (String) -> Unit,
    onRestClick: () -> Unit,
    showSaveButton: Boolean,
    isSaving: Boolean,
    onSaveClick: () -> Unit
) {
    SettingsSectionCard(label = "TRAINING PREFERENCES") {
        PreferenceRow(label = "Unit System") {
            val unitDisplay = if (unitSystem == "IMPERIAL") "LB" else "KG"
            SegmentedToggle(
                options = listOf("KG", "LB"),
                selected = unitDisplay,
                onSelect = { onUnitToggle(if (it == "LB") "IMPERIAL" else "METRIC") }
            )
        }
        SettingsDivider()
        PreferenceRow(label = "Effort Metric") {
            SegmentedToggle(
                options = listOf("RIR", "RPE", "NONE"),
                selected = effortMetric,
                onSelect = onEffortToggle
            )
        }
        SettingsDivider()
        PreferenceRow(label = "Default Rest") {
            Box(
                modifier = Modifier
                    .border(1.dp, CrayolaBlue, RoundedCornerShape(10.dp))
                    .clickable { onRestClick() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(text = defaultRest, color = CrayolaBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (showSaveButton) {
            SettingsDivider()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) { onSaveClick() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = CrayolaBlue, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = "Save Preferences", color = CrayolaBlue, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SettingsDataPrivacySection(onExportClick: () -> Unit, onManageApiClick: () -> Unit) {
    SettingsSectionCard(label = "DATA & PRIVACY") {
        SettingsTextButton(text = "Export Workout History (CSV)", textColor = PureWhite, onClick = onExportClick)
        SettingsDivider()
        SettingsTextButton(text = "Manage API Permissions", textColor = PureWhite, onClick = onManageApiClick)
    }
}

@Composable
private fun SettingsSectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = label,
            color = LightGrey,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ShadowGrey)
        ) { Column { content() } }
    }
}

@Composable
private fun SettingsTextButton(text: String, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PreferenceRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        control()
    }
}

@Composable
private fun SegmentedToggle(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .background(Onyx, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .background(if (isSelected) CrayolaBlue else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) PureWhite else LightGrey,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Onyx, thickness = 1.dp)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme { SettingsScreen() }
}
