package com.example.kaizenfrontend.core.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.kaizenfrontend.R
import com.example.kaizenfrontend.core.ui.theme.CrayolaBlue
import com.example.kaizenfrontend.core.ui.theme.LightGrey
import com.example.kaizenfrontend.core.ui.theme.PureWhite
import com.example.kaizenfrontend.core.ui.theme.ShadowGrey

@Composable
fun PreLoginLanguagePicker() {
    var expanded by remember { mutableStateOf(false) }
    val languageCode = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    
    val currentFlagResId = when {
        languageCode.startsWith("es") -> R.drawable.ic_flag_es
        languageCode.startsWith("ca") -> R.drawable.ic_flag_ca
        else -> R.drawable.ic_flag_en
    }

    Box {
        Image(
            painter = painterResource(id = currentFlagResId),
            contentDescription = stringResource(id = R.string.language_picker_content_description),
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ShadowGrey)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ShadowGrey, RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = { LanguageRow(R.drawable.ic_flag_en, stringResource(id = R.string.language_english)) },
                onClick = { 
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    expanded = false 
                }
            )
            DropdownMenuItem(
                text = { LanguageRow(R.drawable.ic_flag_es, stringResource(id = R.string.language_spanish)) },
                onClick = { 
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                    expanded = false 
                }
            )
            DropdownMenuItem(
                text = { LanguageRow(R.drawable.ic_flag_ca, stringResource(id = R.string.language_catalan)) },
                onClick = { 
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ca"))
                    expanded = false 
                }
            )
        }
    }
}

@Composable
private fun LanguageRow(flagResId: Int, name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = flagResId),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )
        Text(text = name, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}