package com.example.kaizenfrontend

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
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
class MainActivity : AppCompatActivity() {
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
    val activity = context as? Activity
    val sessionManager = remember { SessionManager(context) }
    var restoredRoute by rememberSaveable { mutableStateOf<String?>(null) }
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry) {
        backStackEntry?.destination?.route?.let { route ->
            if (route != "splash") {
                restoredRoute = route
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = restoredRoute ?: "splash"
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
                onGetStartedClick = {
                    navController.navigate("signup") {
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSystemBack = {
                    if (!navController.popBackStack()) {
                        activity?.finish()
                    }
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignUpClick = { needsCalibration ->
                    if (needsCalibration) {
                        navController.navigate("calibration") {
                            popUpTo("start") { inclusive = true }
                        }
                    } else {
                        navController.navigate("dashboard") {
                            popUpTo("start") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onBackClick = {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onSystemBack = {
                    if (!navController.popBackStack()) {
                        activity?.finish()
                    }
                },
                onLoginClick = { needsCalibration ->
                    if (needsCalibration) {
                        navController.navigate("calibration") {
                            popUpTo("start") { inclusive = true }
                        }
                    } else {
                        navController.navigate("dashboard") {
                            popUpTo("start") { inclusive = true }
                        }
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
