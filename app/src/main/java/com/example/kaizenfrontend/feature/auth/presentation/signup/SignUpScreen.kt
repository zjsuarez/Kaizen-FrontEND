package com.example.kaizenfrontend.feature.auth.presentation.signup

import androidx.activity.compose.BackHandler
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
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSystemBack: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onSignUpClick: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { SignUpViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    BackHandler(onBack = onSystemBack)

    LaunchedEffect(uiState) {
        if (uiState is SignUpUiState.Success) {
            val needsCalibration = (uiState as SignUpUiState.Success).needsCalibration
            viewModel.resetState()
            onSignUpClick(needsCalibration)
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
                text = stringResource(id = R.string.auth_lets_get_started),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 50.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 8.dp, end = 4.dp)
            ) {
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

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            hint = stringResource(id = R.string.auth_confirm_password_hint),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = onLoginClick) {
            Text(
                text = stringResource(id = R.string.auth_already_have_account),
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState is SignUpUiState.Error) {
                Text(
                    text = (uiState as SignUpUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.register(email, password, confirmPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp),
            enabled = uiState !is SignUpUiState.Loading
        ) {
            if (uiState is SignUpUiState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text = stringResource(id = R.string.auth_sign_up), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            isLoading = uiState is SignUpUiState.Loading,
            text = stringResource(id = R.string.auth_sign_up_google)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme { SignUpScreen() }
}
