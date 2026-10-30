package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalprince94.expensetracker.ui.boot.BootScreen

/**
 * Backward-compatible bridge to the enhanced Vyxel BootScreen.
 * Displays the AppMark with mathematical Lissajous glow, telemetry stages,
 * and buttery-smooth crossfade.
 */
@Composable
fun BootSequence(
    isDataLoaded: Boolean,
    onBootComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BootScreen(
        isDataLoaded = isDataLoaded,
        onBootComplete = onBootComplete,
        modifier = modifier
    )
}
