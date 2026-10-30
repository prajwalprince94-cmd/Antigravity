package com.prajwalprince94.expensetracker.ui.receipt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.receipt.ParsedReceipt
import com.prajwalprince94.expensetracker.data.receipt.ReceiptOcrEngine
import com.prajwalprince94.expensetracker.data.receipt.ReceiptPreset
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.IconRegistry
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.model.TransactionType
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.InputShape
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ScannerTab(val title: String) {
    PHOTO("Scan Photo"),
    PRESETS("Presets"),
    PASTE_TEXT("Paste Text")
}

/**
 * High-fidelity Smart Receipt OCR Scanner Modal Bottom Sheet.
 * Allows instant scanning via camera, photo selection from gallery,
 * sample realistic receipt presets, or pasting raw receipt text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerSheet(
    sheetState: SheetState,
    accounts: List<AccountItem>,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionItem) -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(ScannerTab.PRESETS) }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rawInputText by remember { mutableStateOf("") }

    // Parsed Result state
    var parsedReceipt by remember {
        mutableStateOf<ParsedReceipt?>(
            ReceiptPreset.defaultPresets().firstOrNull()?.let {
                ReceiptOcrEngine.parseRawText(it.rawText)
            }
        )
    }

    // Editable fields on parsed result
    var editableMerchant by remember(parsedReceipt) {
        mutableStateOf(parsedReceipt?.merchantName ?: "")
    }
    var editableAmountString by remember(parsedReceipt) {
        mutableStateOf(
            parsedReceipt?.let {
                "%.2f".format(Locale.US, it.totalAmountPaise / 100.0)
            } ?: ""
        )
    }
    var selectedCategory by remember(parsedReceipt) {
        mutableStateOf(parsedReceipt?.suggestedCategory ?: "Food & Dining")
    }
    var selectedAccount by remember(parsedReceipt) {
        mutableStateOf(parsedReceipt?.suggestedAccount ?: (accounts.firstOrNull()?.name ?: "Paytm / UPI"))
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            haptics.selection()
            isProcessing = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                ReceiptOcrEngine.scanImageUri(
                    context = context,
                    uri = uri,
                    onSuccess = { result ->
                        parsedReceipt = result
                        isProcessing = false
                        haptics.confirm()
                        onShowMessage("Receipt parsed successfully! Found: ${result.merchantName}")
                    },
                    onError = { err ->
                        isProcessing = false
                        haptics.error()
                        onShowMessage("OCR failed: ${err.localizedMessage ?: "Could not read text"}")
                    }
                )
            } catch (e: Exception) {
                isProcessing = false
                haptics.error()
                onShowMessage("Error reading photo: ${e.message}")
            }
        }
    }

    // Camera Photo Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            haptics.selection()
            selectedBitmap = bitmap
            isProcessing = true
            ReceiptOcrEngine.scanBitmap(
                bitmap = bitmap,
                onSuccess = { result ->
                    parsedReceipt = result
                    isProcessing = false
                    haptics.confirm()
                    onShowMessage("Receipt parsed: ${result.merchantName} (${result.formattedTotalAmount})")
                },
                onError = { err ->
                    isProcessing = false
                    haptics.error()
                    onShowMessage("OCR error: ${err.localizedMessage}")
                }
            )
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
                .padding(bottom = 32.dp),
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
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Receipt OCR",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "On-Device Neural Text Recognition",
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
                    ScannerTab.entries.forEach { tab ->
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

            // Tab Content
            when (activeTab) {
                ScannerTab.PHOTO -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { cameraLauncher.launch(null) },
                                    shape = ButtonShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Take Photo", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = ButtonShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pick Gallery", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Bitmap preview if selected
                            if (selectedBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = selectedBitmap!!.asImageBitmap(),
                                        contentDescription = "Receipt Preview",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isProcessing) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ScannerTab.PRESETS -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Choose a sample receipt to test instant parsing:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(ReceiptPreset.defaultPresets()) { preset ->
                                    val isCurrent = parsedReceipt?.merchantName?.contains(preset.merchant, ignoreCase = true) == true
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.bouncyClickable {
                                            haptics.selection()
                                            selectedBitmap = null
                                            parsedReceipt = ReceiptOcrEngine.parseRawText(preset.rawText)
                                            onShowMessage("Loaded sample ${preset.name}")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = IconRegistry.getIcon(preset.iconKey),
                                                contentDescription = null,
                                                tint = if (isCurrent) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ScannerTab.PASTE_TEXT -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = rawInputText,
                                onValueChange = { rawInputText = it },
                                label = { Text("Paste receipt text, SMS bill, or invoice contents") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = InputShape
                            )
                            Button(
                                onClick = {
                                    if (rawInputText.isNotBlank()) {
                                        haptics.confirm()
                                        selectedBitmap = null
                                        parsedReceipt = ReceiptOcrEngine.parseRawText(rawInputText)
                                        onShowMessage("Receipt text parsed!")
                                    }
                                },
                                enabled = rawInputText.isNotBlank(),
                                shape = ButtonShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Parse Receipt Text", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Parsed Result Card
            if (parsedReceipt != null) {
                val receipt = parsedReceipt!!
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PARSED RECEIPT DETAILS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(IncomeGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${(receipt.confidence * 100).toInt()}% Match",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                            }

                            // Merchant Name Field
                            OutlinedTextField(
                                value = editableMerchant,
                                onValueChange = { editableMerchant = it },
                                label = { Text("Merchant / Store Name") },
                                singleLine = true,
                                shape = InputShape,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Total Amount Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = editableAmountString,
                                    onValueChange = { editableAmountString = it },
                                    label = { Text("Total Amount (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = InputShape,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = receipt.formattedDate,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Date") },
                                    singleLine = true,
                                    shape = InputShape,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Category Selector Chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Category",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(categories) { cat ->
                                        val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategory = cat.name },
                                            label = { Text(cat.name) },
                                            shape = ChipShape
                                        )
                                    }
                                }
                            }

                            // Account Selector Chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Payment Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(accounts) { acc ->
                                        val isSelected = selectedAccount.equals(acc.name, ignoreCase = true)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedAccount = acc.name },
                                            label = { Text(acc.name) },
                                            shape = ChipShape
                                        )
                                    }
                                }
                            }

                            // Line items breakdown if found
                            if (receipt.lineItems.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Itemized Breakdown (${receipt.lineItems.size} items)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    receipt.lineItems.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = item.formattedAmount,
                                                style = MaterialTheme.typography.bodySmall,
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

                // Final Log Transaction Button
                item {
                    val paise = (editableAmountString.toDoubleOrNull()?.times(100))?.toLong() ?: 0L
                    val isLogEnabled = editableMerchant.isNotBlank() && paise > 0L

                    Button(
                        onClick = {
                            if (isLogEnabled) {
                                haptics.confirm()
                                val noteWithItems = if (receipt.lineItems.isNotEmpty()) {
                                    "Scanned via OCR • " + receipt.lineItems.joinToString(", ") { "${it.name} (${it.formattedAmount})" }
                                } else {
                                    "Scanned via Receipt OCR"
                                }

                                val transaction = TransactionItem(
                                    id = "txn_${UUID.randomUUID()}",
                                    title = editableMerchant.trim(),
                                    category = selectedCategory,
                                    account = selectedAccount,
                                    amountPaise = paise,
                                    type = TransactionType.EXPENSE,
                                    formattedDate = receipt.formattedDate,
                                    note = noteWithItems,
                                    iconName = "ReceiptLong"
                                )
                                onSaveTransaction(transaction)
                                onDismiss()
                                onShowMessage("Receipt logged! Added ₹%,.2f to $selectedCategory".format(Locale.US, paise / 100.0))
                            }
                        },
                        enabled = isLogEnabled,
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Expense (₹%,.2f)".format(Locale.US, paise / 100.0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
