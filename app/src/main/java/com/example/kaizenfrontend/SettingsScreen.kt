package com.example.kaizenfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Onyx = Color(0xFF0B0A0F)
private val ShadowGrey = Color(0xFF242328)
private val CrayolaBlue = Color(0xFF2979FF)
private val LightGrey = Color(0xFFAAAAAA)

@Composable
fun SettingsScreen(
    // TODO: Inject real UiState / callbacks from ViewModel
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Manage your account",
            color = LightGrey,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        SettingsGroup(label = "ACCOUNT") {
            SettingsRow(
                icon = Icons.Default.Person,
                title = "Profile",
                subtitle = "Edit your name, email & photo",
                onClick = onProfileClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsGroup(label = "PREFERENCES") {
            SettingsRow(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage push notifications",
                onClick = onNotificationsClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsGroup(label = "LEGAL") {
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                subtitle = "Read our privacy terms",
                onClick = onPrivacyClick
            )
            HorizontalDivider(color = Onyx, thickness = 1.dp)
            SettingsRow(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App version & info",
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = label,
        color = LightGrey,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = CrayolaBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = LightGrey, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = LightGrey,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme { SettingsScreen() }
}
