package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Springy staggered entrance animation inspired by NikhilKain/stride.
 * Cascades screen cards with fade + rise + gentle overshoot scale.
 */
@Composable
fun Modifier.entrance(index: Int = 0): Modifier {
    val alphaAnim = remember { Animatable(0f) }
    val offsetAnim = remember { Animatable(32f) }
    val scaleAnim = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        delay((index * 38L).coerceAtMost(350L))
        launch { alphaAnim.animateTo(1f, tween(200, easing = LinearOutSlowInEasing)) }
        launch {
            offsetAnim.animateTo(
                0f,
                spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
            )
        }
        launch {
            scaleAnim.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    return graphicsLayer {
        alpha = alphaAnim.value
        translationY = offsetAnim.value.dp.toPx()
        scaleX = scaleAnim.value
        scaleY = scaleAnim.value
    }
}

/**
 * Bouncy tap feedback inspired by NikhilKain/stride:
 * The element squashes on press and springs back on release with optional haptic buzz.
 */
@Composable
fun Modifier.bouncyClickable(
    scaleDown: Float = 0.92f,
    enabled: Boolean = true,
    enableHaptic: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyScale"
    )

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        onClick = {
            if (enableHaptic) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    )
}

/** Gentle infinite breathing scale for live indicators and badges */
@Composable
fun Modifier.pulse(from: Float = 0.98f, to: Float = 1.04f, durationMs: Int = 1400): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = from,
        targetValue = to,
        animationSpec = infiniteRepeatable(
            tween(durationMs, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Infinite vertical floating bob for empty state illustrations */
@Composable
fun Modifier.bob(amplitude: Dp = 4.dp, durationMs: Int = 1600): Modifier {
    val transition = rememberInfiniteTransition(label = "bob")
    val offset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMs, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bobOffset"
    )
    return graphicsLayer { translationY = offset * amplitude.toPx() }
}
