package com.example.kaizenfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.network.TokenManager

@Composable
fun SplashScreen(
    onNavigateToStart: () -> Unit,
    onNavigateToCalibration: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val token = TokenManager.getToken(context)
        when {
            token == null -> onNavigateToStart()
            !TokenManager.isCalibrationComplete(context) -> onNavigateToCalibration()
            else -> onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A0F)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "KAIZEN",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp
        )
    }
}
