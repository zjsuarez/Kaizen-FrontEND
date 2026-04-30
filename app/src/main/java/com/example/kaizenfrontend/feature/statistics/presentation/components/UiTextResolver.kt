package com.example.kaizenfrontend.feature.statistics.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.kaizenfrontend.feature.statistics.presentation.UiTextSpec

@Composable
internal fun UiTextSpec.resolve(): String {
    raw?.takeIf { it.isNotBlank() }?.let { return it }
    val id = resId ?: return ""
    return if (args.isEmpty()) {
        stringResource(id = id)
    } else {
        stringResource(id = id, *args.toTypedArray())
    }
}

@Composable
internal fun UiTextSpec?.resolveOrNull(): String? {
    if (this == null) return null
    val resolved = resolve()
    return resolved.takeIf { it.isNotBlank() }
}
