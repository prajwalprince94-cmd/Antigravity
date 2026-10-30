package com.prajwalprince94.expensetracker.ui.export

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.prajwalprince94.expensetracker.data.export.PdfExportOptions
import com.prajwalprince94.expensetracker.data.export.PdfStatementGenerator
import com.prajwalprince94.expensetracker.data.export.StatementPeriod
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Interactive Modal Bottom Sheet for configuring and exporting publication-ready PDF statements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportSheet(
    sheetState: SheetState,
    transactions: List<TransactionItem>,
    accounts: List<AccountItem> = emptyList(),
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var selectedPeriod by remember { mutableStateOf(StatementPeriod.CURRENT_MONTH) }
    var selectedAccount by remember { mutableStateOf<String?>("All Accounts") }
    var includeTaxDeductions by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }

    val options = remember(selectedPeriod, selectedAccount, includeTaxDeductions) {
        PdfExportOptions(
            period = selectedPeriod,
            selectedAccount = if (selectedAccount == "All Accounts") null else selectedAccount,
            includeTaxDeductions = includeTaxDeductions
        )
    }

    val filteredList = remember(transactions, options) {
        PdfStatementGenerator.filterTransactions(transactions, options)
    }

    val taxSummary = remember(filteredList) {
        PdfStatementGenerator.computeTaxSummary(filteredList)
    }

    val totalIncome = filteredList.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
    val totalExpense = filteredList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
    val netSurplus = totalIncome - totalExpense

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Export Statement (PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Financial summary & tax deduction report",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Statement Period Selector
            Text(
                text = "STATEMENT PERIOD",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StatementPeriod.values()) { period ->
                    val isSelected = selectedPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.light()
                            selectedPeriod = period
                        },
                        label = { Text(period.label, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Account Filter Selector
            Text(
                text = "ACCOUNT FILTER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val accountNames = remember(accounts) {
                listOf("All Accounts") + accounts.map { it.name }.distinct()
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accountNames) { accName ->
                    val isSelected = selectedAccount == accName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptics.light()
                            selectedAccount = accName
                        },
                        label = { Text(accName, style = MaterialTheme.typography.labelMedium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Tax Deductions Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include Tax Deductions (80C / 80D)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto-calculate healthcare, insurance & business travel totals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = includeTaxDeductions,
                        onCheckedChange = {
                            haptics.light()
                            includeTaxDeductions = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Live Summary Preview Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentPadding = 14.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STATEMENT SUMMARY PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${filteredList.size} Transactions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+₹%,.0f".format(totalIncome / 100.0), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-₹%,.0f".format(totalExpense / 100.0), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Surplus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹%,.0f".format(netSurplus / 100.0), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (netSurplus >= 0) IncomeGreen else ExpenseRed)
                        }
                    }

                    if (includeTaxDeductions && taxSummary.totalDeductiblePaise > 0L) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Eligible Tax Deductions: ₹%,.2f".format(taxSummary.totalDeductiblePaise / 100.0),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Actions: Share PDF & Open/Print
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary Print Button
                OutlinedButton(
                    onClick = {
                        if (filteredList.isEmpty()) {
                            onShowMessage("No transactions found to export for selected period.")
                            return@OutlinedButton
                        }
                        haptics.confirm()
                        isGenerating = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val pdfFile = PdfStatementGenerator.generatePdfFile(context, filteredList, options)
                                val authority = "${context.packageName}.provider"
                                val contentUri = FileProvider.getUriForFile(context, authority, pdfFile)

                                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(contentUri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                withContext(Dispatchers.Main) {
                                    isGenerating = false
                                    context.startActivity(viewIntent)
                                    onDismiss()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isGenerating = false
                                    onShowMessage("Failed to open PDF: ${e.message}")
                                }
                            }
                        }
                    },
                    shape = ButtonShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / View")
                }

                // Primary Share PDF Button
                Button(
                    onClick = {
                        if (filteredList.isEmpty()) {
                            onShowMessage("No transactions found to export for selected period.")
                            return@Button
                        }
                        haptics.confirm()
                        isGenerating = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val result = PdfStatementGenerator.exportAndShare(context, filteredList, options)
                                withContext(Dispatchers.Main) {
                                    isGenerating = false
                                    result.onSuccess {
                                        onShowMessage("PDF Statement ready! Choose an app to share.")
                                        onDismiss()
                                    }.onFailure { err ->
                                        onShowMessage("Export failed: ${err.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isGenerating = false
                                    onShowMessage("Export error: ${e.message}")
                                }
                            }
                        }
                    },
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1.3f)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share PDF")
                    }
                }
            }
        }
    }
}
