import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
val DarkBackground = Color(0xFF0F0F13)
val SurfaceColor = Color(0xFF2C2C35)
val SurfaceDarker = Color(0xFF1E1E24)
val AccentBlue = Color(0xFF337BFF)
val TextGray = Color(0xFFA0A0B0)
val LabelGray = Color(0xFF808090)

class CalibrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalibrationScreen()
            }
        }
    }
}

@Composable
fun CalibrationScreen() {
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
                onClick = { /* Handle Start Action */ },
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
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = LabelGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SegmentedControl(
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
fun WeightInputField(
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