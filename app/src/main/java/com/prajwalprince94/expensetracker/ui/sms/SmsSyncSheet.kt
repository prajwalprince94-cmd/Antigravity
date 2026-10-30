package com.prajwalprince94.expensetracker.ui.sms

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.sms.ParsedSmsExpense
import com.prajwalprince94.expensetracker.data.sms.SmsExpenseParser
import com.prajwalprince94.expensetracker.data.sms.SmsInboxScanner
import com.prajwalprince94.expensetracker.data.sms.SmsTemplatePreset
import com.prajwalprince94.expensetracker.ui.components.EmptyStateView
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
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
import java.util.UUID

enum class SmsSheetTab(val title: String) {
    INBOX("Inbox Sync"),
    SIMULATOR("Presets"),
    PASTE_SMS("Custom SMS")
}

/**
 * High-fidelity native bottom sheet for automated SMS expense tracking.
 * Scans banking and UPI SMS, provides preset simulation, and allows 1-tap expense logging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSyncSheet(
    sheetState: SheetState,
    existingTransactions: List<TransactionItem>,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionItem) -> Unit,
    onSaveBatchTransactions: (List<TransactionItem>) -> Unit = {},
    onShowMessage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(SmsSheetTab.INBOX) }
    var hasPermission by remember { mutableStateOf(SmsInboxScanner.hasSmsPermission(context)) }
    var isScanning by remember { mutableStateOf(false) }
    var detectedExpenses by remember { mutableStateOf<List<ParsedSmsExpense>>(emptyList()) }

    // Simulator State
    val presets = remember { SmsTemplatePreset.defaultPresets() }
    var selectedPreset by remember { mutableStateOf(presets.first()) }
    var simulatedParsed by remember {
        mutableStateOf(SmsExpenseParser.parse(presets.first().sender, presets.first().body))
    }

    // Custom Paste State
    var customSmsText by remember { mutableStateOf("") }
    var customParsed by remember { mutableStateOf<ParsedSmsExpense?>(null) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            haptics.confirm()
            scope.launch {
                isScanning = true
                detectedExpenses = SmsInboxScanner.scanInbox(context, existingTransactions)
                isScanning = false
                onShowMessage("Found ${detectedExpenses.size} bank transactions from SMS inbox")
            }
        } else {
            haptics.error()
            onShowMessage("SMS Permission denied. You can still test with Presets or Paste.")
        }
    }

    // Auto-scan on open if permission is already granted
    LaunchedEffect(hasPermission) {
        if (hasPermission && detectedExpenses.isEmpty()) {
            isScanning = true
            detectedExpenses = SmsInboxScanner.scanInbox(context, existingTransactions)
            isScanning = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Automated SMS Tracker",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "On-Device Bank & UPI Parser (v3.0)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Selector
            item {
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    SmsSheetTab.entries.forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = {
                                haptics.selection()
                                activeTab = tab
                            },
                            text = { Text(tab.title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // TAB 1: INBOX SYNC
            if (activeTab == SmsSheetTab.INBOX) {
                if (!hasPermission) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 18.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Text(
                                    text = "SMS Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Expense Tracker reads only financial bank & UPI notifications completely on-device. Personal messages are never read and no data leaves your phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                                    },
                                    shape = ButtonShape,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailRead,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Grant SMS Access", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Controls Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val unloggedCount = detectedExpenses.count { !it.isAlreadyLogged }
                            Text(
                                text = "Found ${detectedExpenses.size} transactions ($unloggedCount unlogged)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = {
                                        haptics.selection()
                                        scope.launch {
                                            isScanning = true
                                            detectedExpenses = SmsInboxScanner.scanInbox(context, existingTransactions)
                                            isScanning = false
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (unloggedCount > 1) {
                                    Button(
                                        onClick = {
                                            haptics.confirm()
                                            val newTxns = detectedExpenses.filter { !it.isAlreadyLogged }.map { parsed ->
                                                TransactionItem(
                                                    id = "txn_${UUID.randomUUID()}",
                                                    title = parsed.merchantOrBeneficiary,
                                                    category = parsed.suggestedCategory,
                                                    account = parsed.displayAccount,
                                                    amountPaise = parsed.amountPaise,
                                                    type = parsed.type,
                                                    formattedDate = parsed.formattedDate,
                                                    note = "Auto-imported from ${parsed.bankName} SMS",
                                                    iconName = if (parsed.type == TransactionType.INCOME) "ArrowDownward" else "ReceiptLong"
                                                )
                                            }
                                            onSaveBatchTransactions(newTxns)
                                            onDismiss()
                                            onShowMessage("Logged ${newTxns.size} transactions from SMS!")
                                        },
                                        shape = ChipShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Log All ($unloggedCount)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (isScanning) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                    Text(
                                        text = "Scanning SMS Inbox...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else if (detectedExpenses.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No Bank SMS Found",
                                subtitle = "No recent financial debit/credit SMS messages detected in your inbox. Try the Presets or Custom SMS tab!",
                                icon = Icons.Default.Sms,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(detectedExpenses) { expense ->
                            SmsTransactionCard(
                                expense = expense,
                                onLogTransaction = {
                                    haptics.confirm()
                                    val transaction = TransactionItem(
                                        id = "txn_${UUID.randomUUID()}",
                                        title = expense.merchantOrBeneficiary,
                                        category = expense.suggestedCategory,
                                        account = expense.displayAccount,
                                        amountPaise = expense.amountPaise,
                                        type = expense.type,
                                        formattedDate = expense.formattedDate,
                                        note = "Auto-imported from ${expense.bankName} SMS",
                                        iconName = if (expense.type == TransactionType.INCOME) "ArrowDownward" else "ReceiptLong"
                                    )
                                    onSaveTransaction(transaction)
                                    detectedExpenses = detectedExpenses.map {
                                        if (it.id == expense.id) it.copy(isAlreadyLogged = true) else it
                                    }
                                    onShowMessage("Logged ${expense.merchantOrBeneficiary} • ${expense.formattedAmount}")
                                }
                            )
                        }
                    }
                }
            }

            // TAB 2: SIMULATOR / PRESETS
            if (activeTab == SmsSheetTab.SIMULATOR) {
                item {
                    Text(
                        text = "Select Indian Bank SMS Preset",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { preset ->
                            val isSelected = selectedPreset.id == preset.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptics.selection()
                                    selectedPreset = preset
                                    simulatedParsed = SmsExpenseParser.parse(preset.sender, preset.body)
                                },
                                label = { Text(preset.bankName) },
                                shape = ChipShape
                            )
                        }
                    }
                }

                // Preset Raw Message Card
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 14.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sender: ${selectedPreset.sender}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedPreset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = selectedPreset.body,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Parsed Preview Card
                simulatedParsed?.let { parsed ->
                    item {
                        SmsTransactionCard(
                            expense = parsed,
                            onLogTransaction = {
                                haptics.confirm()
                                val transaction = TransactionItem(
                                    id = "txn_${UUID.randomUUID()}",
                                    title = parsed.merchantOrBeneficiary,
                                    category = parsed.suggestedCategory,
                                    account = parsed.displayAccount,
                                    amountPaise = parsed.amountPaise,
                                    type = parsed.type,
                                    formattedDate = parsed.formattedDate,
                                    note = "Simulated from ${parsed.bankName} SMS",
                                    iconName = if (parsed.type == TransactionType.INCOME) "ArrowDownward" else "ReceiptLong"
                                )
                                onSaveTransaction(transaction)
                                onDismiss()
                                onShowMessage("Logged ${parsed.merchantOrBeneficiary} • ${parsed.formattedAmount}")
                            }
                        )
                    }
                }
            }

            // TAB 3: CUSTOM SMS PASTE
            if (activeTab == SmsSheetTab.PASTE_SMS) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Paste Bank Debit or Credit SMS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = customSmsText,
                            onValueChange = { text ->
                                customSmsText = text
                                customParsed = if (text.isNotBlank()) {
                                    SmsExpenseParser.parse("BANK-SMS", text)
                                } else null
                            },
                            label = { Text("Paste SMS text here") },
                            placeholder = { Text("e.g. Rs 450 debited from A/C XX1234 to Zomato UPI...") },
                            shape = InputShape,
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                customParsed?.let { parsed ->
                    item {
                        SmsTransactionCard(
                            expense = parsed,
                            onLogTransaction = {
                                haptics.confirm()
                                val transaction = TransactionItem(
                                    id = "txn_${UUID.randomUUID()}",
                                    title = parsed.merchantOrBeneficiary,
                                    category = parsed.suggestedCategory,
                                    account = parsed.displayAccount,
                                    amountPaise = parsed.amountPaise,
                                    type = parsed.type,
                                    formattedDate = parsed.formattedDate,
                                    note = "Pasted from SMS",
                                    iconName = if (parsed.type == TransactionType.INCOME) "ArrowDownward" else "ReceiptLong"
                                )
                                onSaveTransaction(transaction)
                                onDismiss()
                                onShowMessage("Logged ${parsed.merchantOrBeneficiary} • ${parsed.formattedAmount}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsTransactionCard(
    expense: ParsedSmsExpense,
    onLogTransaction: () -> Unit
) {
    val isExpense = expense.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Top Row: Bank Badge, Account, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = expense.bankName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (!expense.accountLast4.isNullOrBlank()) {
                        Text(
                            text = "..${expense.accountLast4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (expense.isAlreadyLogged) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Logged",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = expense.formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Middle Row: Merchant, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.merchantOrBeneficiary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${expense.suggestedCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${if (isExpense) "-" else "+"}${expense.formattedAmount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
            }

            // Raw Snippet
            Text(
                text = expense.rawBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // Log Button
            Button(
                onClick = onLogTransaction,
                enabled = !expense.isAlreadyLogged,
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = if (expense.isAlreadyLogged) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (expense.isAlreadyLogged) "Already in Database" else "Log Expense (1-Tap)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
