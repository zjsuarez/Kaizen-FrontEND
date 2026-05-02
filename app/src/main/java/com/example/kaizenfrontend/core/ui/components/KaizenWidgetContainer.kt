package com.example.kaizenfrontend.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Legacy widget container. Phase 1 routes this through [KaizenCard]
 * so the dashboard widgets pick up the new chrome immediately;
 * Phase 3 migrates each call site to [KaizenCard] directly and this
 * file is deleted.
 */
@Deprecated(
    message = "Use KaizenCard directly",
    replaceWith = ReplaceWith(
        "KaizenCard(modifier = modifier, onClick = onClick) { content() }",
        "com.example.kaizenfrontend.core.ui.components.KaizenCard"
    )
)
@Composable
fun KaizenWidgetContainer(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    KaizenCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(16.dp),
        content = content
    )
}
