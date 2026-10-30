package com.prajwalprince94.expensetracker.ui.boot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.brand.AppMark
import com.prajwalprince94.expensetracker.ui.brand.AppWordmark
import com.prajwalprince94.expensetracker.ui.brand.LissajousGlow
import com.prajwalprince94.expensetracker.ui.brand.VyxelBrand
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Vyxel Boot Sequence Screen inspired by NikhilKain/vyxel-apps.
 *
 * Sequence:
 * 1. Deep obsidian backdrop with living Lissajous plasma glow.
 * 2. Vector AppMark spring entrance + photon orbit initialization.
 * 3. Typographic AppWordmark staggered reveal.
 * 4. Staged telemetry diagnostic steps (Kernel, Vault, Supabase, Engines, Armed).
 * 5. Shimmering progress bar and seamless transition trigger.
 * 6. Responsive to user tap to skip instantly.
 */
@Composable
fun BootScreen(
    isDataLoaded: Boolean,
    onBootComplete: () -> Unit,
    modifier: Modifier = Modifier,
    minDisplayDurationMillis: Long = 1900L
) {
    val markScale = remember { Animatable(0.28f) }
    val markAlpha = remember { Animatable(0f) }
    val markProgress = remember { Animatable(0f) }

    val wordmarkAlpha = remember { Animatable(0f) }
    val wordmarkSlide = remember { Animatable(24f) }

    val telemetryAlpha = remember { Animatable(0f) }
    val progressBar = remember { Animatable(0.08f) }

    var telemetryStage by remember { mutableIntStateOf(0) }
    var bootCompletedTriggered by remember { mutableStateOf(false) }

    val telemetryMessages = remember {
        listOf(
            "INITIALIZING VYXEL KERNEL...",
            "MOUNTING ENCRYPTED SQLITE VAULT...",
            "CHECKING SUPABASE REAL-TIME QUEUE...",
            "CALIBRATING MULTI-CURRENCY FX ENGINE...",
            "SYSTEM ARMED & SECURE"
        )
    }

    // Master Entrance Animation Coordinator
    LaunchedEffect(Unit) {
        // Step 1: AppMark entrance
        launch {
            markAlpha.animateTo(1f, tween(320, easing = LinearOutSlowInEasing))
        }
        launch {
            markScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            markProgress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }

        // Step 2: Wordmark entrance
        delay(360L)
        launch {
            wordmarkAlpha.animateTo(1f, tween(400, easing = LinearOutSlowInEasing))
        }
        launch {
            wordmarkSlide.animateTo(
                0f,
                spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
            )
        }

        // Step 3: Telemetry entrance & stepped progression
        delay(200L)
        launch {
            telemetryAlpha.animateTo(1f, tween(300))
        }

        val stepInterval = (minDisplayDurationMillis - 600L) / telemetryMessages.size
        for (i in telemetryMessages.indices) {
            telemetryStage = i
            val targetProgress = (i + 1).toFloat() / telemetryMessages.size
            launch {
                progressBar.animateTo(
                    targetProgress.coerceAtMost(0.95f),
                    tween(stepInterval.toInt(), easing = LinearEasing)
                )
            }
            delay(stepInterval)
        }
    }

    // Completion Gatekeeper: waits for both data load & min duration
    LaunchedEffect(isDataLoaded) {
        delay(minDisplayDurationMillis)
        if (!bootCompletedTriggered) {
            bootCompletedTriggered = true
            progressBar.animateTo(1f, tween(180))
            delay(120L)
            onBootComplete()
        }
    }

    // Tap to Skip handler
    val tapToSkip: () -> Unit = {
        if (!bootCompletedTriggered) {
            bootCompletedTriggered = true
            onBootComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VyxelBrand.ObsidianCard,
                        VyxelBrand.Obsidian
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = tapToSkip
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- LAYER 1: Lissajous Plasma Glow Background ---
        LissajousGlow(
            modifier = Modifier.fillMaxSize(),
            alpha = 0.88f,
            speedMillis = 5200
        )

        // --- LAYER 2: Central Brand & Boot Telemetry ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // AppMark Centerpiece
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = markAlpha.value
                    scaleX = markScale.value
                    scaleY = markScale.value
                },
                contentAlignment = Alignment.Center
            ) {
                AppMark(
                    size = 118.dp,
                    progress = markProgress.value,
                    orbiting = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // AppWordmark
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkAlpha.value
                    translationY = wordmarkSlide.value.dp.toPx()
                }
            ) {
                AppWordmark(
                    subtitle = "VYXEL CORE • OFFLINE-FIRST VAULT"
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Boot Telemetry Capsule
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = telemetryAlpha.value
                }
            ) {
                // Stepped Telemetry Status Message
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.height(26.dp)
                ) {
                    // Pulsing Status Beacon Dot
                    PulsingBeaconDot(
                        color = if (telemetryStage == telemetryMessages.lastIndex)
                            VyxelBrand.TealBright else VyxelBrand.VioletBright
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AnimatedContent(
                        targetState = telemetryMessages.getOrElse(telemetryStage) { "INITIALIZING..." },
                        transitionSpec = {
                            (fadeIn(tween(180)) togetherWith fadeOut(tween(120)))
                        },
                        label = "telemetryTransition"
                    ) { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = VyxelBrand.WhiteGlow.copy(alpha = 0.82f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shimmering Progress Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VyxelBrand.VioletDeep.copy(alpha = 0.40f),
                    modifier = Modifier
                        .width(210.dp)
                        .height(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Active filled bar
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = progressBar.value
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                                }
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            VyxelBrand.VioletBright,
                                            VyxelBrand.TealBright,
                                            VyxelBrand.WhiteGlow
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        // --- LAYER 3: Bottom Version & Skip Prompt ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .graphicsLayer { alpha = telemetryAlpha.value }
        ) {
            Text(
                text = "TAP ANYWHERE TO SKIP",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.6.sp,
                    fontSize = 9.sp
                ),
                color = VyxelBrand.WhiteGlow.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun PulsingBeaconDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "beacon")
    val scale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(CircleShape)
            .background(color)
    )
}
