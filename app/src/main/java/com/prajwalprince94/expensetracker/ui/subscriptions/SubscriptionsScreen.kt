package com.prajwalprince94.expensetracker.ui.subscriptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.BillingCycle
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
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
import java.util.Locale
import java.util.UUID

/**
 * Screen for managing recurring subscriptions, scheduled bills, and auto-logging.
 * Follows Stride & Appllama native design system guidelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionItem>,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onAddSubscription: (SubscriptionItem) -> Unit,
    onUpdateSubscription: (SubscriptionItem) -> Unit,
    onDeleteSubscription: (String) -> Unit,
    onLogPayment: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val horizontalPadding = responsiveHorizontalPadding()

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Upcoming", "Active", "Paused"
    var showAddSheet by remember { mutableStateOf(false) }
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionItem?>(null) }
    var subscriptionToDelete by remember { mutableStateOf<SubscriptionItem?>(null) }

    // Calculate total monthly committed expenditure
    val totalMonthlyPaise = remember(subscriptions) {
        subscriptions.filter { it.isActive }.sumOf { sub ->
            when (sub.billingCycle) {
                BillingCycle.MONTHLY -> sub.amountPaise
                BillingCycle.QUARTERLY -> sub.amountPaise / 3
                BillingCycle.YEARLY -> sub.amountPaise / 12
            }
        }
    }
    val formattedTotalMonthly = "₹%,.2f".format(Locale.US, totalMonthlyPaise / 100.0)

    val activeCount = subscriptions.count { it.isActive }
    val upcomingCount = subscriptions.count { it.isActive && it.daysUntilDue in 0..7 }

    val filteredList = subscriptions.filter { sub ->
        when (selectedFilter) {
            "Upcoming" -> sub.isActive && sub.daysUntilDue in 0..7
            "Active" -> sub.isActive
            "Paused" -> !sub.isActive
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top App Bar with Back Button
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(0),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .size(42.dp)
                        .bouncyClickable(scaleDown = 0.88f) {
                            haptics.light()
                            onNavigateBack()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Subscriptions & Bills",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Track recurring commitments & due dates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Monthly Committed Spend Glass Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(1),
                contentPadding = 18.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Committed Spend",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = ChipShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$activeCount Active",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$formattedTotalMonthly / mo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Default
                        ),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (upcomingCount > 0) "$upcomingCount bill(s) due within the next 7 days" else "No bills due in the next 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (upcomingCount > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (upcomingCount > 0) FontWeight.Bold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            haptics.confirm()
                            showAddSheet = true
                        },
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .bouncyClickable {
                                haptics.confirm()
                                showAddSheet = true
                            }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Subscription / Bill", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Filter Chips
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "All" to subscriptions.size,
                    "Upcoming" to upcomingCount,
                    "Active" to activeCount,
                    "Paused" to (subscriptions.size - activeCount)
                )
                items(filters) { (filter, count) ->
                    val selected = selectedFilter == filter
                    Surface(
                        shape = PillShape,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable {
                            haptics.selection()
                            selectedFilter = filter
                        }
                    ) {
                        Text(
                            text = "$filter ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 4. Subscriptions List
        if (filteredList.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.CalendarMonth,
                    title = "No Subscriptions Found",
                    subtitle = "Add your recurring bills, streaming apps, or rent to track payments.",
                    actionLabel = "Add Bill",
                    onAction = { showAddSheet = true }
                )
            }
        } else {
            items(filteredList) { sub ->
                val subColor = IconRegistry.parseColor(sub.colorHex)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(3),
                    contentPadding = 16.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(ChipShape)
                                    .background(subColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconRegistry.getIcon(sub.iconKey),
                                    contentDescription = null,
                                    tint = subColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sub.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sub.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (!sub.isActive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = PillShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                                        ) {
                                            Text(
                                                text = "Paused",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${sub.billingCycle.displayName} • Due day ${sub.dueDay} • ${sub.account}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            // Amount
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = sub.formattedAmount,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (sub.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = sub.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer Status & Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Due status pill
                            val (dueStatusBg, dueStatusText, dueStatusLabel) = when {
                                !sub.isActive -> Triple(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    "Paused"
                                )
                                sub.isOverdue -> Triple(
                                    ExpenseRed.copy(alpha = 0.15f),
                                    ExpenseRed,
                                    "Overdue (${sub.formattedNextDue})"
                                )
                                sub.daysUntilDue <= 3 -> Triple(
                                    Color(0xFFF59E0B).copy(alpha = 0.15f),
                                    Color(0xFFF59E0B),
                                    "Due in ${sub.daysUntilDue} day(s)"
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                    "Next: ${sub.formattedNextDue}"
                                )
                            }

                            Surface(
                                shape = PillShape,
                                color = dueStatusBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (sub.isOverdue) {
                                        Icon(
                                            Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = dueStatusText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = dueStatusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = dueStatusText
                                    )
                                }
                            }

                            // Quick Action Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Log Payment button
                                Surface(
                                    shape = ChipShape,
                                    color = IncomeGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.bouncyClickable(scaleDown = 0.92f) {
                                        haptics.confirm()
                                        onLogPayment(sub.id)
                                        onShowMessage("Logged payment for ${sub.name}")
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mark Paid",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Pause / Resume toggle
                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        val toggled = sub.copy(isActive = !sub.isActive)
                                        onUpdateSubscription(toggled)
                                        onShowMessage(if (toggled.isActive) "Resumed ${sub.name}" else "Paused ${sub.name}")
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (sub.isActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = if (sub.isActive) "Pause" else "Resume",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Edit button
                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        subscriptionToEdit = sub
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        subscriptionToDelete = sub
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ExpenseRed.copy(alpha = 0.85f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Modal Bottom Sheet: Add / Edit Subscription
    if (showAddSheet || subscriptionToEdit != null) {
        val editingSub = subscriptionToEdit
        SubscriptionEditorBottomSheet(
            existing = editingSub,
            accounts = accounts,
            categories = categories,
            onDismiss = {
                showAddSheet = false
                subscriptionToEdit = null
            },
            onSave = { saved ->
                if (editingSub != null) {
                    onUpdateSubscription(saved)
                    onShowMessage("Updated ${saved.name}")
                } else {
                    onAddSubscription(saved)
                    onShowMessage("Added ${saved.name}")
                }
                showAddSheet = false
                subscriptionToEdit = null
            }
        )
    }

    // Deletion confirmation dialog
    subscriptionToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            title = { Text("Delete Subscription?") },
            text = { Text("Are you sure you want to remove '${sub.name}'? Past logged payments will remain safely in your transaction history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.confirm()
                        onDeleteSubscription(sub.id)
                        onShowMessage("Subscription deleted")
                        subscriptionToDelete = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Bottom Sheet for creating or modifying a recurring subscription or bill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionEditorBottomSheet(
    existing: SubscriptionItem?,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSave: (SubscriptionItem) -> Unit
) {
    val haptics = rememberAppHaptics()
    var nameInput by remember { mutableStateOf(existing?.name ?: "") }
    var amountInput by remember {
        mutableStateOf(if (existing != null) "%.2f".format(Locale.US, existing.amountPaise / 100.0) else "")
    }
    var selectedCycle by remember { mutableStateOf(existing?.billingCycle ?: BillingCycle.MONTHLY) }
    var dueDayInput by remember { mutableStateOf(existing?.dueDay?.toString() ?: "1") }
    var selectedCategory by remember {
        mutableStateOf(existing?.category ?: categories.firstOrNull()?.name ?: "Rent & Bills")
    }
    var selectedAccount by remember {
        mutableStateOf(existing?.account ?: accounts.firstOrNull()?.name ?: "Paytm / UPI")
    }
    var autoLog by remember { mutableStateOf(existing?.autoLog ?: false) }
    var selectedColor by remember { mutableStateOf(existing?.colorHex ?: IconRegistry.availableColorHexes.first()) }
    var selectedIcon by remember { mutableStateOf(existing?.iconKey ?: "Movie") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (existing != null) "Edit Subscription" else "Add Subscription / Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Name
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Service or Bill Name") },
                placeholder = { Text("e.g. Netflix, Gym, Rent, Electricity") },
                singleLine = true,
                shape = InputShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount & Due Day in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = InputShape,
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = dueDayInput,
                    onValueChange = {
                        val num = it.filter { ch -> ch.isDigit() }
                        if (num.isEmpty() || (num.toIntOrNull() in 1..31)) {
                            dueDayInput = num
                        }
                    },
                    label = { Text("Due Day") },
                    placeholder = { Text("1-31") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = InputShape,
                    modifier = Modifier.weight(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Billing Cycle Chips
            Text(
                text = "Billing Cycle",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingCycle.entries.forEach { cycle ->
                    val selected = selectedCycle == cycle
                    Surface(
                        shape = PillShape,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .weight(1f)
                            .bouncyClickable {
                                haptics.selection()
                                selectedCycle = cycle
                            }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = cycle.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text(
                text = "Expense Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val selected = selectedCategory.equals(cat.name, ignoreCase = true)
                    Surface(
                        shape = ChipShape,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable {
                            haptics.selection()
                            selectedCategory = cat.name
                        }
                    ) {
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Chips
            Text(
                text = "Deduct From Account",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    val selected = selectedAccount.equals(acc.name, ignoreCase = true)
                    Surface(
                        shape = ChipShape,
                        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable {
                            haptics.selection()
                            selectedAccount = acc.name
                        }
                    ) {
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color Swatches
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(IconRegistry.availableColorHexes) { hex ->
                    val swatchColor = IconRegistry.parseColor(hex)
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .bouncyClickable(scaleDown = 0.88f) {
                                haptics.selection()
                                selectedColor = hex
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Icon Picker
            Text(
                text = "Icon",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IconRegistry.availableIcons) { (key, icon) ->
                    val isSelected = selectedIcon == key
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(ChipShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .bouncyClickable(scaleDown = 0.88f) {
                                haptics.selection()
                                selectedIcon = key
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = key,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Save Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        haptics.confirm()
                        val amtDouble = amountInput.toDoubleOrNull() ?: 0.0
                        val amtPaise = (amtDouble * 100).toLong()
                        val day = dueDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1

                        val item = SubscriptionItem(
                            id = existing?.id ?: "sub_${UUID.randomUUID()}",
                            name = nameInput.trim(),
                            amountPaise = amtPaise,
                            category = selectedCategory,
                            account = selectedAccount,
                            billingCycle = selectedCycle,
                            dueDay = day,
                            nextDueDateMillis = SubscriptionItem.calculateNextDueDate(day, selectedCycle),
                            isActive = existing?.isActive ?: true,
                            autoLog = autoLog,
                            colorHex = selectedColor,
                            iconKey = selectedIcon
                        )
                        onSave(item)
                    }
                },
                enabled = nameInput.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0.0,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (existing != null) "Update Subscription" else "Save Subscription",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
