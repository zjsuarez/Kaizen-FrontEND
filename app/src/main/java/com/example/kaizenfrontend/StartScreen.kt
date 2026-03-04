package com.example.kaizenfrontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontFace
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R


private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val OswaldFont = GoogleFont("Oswald")
private val InterFont = GoogleFont("Inter")

private val OswaldFontFamily = FontFamily(
    GoogleFontFace(googleFont = OswaldFont, fontProvider = provider)
)

private val InterFontFamily = FontFamily(
    GoogleFontFace(googleFont = InterFont, fontProvider = provider)
)

private val BackgroundColor = Color(0xFF060814)
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
                    fontFamily = OswaldFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = WhiteSoft
                )

                Spacer(modifier = Modifier.height(40.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Dumbbell",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(200.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "CONTINUOUS\nIMPROVEMENT",
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.em, // 50% of font size
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
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "I already have an account",
                    color = GraySoft,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
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
