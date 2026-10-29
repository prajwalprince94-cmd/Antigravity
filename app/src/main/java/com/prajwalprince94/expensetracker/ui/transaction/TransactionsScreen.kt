package com.prajwalprince94.expensetracker.ui.transaction

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun TransactionsScreen(
    transactions: List<TransactionItem>,
    onSelectTransaction: (TransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filterOptions = listOf("All", "Expenses", "Income", "Transfers")

    // Real-time dynamic filtering
    val filteredList = transactions.filter { txn ->
        val matchesSearch = searchQuery.isBlank() ||
                txn.title.contains(searchQuery, ignoreCase = true) ||
                txn.category.contains(searchQuery, ignoreCase = true) ||
                txn.account.contains(searchQuery, ignoreCase = true) ||
                txn.note.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Expenses" -> txn.type == TransactionType.EXPENSE
            "Income" -> txn.type == TransactionType.INCOME
            "Transfers" -> txn.type == TransactionType.TRANSFER
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Smartphone Search Box with working clear button
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search transactions, notes...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal filter chips with real-time list filtering
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic filtered transaction list
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Try adjusting your search or filter",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { txn ->
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

                // Bottom spacing for floating bar
                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}
