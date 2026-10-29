package com.prajwalprince94.expensetracker.ui.home

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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.components.AnimatedRupeeTicker
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.glow
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRedDark
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRedLight
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreenDark
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreenLight
import com.prajwalprince94.expensetracker.ui.theme.TransferBlue
import com.prajwalprince94.expensetracker.ui.theme.TransferBlueDark
import com.prajwalprince94.expensetracker.ui.theme.TransferBlueLight
import com.prajwalprince94.expensetracker.ui.theme.getFinancialColor
import com.prajwalprince94.expensetracker.ui.theme.getFinancialContainerColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
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
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "My Finances",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable(scaleDown = 0.88f) {
                            onShowMessage("All caught up! No unread notifications.")
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Hero Net Balance GlassCard with Soft Glow & Rolling Odometer
        item {
            val isDark = isSystemInDarkTheme()
            val incomePillBg = if (isDark) Color(0xFF10B981).copy(alpha = 0.16f) else IncomeGreenLight
            val incomePillText = if (isDark) IncomeGreenDark else Color(0xFF059669)
            val expensePillBg = if (isDark) Color(0xFFEF4444).copy(alpha = 0.16f) else ExpenseRedLight
            val expensePillText = if (isDark) ExpenseRedDark else Color(0xFFDC2626)

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .glow(MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), radius = 90.dp, alpha = 0.25f)
                    .entrance(1),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentPadding = 22.dp
            ) {
                Text(
                    text = "TOTAL NET BALANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                AnimatedRupeeTicker(
                    paise = netBalancePaise,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Income vs Expense Pills with Stride/Range bouncy clicks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Income Pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable(scaleDown = 0.94f) {
                                onOpenQuickAdd(TransactionType.INCOME)
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = incomePillBg,
                        border = if (isDark) BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.35f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Income", fontSize = 11.sp, color = incomePillText, fontWeight = FontWeight.Bold)
                                AnimatedRupeeTicker(
                                    paise = totalIncomePaise,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = incomePillText,
                                    prefix = "+"
                                )
                            }
                        }
                    }

                    // Expense Pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable(scaleDown = 0.94f) {
                                onOpenQuickAdd(TransactionType.EXPENSE)
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = expensePillBg,
                        border = if (isDark) BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.35f)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Expense", fontSize = 11.sp, color = expensePillText, fontWeight = FontWeight.Bold)
                                AnimatedRupeeTicker(
                                    paise = totalExpensePaise,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = expensePillText,
                                    prefix = "-"
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Expense",
                    containerColor = ExpenseRed,
                    contentColor = Color.White,
                    onClick = { onOpenQuickAdd(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.ArrowDownward,
                    label = "Income",
                    containerColor = IncomeGreen,
                    contentColor = Color.White,
                    onClick = { onOpenQuickAdd(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.SwapHoriz,
                    label = "Transfer",
                    containerColor = TransferBlue,
                    contentColor = Color.White,
                    onClick = { onOpenQuickAdd(TransactionType.TRANSFER) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { onShowMessage("Camera Scanner ready! Select expense category to auto-log.") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Accounts & Wallets Carousel with GlassCard
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(3)
            ) {
                SectionHeader(
                    title = "Accounts & Wallets",
                    trailing = {
                        Text(
                            text = "3 Linked",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(accounts) { acc ->
                        GlassCard(
                            modifier = Modifier
                                .width(185.dp)
                                .bouncyClickable(scaleDown = 0.94f) {
                                    onShowMessage("${acc.name} selected: ${acc.balance}")
                                },
                            contentPadding = 16.dp
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = acc.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = acc.type,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Text(
                                text = acc.balance,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Transactions Header with "See All"
        item {
            SectionHeader(
                title = "Recent Transactions",
                trailing = {
                    Text(
                        text = "See All (${transactions.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                            onNavigateToTransactions()
                        }
                    )
                },
                modifier = Modifier.entrance(4)
            )
        }

        // 6. Recent Transaction Items (Top 5) with GlassCard & StatusPill
        val recentList = transactions.take(5)
        val isDarkTheme = isSystemInDarkTheme()
        items(recentList) { txn ->
            val tintColor = getFinancialColor(txn.type, isDarkTheme)
            val bgColor = getFinancialContainerColor(txn.type, isDarkTheme)

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable(scaleDown = 0.95f) {
                        onSelectTransaction(txn)
                    },
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(
                                label = txn.type.name,
                                color = tintColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${txn.category} • ${txn.formattedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = txn.formattedAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = tintColor
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .bouncyClickable(scaleDown = 0.9f, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
