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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Nota: Asumo que DarkBackground, InputFieldColor, LightGrayText y CustomTextField
// ya están disponibles en tu proyecto por el código anterior. Si pones esto en un
// archivo nuevo, asegúrate de importar/copiar esos valores.

@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121215)) // DarkBackground
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // Botón de retroceso
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Contenedor central (Logo y campos) que empuja los botones hacia abajo
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Placeholder para el Logo (el cuadrado del wireframe)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF282832), RoundedCornerShape(24.dp)), // InputFieldColor
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LOGO",
                    color = Color(0xFFA0A0B0), // LightGrayText
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            // Campo de Email
            CustomTextField(
                hint = "Email",
                leadingIcon = Icons.Outlined.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            CustomTextField(
                hint = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true
            )
        }

        // Sección inferior (Separador y Botones)
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 24.dp),
            color = Color(0xFFA0A0B0).copy(alpha = 0.2f) // LightGrayText transparente
        )

        // Botón "GET STARTED"
        Button(
            onClick = onLoginClick,
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
                text = "GET STARTED",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón "SIGN IN WITH GOOGLE"
        Button(
            onClick = { /* Handle Google Auth */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF282832), // InputFieldColor
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SIGN IN WITH GOOGLE",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}