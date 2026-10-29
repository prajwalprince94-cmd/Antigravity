package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/** Set from settings; when true every ambient animation holds still to save power. */
val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * Ambient background animation phase with phase deferral.
 * Returns a [State<Float>] intended to be read exclusively in the Draw phase
 * (such as [Modifier.drawBehind] or [Modifier.drawWithCache]), completely
 * avoiding Compose recomposition overhead.
 */
@Composable
fun rememberAmbientPhase(
    durationMs: Int = 36_000,
    steps: Int = 180,
    label: String = "ambient",
    hold: Float = 0.2f
): State<Float> {
    if (LocalReduceMotion.current) return remember { mutableFloatStateOf(hold) }
    val transition = rememberInfiniteTransition(label = label)
    val raw: State<Float> = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "${label}Raw"
    )
    return remember(steps) {
        derivedStateOf { (raw.value * steps).toInt() / steps.toFloat() }
    }
}
