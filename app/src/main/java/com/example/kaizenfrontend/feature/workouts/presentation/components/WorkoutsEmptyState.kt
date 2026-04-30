package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.Onyx
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey

@Composable
fun WorkoutsEmptyState(
    onCreatePlanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration or Icon
        Surface(
            modifier = Modifier.size(100.dp),
            color = ShadowGrey,
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = CrayolaBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.workouts_empty_title),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.workouts_empty_description),
            color = LightGrey,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Step Guide
        OnboardingStepItem(
            icon = Icons.Default.Layers,
            title = stringResource(id = R.string.workouts_empty_step1_title),
            description = stringResource(id = R.string.workouts_empty_step1_description)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingStepItem(
            icon = Icons.Default.Add,
            title = stringResource(id = R.string.workouts_empty_step2_title),
            description = stringResource(id = R.string.workouts_empty_step2_description),
            isPending = true
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onCreatePlanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.workouts_empty_create_plan_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnboardingStepItem(
    icon: ImageVector,
    title: String,
    description: String,
    isPending: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShadowGrey.copy(alpha = if (isPending) 0.5f else 1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = if (isPending) Onyx else CrayolaBlue.copy(alpha = 0.2f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isPending) LightGrey else CrayolaBlue
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                color = if (isPending) LightGrey else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = LightGrey.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}
