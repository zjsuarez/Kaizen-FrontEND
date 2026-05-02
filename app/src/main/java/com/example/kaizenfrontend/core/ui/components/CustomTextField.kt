package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

/**
 * Kaizen text field. Used by every form (auth, calibration, settings,
 * sheet bodies). Filled style on `surfaceContainer` (the standard
 * raised Kaizen card surface), no underline, brand cursor.
 *
 * The legacy two-arg signature (`hint` + required `leadingIcon`) is
 * preserved so existing auth screens compile unchanged.
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(text = hint, color = scheme.onSurfaceVariant) },
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant
                )
            }
        },
        supportingText = supportingText?.let { text ->
            { Text(text = text, color = if (isError) scheme.error else scheme.onSurfaceVariant) }
        },
        isError = isError,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainer,
            unfocusedContainerColor = scheme.surfaceContainer,
            errorContainerColor = scheme.surfaceContainer,
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            errorTextColor = scheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            cursorColor = scheme.primary,
            errorCursorColor = scheme.error,
            focusedLeadingIconColor = scheme.primary,
            unfocusedLeadingIconColor = scheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
