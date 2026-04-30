package com.example.kaizenfrontend.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.components.CustomTextField
import com.example.kaizenfrontend.core.ui.theme.DarkBackground
import com.example.kaizenfrontend.core.ui.theme.InputFieldColor
import com.example.kaizenfrontend.core.ui.theme.LightGrayText
import com.example.kaizenfrontend.core.ui.components.GoogleSignInButton

@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { LoginViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Navigate on success
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val needsCalibration = (uiState as LoginUiState.Success).needsCalibration
            viewModel.resetState()
            onLoginClick(needsCalibration)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.auth_back),
                tint = Color.White
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.auth_welcome_back),
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 50.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)) {
                com.example.kaizenfrontend.core.ui.components.PreLoginLanguagePicker()
            }
        }

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            hint = stringResource(id = R.string.auth_email_hint),
            leadingIcon = Icons.Outlined.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            hint = stringResource(id = R.string.auth_password_hint),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState is LoginUiState.Error) {
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = uiState !is LoginUiState.Loading
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text = stringResource(id = R.string.auth_log_in), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = LightGrayText.copy(alpha = 0.5f))
            Text(text = stringResource(id = R.string.auth_or), color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = LightGrayText.copy(alpha = 0.5f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        GoogleSignInButton(
            onClick = { viewModel.signInWithGoogle(context) },
            isLoading = uiState is LoginUiState.Loading
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme { LoginScreen() }
}
