package com.prajwalprince94.expensetracker.ui.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.ui.components.EmptyStateView
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.IconRegistry
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.components.responsiveHorizontalPadding
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.InputShape
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Screen for managing multi-accounts (Bank, UPI, Cash) and custom categories.
 * Follows Stride & Appllama native design system guidelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsAndCategoriesScreen(
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    transactions: List<TransactionItem>,
    onAddAccount: (AccountItem) -> Unit,
    onUpdateAccount: (AccountItem) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onAddCategory: (CategoryItem) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val isDark = isSystemInDarkTheme()
    val horizontalPadding = responsiveHorizontalPadding()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Accounts, 1: Categories
    var categoryFilter by remember { mutableStateOf("All") } // "All", "Expenses", "Income"

    // Sheet states
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountItem?>(null) }
    var showAddCategorySheet by remember { mutableStateOf(false) }

    // Deletion confirmation states
    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Type, Id)

    // Calculate total net balance across all accounts
    val totalLiveBalancePaise = remember(accounts, transactions) {
        accounts.sumOf { acc ->
            val credited = transactions
                .filter { it.type == TransactionType.INCOME && it.account.equals(acc.name, ignoreCase = true) }
                .sumOf { it.amountPaise }
            val debited = transactions
                .filter { it.type == TransactionType.EXPENSE && it.account.equals(acc.name, ignoreCase = true) }
                .sumOf { it.amountPaise }
            (acc.initialBalancePaise + credited - debited).coerceAtLeast(0L)
        }
    }

    val formattedNetWorth = "₹%,.2f".format(Locale.US, totalLiveBalancePaise / 100.0)

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
                        text = "Accounts & Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage wallets, banks and custom tags",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Segmented Pill Tab Switcher
        item {
            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(1)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Accounts (${accounts.size})", "Categories (${categories.size})")
                    tabs.forEachIndexed { index, title ->
                        val selected = selectedTabIndex == index
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tabBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            label = "tabText"
                        )

                        Surface(
                            shape = PillShape,
                            color = bgColor,
                            modifier = Modifier
                                .weight(1f)
                                .bouncyClickable(scaleDown = 0.94f) {
                                    haptics.selection()
                                    selectedTabIndex = index
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- TAB 0: ACCOUNTS ---
        if (selectedTabIndex == 0) {
            // Net Worth Summary GlassCard
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(2),
                    contentPadding = 18.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Net Worth",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = ChipShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "${accounts.size} Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = formattedNetWorth,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Default
                            ),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                haptics.confirm()
                                showAddAccountSheet = true
                            },
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .bouncyClickable {
                                    haptics.confirm()
                                    showAddAccountSheet = true
                                }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Accounts List
            if (accounts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "No Accounts Found",
                        subtitle = "Create your first bank, UPI wallet, or cash container.",
                        actionLabel = "Add Account",
                        onAction = { showAddAccountSheet = true }
                    )
                }
            } else {
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

                    val liveBalanceStr = "₹%,.2f".format(Locale.US, liveBalancePaise / 100.0)
                    val accColor = IconRegistry.parseColor(acc.colorHex)

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .entrance(3),
                        contentPadding = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon Box
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(ChipShape)
                                    .background(accColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconRegistry.getIcon(acc.iconKey),
                                    contentDescription = null,
                                    tint = accColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = PillShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = acc.type,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Opening: ${acc.formattedInitialBalance}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            // Balance & Actions
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = liveBalanceStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            haptics.light()
                                            accountToEdit = acc
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

                                    IconButton(
                                        onClick = {
                                            if (accounts.size <= 1) {
                                                onShowMessage("Cannot delete the only remaining account.")
                                            } else {
                                                haptics.light()
                                                itemToDelete = Pair("Account", acc.id)
                                            }
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
        }

        // --- TAB 1: CATEGORIES ---
        if (selectedTabIndex == 1) {
            // Category Filter Chips & Add Button Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .entrance(2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val filters = listOf("All", "Expenses", "Income")
                        items(filters) { filter ->
                            val selected = categoryFilter == filter
                            Surface(
                                shape = PillShape,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.bouncyClickable {
                                    haptics.selection()
                                    categoryFilter = filter
                                }
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = ChipShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.bouncyClickable {
                            haptics.confirm()
                            showAddCategorySheet = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Tag",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val filteredCategories = categories.filter { cat ->
                when (categoryFilter) {
                    "Expenses" -> cat.type == TransactionType.EXPENSE
                    "Income" -> cat.type == TransactionType.INCOME
                    else -> true
                }
            }

            if (filteredCategories.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Category,
                        title = "No Categories Found",
                        subtitle = "Create custom category tags to classify your spending.",
                        actionLabel = "Add Category",
                        onAction = { showAddCategorySheet = true }
                    )
                }
            } else {
                items(filteredCategories) { cat ->
                    val catColor = IconRegistry.parseColor(cat.colorHex)
                    val txnCount = remember(cat, transactions) {
                        transactions.count { it.category.equals(cat.name, ignoreCase = true) }
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .entrance(3),
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(ChipShape)
                                    .background(catColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = IconRegistry.getIcon(cat.iconKey),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = PillShape,
                                        color = if (cat.isSystem) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = if (cat.isSystem) "Default" else "Custom",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (cat.isSystem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${cat.type.name.lowercase().replaceFirstChar { it.uppercase() }} • $txnCount transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (cat.isSystem) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "System Category",
                                    tint = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 4.dp)
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        itemToDelete = Pair("Category", cat.id)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Category",
                                        tint = ExpenseRed.copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
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

    // Modal Bottom Sheet: Add / Edit Account
    if (showAddAccountSheet || accountToEdit != null) {
        val editingAcc = accountToEdit
        AccountEditorBottomSheet(
            existingAccount = editingAcc,
            onDismiss = {
                showAddAccountSheet = false
                accountToEdit = null
            },
            onSave = { acc ->
                if (editingAcc != null) {
                    onUpdateAccount(acc)
                    onShowMessage("Updated ${acc.name}")
                } else {
                    onAddAccount(acc)
                    onShowMessage("Added ${acc.name}")
                }
                showAddAccountSheet = false
                accountToEdit = null
            }
        )
    }

    // Modal Bottom Sheet: Add Custom Category
    if (showAddCategorySheet) {
        CategoryEditorBottomSheet(
            onDismiss = { showAddCategorySheet = false },
            onSave = { cat ->
                onAddCategory(cat)
                onShowMessage("Added custom tag: ${cat.name}")
                showAddCategorySheet = false
            }
        )
    }

    // Deletion Confirmation Dialog
    itemToDelete?.let { (type, id) ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete $type?") },
            text = { Text("Are you sure you want to remove this $type? Historical transactions will remain preserved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.confirm()
                        if (type == "Account") {
                            onDeleteAccount(id)
                            onShowMessage("Account deleted")
                        } else {
                            onDeleteCategory(id)
                            onShowMessage("Category deleted")
                        }
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Editor Bottom Sheet for creating or editing accounts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorBottomSheet(
    existingAccount: AccountItem?,
    onDismiss: () -> Unit,
    onSave: (AccountItem) -> Unit
) {
    val haptics = rememberAppHaptics()
    var nameInput by remember { mutableStateOf(existingAccount?.name ?: "") }
    var selectedType by remember { mutableStateOf(existingAccount?.type ?: "UPI") }
    var balanceInput by remember {
        mutableStateOf(
            if (existingAccount != null) "%.2f".format(Locale.US, existingAccount.initialBalancePaise / 100.0) else ""
        )
    }
    var selectedColor by remember { mutableStateOf(existingAccount?.colorHex ?: IconRegistry.availableColorHexes.first()) }
    var selectedIcon by remember { mutableStateOf(existingAccount?.iconKey ?: "PhoneAndroid") }

    val accountTypes = listOf("UPI", "BANK", "CASH", "CARD", "SAVINGS")

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
                text = if (existingAccount != null) "Edit Account" else "Add New Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Account Name
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g. ICICI Bank, Paytm, Cash") },
                singleLine = true,
                shape = InputShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Type Chips
            Text(
                text = "Account Classification",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accountTypes) { type ->
                    val selected = selectedType == type
                    Surface(
                        shape = PillShape,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable {
                            haptics.selection()
                            selectedType = type
                        }
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Initial Balance
            OutlinedTextField(
                value = balanceInput,
                onValueChange = { balanceInput = it },
                label = { Text("Opening Balance (₹)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = InputShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Color Swatches
            Text(
                text = "Account Accent Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(IconRegistry.availableColorHexes) { hex ->
                    val swatchColor = IconRegistry.parseColor(hex)
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .bouncyClickable(scaleDown = 0.88f) {
                                haptics.selection()
                                selectedColor = hex
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Icon Picker
            Text(
                text = "Account Icon",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(IconRegistry.availableIcons) { (key, icon) ->
                    val isSelected = selectedIcon == key
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        haptics.confirm()
                        val amtDouble = balanceInput.toDoubleOrNull() ?: 0.0
                        val amtPaise = (amtDouble * 100).toLong()

                        val account = AccountItem(
                            id = existingAccount?.id ?: "acc_${UUID.randomUUID()}",
                            name = nameInput.trim(),
                            type = selectedType,
                            initialBalancePaise = amtPaise,
                            colorHex = selectedColor,
                            iconKey = selectedIcon
                        )
                        onSave(account)
                    }
                },
                enabled = nameInput.isNotBlank(),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (existingAccount != null) "Update Account" else "Create Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Editor Bottom Sheet for creating custom categories.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorBottomSheet(
    onDismiss: () -> Unit,
    onSave: (CategoryItem) -> Unit
) {
    val haptics = rememberAppHaptics()
    var nameInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedColor by remember { mutableStateOf(IconRegistry.availableColorHexes.first()) }
    var selectedIcon by remember { mutableStateOf("Category") }

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
                text = "Add Custom Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Name
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Category Name") },
                placeholder = { Text("e.g. Gym & Fitness, Online Courses, Subscriptions") },
                singleLine = true,
                shape = InputShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Type Toggle
            Text(
                text = "Category Type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = PillShape,
                    color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .weight(1f)
                        .bouncyClickable {
                            haptics.selection()
                            selectedType = TransactionType.EXPENSE
                        }
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = PillShape,
                    color = if (selectedType == TransactionType.INCOME) IncomeGreen else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .weight(1f)
                        .bouncyClickable {
                            haptics.selection()
                            selectedType = TransactionType.INCOME
                        }
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color Swatches
            Text(
                text = "Category Color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(IconRegistry.availableColorHexes) { hex ->
                    val swatchColor = IconRegistry.parseColor(hex)
                    val isSelected = selectedColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .bouncyClickable(scaleDown = 0.88f) {
                                haptics.selection()
                                selectedColor = hex
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Icon Picker
            Text(
                text = "Category Icon",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(IconRegistry.availableIcons) { (key, icon) ->
                    val isSelected = selectedIcon == key
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    if (nameInput.isNotBlank()) {
                        haptics.confirm()
                        val category = CategoryItem(
                            id = "cat_${UUID.randomUUID()}",
                            name = nameInput.trim(),
                            type = selectedType,
                            colorHex = selectedColor,
                            iconKey = selectedIcon,
                            isSystem = false
                        )
                        onSave(category)
                    }
                },
                enabled = nameInput.isNotBlank(),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Save Custom Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
