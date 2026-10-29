package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Interactive Tape-Measure Budget Dial inspired by NikhilKain/Range.
 * Allows scrubbing amounts with tactile haptic tick feedback and smooth inertia.
 */
@Composable
fun BudgetTape(
    valuePaise: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    minPaise: Long = 50000L,       // ₹500
    maxPaise: Long = 5000000L,     // ₹50,000
    stepPaise: Long = 50000L       // ₹500 steps
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val measurer = rememberTextMeasurer()

    val pxPerStep = with(density) { 32.dp.toPx() }
    var localPaise by remember(valuePaise) { mutableLongStateOf(valuePaise) }
    var lastTickPaise by remember { mutableLongStateOf(valuePaise) }

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tickMajor = MaterialTheme.colorScheme.onSurface
    val tickMinor = MaterialTheme.colorScheme.outlineVariant
    val needleColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { deltaX ->
                    val deltaSteps = deltaX / pxPerStep
                    val next = (localPaise - (deltaSteps * stepPaise).toLong()).coerceIn(minPaise, maxPaise)
                    localPaise = next
                    if (abs(next - lastTickPaise) >= stepPaise) {
                        lastTickPaise = next
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onValueChange(next)
                }
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(66.dp)) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f

            val currentStepIndex = localPaise / stepPaise.toFloat()

            // Draw ticks
            val visibleStepsRange = (centerX / pxPerStep).toInt() + 2
            val minIndex = (currentStepIndex - visibleStepsRange).toInt().coerceAtLeast(0)
            val maxIndex = (currentStepIndex + visibleStepsRange).toInt().coerceAtMost((maxPaise / stepPaise).toInt())

            for (i in minIndex..maxIndex) {
                val tickX = centerX + (i - currentStepIndex) * pxPerStep
                val isMajor = i % 5 == 0
                val tickHeight = if (isMajor) 28.dp.toPx() else 14.dp.toPx()
                val tickColor = if (isMajor) tickMajor else tickMinor

                drawLine(
                    color = tickColor,
                    start = Offset(tickX, height - tickHeight),
                    end = Offset(tickX, height),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                if (isMajor) {
                    val rupeeAmount = (i * stepPaise) / 100
                    val label = if (rupeeAmount >= 1000) "${rupeeAmount / 1000}k" else "$rupeeAmount"
                    val textLayout = measurer.measure(
                        text = label,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    )
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(tickX - textLayout.size.width / 2f, 2.dp.toPx())
                    )
                }
            }

            // Central active indicator needle
            drawLine(
                color = needleColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, height),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
