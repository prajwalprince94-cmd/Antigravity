package com.prajwalprince94.expensetracker.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.InputShape
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
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
    val haptics = rememberAppHaptics()
    var selectedType by remember { mutableStateOf(initialType) }
    var rawAmountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (initialType == TransactionType.INCOME) "Salary" else "Food & Dining") }
    var selectedAccount by remember { mutableStateOf("Paytm / UPI") }
    var noteInput by remember { mutableStateOf("") }

    val expenseCategories = listOf(
        "Food & Dining" to Icons.Default.Fastfood,
        "Groceries" to Icons.Default.ShoppingCart,
        "Fuel & Travel" to Icons.Default.LocalGasStation,
        "Rent & Bills" to Icons.Default.Home,
        "Shopping" to Icons.Default.ReceiptLong,
        "Health & Fit" to Icons.Default.FitnessCenter,
        "Education" to Icons.Default.School,
        "Vacation" to Icons.Default.CardTravel
    )

    val incomeCategories = listOf(
        "Salary" to Icons.Default.Work,
        "Freelance" to Icons.Default.Payments,
        "Investments" to Icons.Default.AccountBalance,
        "Gifts" to Icons.Default.LocalAtm
    )

    val accounts = listOf(
        "Paytm / UPI" to Icons.Default.PhoneAndroid,
        "Cash" to Icons.Default.Payments,
        "HDFC Bank" to Icons.Default.AccountBalance
    )

    val activeColor by animateColorAsState(
        targetValue = when (selectedType) {
            TransactionType.EXPENSE -> ExpenseRed
            TransactionType.INCOME -> IncomeGreen
            TransactionType.TRANSFER -> TransferBlue
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "activeColor"
    )

    // Compute paise with precision
    val parsedAmountPaise = remember(rawAmountInput) {
        val amountDouble = rawAmountInput.toDoubleOrNull() ?: 0.0
        (amountDouble * 100).toLong()
    }

    val displayAmountString = remember(rawAmountInput) {
        if (rawAmountInput.isEmpty()) "0" else rawAmountInput
    }

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Three-way Type Selector Segmented Pill
            Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TypePillItem(
                        title = "Expense",
                        isSelected = selectedType == TransactionType.EXPENSE,
                        selectedColor = ExpenseRed,
                        onClick = {
                            haptics.selection()
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = "Food & Dining"
                        }
                    )
                    TypePillItem(
                        title = "Income",
                        isSelected = selectedType == TransactionType.INCOME,
                        selectedColor = IncomeGreen,
                        onClick = {
                            haptics.selection()
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
                            selectedCategory = "Account Transfer"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Hero Amount Display with Odometer Rolling Animation
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${selectedType.name.lowercase().replaceFirstChar { it.uppercase() }} Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.displayMedium,
                            color = activeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        AnimatedCounter(
                            text = displayAmountString,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = activeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector Chips
            if (selectedType != TransactionType.TRANSFER) {
                val currentCategories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(currentCategories) { (catName, icon) ->
                            val isSelected = selectedCategory == catName
                            BouncyChip(
                                text = catName,
                                icon = icon,
                                isSelected = isSelected,
                                activeColor = activeColor,
                                onClick = { selectedCategory = catName }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Account Selector Chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Account / Wallet",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts) { (accName, icon) ->
                        val isSelected = selectedAccount == accName
                        BouncyChip(
                            text = accName,
                            icon = icon,
                            isSelected = isSelected,
                            activeColor = MaterialTheme.colorScheme.primary,
                            onClick = { selectedAccount = accName }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note input field
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Add remark or note (optional)") },
                singleLine = true,
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = activeColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tactile Numeric Keypad with Bouncy Spring Feedback
            NumericKeypad(
                onDigitClick = { digit ->
                    if (digit == "." && rawAmountInput.contains(".")) {
                        // Ignore duplicate decimal
                    } else if (rawAmountInput.length < 9) {
                        rawAmountInput += digit
                    }
                },
                onBackspace = {
                    if (rawAmountInput.isNotEmpty()) {
                        rawAmountInput = rawAmountInput.dropLast(1)
                    }
                },
                onClear = {
                    rawAmountInput = ""
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Big Save Action Button with Stride Bouncy Physical Press
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bouncyClickable(
                        scaleDown = 0.94f,
                        enabled = parsedAmountPaise > 0,
                        onClick = {
                            if (parsedAmountPaise > 0) {
                                haptics.confirm()
                                val iconName = when (selectedCategory) {
                                    "Food & Dining" -> "Fastfood"
                                    "Groceries" -> "ShoppingCart"
                                    "Fuel & Travel" -> "TimeToLeave"
                                    "Rent & Bills" -> "Home"
                                    "Shopping" -> "ReceiptLong"
                                    "Salary" -> "Work"
                                    "Freelance" -> "Payments"
                                    else -> if (selectedType == TransactionType.TRANSFER) "SwapHoriz" else "ReceiptLong"
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
                                    iconName = iconName
                                )
                                onSaveTransaction(newTxn)
                            }
                        }
                    ),
                shape = ButtonShape,
                color = if (parsedAmountPaise > 0) activeColor else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (parsedAmountPaise > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save ${selectedType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (parsedAmountPaise > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        shape = PillShape,
        color = if (isSelected) selectedColor else Color.Transparent,
        modifier = Modifier.bouncyClickable(scaleDown = 0.92f, onClick = onClick)
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun BouncyChip(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = ChipShape,
        color = containerColor,
        modifier = Modifier
            .bouncyClickable(scaleDown = 0.9f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keypadGrid.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(ButtonShape)
                            .bouncyClickable(
                                scaleDown = 0.88f,
                                onClick = {
                                    when (key) {
                                        "⌫" -> onBackspace()
                                        else -> onDigitClick(key)
                                    }
                                }
                            ),
                        shape = ButtonShape,
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
                                    fontWeight = FontWeight.Bold,
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
