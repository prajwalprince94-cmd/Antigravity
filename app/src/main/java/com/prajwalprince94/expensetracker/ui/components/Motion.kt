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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
 * Tap feedback the expressive way (Stride): the element itself squashes on press
 * and springs back on release. Replaces the ripple.
 */
@Composable
fun Modifier.bouncyClickable(
    scaleDown: Float = 0.90f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyPress"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        interactionSource = interaction,
        indication = null,
        enabled = enabled,
        onClick = onClick
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

/**
 * Smoothly blends list items at the top and bottom edges without offscreen buffer allocation,
 * ensuring high frame rates and stutter-free scrolling on all devices.
 */
fun Modifier.fadingEdges(
    top: Dp = 0.dp,
    bottom: Dp = 32.dp,
    fadeColor: Color = Color.Unspecified
): Modifier = composed {
    val density = LocalDensity.current
    val topPx = with(density) { top.toPx() }
    val bottomPx = with(density) { bottom.toPx() }
    val color = if (fadeColor != Color.Unspecified) fadeColor else MaterialTheme.colorScheme.background

    drawWithContent {
        drawContent()
        if (topPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, Color.Transparent),
                    startY = 0f,
                    endY = topPx
                )
            )
        }
        if (bottomPx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, color),
                    startY = size.height - bottomPx,
                    endY = size.height
                )
            )
        }
    }
}

/** Sweeping highlight shimmer effect inspired by NikhilKain/vyxel-apps. */
fun Modifier.shimmer(
    baseColor: Color? = null,
    highlightColor: Color? = null,
): Modifier = composed {
    val base = baseColor ?: Color.White.copy(alpha = 0.08f)
    val highlight = highlightColor ?: Color.White.copy(alpha = 0.25f)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    drawWithCache {
        val width = size.width.coerceAtLeast(1f)
        val travel = width * 2f
        val start = -width + travel * progress
        val brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(start, 0f),
            end = Offset(start + width, size.height)
        )
        onDrawBehind { drawRect(brush) }
    }
}
