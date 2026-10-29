package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Living ambient backdrop inspired by NikhilKain/Range.
 * Features phase-deferred drawing so that ambient aurora blooms and twinkling starfield
 * render exclusively in the Draw phase with ZERO Compose recompositions.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 0.85f,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val phaseState = if (animated) {
        rememberAmbientPhase(durationMs = 42_000, steps = 180, label = "aurora")
    } else {
        remember { mutableFloatStateOf(0.22f) }
    }
    val dark = isSystemInDarkTheme()

    val stars = remember {
        val rnd = Random(42)
        List(30) {
            Triple(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat() * 0.7f + 0.3f)
        }
    }

    val base = MaterialTheme.colorScheme.background
    val bloomA = MaterialTheme.colorScheme.primary
    val bloomB = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .background(base)
            .drawBehind {
                val phase = phaseState.value // Phase read deferred to Draw phase! Never causes recomposition!
                drawAurora(phase, intensity * if (dark) 1f else 0.40f, bloomA, bloomB, base, dark)
                if (dark) {
                    drawStars(stars, phase)
                }
            }
    ) {
        content()
    }
}

private fun DrawScope.drawAurora(
    phase: Float,
    intensity: Float,
    bloomA: Color,
    bloomB: Color,
    base: Color,
    dark: Boolean
) {
    val w = size.width
    val h = size.height
    val a1 = phase * 2f * Math.PI.toFloat()
    val a2 = a1 * 0.65f + 1.2f

    // Primary Ambient Bloom
    val c1 = Offset(w * (0.22f + 0.12f * cos(a1)), h * (0.16f + 0.05f * sin(a1)))
    val r1 = w * 0.95f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                bloomA.copy(alpha = 0.20f * intensity),
                bloomA.copy(alpha = 0.06f * intensity),
                Color.Transparent
            ),
            center = c1,
            radius = r1
        ),
        radius = r1,
        center = c1
    )

    // Secondary Ambient Bloom
    val c2 = Offset(w * (0.84f + 0.08f * sin(a2)), h * (0.32f + 0.08f * cos(a2)))
    val r2 = w * 0.8f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                bloomB.copy(alpha = 0.15f * intensity),
                bloomB.copy(alpha = 0.05f * intensity),
                Color.Transparent
            ),
            center = c2,
            radius = r2
        ),
        radius = r2,
        center = c2
    )

    // Smooth gradient fade toward the bottom to maintain foreground legibility
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.55f to base.copy(alpha = if (dark) 0.65f else 0.82f),
            1f to base
        )
    )
}

private fun DrawScope.drawStars(stars: List<Triple<Float, Float, Float>>, phase: Float) {
    stars.forEachIndexed { i, (x, y, seed) ->
        val twinkle = 0.35f + 0.65f * ((sin(phase * 6.28f * (0.4f + seed) + i) + 1f) / 2f)
        drawCircle(
            color = Color.White.copy(alpha = 0.04f + 0.14f * twinkle * seed),
            radius = (0.7f + seed * 1.3f),
            center = Offset(x * size.width, y * size.height * 0.72f)
        )
    }
}
