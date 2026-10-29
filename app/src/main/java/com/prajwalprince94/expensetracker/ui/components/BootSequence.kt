package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Branded boot sequence inspired by NikhilKain/vyxel-apps.
 * Displays a pulsating brand medallion, spring scale overshoot, and smooth transition
 * into the main application once local storage is initialized.
 */
@Composable
fun BootSequence(
    isDataLoaded: Boolean,
    onBootComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0.1f) }

    LaunchedEffect(Unit) {
        launch {
            alphaAnim.animateTo(1f, tween(300, easing = LinearOutSlowInEasing))
        }
        launch {
            scaleAnim.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            progressAnim.animateTo(0.9f, tween(700, easing = FastOutSlowInEasing))
        }
    }

    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            delay(850L) // Ensure smooth presentation without jarring flash
            progressAnim.animateTo(1f, tween(200))
            delay(150L)
            onBootComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                alpha = alphaAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
        ) {
            // Glowing Rupee Brand Medallion
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .glow(MaterialTheme.colorScheme.primary, radius = 70.dp, alpha = 0.45f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pulse(from = 0.96f, to = 1.05f, durationMs = 1300),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 46.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "EXPENSE TRACKER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Stride Edition • Offline SQLite Vault",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Loading pill indicator with shimmer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .width(160.dp)
                    .height(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { scaleX = progressAnim.value }
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
