package com.example.kaizen.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image

private val BackgroundColor = Color(0xFF060814)
private val BluePrimary = Color(0xFF2F80ED)
private val WhiteSoft = Color(0xFFEDEDED)
private val GraySoft = Color(0xFFBDBDBD)

@Composable
fun StartScreen(
    onGetStartedClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // Parte superior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "KAIZEN",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteSoft
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Aquí deberías poner tu PNG/SVG de la mancuerna
                Image(
                    painter = painterResource(id = R.drawable.ic_dumbbell),
                    contentDescription = "Dumbbell",
                    modifier = Modifier
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "CONTINUOUS\nIMPROVEMENT",
                    fontSize = 14.sp,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    color = GraySoft
                )
            }

            // Parte inferior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhiteSoft
                    )
                ) {
                    Text(
                        text = "GET STARTED",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "I already have an account",
                    color = GraySoft,
                    modifier = Modifier.clickable { onLoginClick() }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=1080px,height=2340px,dpi=440"
)
@Composable
fun StartScreenPreview() {
    MaterialTheme {
        StartScreen()
    }
}