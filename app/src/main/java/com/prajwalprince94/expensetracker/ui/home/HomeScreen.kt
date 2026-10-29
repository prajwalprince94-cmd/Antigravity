package com.prajwalprince94.expensetracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRedLight
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreenLight
import com.prajwalprince94.expensetracker.ui.theme.TransferBlue
import com.prajwalprince94.expensetracker.ui.theme.TransferBlueLight

private data class AccountPreview(
    val name: String,
    val type: String,
    val balance: String,
    val icon: ImageVector
)

@Composable
fun HomeScreen(
    transactions: List<TransactionItem>,
    onOpenQuickAdd: (TransactionType) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onSelectTransaction: (TransactionItem) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic calculations from current transactions
    val totalIncomePaise = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amountPaise }

    val totalExpensePaise = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amountPaise }

    val netBalancePaise = (totalIncomePaise - totalExpensePaise).coerceAtLeast(0L)

    val accounts = listOf(
        AccountPreview("Paytm / UPI", "Primary UPI", "₹7,260.00", Icons.Default.PhoneAndroid),
        AccountPreview("HDFC Bank", "Savings Account", "₹35,000.00", Icons.Default.AccountBalance),
        AccountPreview("Cash Wallet", "Physical Cash", "₹4,240.00", Icons.Default.Payments)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Smartphone Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste 🙏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "My Finances",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { onShowMessage("All caught up! No unread notifications.") },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hero Net Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Total Net Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹%,.2f".format(netBalancePaise / 100.0),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Monthly Income vs Expense Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Income Pill
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenQuickAdd(TransactionType.INCOME) },
                            shape = RoundedCornerShape(18.dp),
                            color = IncomeGreenLight.copy(alpha = 0.95f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Income", fontSize = 11.sp, color = IncomeGreen)
                                    Text(
                                        text = "+₹%,.0f".format(totalIncomePaise / 100.0),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }

                        // Expense Pill
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenQuickAdd(TransactionType.EXPENSE) },
                            shape = RoundedCornerShape(18.dp),
                            color = ExpenseRedLight.copy(alpha = 0.95f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Expense", fontSize = 11.sp, color = ExpenseRed)
                                    Text(
                                        text = "-₹%,.0f".format(totalExpensePaise / 100.0),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Thumb Buttons (Working Click Handlers)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onOpenQuickAdd(TransactionType.EXPENSE) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { onOpenQuickAdd(TransactionType.INCOME) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = IncomeGreenLight,
                        contentColor = IncomeGreen
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Income", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { onOpenQuickAdd(TransactionType.TRANSFER) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Transfer",
                        tint = TransferBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Accounts Carousel Header
        item {
            Text(
                text = "Accounts (3 Active)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Interactive Account Cards Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { acc ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onShowMessage("Account: ${acc.name} (${acc.balance})") },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = acc.icon,
                                    contentDescription = acc.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = acc.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = acc.type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = acc.balance,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header with working "See All"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }
        }

        // Interactive Recent Transactions list
        items(transactions.take(5)) { txn ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectTransaction(txn) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val bgColor = when (txn.type) {
                        TransactionType.TRANSFER -> TransferBlueLight
                        TransactionType.EXPENSE -> ExpenseRedLight
                        TransactionType.INCOME -> IncomeGreenLight
                    }
                    val tintColor = when (txn.type) {
                        TransactionType.TRANSFER -> TransferBlue
                        TransactionType.EXPENSE -> ExpenseRed
                        TransactionType.INCOME -> IncomeGreen
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = txn.icon,
                            contentDescription = txn.category,
                            tint = tintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = txn.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${txn.category} • ${txn.account}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = txn.formattedAmount,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = tintColor
                        )
                        Text(
                            text = txn.formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        // Extra padding at bottom to clear floating nav
        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
