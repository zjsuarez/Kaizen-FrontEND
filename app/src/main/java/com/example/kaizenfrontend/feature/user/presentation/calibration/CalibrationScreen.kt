package com.example.kaizenfrontend.feature.user.presentation.calibration

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.*

@Composable
fun CalibrationScreen(onStartClick: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel = remember { CalibrationViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    var selectedUnit by remember { mutableStateOf("KG") }
    var bodyWeight by remember { mutableStateOf("") }
    var selectedEffort by remember { mutableStateOf("RIR") }

    LaunchedEffect(uiState) {
        if (uiState is CalibrationUiState.Success) {
            viewModel.resetState()
            onStartClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp)
        ) {
            Text(
                text = "Calibration",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Set your system preferences",
                color = TextGray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            CalibrationSectionLabel(text = "SELECT UNIT SYSTEM")
            CalibrationSegmentedControl(
                options = listOf("KG", "LB"),
                selectedOption = selectedUnit,
                onOptionSelected = { selectedUnit = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            CalibrationSectionLabel(text = "CURRENT BODY WEIGHT")
            WeightInputField(
                value = bodyWeight,
                onValueChange = {
                    bodyWeight = it
                        .filter(Char::isDigit)
                        .take(3)
                },
                unit = selectedUnit
            )

            Spacer(modifier = Modifier.height(32.dp))

            CalibrationSectionLabel(text = "SELECT EFFORT MEASURE")
            CalibrationSegmentedControl(
                options = listOf("RIR", "RPE", "NONE"),
                selectedOption = selectedEffort,
                onOptionSelected = { selectedEffort = it }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Onyx)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState is CalibrationUiState.Error) {
                    Text(
                        text = (uiState as CalibrationUiState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.submitCalibration(selectedUnit, bodyWeight, selectedEffort) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrayolaBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState !is CalibrationUiState.Loading
                ) {
                    if (uiState is CalibrationUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "START", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalibrationSectionLabel(text: String) {
    Text(
        text = text,
        color = LabelGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun CalibrationSegmentedControl(
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
                    .background(if (isSelected) CrayolaBlue else Color.Transparent)
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(CrayolaBlue),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(text = "0", color = TextGray, fontSize = 18.sp)
                    }
                    innerTextField()
                }
            }
        )
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(SurfaceDarker),
            contentAlignment = Alignment.Center
        ) {
            Text(text = unit, color = TextGray, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalibrationScreenPreview() {
    MaterialTheme { CalibrationScreen() }
}
