package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.util.Locale

/**
 * High-performance Animated Indian Rupee Ticker for Paise Minor Units.
 * Smoothly transitions balances with Stride vertical slide + fade spring motion
 * without subcomposition overhead.
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

    AnimatedContent(
        targetState = formatted,
        transitionSpec = {
            val springSpec = spring<androidx.compose.ui.unit.IntOffset>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            (slideInVertically(springSpec) { it / 2 } + fadeIn(spring())) togetherWith
                    (slideOutVertically(springSpec) { -it / 2 } + fadeOut(spring()))
        },
        label = "rupeeTicker",
        modifier = modifier
    ) { text ->
        Text(
            text = text,
            style = style.copy(fontFeatureSettings = "tnum"),
            color = color
        )
    }
}

/** Fallback AnimatedCounter */
@Composable
fun AnimatedCounter(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically { it / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { -it / 2 } + fadeOut())
        },
        label = "counter",
        modifier = modifier
    ) { str ->
        Text(text = str, style = style.copy(fontFeatureSettings = "tnum"), color = color)
    }
}
