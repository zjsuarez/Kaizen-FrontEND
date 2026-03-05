package com.example.kaizenfrontend

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

// Color Palette based on the image
private val DarkBackground = Color(0xFF121215)
private val InputFieldColor = Color(0xFF282832)
private val LightGrayText = Color(0xFFA0A0B0)

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Title
        Text(
            text = "Let's Get\nStarted",
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 50.sp,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Email Field
        CustomTextField(
            hint = "Email",
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        CustomTextField(
            hint = "Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        CustomTextField(
            hint = "Confirm password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "SIGN UP",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "OR" Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = LightGrayText.copy(alpha = 0.5f)
            )
            Text(
                text = "or",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = LightGrayText.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Google Sign Up Button
        Button(
            onClick = { /* Handle Google Auth */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = InputFieldColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            // Note: Replace android.R.drawable.ic_menu_gallery with your actual Google logo drawable (e.g., R.drawable.ic_google)
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = "Google Logo",
                tint = Color.Unspecified, // Keeps original icon colors if it's an SVG
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SIGN UP WITH GOOGLE",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CustomTextField(
    hint: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false
) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = hint, color = LightGrayText)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = hint,
                tint = LightGrayText
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputFieldColor,
            unfocusedContainerColor = InputFieldColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}


@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen()
    }
}
