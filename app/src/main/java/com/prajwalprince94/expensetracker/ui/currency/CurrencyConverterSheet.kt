package com.prajwalprince94.expensetracker.ui.currency

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.currency.CurrencyEngine
import com.prajwalprince94.expensetracker.data.currency.CurrencyInfo
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Publication-ready Interactive Currency Converter & Live FX Rates Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterSheet(
    sheetState: SheetState,
    preferences: AppPreferences,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var fromCurrencyCode by remember { mutableStateOf("USD") }
    var toCurrencyCode by remember { mutableStateOf(preferences.baseCurrency) }
    var inputAmountString by remember { mutableStateOf("100") }
    var isRefreshing by remember { mutableStateOf(false) }

    val exchangeRates by CurrencyEngine.exchangeRates.collectAsState()
    val lastUpdatedMillis by CurrencyEngine.lastUpdatedMillis.collectAsState()

    val fromCurrency = remember(fromCurrencyCode) { CurrencyEngine.getCurrency(fromCurrencyCode) }
    val toCurrency = remember(toCurrencyCode) { CurrencyEngine.getCurrency(toCurrencyCode) }

    val inputAmount = inputAmountString.toDoubleOrNull() ?: 0.0
    val conversionRate = remember(fromCurrencyCode, toCurrencyCode, exchangeRates) {
        CurrencyEngine.getExchangeRate(fromCurrencyCode, toCurrencyCode)
    }
    val convertedAmount = inputAmount * conversionRate

    val lastUpdatedText = remember(lastUpdatedMillis) {
        val sdf = SimpleDateFormat("h:mm a, dd MMM", Locale.US)
        sdf.format(Date(lastUpdatedMillis))
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
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Default.CurrencyExchange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Live FX & Currency Converter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Updated: $lastUpdatedText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptics.selection()
                        isRefreshing = true
                        scope.launch {
                            delay(400)
                            CurrencyEngine.refreshRates()
                            isRefreshing = false
                            onShowMessage("Exchange rates updated to latest market rates!")
                        }
                    }
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Rates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Interactive Converter Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 18.dp
            ) {
                // From Currency & Amount Row
                Text(
                    text = "Amount to Convert",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputAmountString,
                        onValueChange = { newVal ->
                            if (newVal.count { it == '.' } <= 1 && newVal.length <= 10) {
                                inputAmountString = newVal
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        leadingIcon = {
                            Text(
                                text = fromCurrency.symbol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    // From Currency Pill Button
                    CurrencyPickerMenu(
                        selectedCode = fromCurrencyCode,
                        onSelect = { code ->
                            haptics.selection()
                            fromCurrencyCode = code
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Swap Button & Exchange Rate Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "1 %s = %,.4f %s".format(Locale.US, fromCurrencyCode, conversionRate, toCurrencyCode),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Swap Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(38.dp)
                            .bouncyClickable {
                                haptics.selection()
                                val temp = fromCurrencyCode
                                fromCurrencyCode = toCurrencyCode
                                toCurrencyCode = temp
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap Currencies",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // To Currency Pill Button
                    CurrencyPickerMenu(
                        selectedCode = toCurrencyCode,
                        onSelect = { code ->
                            haptics.selection()
                            toCurrencyCode = code
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prominent Converted Hero Result
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Converted Value",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyEngine.formatAmount(convertedAmount, toCurrencyCode),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${toCurrency.name} (${toCurrency.flagEmoji})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Reference Rates Board to Base Currency
            Text(
                text = "Market FX Rates to ${preferences.baseCurrency}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(CurrencyEngine.SUPPORTED_CURRENCIES.filter { it.code != preferences.baseCurrency }) { curr ->
                    val rate = CurrencyEngine.getExchangeRate(curr.code, preferences.baseCurrency)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.bouncyClickable {
                            haptics.selection()
                            fromCurrencyCode = curr.code
                            toCurrencyCode = preferences.baseCurrency
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = curr.flagEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = curr.code,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1 ${curr.symbol} = ₹%,.2f".format(Locale.US, rate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = curr.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Base Currency Setting Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Default Base Currency",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "All totals & budgets calculate in this currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    CurrencyPickerMenu(
                        selectedCode = preferences.baseCurrency,
                        onSelect = { newBase ->
                            haptics.confirm()
                            onUpdatePreferences(preferences.copy(baseCurrency = newBase))
                            onShowMessage("Base currency set to $newBase!")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Close button
            Button(
                onClick = {
                    haptics.selection()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = ButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Done", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Dropdown-style picker chip for selecting from available currencies.
 */
@Composable
private fun CurrencyPickerMenu(
    selectedCode: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currency = CurrencyEngine.getCurrency(selectedCode)

    Box {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.bouncyClickable {
                expanded = !expanded
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = currency.flagEmoji, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "▼",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            CurrencyEngine.SUPPORTED_CURRENCIES.forEach { curr ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = curr.flagEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${curr.code} - ${curr.name} (${curr.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (curr.code == selectedCode) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onSelect(curr.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
