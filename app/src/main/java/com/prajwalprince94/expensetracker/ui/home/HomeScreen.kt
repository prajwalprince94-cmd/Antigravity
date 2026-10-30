package com.prajwalprince94.expensetracker.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.components.AnimatedRupeeTicker
import com.prajwalprince94.expensetracker.ui.components.CategoryDistributionCard
import com.prajwalprince94.expensetracker.ui.components.EmptyStateView
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.SpendingTrendLineChart
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.glow
import com.prajwalprince94.expensetracker.ui.components.isWideScreen
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.components.responsiveHorizontalPadding
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
import com.prajwalprince94.expensetracker.ui.components.IconRegistry
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRedDark
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRedLight
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreenDark
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreenLight
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.TransferBlue
import com.prajwalprince94.expensetracker.ui.theme.getFinancialColor
import com.prajwalprince94.expensetracker.ui.theme.getFinancialContainerColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    transactions: List<TransactionItem>,
    accounts: List<AccountItem> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    onOpenQuickAdd: (TransactionType) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onLogSubscriptionPayment: (String) -> Unit = {},
    onSelectTransaction: (TransactionItem) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val wide = isWideScreen()
    val horizontalPadding = responsiveHorizontalPadding()
    val haptics = rememberAppHaptics()

    val totalIncomePaise = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amountPaise }

    val totalExpensePaise = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amountPaise }

    val netBalancePaise = (totalIncomePaise - totalExpensePaise).coerceAtLeast(0L)

    val effectiveAccounts = if (accounts.isNotEmpty()) accounts else AccountItem.defaultList()

    val incomePillBg = if (isDark) Color(0xFF10B981).copy(alpha = 0.16f) else IncomeGreenLight
    val incomePillText = if (isDark) IncomeGreenDark else Color(0xFF059669)
    val expensePillBg = if (isDark) Color(0xFFEF4444).copy(alpha = 0.16f) else ExpenseRedLight
    val expensePillText = if (isDark) ExpenseRedDark else Color(0xFFDC2626)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header with Current Date & Notifications
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
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(44.dp)
                        .bouncyClickable(scaleDown = 0.88f) {
                            haptics.light()
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

        // Adaptive Layout: If wide screen (tablet/foldable), render Hero + Actions and Charts side by side
        if (wide) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HeroBalanceCard(
                            netBalancePaise = netBalancePaise,
                            totalIncomePaise = totalIncomePaise,
                            totalExpensePaise = totalExpensePaise,
                            incomePillBg = incomePillBg,
                            incomePillText = incomePillText,
                            expensePillBg = expensePillBg,
                            expensePillText = expensePillText,
                            isDark = isDark,
                            onOpenQuickAdd = onOpenQuickAdd
                        )

                        QuickActionsRow(
                            onOpenQuickAdd = onOpenQuickAdd,
                            onShowMessage = onShowMessage
                        )

                        SpendingTrendLineChart(transactions = transactions)
                    }

                    // Right Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryDistributionCard(transactions = transactions)
                        AccountsCarousel(
                            accounts = effectiveAccounts,
                            transactions = transactions,
                            onNavigateToAccounts = onNavigateToAccounts,
                            onShowMessage = onShowMessage
                        )
                    }
                }
            }
        } else {
            // Standard Smartphone Layout (Single Column)
            // 2. Hero Net Balance GlassCard
            item {
                HeroBalanceCard(
                    netBalancePaise = netBalancePaise,
                    totalIncomePaise = totalIncomePaise,
                    totalExpensePaise = totalExpensePaise,
                    incomePillBg = incomePillBg,
                    incomePillText = incomePillText,
                    expensePillBg = expensePillBg,
                    expensePillText = expensePillText,
                    isDark = isDark,
                    onOpenQuickAdd = onOpenQuickAdd
                )
            }

            // 3. Quick Action Buttons
            item {
                QuickActionsRow(
                    onOpenQuickAdd = onOpenQuickAdd,
                    onShowMessage = onShowMessage
                )
            }

            // 4. Interactive 7-Day Spending Bezier Curve Line Chart
            item {
                SpendingTrendLineChart(
                    transactions = transactions,
                    modifier = Modifier.entrance(2)
                )
            }

            // 5. Category Distribution Breakdown Card
            item {
                CategoryDistributionCard(
                    transactions = transactions,
                    modifier = Modifier.entrance(3)
                )
            }

            // 5.5 Upcoming Subscriptions & Bills Carousel
            if (subscriptions.isNotEmpty()) {
                item {
                    UpcomingBillsCarousel(
                        subscriptions = subscriptions,
                        onNavigateToSubscriptions = onNavigateToSubscriptions,
                        onLogPayment = onLogSubscriptionPayment,
                        onShowMessage = onShowMessage
                    )
                }
            }

            // 6. Accounts & Wallets Carousel
            item {
                AccountsCarousel(
                    accounts = effectiveAccounts,
                    transactions = transactions,
                    onNavigateToAccounts = onNavigateToAccounts,
                    onShowMessage = onShowMessage
                )
            }
        }

        // Recent Transactions Section Header
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

        // Recent Transaction Items
        if (transactions.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Transactions Yet",
                    subtitle = "Tap below or use the quick action buttons to log your first income, expense, or transfer.",
                    actionLabel = "+ Add Transaction",
                    onAction = { onOpenQuickAdd(TransactionType.EXPENSE) },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            val recentList = transactions.take(5)
            items(recentList) { txn ->
                val tintColor = getFinancialColor(txn.type, isDark)
                val bgColor = getFinancialContainerColor(txn.type, isDark)

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
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun HeroBalanceCard(
    netBalancePaise: Long,
    totalIncomePaise: Long,
    totalExpensePaise: Long,
    incomePillBg: Color,
    incomePillText: Color,
    expensePillBg: Color,
    expensePillText: Color,
    isDark: Boolean,
    onOpenQuickAdd: (TransactionType) -> Unit
) {
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

@Composable
private fun QuickActionsRow(
    onOpenQuickAdd: (TransactionType) -> Unit,
    onShowMessage: (String) -> Unit
) {
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = { onShowMessage("Camera Scanner ready! Select expense category to auto-log.") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AccountsCarousel(
    accounts: List<AccountItem>,
    transactions: List<TransactionItem>,
    onNavigateToAccounts: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .entrance(3)
    ) {
        SectionHeader(
            title = "Accounts & Wallets",
            trailing = {
                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                        onNavigateToAccounts()
                    }
                ) {
                    Text(
                        text = "Manage (${accounts.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(accounts) { acc ->
                val liveBalancePaise = remember(acc, transactions) {
                    val credited = transactions
                        .filter { it.type == TransactionType.INCOME && it.account.equals(acc.name, ignoreCase = true) }
                        .sumOf { it.amountPaise }
                    val debited = transactions
                        .filter { it.type == TransactionType.EXPENSE && it.account.equals(acc.name, ignoreCase = true) }
                        .sumOf { it.amountPaise }
                    (acc.initialBalancePaise + credited - debited).coerceAtLeast(0L)
                }
                val formattedLiveBalance = "₹%,.2f".format(Locale.US, liveBalancePaise / 100.0)
                val accColor = IconRegistry.parseColor(acc.colorHex)

                GlassCard(
                    modifier = Modifier
                        .width(185.dp)
                        .bouncyClickable(scaleDown = 0.94f) {
                            onNavigateToAccounts()
                        },
                    contentPadding = 16.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(accColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconRegistry.getIcon(acc.iconKey),
                                contentDescription = null,
                                tint = accColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                        text = formattedLiveBalance,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
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

@Composable
private fun UpcomingBillsCarousel(
    subscriptions: List<SubscriptionItem>,
    onNavigateToSubscriptions: () -> Unit,
    onLogPayment: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val haptics = rememberAppHaptics()
    val activeSubs = subscriptions.filter { it.isActive }
    val upcomingSubs = activeSubs.sortedBy { it.nextDueDateMillis }

    Column(modifier = Modifier.entrance(3)) {
        SectionHeader(
            title = "Recurring Bills & Subscriptions",
            trailing = {
                Surface(
                    shape = ChipShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                        haptics.selection()
                        onNavigateToSubscriptions()
                    }
                ) {
                    Text(
                        text = "Manage (${activeSubs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(upcomingSubs) { sub ->
                val subColor = IconRegistry.parseColor(sub.colorHex)

                GlassCard(
                    modifier = Modifier
                        .width(200.dp)
                        .bouncyClickable(scaleDown = 0.94f) {
                            onNavigateToSubscriptions()
                        },
                    contentPadding = 14.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(ChipShape)
                                    .background(subColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconRegistry.getIcon(sub.iconKey),
                                    contentDescription = null,
                                    tint = subColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = sub.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.formattedAmount,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                shape = PillShape,
                                color = if (sub.isOverdue) ExpenseRed.copy(alpha = 0.15f)
                                else if (sub.daysUntilDue <= 3) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = if (sub.isOverdue) "Overdue" else if (sub.daysUntilDue == 0) "Today" else "In ${sub.daysUntilDue}d",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sub.isOverdue) ExpenseRed else if (sub.daysUntilDue <= 3) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = PillShape,
                            color = IncomeGreen.copy(alpha = 0.14f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .bouncyClickable(scaleDown = 0.94f) {
                                    haptics.confirm()
                                    onLogPayment(sub.id)
                                    onShowMessage("Logged payment for ${sub.name}")
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Mark Paid",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
