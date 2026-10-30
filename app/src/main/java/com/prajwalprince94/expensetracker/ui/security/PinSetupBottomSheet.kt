package com.prajwalprince94.expensetracker.ui.security

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.security.PinSecurityHelper
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 2-step setup sheet to establish or change the user's 4-digit App Lock PIN.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupBottomSheet(
    onDismiss: () -> Unit,
    onPinConfigured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) } // 1: Enter new PIN, 2: Confirm PIN
    var firstPin by remember { mutableStateOf("") }
    var secondPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }
    val currentPin = if (step == 1) firstPin else secondPin

    fun triggerShakeAnimation(msg: String) {
        scope.launch {
            isError = true
            errorMessage = msg
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -18f at 50 using FastOutSlowInEasing
                    18f at 100 using FastOutSlowInEasing
                    -12f at 150 using FastOutSlowInEasing
                    12f at 200 using FastOutSlowInEasing
                    0f at 350
                }
            )
            secondPin = ""
            isError = false
        }
    }

    fun onDigit(d: String) {
        if (currentPin.length < 4) {
            haptics.light()
            if (step == 1) {
                val next = firstPin + d
                firstPin = next
                if (next.length == 4) {
                    haptics.selection()
                    step = 2
                }
            } else {
                val next = secondPin + d
                secondPin = next
                if (next.length == 4) {
                    if (next == firstPin) {
                        haptics.confirm()
                        val hash = PinSecurityHelper.hashPin(next)
                        onPinConfigured(hash)
                    } else {
                        haptics.error()
                        triggerShakeAnimation("PINs do not match. Re-enter PIN.")
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (currentPin.isNotEmpty()) {
            haptics.light()
            if (step == 1) {
                firstPin = firstPin.dropLast(1)
            } else {
                secondPin = secondPin.dropLast(1)
            }
            isError = false
            errorMessage = ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(ChipShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (step == 1) "Set 4-Digit PIN" else "Confirm 4-Digit PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    isError -> errorMessage
                    step == 1 -> "Create a secure PIN to protect your expense records"
                    else -> "Re-enter the same 4 digits to confirm"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Dots
            Row(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < currentPin.length
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isError -> ExpenseRed
                            isFilled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        animationSpec = spring(),
                        label = "dotColor"
                    )
                    val dotScale = if (isFilled) 1.2f else 1.0f

                    Box(
                        modifier = Modifier
                            .size(15.dp)
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

            Spacer(modifier = Modifier.height(24.dp))

            // 3x4 Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "" -> Spacer(modifier = Modifier.size(62.dp))
                                "DEL" -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .bouncyClickable(scaleDown = 0.88f) { onBackspace() }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .bouncyClickable(scaleDown = 0.88f) { onDigit(key) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 22.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
