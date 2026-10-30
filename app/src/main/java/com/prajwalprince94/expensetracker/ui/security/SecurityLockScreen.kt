package com.prajwalprince94.expensetracker.ui.security

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.prajwalprince94.expensetracker.security.BiometricSecurityManager
import com.prajwalprince94.expensetracker.security.PinSecurityHelper
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Benchmark Stride & Appllama security lock screen.
 * Supports tactile 4-digit PIN authentication with spring shake error states,
 * biometric quick-unlock trigger, and OLED-optimized deep background.
 */
@Composable
fun SecurityLockScreen(
    storedPinHash: String,
    isBiometricEnabled: Boolean,
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }
    val isBiometricAvailable = remember(context) { BiometricSecurityManager.isBiometricAvailable(context) }

    // Auto-prompt biometrics once on screen display if enabled
    LaunchedEffect(isBiometricEnabled, isBiometricAvailable) {
        if (isBiometricEnabled && isBiometricAvailable) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                BiometricSecurityManager.promptBiometric(
                    activity = activity,
                    onSuccess = {
                        haptics.confirm()
                        onUnlockSuccess()
                    },
                    onError = { _, _ -> /* Fallback quietly to PIN keypad */ },
                    onFailed = { haptics.error() }
                )
            }
        }
    }

    fun triggerShakeAnimation() {
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -20f at 50 using FastOutSlowInEasing
                    20f at 100 using FastOutSlowInEasing
                    -15f at 150 using FastOutSlowInEasing
                    15f at 200 using FastOutSlowInEasing
                    -10f at 250 using FastOutSlowInEasing
                    10f at 300 using FastOutSlowInEasing
                    0f at 400
                }
            )
            enteredPin = ""
            isError = false
        }
    }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            haptics.light()
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == 4) {
                if (PinSecurityHelper.verifyPin(newPin, storedPinHash)) {
                    haptics.confirm()
                    onUnlockSuccess()
                } else {
                    haptics.error()
                    isError = true
                    errorMessage = "Incorrect PIN. Try again."
                    triggerShakeAnimation()
                }
            }
        }
    }

    fun onBackspacePress() {
        if (enteredPin.isNotEmpty()) {
            haptics.light()
            enteredPin = enteredPin.dropLast(1)
            isError = false
            errorMessage = ""
        }
    }

    fun onBiometricIconPress() {
        val activity = context as? FragmentActivity
        if (activity != null && isBiometricAvailable) {
            haptics.selection()
            BiometricSecurityManager.promptBiometric(
                activity = activity,
                onSuccess = {
                    haptics.confirm()
                    onUnlockSuccess()
                },
                onError = { _, err ->
                    errorMessage = err.toString()
                },
                onFailed = {
                    haptics.error()
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shield Icon inside squircle container
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(ChipShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .entrance(0),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Shield",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Expense Tracker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isError) errorMessage else "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 4 PIN Dots indicator with shake offset
            Row(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isError -> ExpenseRed
                            isFilled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        animationSpec = spring(),
                        label = "dotColor"
                    )
                    val dotScale = if (isFilled) 1.25f else 1.0f

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(
                                width = if (isFilled || isError) 0.dp else 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Responsive 12-button Keypad Grid (3x4)
            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypadRows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "BIO" -> {
                                    if (isBiometricEnabled && isBiometricAvailable) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .bouncyClickable(scaleDown = 0.88f) { onBiometricIconPress() }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Fingerprint,
                                                    contentDescription = "Biometric Unlock",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(68.dp))
                                    }
                                }
                                "DEL" -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .bouncyClickable(scaleDown = 0.88f) { onBackspacePress() }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Delete Digit",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .bouncyClickable(scaleDown = 0.88f) { onDigitPress(key) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 26.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
