package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import java.util.Locale

/**
 * Odometer-style rolling numerals inspired by NikhilKain/stride.
 * Each character/digit springs vertically when changed, with smooth slide + fade physics.
 */
@Composable
fun AnimatedCounter(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { index, char ->
            // Key on distance from right so length changes don't unnecessarily remount everything
            val slot = text.length - index
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    val springSpec = spring<IntOffset>(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                    (slideInVertically(springSpec) { it } + fadeIn()) togetherWith
                            (slideOutVertically(springSpec) { -it } + fadeOut())
                },
                label = "odometer_slot_$slot"
            ) { c ->
                Text(
                    text = c.toString(),
                    style = style,
                    color = color
                )
            }
        }
    }
}

/**
 * Animated Indian Rupee Ticker for Paise Minor Units.
 * Converts Long paise to formatted Indian rupee string (e.g. ₹46,500.00 or ₹46,500)
 * and animates the digits with rolling spring physics.
 */
@Composable
fun AnimatedRupeeTicker(
    paise: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    showPaise: Boolean = true,
    prefix: String = "₹"
) {
    val formatted = if (showPaise) {
        String.format(Locale.getDefault(), "%s%,.2f", prefix, paise / 100.0)
    } else {
        String.format(Locale.getDefault(), "%s%,.0f", prefix, paise / 100.0)
    }

    AnimatedCounter(
        text = formatted,
        style = style,
        modifier = modifier,
        color = color
    )
}
