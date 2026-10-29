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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.components.AnimatedRupeeTicker
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CategoryBudgetItem(
    val id: String,
    val category: String,
    val spentPaise: Long,
    var limitPaise: Long,
    val icon: ImageVector
) {
    val progress: Float get() = (spentPaise.toFloat() / limitPaise.coerceAtLeast(1L).toFloat()).coerceAtLeast(0f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val budgetItems = remember {
        mutableStateListOf(
            CategoryBudgetItem("1", "Food & Dining", 620000L, 1000000L, Icons.Default.Fastfood),
            CategoryBudgetItem("2", "Groceries", 780000L, 900000L, Icons.Default.ShoppingCart),
            CategoryBudgetItem("3", "Shopping & Leisure", 540000L, 500000L, Icons.Default.ReceiptLong),
            CategoryBudgetItem("4", "Fuel & Transport", 280000L, 400000L, Icons.Default.LocalGasStation),
            CategoryBudgetItem("5", "Health & Fitness", 120000L, 300000L, Icons.Default.FitnessCenter)
        )
    }

    var editingBudget by remember { mutableStateOf<CategoryBudgetItem?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { editingBudget = budgetItems.first() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                        editingBudget = budgetItems.first()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Limit", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hero Monthly Budget Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(1),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Budget Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AnimatedRupeeTicker(
                                paise = totalSpentPaise,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "%.0f%%".format(overallProgress * 100),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expressive Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedOverallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (overallProgress > 0.9f) ExpenseRed else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Remaining: ₹%,.0f".format((totalLimitPaise - totalSpentPaise).coerceAtLeast(0L) / 100.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Limit: ₹%,.0f".format(totalLimitPaise / 100.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Category Limits (Tap to customize)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.entrance(2)
            )
        }

        // Category Budget Items with Spring Progress and Bouncy Click
        itemsIndexed(budgetItems) { index, budget ->
            val animatedProgress by animateFloatAsState(
                targetValue = budget.progress.coerceIn(0f, 1f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "catProgress$index"
            )

            val progressColor = when {
                budget.progress > 1f -> ExpenseRed
                budget.progress > 0.8f -> Color(0xFFE65100)
                else -> IncomeGreen
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(3 + index)
                    .bouncyClickable(scaleDown = 0.95f) {
                        editingBudget = budget
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = budget.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = budget.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Spent: ₹%,.0f / ₹%,.0f".format(budget.spentPaise / 100.0, budget.limitPaise / 100.0),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Modal Sheet to edit budget limits (Interactive GoalEditor style)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit ${itemToEdit.category} Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Current Limit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                AnimatedRupeeTicker(
                    paise = itemToEdit.limitPaise,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                                    val newLimit = (itemToEdit.limitPaise + delta).coerceAtLeast(100000L)
                                    val idx = budgetItems.indexOfFirst { it.id == itemToEdit.id }
                                    if (idx != -1) {
                                        budgetItems[idx] = budgetItems[idx].copy(limitPaise = newLimit)
                                        editingBudget = budgetItems[idx]
                                    }
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

                Button(
                    onClick = {
                        onShowMessage("Updated ${itemToEdit.category} limit to ₹%,.0f".format(itemToEdit.limitPaise / 100.0))
                        editingBudget = null
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .bouncyClickable(scaleDown = 0.94f) {
                            onShowMessage("Updated ${itemToEdit.category} limit to ₹%,.0f".format(itemToEdit.limitPaise / 100.0))
                            editingBudget = null
                        }
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
