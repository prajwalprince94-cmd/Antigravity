package com.prajwalprince94.expensetracker.ui.brand

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vyxel brand palette tokens matching the Vyxel aesthetic.
 */
object VyxelBrand {
    val Violet = Color(0xFF6242E0)
    val VioletBright = Color(0xFF886BFF)
    val VioletDeep = Color(0xFF280A68)
    val Teal = Color(0xFF4EDBD4)
    val TealBright = Color(0xFF82F4EE)
    val TealDeep = Color(0xFF003735)
    val Amber = Color(0xFFFFB86B)
    val AmberBright = Color(0xFFFFD49F)
    val Obsidian = Color(0xFF08070D)
    val ObsidianCard = Color(0xFF13111C)
    val WhiteGlow = Color(0xFFF6F4FF)
}

/**
 * High-fidelity vector AppMark inspired by NikhilKain/vyxel-apps.
 *
 * Renders an intricate geometric vault crest:
 * - Outer rounded hex-shield boundary with multi-spectral gradient
 * - Interlocking geometric facets representing wealth preservation & growth
 * - Central luminous currency glyph ("₹") forged into an infinity loop
 * - Orbiting photon rider circling the outer perimeter
 */
@Composable
fun AppMark(
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    progress: Float = 1f,
    orbiting: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "appMarkOrbit")
    val orbitPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitPhase"
    )

    val pulsePhase by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsePhase"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val c = Offset(w / 2f, h / 2f)
        val radius = (minOf(w, h) / 2f) * 0.88f * progress

        if (radius <= 0f) return@Canvas

        // 1. Ambient Background Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VyxelBrand.Violet.copy(alpha = 0.28f * progress),
                    VyxelBrand.Teal.copy(alpha = 0.12f * progress),
                    Color.Transparent
                ),
                center = c,
                radius = radius * 1.35f
            ),
            radius = radius * 1.35f,
            center = c
        )

        // 2. Outer Hex-Shield Vault Silhouette
        val shieldPath = Path().apply {
            val sides = 6
            val step = (2.0 * Math.PI / sides)
            for (i in 0 until sides) {
                val angle = i * step - Math.PI / 2.0
                val px = c.x + (radius * 0.98f) * cos(angle).toFloat()
                val py = c.y + (radius * 0.98f) * sin(angle).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }

        // Vault backdrop fill
        drawPath(
            path = shieldPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    VyxelBrand.VioletDeep.copy(alpha = 0.85f * progress),
                    VyxelBrand.ObsidianCard.copy(alpha = 0.95f * progress)
                ),
                center = c,
                radius = radius
            )
        )

        // Shield border stroke with sweep gradient
        drawPath(
            path = shieldPath,
            brush = Brush.sweepGradient(
                0.0f to VyxelBrand.VioletBright,
                0.3f to VyxelBrand.TealBright,
                0.6f to VyxelBrand.Amber,
                0.85f to VyxelBrand.Violet,
                1.0f to VyxelBrand.VioletBright
            ),
            style = Stroke(
                width = 3.5.dp.toPx() * progress,
                join = StrokeJoin.Round
            )
        )

        // 3. Inner Intersecting Geometric Facets
        val facetScale = 0.72f * pulsePhase
        val facetPathA = Path().apply {
            moveTo(c.x, c.y - radius * facetScale)
            lineTo(c.x + radius * facetScale * 0.866f, c.y)
            lineTo(c.x, c.y + radius * facetScale * 0.45f)
            lineTo(c.x - radius * facetScale * 0.866f, c.y)
            close()
        }
        drawPath(
            path = facetPathA,
            brush = Brush.verticalGradient(
                colors = listOf(
                    VyxelBrand.TealBright.copy(alpha = 0.22f * progress),
                    VyxelBrand.Violet.copy(alpha = 0.08f * progress)
                ),
                startY = c.y - radius * facetScale,
                endY = c.y + radius * facetScale
            )
        )

        // 4. Dual Infinity Light Arcs
        rotate(degrees = -18f, pivot = c) {
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to VyxelBrand.TealBright,
                    0.5f to VyxelBrand.VioletBright,
                    1.0f to VyxelBrand.AmberBright
                ),
                startAngle = 30f,
                sweepAngle = 280f * progress,
                useCenter = false,
                topLeft = Offset(c.x - radius * 0.75f, c.y - radius * 0.75f),
                size = Size(radius * 1.5f, radius * 1.5f),
                style = Stroke(
                    width = 2.2.dp.toPx() * progress,
                    cap = StrokeCap.Round
                )
            )
        }

        // 5. Stylized Central Rupee & Infinity Core
        if (progress > 0.45f) {
            val coreAlpha = ((progress - 0.45f) / 0.55f).coerceIn(0f, 1f)
            val strokeW = 4.2.dp.toPx() * progress
            val glyphRadius = radius * 0.38f

            // Upper horizontal bar
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(VyxelBrand.TealBright, VyxelBrand.WhiteGlow)
                ),
                start = Offset(c.x - glyphRadius * 0.85f, c.y - glyphRadius * 0.85f),
                end = Offset(c.x + glyphRadius * 0.85f, c.y - glyphRadius * 0.85f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Middle horizontal bar
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(VyxelBrand.Teal, VyxelBrand.VioletBright)
                ),
                start = Offset(c.x - glyphRadius * 0.85f, c.y - glyphRadius * 0.35f),
                end = Offset(c.x + glyphRadius * 0.65f, c.y - glyphRadius * 0.35f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Rupee curved spine & diagonal kick
            val spinePath = Path().apply {
                moveTo(c.x - glyphRadius * 0.35f, c.y - glyphRadius * 0.85f)
                lineTo(c.x - glyphRadius * 0.35f, c.y + glyphRadius * 0.95f)
                // Upper bow
                moveTo(c.x - glyphRadius * 0.35f, c.y - glyphRadius * 0.85f)
                cubicTo(
                    c.x + glyphRadius * 0.85f, c.y - glyphRadius * 0.85f,
                    c.x + glyphRadius * 0.85f, c.y + glyphRadius * 0.15f,
                    c.x - glyphRadius * 0.35f, c.y + glyphRadius * 0.15f
                )
                // Diagonal leg extending into infinity flourish
                moveTo(c.x - glyphRadius * 0.05f, c.y + glyphRadius * 0.15f)
                lineTo(c.x + glyphRadius * 0.95f, c.y + glyphRadius * 0.95f)
            }

            drawPath(
                path = spinePath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        VyxelBrand.WhiteGlow.copy(alpha = coreAlpha),
                        VyxelBrand.TealBright.copy(alpha = coreAlpha),
                        VyxelBrand.VioletBright.copy(alpha = coreAlpha)
                    )
                ),
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Central core glow dot
            drawCircle(
                color = VyxelBrand.TealBright.copy(alpha = 0.9f * coreAlpha),
                radius = 3.dp.toPx() * progress,
                center = Offset(c.x + glyphRadius * 0.95f, c.y + glyphRadius * 0.95f)
            )
        }

        // 6. Orbiting Photon Rider
        if (orbiting && progress > 0.6f) {
            val photonT = orbitPhase
            val orbitR = radius * 0.98f
            // Hexagonal parameterization or circular orbit
            val px = c.x + orbitR * cos(photonT)
            val py = c.y + orbitR * sin(photonT)
            val photonOffset = Offset(px, py)

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        VyxelBrand.TealBright.copy(alpha = 0.85f * progress),
                        VyxelBrand.Teal.copy(alpha = 0.35f * progress),
                        Color.Transparent
                    ),
                    center = photonOffset,
                    radius = 16.dp.toPx()
                ),
                radius = 16.dp.toPx(),
                center = photonOffset
            )

            // Core bead
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx() * progress,
                center = photonOffset
            )
        }
    }
}

/**
 * Vyxel typographic Wordmark.
 * Set wide with expansive tracking, gradient glyph styling, and refined subtitle badge.
 */
@Composable
fun AppWordmark(
    modifier: Modifier = Modifier,
    subtitle: String = "VYXEL EDITION • OFFLINE-FIRST VAULT"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            "EXPENSE TRACKER".forEachIndexed { index, ch ->
                val isAccent = ch == 'E' || ch == 'T' || ch == 'X'
                Text(
                    text = ch.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.4.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = if (isAccent) VyxelBrand.TealBright else VyxelBrand.WhiteGlow.copy(alpha = 0.94f),
                    modifier = Modifier.padding(horizontal = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.2.sp,
                fontSize = 10.sp
            ),
            color = VyxelBrand.Teal.copy(alpha = 0.85f)
        )
    }
}
