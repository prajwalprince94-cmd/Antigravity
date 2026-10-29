package com.prajwalprince94.expensetracker.ui.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen

private data class BudgetPreviewItem(
    val category: String,
    val spentPaise: Long,
    val limitPaise: Long,
    val progressColor: Color
) {
    val spentFormatted: String get() = "₹%,.0f".format(spentPaise / 100.0)
    val limitFormatted: String get() = "₹%,.0f".format(limitPaise / 100.0)
    val progress: Float get() = (spentPaise.toFloat() / limitPaise.toFloat()).coerceAtLeast(0f)
}

@Composable
fun BudgetsScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryBudgets = listOf(
        BudgetPreviewItem("Food & Dining", 620000L, 1000000L, IncomeGreen),
        BudgetPreviewItem("Groceries", 780000L, 900000L, Color(0xFFE65100)), // Orange warning
        BudgetPreviewItem("Shopping & Leisure", 540000L, 500000L, ExpenseRed), // Over budget
        BudgetPreviewItem("Utilities & Bills", 310000L, 600000L, IncomeGreen)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Budgets",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "October 2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onShowMessage("Custom Budget limits unlock in Level 5 with Room persistence!") },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Budget")
                }
            }
        }

        // Overall Monthly Budget Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowMessage("Total budget: ₹35,000. Remaining: ₹12,500. On track!") },
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Spending Limit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IncomeGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "On Track",
                                color = IncomeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹22,500",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹12,500",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic progress bar
                    LinearProgressIndicator(
                        progress = { 0.64f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = IncomeGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "64% of ₹35,000 monthly target spent (Tap to inspect)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        item {
            Text(
                text = "Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Category Budget Cards with interactive tap
        items(categoryBudgets) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val remaining = (item.limitPaise - item.spentPaise) / 100.0
                        if (remaining >= 0) {
                            onShowMessage("${item.category}: ₹%,.0f remaining in budget".format(remaining))
                        } else {
                            onShowMessage("⚠️ ${item.category}: Exceeded by ₹%,.0f!".format(-remaining))
                        }
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.spentFormatted} / ${item.limitFormatted}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = item.progressColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { item.progress.coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = item.progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (item.progress > 1f) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ ₹400 over budget!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Bottom spacing for floating nav
        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
