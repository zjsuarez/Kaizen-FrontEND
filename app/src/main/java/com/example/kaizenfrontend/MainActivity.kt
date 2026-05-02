package com.example.kaizenfrontend

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kaizenfrontend.core.data.local.SessionManager
import com.example.kaizenfrontend.core.ui.navigation.KaizenAppHost
import com.example.kaizenfrontend.core.ui.navigation.KaizenDestinations
import com.example.kaizenfrontend.core.ui.theme.KaizenFrontEndTheme
import com.example.kaizenfrontend.feature.auth.presentation.login.LoginScreen
import com.example.kaizenfrontend.feature.auth.presentation.signup.SignUpScreen
import com.example.kaizenfrontend.feature.auth.presentation.splash.SplashScreen
import com.example.kaizenfrontend.feature.auth.presentation.start.StartScreen
import com.example.kaizenfrontend.feature.dashboard.presentation.DashboardScreen
import com.example.kaizenfrontend.feature.onboarding.presentation.OnboardingScreen
import com.example.kaizenfrontend.feature.statistics.presentation.StatisticsScreen
import com.example.kaizenfrontend.feature.user.presentation.calibration.CalibrationScreen
import com.example.kaizenfrontend.feature.user.presentation.settings.SettingsScreen
import com.example.kaizenfrontend.feature.workouts.presentation.WorkoutsScreen
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
            if (route != KaizenDestinations.SPLASH) {
                restoredRoute = route
            }
        }
    }

    /**
     * Decide the post-auth landing screen for any flow that resolves to
     * "fully authenticated" (login success, signup success without
     * calibration, splash with completed user).
     */
    fun routePostAuth() {
        val target = if (sessionManager.isOnboardingCompleted()) {
            KaizenDestinations.DASHBOARD
        } else {
            KaizenDestinations.ONBOARDING
        }
        navController.navigate(target) {
            popUpTo(KaizenDestinations.START) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = restoredRoute ?: KaizenDestinations.SPLASH
        ) {
            composable(KaizenDestinations.SPLASH) {
                SplashScreen(
                    onNavigateToStart = {
                        navController.navigate(KaizenDestinations.START) {
                            popUpTo(KaizenDestinations.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToCalibration = {
                        navController.navigate(KaizenDestinations.CALIBRATION) {
                            popUpTo(KaizenDestinations.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(KaizenDestinations.ONBOARDING) {
                            popUpTo(KaizenDestinations.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(KaizenDestinations.DASHBOARD) {
                            popUpTo(KaizenDestinations.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
            composable(KaizenDestinations.START) {
                StartScreen(
                    onGetStartedClick = {
                        navController.navigate(KaizenDestinations.SIGNUP) {
                            launchSingleTop = true
                        }
                    },
                    onLoginClick = {
                        navController.navigate(KaizenDestinations.LOGIN) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(KaizenDestinations.SIGNUP) {
                SignUpScreen(
                    onBackClick = {
                        navController.navigate(KaizenDestinations.START) {
                            popUpTo(KaizenDestinations.START) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSystemBack = {
                        if (!navController.popBackStack()) {
                            activity?.finish()
                        }
                    },
                    onLoginClick = {
                        navController.navigate(KaizenDestinations.LOGIN) {
                            popUpTo(KaizenDestinations.SIGNUP) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onSignUpClick = { needsCalibration ->
                        if (needsCalibration) {
                            navController.navigate(KaizenDestinations.CALIBRATION) {
                                popUpTo(KaizenDestinations.START) { inclusive = true }
                            }
                        } else {
                            routePostAuth()
                        }
                    }
                )
            }
            composable(KaizenDestinations.LOGIN) {
                LoginScreen(
                    onBackClick = {
                        navController.navigate(KaizenDestinations.START) {
                            popUpTo(KaizenDestinations.START) { inclusive = false }
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
                            navController.navigate(KaizenDestinations.CALIBRATION) {
                                popUpTo(KaizenDestinations.START) { inclusive = true }
                            }
                        } else {
                            routePostAuth()
                        }
                    }
                )
            }
            composable(KaizenDestinations.CALIBRATION) {
                CalibrationScreen(
                    onStartClick = {
                        // After calibration, every user passes through onboarding once.
                        navController.navigate(KaizenDestinations.ONBOARDING) {
                            popUpTo(KaizenDestinations.CALIBRATION) { inclusive = true }
                        }
                    }
                )
            }
            composable(KaizenDestinations.ONBOARDING) {
                OnboardingScreen(
                    onContinue = {
                        navController.navigate(KaizenDestinations.DASHBOARD) {
                            popUpTo(KaizenDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(KaizenDestinations.DASHBOARD) {
                DashboardScreen(navController = navController)
            }
            composable(KaizenDestinations.WORKOUTS) {
                WorkoutsScreen(navController = navController)
            }
            composable(KaizenDestinations.STATISTICS) {
                StatisticsScreen(navController = navController)
            }
            composable(KaizenDestinations.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    onLogoutClick = {
                        navController.navigate(KaizenDestinations.START) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Active-workout overlays follow the user across every tab.
        // Hidden when no workout is active. Hosted at the app shell so a
        // started workout survives splash → tab transitions.
        KaizenAppHost(modifier = Modifier.fillMaxSize())
    }
}
