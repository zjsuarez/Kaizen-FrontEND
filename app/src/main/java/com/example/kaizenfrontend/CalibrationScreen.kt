package com.example.kaizenfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette based on the design
private val DarkBackground = Color(0xFF0B0A0F)
private val SurfaceColor = Color(0xFF2A2733)
private val SurfaceDarker = Color(0xFF18181B)
private val AccentBlue = Color(0xFF2979FF)
private val TextGray = Color(0xFFA0A0B0)
private val LabelGray = Color(0xFF808090)

@Composable
fun CalibrationScreen(onStartClick: () -> Unit = {}) {
    // State variables to hold user selections
    var selectedUnit by remember { mutableStateOf("KG") }
    var bodyWeight by remember { mutableStateOf("") }
    var selectedEffort by remember { mutableStateOf("RIR") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Main scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp)
        ) {
            // Header
            Text(
                text = "Calibration",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black, // Simulating the thick, condensed font
                letterSpacing = 2.sp
            )
            Text(
                text = "Set your system preferences",
                color = TextGray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            // Unit System Selection
            SectionLabel(text = "SELECT UNIT SYSTEM")
            SegmentedControl(
                options = listOf("KG", "LB"),
                selectedOption = selectedUnit,
                onOptionSelected = { selectedUnit = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Body Weight Input
            SectionLabel(text = "CURRENT BODY WEIGHT")
            WeightInputField(
                value = bodyWeight,
                onValueChange = { bodyWeight = it },
                unit = selectedUnit
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Effort Measure Selection
            SectionLabel(text = "SELECT EFFORT MEASURE")
            SegmentedControl(
                options = listOf("RIR", "RPE", "NONE"),
                selectedOption = selectedEffort,
                onOptionSelected = { selectedEffort = it }
            )
        }

        // Bottom Action Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(24.dp)
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "START",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = LabelGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun SegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) AccentBlue else Color.Transparent)
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else TextGray,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun WeightInputField(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Input Area
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(AccentBlue),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0.0",
                            color = TextGray,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Fixed Unit Indicator
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(SurfaceDarker),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unit,
                color = TextGray,
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalibrationScreenPreview() {
    MaterialTheme {
        CalibrationScreen()
    }
}
