package com.example.kaizenfrontend.feature.workouts.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey

@Composable
fun WorkoutsEmptyState(
    onCreatePlanClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.72f,
        animationSpec = tween(durationMillis = 400),
        label = "empty_icon_scale"
    )
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Layers,
            contentDescription = null,
            modifier = Modifier
                .size(112.dp)
                .scale(iconScale),
            tint = CrayolaBlue.copy(alpha = 0.18f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(id = R.string.workouts_empty_title),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(id = R.string.workouts_empty_subtitle),
            color = LightGrey,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onCreatePlanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CrayolaBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.workouts_empty_create_plan_button),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
