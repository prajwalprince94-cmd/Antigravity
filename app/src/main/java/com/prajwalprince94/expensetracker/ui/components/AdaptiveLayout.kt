package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT,   // Standard mobile portrait (< 600dp)
    MEDIUM,    // Foldables unfolded / small tablets (600dp - 840dp)
    EXPANDED   // Large tablets, landscape, desktop (> 840dp)
}

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.COMPACT }

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    return when {
        widthDp < 600 -> WindowSizeClass.COMPACT
        widthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

@Composable
fun responsiveHorizontalPadding(): Dp = when (rememberWindowSizeClass()) {
    WindowSizeClass.COMPACT -> 16.dp
    WindowSizeClass.MEDIUM -> 24.dp
    WindowSizeClass.EXPANDED -> 36.dp
}

@Composable
fun isWideScreen(): Boolean = rememberWindowSizeClass() != WindowSizeClass.COMPACT
