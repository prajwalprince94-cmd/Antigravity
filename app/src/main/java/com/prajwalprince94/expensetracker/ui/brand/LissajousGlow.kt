package com.prajwalprince94.expensetracker.ui.brand

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Mathematical Lissajous Glow Engine inspired by NikhilKain/vyxel-apps.
 *
 * Simulates resonant harmonic plasma trails and orbiting chromatic blooms
 * driven by orthogonal trigonometric oscillators:
 *   x(t) = A * sin(a * t + δ)
 *   y(t) = B * sin(b * t)
 *
 * Creates an organic, living figure-eight resonance field around the AppMark
 * with zero garbage-collection overhead on the draw loop.
 */
@Composable
fun LissajousGlow(
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f,
    speedMillis: Int = 5400,
    trailLengthSamples: Int = 45,
    primaryColor: Color = VyxelBrand.Violet,
    secondaryColor: Color = VyxelBrand.Teal,
    accentColor: Color = VyxelBrand.Amber
) {
    val transition = rememberInfiniteTransition(label = "lissajousClock")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lissajousPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)

        val rx = w * 0.36f
        val ry = h * 0.28f
        val minDim = minOf(w, h)

        if (rx <= 0f || ry <= 0f) return@Canvas

        // Frequency parameters (3:2 harmonic ratio creates iconic figure-eight / trefoil resonance)
        val a1 = 3.0
        val b1 = 2.0
        val delta1 = Math.PI / 2.0

        val t = phase.toDouble()

        // 1. Primary Orbit Coordinates
        val x1 = (c.x + rx * sin(a1 * t + delta1)).toFloat()
        val y1 = (c.y + ry * sin(b1 * t)).toFloat()

        // 2. Secondary Orbit Coordinates (counter-harmonic 2:3)
        val a2 = 2.0
        val b2 = 3.0
        val delta2 = Math.PI / 4.0
        val x2 = (c.x + rx * 0.82f * sin(a2 * t + delta2)).toFloat()
        val y2 = (c.y + ry * 0.82f * sin(b2 * t)).toFloat()

        // 3. Tertiary Orbit Coordinates (circular precession)
        val x3 = (c.x + rx * 0.65f * cos(t)).toFloat()
        val y3 = (c.y + ry * 0.65f * sin(t)).toFloat()

        // --- LAYER A: Ambient Deep Radial Plasma Blooms ---
        val bloomRadiusPrimary = minDim * 0.62f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.38f * alpha),
                    primaryColor.copy(alpha = 0.12f * alpha),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = bloomRadiusPrimary
            ),
            radius = bloomRadiusPrimary,
            center = Offset(x1, y1),
            style = Fill
        )

        val bloomRadiusSecondary = minDim * 0.52f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = 0.32f * alpha),
                    secondaryColor.copy(alpha = 0.08f * alpha),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = bloomRadiusSecondary
            ),
            radius = bloomRadiusSecondary,
            center = Offset(x2, y2),
            style = Fill
        )

        val bloomRadiusTertiary = minDim * 0.42f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.22f * alpha),
                    Color.Transparent
                ),
                center = Offset(x3, y3),
                radius = bloomRadiusTertiary
            ),
            radius = bloomRadiusTertiary,
            center = Offset(x3, y3),
            style = Fill
        )

        // --- LAYER B: Phosphor Lissajous Trail 1 (Figure-8 Ribbon) ---
        val path1 = Path()
        val trailDt = 0.95 / trailLengthSamples
        for (i in trailLengthSamples downTo 0) {
            val sampleT = t - (i * trailDt)
            val px = (c.x + rx * sin(a1 * sampleT + delta1)).toFloat()
            val py = (c.y + ry * sin(b1 * sampleT)).toFloat()
            if (i == trailLengthSamples) path1.moveTo(px, py) else path1.lineTo(px, py)
        }

        drawPath(
            path = path1,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    secondaryColor.copy(alpha = 0.25f * alpha),
                    primaryColor.copy(alpha = 0.65f * alpha),
                    VyxelBrand.WhiteGlow.copy(alpha = 0.90f * alpha)
                ),
                start = Offset(c.x - rx, c.y - ry),
                end = Offset(x1, y1)
            ),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // --- LAYER C: Phosphor Lissajous Trail 2 (Counter-Harmonic Ribbon) ---
        val path2 = Path()
        for (i in trailLengthSamples downTo 0) {
            val sampleT = t - (i * trailDt)
            val px = (c.x + rx * 0.82f * sin(a2 * sampleT + delta2)).toFloat()
            val py = (c.y + ry * 0.82f * sin(b2 * sampleT)).toFloat()
            if (i == trailLengthSamples) path2.moveTo(px, py) else path2.lineTo(px, py)
        }

        drawPath(
            path = path2,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.20f * alpha),
                    secondaryColor.copy(alpha = 0.60f * alpha),
                    VyxelBrand.TealBright.copy(alpha = 0.85f * alpha)
                ),
                start = Offset(c.x + rx, c.y + ry),
                end = Offset(x2, y2)
            ),
            style = Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // --- LAYER D: Leading Chromatic Photon Beads ---
        // Bead 1 (Primary)
        drawCircle(
            color = VyxelBrand.WhiteGlow.copy(alpha = 0.95f * alpha),
            radius = 3.5.dp.toPx(),
            center = Offset(x1, y1)
        )
        // Bead 2 (Secondary)
        drawCircle(
            color = VyxelBrand.TealBright.copy(alpha = 0.90f * alpha),
            radius = 2.8.dp.toPx(),
            center = Offset(x2, y2)
        )
    }
}

/**
 * Modifier extension to apply ambient Lissajous glow behind any surface.
 */
fun Modifier.lissajousGlow(
    alpha: Float = 0.55f,
    speedMillis: Int = 6000,
    primaryColor: Color = VyxelBrand.Violet,
    secondaryColor: Color = VyxelBrand.Teal
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "modLissajous")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = speedMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "modLissajousPhase"
    )

    drawWithCache {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        val rx = w * 0.38f
        val ry = h * 0.30f
        val minDim = minOf(w, h)

        val t = phase.toDouble()
        val x1 = (c.x + rx * sin(3.0 * t + Math.PI / 2.0)).toFloat()
        val y1 = (c.y + ry * sin(2.0 * t)).toFloat()
        val x2 = (c.x + rx * 0.85f * sin(2.0 * t + Math.PI / 4.0)).toFloat()
        val y2 = (c.y + ry * 0.85f * sin(3.0 * t)).toFloat()

        onDrawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f * alpha),
                        Color.Transparent
                    ),
                    center = Offset(x1, y1),
                    radius = minDim * 0.55f
                ),
                radius = minDim * 0.55f,
                center = Offset(x1, y1)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.22f * alpha),
                        Color.Transparent
                    ),
                    center = Offset(x2, y2),
                    radius = minDim * 0.45f
                ),
                radius = minDim * 0.45f,
                center = Offset(x2, y2)
            )
        }
    }
}
