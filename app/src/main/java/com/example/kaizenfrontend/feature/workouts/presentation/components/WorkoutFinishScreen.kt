package com.example.kaizenfrontend.feature.workouts.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.PrGold
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey
import kotlin.math.roundToInt

data class WorkoutFinishSummary(
    val title: String,
    val dateLabel: String,
    val durationLabel: String,
    val workoutNumber: Int,
    val totalVolumeKg: Double,
    val recordsBeaten: Int
)

@Composable
fun WorkoutFinishScreen(
    summary: WorkoutFinishSummary,
    initialNotes: String,
    isSubmitting: Boolean,
    submissionError: String?,
    onBack: () -> Unit,
    onFinish: (notes: String, imageUri: Uri?) -> Unit,
    onShare: () -> Unit
) {
    var notesField by remember { mutableStateOf(TextFieldValue(initialNotes)) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Onyx)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = !isSubmitting) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = LightGrey
                )
            }

            Text(
                text = "SESSION COMPLETE",
                color = PrGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = summary.title,
            color = PureWhite,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            lineHeight = 44.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = summary.dateLabel, color = LightGrey, fontSize = 16.sp)
            Text(text = summary.durationLabel, color = LightGrey, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinishMetricCard(
                modifier = Modifier.weight(1f),
                label = "WORKOUT NUMBER",
                value = summary.workoutNumber.toString(),
                suffix = "TOTAL"
            )
            FinishMetricCard(
                modifier = Modifier.weight(1f),
                label = "RECORDS",
                value = summary.recordsBeaten.toString()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ShadowGrey.copy(alpha = 0.85f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "TOTAL VOLUME", color = LightGrey, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = summary.totalVolumeKg.roundToInt().toString(),
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp,
                        lineHeight = 52.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "kg", color = LightGrey, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, LightGrey.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .background(Color(0xFF18171D), RoundedCornerShape(24.dp))
                .clickable(enabled = !isSubmitting) { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload workout image",
                    tint = if (selectedImageUri == null) LightGrey else PrGold,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (selectedImageUri == null) "Upload workout image" else "Image selected",
                    color = LightGrey,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101116))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "WORKOUT NOTES", color = LightGrey, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                BasicTextField(
                    value = notesField,
                    onValueChange = { notesField = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (notesField.text.isBlank()) {
                            Text("Write your notes...", color = LightGrey.copy(alpha = 0.7f), fontSize = 24.sp)
                        }
                        inner()
                    }
                )
            }
        }

        if (!submissionError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = submissionError,
                color = Color(0xFFFF7A7A),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onFinish(notesField.text, selectedImageUri) },
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f).height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(PrGold.copy(alpha = 0.7f), PrGold)),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Onyx,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FINISH",
                                color = Onyx,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Onyx
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onShare,
                enabled = !isSubmitting,
                modifier = Modifier.width(72.dp).height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShadowGrey)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share workout",
                    tint = CrayolaBlue
                )
            }
        }
    }
}

@Composable
private fun FinishMetricCard(
    modifier: Modifier,
    label: String,
    value: String,
    suffix: String? = null
) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ShadowGrey.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = LightGrey, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = PureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp,
                    lineHeight = 56.sp
                )
                if (!suffix.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = suffix,
                        color = LightGrey,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}
