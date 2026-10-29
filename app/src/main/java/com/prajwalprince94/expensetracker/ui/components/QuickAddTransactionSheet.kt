package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TimeToLeave
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.TransferBlue
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddTransactionSheet(
    sheetState: SheetState,
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionItem) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var rawAmountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (initialType == TransactionType.INCOME) "Salary" else "Food & Dining") }
    var selectedAccount by remember { mutableStateOf("Paytm / UPI") }
    var noteInput by remember { mutableStateOf("") }

    val expenseCategories = listOf("Food & Dining", "Groceries", "Shopping", "Transportation", "Rent & Bills", "Entertainment")
    val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts")
    val accounts = listOf("Paytm / UPI", "Cash", "HDFC Bank")

    val activeColor by animateColorAsState(
        targetValue = when (selectedType) {
            TransactionType.EXPENSE -> ExpenseRed
            TransactionType.INCOME -> IncomeGreen
            TransactionType.TRANSFER -> TransferBlue
        },
        label = "activeColor"
    )

    // Parse amount safely
    val parsedAmountPaise = remember(rawAmountInput) {
        val amountDouble = rawAmountInput.toDoubleOrNull() ?: 0.0
        (amountDouble * 100).toLong()
    }

    val displayAmountString = remember(rawAmountInput) {
        if (rawAmountInput.isEmpty()) "0.00" else rawAmountInput
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Three-way Type Selector Segmented Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TypePillItem(
                        title = "Expense",
                        isSelected = selectedType == TransactionType.EXPENSE,
                        selectedColor = ExpenseRed,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = "Food & Dining"
                        }
                    )
                    TypePillItem(
                        title = "Income",
                        isSelected = selectedType == TransactionType.INCOME,
                        selectedColor = IncomeGreen,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = "Salary"
                        }
                    )
                    TypePillItem(
                        title = "Transfer",
                        isSelected = selectedType == TransactionType.TRANSFER,
                        selectedColor = TransferBlue,
                        onClick = {
                            selectedType = TransactionType.TRANSFER
                            selectedCategory = "Transfer"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Big Amount Display
            Text(
                text = "₹ $displayAmountString",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Chips Row (Skip for transfer)
            if (selectedType != TransactionType.TRANSFER) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentCategories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories
                    items(currentCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor = activeColor
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Account Chips Row
            Text(
                text = if (selectedType == TransactionType.TRANSFER) "From Account" else "Account",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts) { acc ->
                    FilterChip(
                        selected = selectedAccount == acc,
                        onClick = { selectedAccount = acc },
                        label = { Text(acc) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note Input Field
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Note / Merchant (e.g. Swiggy, Amazon)...") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Number Keypad (Fast smartphone entry)
            NumericKeypad(
                onDigitClick = { digit ->
                    if (rawAmountInput.length < 9) {
                        if (digit == "." && rawAmountInput.contains(".")) return@NumericKeypad
                        rawAmountInput += digit
                    }
                },
                onBackspace = {
                    if (rawAmountInput.isNotEmpty()) {
                        rawAmountInput = rawAmountInput.dropLast(1)
                    }
                },
                onClear = { rawAmountInput = "" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Transaction Button
            Button(
                onClick = {
                    if (parsedAmountPaise > 0) {
                        val icon = when (selectedCategory) {
                            "Food & Dining" -> Icons.Default.Fastfood
                            "Groceries", "Shopping" -> Icons.Default.ShoppingCart
                            "Transportation" -> Icons.Default.TimeToLeave
                            "Salary" -> Icons.Default.Work
                            "Transfer" -> Icons.Default.SwapHoriz
                            else -> Icons.Default.ReceiptLong
                        }

                        val newTxn = TransactionItem(
                            id = UUID.randomUUID().toString(),
                            title = if (noteInput.isNotBlank()) noteInput else selectedCategory,
                            category = selectedCategory,
                            account = selectedAccount,
                            amountPaise = parsedAmountPaise,
                            type = selectedType,
                            formattedDate = "Just now",
                            note = noteInput,
                            icon = icon
                        )
                        onSaveTransaction(newTxn)
                    }
                },
                enabled = parsedAmountPaise > 0,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save ${selectedType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TypePillItem(
    title: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) selectedColor else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadGrid = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keypadGrid.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                when (key) {
                                    "⌫" -> onBackspace()
                                    else -> onDigitClick(key)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (key == "⌫") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
