package com.example.kaizenfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.theme.KaizenFrontEndTheme
import com.example.kaizenfrontend.feature.auth.presentation.login.LoginScreen
import com.example.kaizenfrontend.feature.auth.presentation.signup.SignUpScreen
import com.example.kaizenfrontend.feature.auth.presentation.splash.SplashScreen
import com.example.kaizenfrontend.feature.auth.presentation.start.StartScreen
import com.example.kaizenfrontend.feature.dashboard.presentation.DashboardScreen
import com.example.kaizenfrontend.feature.user.presentation.calibration.CalibrationScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaizenFrontEndTheme {
                KaizenNavHost()
            }
        }
    }
}

@Composable
fun KaizenNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToStart = {
                    navController.navigate("start") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToCalibration = {
                    navController.navigate("calibration") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("start") {
            StartScreen(
                onGetStartedClick = { navController.navigate("signup") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = {
                    navController.navigate("calibration") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate("splash") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }
        composable("calibration") {
            CalibrationScreen(
                onStartClick = {
                    navController.navigate("dashboard") {
                        popUpTo("calibration") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onLogoutClick = {
                    navController.navigate("start") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
