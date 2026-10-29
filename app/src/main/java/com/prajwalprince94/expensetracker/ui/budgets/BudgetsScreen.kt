package com.prajwalprince94.expensetracker.ui.budgets

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.model.CategoryBudgetItem
import com.prajwalprince94.expensetracker.ui.components.AnimatedRupeeTicker
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.isWideScreen
import com.prajwalprince94.expensetracker.ui.components.responsiveHorizontalPadding
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    budgetItems: List<CategoryBudgetItem>,
    onUpdateBudgetLimit: (String, Long) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingBudget by remember { mutableStateOf<CategoryBudgetItem?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val wide = isWideScreen()
    val horizontalPadding = responsiveHorizontalPadding()

    val totalSpentPaise = budgetItems.sumOf { it.spentPaise }
    val totalLimitPaise = budgetItems.sumOf { it.limitPaise }
    val overallProgress = (totalSpentPaise.toFloat() / totalLimitPaise.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

    val animatedOverallProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "budgetProgress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(0),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable(scaleDown = 0.88f) {
                            onShowMessage("Tap any budget category below to adjust its monthly limit.")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Budget",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Hero Overall Budget GlassCard
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(1),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentPadding = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL BUDGET SPENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedRupeeTicker(
                            paise = totalSpentPaise,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL LIMIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹%,.0f".format(totalLimitPaise / 100.0),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedOverallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (overallProgress > 0.9f) ExpenseRed else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%.1f%% Utilized".format(overallProgress * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overallProgress > 0.9f) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹%,.0f Left".format((totalLimitPaise - totalSpentPaise).coerceAtLeast(0L) / 100.0),
                        style = MaterialTheme.typography.bodySmall,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Category Budgets Header
        item {
            SectionHeader(
                title = "Category Breakdown (${budgetItems.size})",
                trailing = {
                    Text(
                        text = "Tap to Adjust",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.entrance(2)
            )
        }

        // 4. Budget Items with Stride GlassCard & Edit Action
        items(budgetItems) { budget ->
            val isOver = budget.isOverBudget
            val progressColor = if (isOver) ExpenseRed else if (budget.progress > 0.8f) Color(0xFFFF9800) else IncomeGreen

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable(scaleDown = 0.96f) {
                        editingBudget = budget
                    },
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = budget.icon,
                            contentDescription = budget.category,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = budget.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusPill(
                                label = if (isOver) "Exceeded" else "%.0f%%".format(budget.progress * 100),
                                color = progressColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Spent: ₹%,.0f".format(budget.spentPaise / 100.0),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Limit: ₹%,.0f".format(budget.limitPaise / 100.0),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { budget.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier.bouncyClickable(scaleDown = 0.92f) {
                            editingBudget = budget
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Modal Bottom Sheet with Stride Interactive Budget Scrubber
    editingBudget?.let { itemToEdit ->
        ModalBottomSheet(
            onDismissRequest = { editingBudget = null },
            sheetState = editSheetState,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adjust ${itemToEdit.category} Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Slide or use quick increments to adjust monthly spending limit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "NEW MONTHLY LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))
                AnimatedRupeeTicker(
                    paise = itemToEdit.limitPaise,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Stride Interactive Smooth Limit Slider
                Slider(
                    value = itemToEdit.limitPaise.toFloat(),
                    onValueChange = { newFloat ->
                        val roundedPaise = (newFloat.toLong() / 50000L) * 50000L
                        onUpdateBudgetLimit(itemToEdit.id, roundedPaise)
                        editingBudget = itemToEdit.copy(limitPaise = roundedPaise)
                    },
                    valueRange = 100000f..5000000f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Increment / Decrement Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val increments = listOf(-200000L, -50000L, 50000L, 200000L)
                    increments.forEach { delta ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bouncyClickable(scaleDown = 0.9f) {
                                    val newLimit = (itemToEdit.limitPaise + delta).coerceIn(100000L, 5000000L)
                                    onUpdateBudgetLimit(itemToEdit.id, newLimit)
                                    editingBudget = itemToEdit.copy(limitPaise = newLimit)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (delta > 0) "+₹%,.0f".format(delta / 100.0) else "-₹%,.0f".format(-delta / 100.0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bouncyClickable(scaleDown = 0.94f) {
                            onShowMessage("Saved ${itemToEdit.category} limit: ₹%,.0f".format(itemToEdit.limitPaise / 100.0))
                            editingBudget = null
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Save Budget",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
