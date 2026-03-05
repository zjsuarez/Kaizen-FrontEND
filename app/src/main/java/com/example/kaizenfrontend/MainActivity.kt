package com.example.kaizenfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kaizenfrontend.ui.theme.KaizenFrontEndTheme

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
    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            StartScreen(
                onGetStartedClick = { 
                    navController.navigate("signup")
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSignUpClick = {
                    navController.navigate("calibration")
                }
            )
        }
        composable("login") {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    // Navigate to home or next screen after login
                }
            )
        }
        composable("calibration") {
            CalibrationScreen(
                onStartClick = {
                    // Navigate to next screen after calibration
                }
            )
        }
    }
}
