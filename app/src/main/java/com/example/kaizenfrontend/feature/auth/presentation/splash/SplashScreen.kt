package com.example.kaizenfrontend.feature.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.kaizenfrontend.core.ui.components.PreLoginLanguagePicker
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.di.hiltServiceEntryPoint
import com.example.kaizenfrontend.feature.user.data.repository.UserRepositoryImpl
import com.example.kaizenfrontend.feature.user.domain.usecase.GetCurrentUserUseCase

@Composable
fun SplashScreen(
    onNavigateToStart: () -> Unit,
    onNavigateToCalibration: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val getCurrentUserUseCase = remember {
        GetCurrentUserUseCase(
            UserRepositoryImpl(
                context.applicationContext.hiltServiceEntryPoint().userApiService(),
                sessionManager
            )
        )
    }

    LaunchedEffect(Unit) {
        val token = sessionManager.getToken()

        if (token == null) {
            onNavigateToStart()
            return@LaunchedEffect
        }

        val profileResult = getCurrentUserUseCase()
        profileResult.fold(
            onSuccess = { user ->
                val isCalibrated = user.primaryGoal?.isNotBlank() == true

                if (isCalibrated) {
                    sessionManager.saveCalibrationComplete(true)
                    if (sessionManager.isOnboardingCompleted()) {
                        onNavigateToDashboard()
                    } else {
                        onNavigateToOnboarding()
                    }
                } else {
                    sessionManager.saveCalibrationComplete(false)
                    onNavigateToCalibration()
                }
            },
            onFailure = {
                // If token is stale/invalid, restart auth flow.
                sessionManager.clearToken()
                sessionManager.saveCalibrationComplete(false)
                onNavigateToStart()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 8.dp, end = 16.dp)
        ) {
            PreLoginLanguagePicker()
        }
        Text(
            text = stringResource(id = R.string.auth_app_name_splash),
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 6.sp
        )
    }
}
