package com.prajwalprince94.expensetracker.ui.split

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.analytics.SplitDebtCalculator
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.model.DebtTransfer
import com.prajwalprince94.expensetracker.data.model.GroupBalanceSummary
import com.prajwalprince94.expensetracker.data.model.SplitBillItem
import com.prajwalprince94.expensetracker.data.model.SplitGroup
import com.prajwalprince94.expensetracker.data.model.SplitMode
import com.prajwalprince94.expensetracker.ui.components.EmptyStateView
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.IconRegistry
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.components.responsiveHorizontalPadding
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.InputShape
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

/**
 * Screen for managing group bill splitting, member balances, greedy debt minimization,
 * and one-tap real-world account settlements.
 * Follows Stride & Appllama native design system guidelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitGroupsScreen(
    groups: List<SplitGroup>,
    bills: List<SplitBillItem>,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onAddGroup: (SplitGroup) -> Unit,
    onUpdateGroup: (SplitGroup) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onAddBill: (SplitBillItem) -> Unit,
    onDeleteBill: (String) -> Unit,
    onSettleDebt: (groupId: String, fromMember: String, toMember: String, amountPaise: Long, accountName: String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    var selectedGroup by remember { mutableStateOf<SplitGroup?>(null) }
    var showCreateGroupSheet by remember { mutableStateOf(false) }
    var showAddBillSheet by remember { mutableStateOf(false) }
    var activeFilter by remember { mutableStateOf(SplitFilter.ALL) }
    var debtToSettle by remember { mutableStateOf<Pair<SplitGroup, DebtTransfer>?>(null) }
    var groupToDelete by remember { mutableStateOf<SplitGroup?>(null) }
    var billToDelete by remember { mutableStateOf<SplitBillItem?>(null) }

    val allGroupsSummary = remember(groups, bills) {
        SplitDebtCalculator.calculateAllGroupsSummary(groups, bills)
    }

    val currentSelectedGroup = groups.find { it.id == selectedGroup?.id }
    val selectedGroupSummary = currentSelectedGroup?.let {
        remember(it, bills) { SplitDebtCalculator.calculateGroupSummary(it, bills) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            haptics.light()
                            if (currentSelectedGroup != null) {
                                selectedGroup = null
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentSelectedGroup != null) currentSelectedGroup.name else "Split Bills",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (currentSelectedGroup != null) {
                                "${currentSelectedGroup.members.size} members • ${bills.filter { it.groupId == currentSelectedGroup.id }.size} expenses"
                            } else {
                                "${groups.size} groups active"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentSelectedGroup != null) {
                        IconButton(
                            onClick = {
                                haptics.light()
                                groupToDelete = currentSelectedGroup
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Group",
                                tint = ExpenseRed
                            )
                        }
                    }
                    Button(
                        onClick = {
                            haptics.confirm()
                            if (currentSelectedGroup != null) {
                                showAddBillSheet = true
                            } else {
                                showCreateGroupSheet = true
                            }
                        },
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentSelectedGroup != null) "Add Bill" else "New Group",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- Body Content ---
            if (currentSelectedGroup != null && selectedGroupSummary != null) {
                // Group Detail View
                GroupDetailContent(
                    group = currentSelectedGroup,
                    summary = selectedGroupSummary,
                    bills = bills.filter { it.groupId == currentSelectedGroup.id },
                    onSettleUp = { transfer ->
                        haptics.light()
                        debtToSettle = currentSelectedGroup to transfer
                    },
                    onDeleteBill = { bill ->
                        haptics.light()
                        billToDelete = bill
                    },
                    onAddBillClick = {
                        haptics.confirm()
                        showAddBillSheet = true
                    }
                )
            } else {
                // All Groups Overview
                AllGroupsContent(
                    groups = groups,
                    bills = bills,
                    overallSummary = allGroupsSummary,
                    activeFilter = activeFilter,
                    onFilterSelected = {
                        haptics.selection()
                        activeFilter = it
                    },
                    onGroupClick = { group ->
                        haptics.light()
                        selectedGroup = group
                    },
                    onCreateGroupClick = {
                        haptics.confirm()
                        showCreateGroupSheet = true
                    }
                )
            }
        }

        // --- Create Group Bottom Sheet ---
        if (showCreateGroupSheet) {
            CreateGroupBottomSheet(
                onDismiss = { showCreateGroupSheet = false },
                onConfirm = { newGroup ->
                    onAddGroup(newGroup)
                    showCreateGroupSheet = false
                    onShowMessage("Group '${newGroup.name}' created")
                }
            )
        }

        // --- Add Bill Bottom Sheet ---
        if (showAddBillSheet && currentSelectedGroup != null) {
            AddSplitBillBottomSheet(
                group = currentSelectedGroup,
                accounts = accounts,
                categories = categories,
                onDismiss = { showAddBillSheet = false },
                onConfirm = { newBill ->
                    onAddBill(newBill)
                    showAddBillSheet = false
                    onShowMessage("Bill '${newBill.title}' added to ${currentSelectedGroup.name}")
                }
            )
        }

        // --- Settle Debt Dialog ---
        if (debtToSettle != null) {
            val (group, transfer) = debtToSettle!!
            SettleDebtDialog(
                group = group,
                transfer = transfer,
                accounts = accounts,
                onDismiss = { debtToSettle = null },
                onConfirm = { accountName ->
                    onSettleDebt(group.id, transfer.fromMember, transfer.toMember, transfer.amountPaise, accountName)
                    debtToSettle = null
                    onShowMessage("Settled ${transfer.formattedAmount} with ${if (transfer.fromMember == "You") transfer.toMember else transfer.fromMember}")
                }
            )
        }

        // --- Delete Group Confirmation ---
        if (groupToDelete != null) {
            AlertDialog(
                onDismissRequest = { groupToDelete = null },
                title = { Text("Delete Group?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete '${groupToDelete!!.name}' and all associated split expenses? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = groupToDelete!!.id
                            val name = groupToDelete!!.name
                            groupToDelete = null
                            selectedGroup = null
                            onDeleteGroup(id)
                            onShowMessage("Group '$name' deleted")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { groupToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // --- Delete Bill Confirmation ---
        if (billToDelete != null) {
            AlertDialog(
                onDismissRequest = { billToDelete = null },
                title = { Text("Delete Expense?", fontWeight = FontWeight.Bold) },
                text = { Text("Remove '${billToDelete!!.title}' (${billToDelete!!.formattedTotalAmount}) from this group?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = billToDelete!!.id
                            billToDelete = null
                            onDeleteBill(id)
                            onShowMessage("Bill removed")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { billToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

enum class SplitFilter(val title: String) {
    ALL("All Groups"),
    OWED_TO_YOU("Owed to You"),
    YOU_OWE("You Owe")
}

@Composable
private fun AllGroupsContent(
    groups: List<SplitGroup>,
    bills: List<SplitBillItem>,
    overallSummary: GroupBalanceSummary,
    activeFilter: SplitFilter,
    onFilterSelected: (SplitFilter) -> Unit,
    onGroupClick: (SplitGroup) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val filteredGroups = remember(groups, bills, activeFilter) {
        when (activeFilter) {
            SplitFilter.ALL -> groups
            SplitFilter.OWED_TO_YOU -> groups.filter { group ->
                val s = SplitDebtCalculator.calculateGroupSummary(group, bills)
                s.netBalancePaise > 50L
            }
            SplitFilter.YOU_OWE -> groups.filter { group ->
                val s = SplitDebtCalculator.calculateGroupSummary(group, bills)
                s.netBalancePaise < -50L
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadingEdges()
            .padding(horizontal = responsiveHorizontalPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Net Balance Card
        item {
            HeroNetBalanceCard(overallSummary)
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplitFilter.entries.forEach { filter ->
                    val isSelected = activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = ChipShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Groups Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Shared Groups",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredGroups.size} shown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredGroups.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Group,
                    title = if (groups.isEmpty()) "No Groups Yet" else "No Groups Match Filter",
                    subtitle = if (groups.isEmpty()) {
                        "Create a group for trips, roommates, or dinner bills to track shared expenses and settle up."
                    } else {
                        "Try switching to 'All Groups' to see all shared groups."
                    },
                    actionLabel = if (groups.isEmpty()) "Create Group" else null,
                    onAction = if (groups.isEmpty()) onCreateGroupClick else null,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(filteredGroups, key = { it.id }) { group ->
                val groupSummary = remember(group, bills) {
                    SplitDebtCalculator.calculateGroupSummary(group, bills)
                }
                GroupCard(
                    group = group,
                    summary = groupSummary,
                    billsCount = bills.count { it.groupId == group.id },
                    onClick = { onGroupClick(group) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun HeroNetBalanceCard(summary: GroupBalanceSummary) {
    val netPaise = summary.netBalancePaise
    val isOwed = netPaise > 50L
    val isOwe = netPaise < -50L

    val accentColor = when {
        isOwed -> IncomeGreen
        isOwe -> ExpenseRed
        else -> MaterialTheme.colorScheme.primary
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .entrance(1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL NET BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            isOwed -> "You are owed"
                            isOwe -> "You owe"
                            else -> "Settled Up"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "₹%,.2f".format(Locale.US, abs(netPaise) / 100.0),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Owed to You",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.formattedYouAreOwed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "You Owe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.formattedYouOwe,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Group Spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.formattedTotalSpent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: SplitGroup,
    summary: GroupBalanceSummary,
    billsCount: Int,
    onClick: () -> Unit
) {
    val netPaise = summary.netBalancePaise
    val isOwed = netPaise > 50L
    val isOwe = netPaise < -50L

    val badgeColor = when {
        isOwed -> IncomeGreen
        isOwe -> ExpenseRed
        else -> MaterialTheme.colorScheme.outline
    }

    val badgeText = when {
        isOwed -> "You get back ₹%,.2f".format(Locale.US, netPaise / 100.0)
        isOwe -> "You owe ₹%,.2f".format(Locale.US, abs(netPaise) / 100.0)
        else -> "Settled up"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val groupColor = try {
                    Color(android.graphics.Color.parseColor(group.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(groupColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconRegistry.getIcon(group.iconKey),
                        contentDescription = null,
                        tint = groupColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (group.description.isNotBlank()) {
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(badgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Member Initials and Bills count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    group.members.take(4).forEachIndexed { index, member ->
                        val initial = member.firstOrNull()?.uppercase() ?: "U"
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (group.members.size > 4) {
                        Text(
                            text = "+${group.members.size - 4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }

                Text(
                    text = "$billsCount bills • Total ₹%,.2f".format(Locale.US, summary.totalSpentPaise / 100.0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GroupDetailContent(
    group: SplitGroup,
    summary: GroupBalanceSummary,
    bills: List<SplitBillItem>,
    onSettleUp: (DebtTransfer) -> Unit,
    onDeleteBill: (SplitBillItem) -> Unit,
    onAddBillClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .fadingEdges()
            .padding(horizontal = responsiveHorizontalPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Net Balance Card for this specific group
        item {
            HeroNetBalanceCard(summary)
        }

        // Member Balances Breakdown
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Member Balances",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    summary.memberBalances.forEach { (member, balPaise) ->
                        val isPos = balPaise > 50L
                        val isNeg = balPaise < -50L
                        val balColor = when {
                            isPos -> IncomeGreen
                            isNeg -> ExpenseRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val balLabel = when {
                            isPos -> "+₹%,.2f".format(Locale.US, balPaise / 100.0)
                            isNeg -> "-₹%,.2f".format(Locale.US, abs(balPaise) / 100.0)
                            else -> "Settled"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = member,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (member == "You") FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = balLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = balColor
                            )
                        }
                    }
                }
            }
        }

        // Simplified Debt Settlements (Greedy Minimization)
        if (summary.simplifiedDebts.isNotEmpty()) {
            item {
                Text(
                    text = "Suggested Settlements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(summary.simplifiedDebts) { transfer ->
                val involvesUser = transfer.fromMember == "You" || transfer.toMember == "You"
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (transfer.fromMember == "You") ExpenseRed.copy(alpha = 0.15f)
                                        else IncomeGreen.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = if (transfer.fromMember == "You") ExpenseRed else IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${transfer.fromMember} owes ${transfer.toMember}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = transfer.formattedAmount,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transfer.fromMember == "You") ExpenseRed else IncomeGreen
                                )
                            }
                        }

                        if (involvesUser) {
                            Button(
                                onClick = { onSettleUp(transfer) },
                                shape = ButtonShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(
                                    text = "Settle Up",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bills in this group
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expenses & Bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${bills.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (bills.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Bills in this Group",
                    subtitle = "Add bills like groceries, rent, or dinner to start splitting.",
                    actionLabel = "Add Bill",
                    onAction = onAddBillClick,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                SplitBillItemRow(
                    bill = bill,
                    onDelete = { onDeleteBill(bill) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SplitBillItemRow(
    bill: SplitBillItem,
    onDelete: () -> Unit
) {
    val yourShare = bill.shareForMember("You")

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${bill.paidBy} paid ${bill.formattedTotalAmount} • ${bill.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Your share: ${bill.formattedShareForMember("You")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (bill.paidBy == "You") IncomeGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = bill.splitMode.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Bottom Sheet for creating a new split expense group.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateGroupBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (SplitGroup) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val members = remember { mutableStateListOf("You") }
    var currentMemberInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3B82F6") }
    var selectedIcon by remember { mutableStateOf("Home") }

    val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899", "#06B6D4")
    val icons = listOf("Home", "Flight", "DirectionsCar", "Fastfood", "Coffee", "CardTravel", "ShoppingBag")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Split Group",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group Name (e.g. Flatmates, Goa Trip)") },
                shape = InputShape,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                shape = InputShape,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Members Input
            Text(
                text = "Group Members",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEach { m ->
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = m,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (m != "You") {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .bouncyClickable { members.remove(m) }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentMemberInput,
                    onValueChange = { currentMemberInput = it },
                    placeholder = { Text("Add friend name (e.g. Rahul)") },
                    shape = InputShape,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val trimmed = currentMemberInput.trim()
                        if (trimmed.isNotBlank() && !members.contains(trimmed)) {
                            members.add(trimmed)
                            currentMemberInput = ""
                        }
                    },
                    shape = ButtonShape
                ) {
                    Text("Add")
                }
            }

            // Color Selector
            Text(
                text = "Group Accent Color",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(colors) { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (selectedColor == hex) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .bouncyClickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == hex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Icon Selector
            Text(
                text = "Group Icon",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(icons) { iconKey ->
                    val isSelected = selectedIcon == iconKey
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .bouncyClickable { selectedIcon = iconKey },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconRegistry.getIcon(iconKey),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            SplitGroup(
                                name = name.trim(),
                                description = description.trim(),
                                members = members.toList(),
                                colorHex = selectedColor,
                                iconKey = selectedIcon
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = ButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Create Group", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Bottom sheet to add a split bill or expense to a group with customizable split modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSplitBillBottomSheet(
    group: SplitGroup,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onConfirm: (SplitBillItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("You") }
    var payerAccount by remember {
        mutableStateOf(accounts.firstOrNull()?.name ?: "Cash")
    }
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull()?.name ?: "Food & Dining")
    }
    var splitMode by remember { mutableStateOf(SplitMode.EQUAL) }

    // Exact amounts or percentages map
    val customInputs = remember { mutableStateMapOf<String, String>() }

    val members = group.members
    val totalAmountPaise = (amountString.toDoubleOrNull()?.times(100))?.toLong() ?: 0L

    // Calculate distributed shares based on split mode
    val calculatedShares = remember(totalAmountPaise, splitMode, members, customInputs.toMap()) {
        val shares = mutableMapOf<String, Long>()
        if (totalAmountPaise <= 0L || members.isEmpty()) return@remember shares

        when (splitMode) {
            SplitMode.EQUAL -> {
                val perPerson = totalAmountPaise / members.size
                var remainder = totalAmountPaise % members.size
                members.forEach { m ->
                    val extra = if (remainder > 0L) { remainder--; 1L } else 0L
                    shares[m] = perPerson + extra
                }
            }
            SplitMode.EXACT_AMOUNTS -> {
                members.forEach { m ->
                    val enteredPaise = (customInputs[m]?.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                    shares[m] = enteredPaise
                }
            }
            SplitMode.PERCENTAGES -> {
                members.forEach { m ->
                    val pct = customInputs[m]?.toDoubleOrNull() ?: 0.0
                    shares[m] = (totalAmountPaise * (pct / 100.0)).toLong()
                }
            }
        }
        shares
    }

    val sumCustomPaise = calculatedShares.values.sum()
    val isValidSplit = when (splitMode) {
        SplitMode.EQUAL -> totalAmountPaise > 0L
        SplitMode.EXACT_AMOUNTS -> abs(totalAmountPaise - sumCustomPaise) <= 10L // Within 10 paise tolerance
        SplitMode.PERCENTAGES -> {
            val totalPct = members.sumOf { customInputs[it]?.toDoubleOrNull() ?: 0.0 }
            abs(100.0 - totalPct) <= 0.1
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Add Bill to ${group.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Description (e.g. Dinner, Rent)") },
                    shape = InputShape,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = amountString,
                    onValueChange = { amountString = it },
                    label = { Text("Total Bill Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = InputShape,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Paid By Selector
            item {
                Text(
                    text = "Paid By",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(members) { m ->
                        val isSelected = paidBy == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { paidBy = m },
                            label = { Text(m) },
                            shape = ChipShape
                        )
                    }
                }
            }

            // Split Mode Segmented Control
            item {
                Text(
                    text = "Split Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TabRow(
                    selectedTabIndex = splitMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    SplitMode.entries.forEach { mode ->
                        Tab(
                            selected = splitMode == mode,
                            onClick = { splitMode = mode },
                            text = { Text(mode.displayName, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Shares Breakdown depending on mode
            if (splitMode == SplitMode.EQUAL) {
                item {
                    val perHead = if (members.isNotEmpty()) totalAmountPaise / members.size else 0L
                    Text(
                        text = "Split equally: ₹%,.2f per person across ${members.size} members".format(
                            Locale.US,
                            perHead / 100.0
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                items(members) { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = m,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = customInputs[m] ?: "",
                            onValueChange = { customInputs[m] = it },
                            label = {
                                Text(if (splitMode == SplitMode.EXACT_AMOUNTS) "Amount (₹)" else "Percent (%)")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = InputShape,
                            modifier = Modifier.width(150.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Category Selector
            item {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name) },
                            shape = ChipShape
                        )
                    }
                }
            }

            // Account Selector (Paid from)
            item {
                Text(
                    text = "Payment Account",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        val isSelected = payerAccount == acc.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { payerAccount = acc.name },
                            label = { Text(acc.name) },
                            shape = ChipShape
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank() && totalAmountPaise > 0L && isValidSplit) {
                            val now = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
                            onConfirm(
                                SplitBillItem(
                                    groupId = group.id,
                                    title = title.trim(),
                                    totalAmountPaise = totalAmountPaise,
                                    paidBy = paidBy,
                                    payerAccount = payerAccount,
                                    splitMode = splitMode,
                                    memberShares = calculatedShares,
                                    formattedDate = now,
                                    category = selectedCategory
                                )
                            )
                        }
                    },
                    enabled = title.isNotBlank() && totalAmountPaise > 0L && isValidSplit,
                    shape = ButtonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Add Bill", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog to confirm debt settlement and choose which bank/wallet account receives/pays.
 */
@Composable
fun SettleDebtDialog(
    group: SplitGroup,
    transfer: DebtTransfer,
    accounts: List<AccountItem>,
    onDismiss: () -> Unit,
    onConfirm: (accountName: String) -> Unit
) {
    var selectedAccount by remember {
        mutableStateOf(accounts.firstOrNull()?.name ?: "Paytm / UPI")
    }

    val isReceiving = transfer.toMember == "You"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isReceiving) "Receive Settlement" else "Pay Settlement",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isReceiving) {
                        "${transfer.fromMember} is paying you ${transfer.formattedAmount}."
                    } else {
                        "You are paying ${transfer.toMember} ${transfer.formattedAmount}."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (isReceiving) "Deposit to account:" else "Deduct from account:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        val isSelected = selectedAccount == acc.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccount = acc.name },
                            label = { Text(acc.name) },
                            shape = ChipShape
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedAccount) },
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReceiving) IncomeGreen else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Confirm Settlement", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
