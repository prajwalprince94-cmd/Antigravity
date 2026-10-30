package com.prajwalprince94.expensetracker.ui.sync

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.prajwalprince94.expensetracker.data.sync.ConflictResolutionStrategy
import com.prajwalprince94.expensetracker.data.sync.SupabaseSyncEngine
import com.prajwalprince94.expensetracker.data.sync.SyncState
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.StatusPill
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.IncomeGreen
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import com.prajwalprince94.expensetracker.ui.theme.SheetShape
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interactive Supabase Real-Time Cloud Sync & Offline Conflict Engine Bottom Sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseSyncSheet(
    sheetState: SheetState,
    preferences: AppPreferences,
    transactions: List<TransactionItem>,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val haptics = rememberAppHaptics()
    val scope = rememberCoroutineScope()

    val syncState by SupabaseSyncEngine.syncState.collectAsState()
    val lastResult by SupabaseSyncEngine.lastResult.collectAsState()
    val recentLogs by SupabaseSyncEngine.recentLogs.collectAsState()

    var isTestingConnection by remember { mutableStateOf(false) }
    var connectionTestMessage by remember { mutableStateOf<String?>(null) }
    var connectionTestSuccess by remember { mutableStateOf(false) }

    var urlInput by remember { mutableStateOf(preferences.supabaseUrl) }
    var keyInput by remember { mutableStateOf(preferences.supabaseAnonKey) }
    var isSandbox by remember { mutableStateOf(preferences.supabaseSandboxMode) }
    var selectedStrategy by remember {
        mutableStateOf(
            runCatching { ConflictResolutionStrategy.valueOf(preferences.supabaseConflictStrategy) }
                .getOrDefault(ConflictResolutionStrategy.LATEST_TIMESTAMP)
        )
    }

    val lastSyncFormatted = remember(preferences.lastSyncTimestampMillis) {
        if (preferences.lastSyncTimestampMillis == 0L) "Never"
        else {
            val sdf = SimpleDateFormat("h:mm a, dd MMM", Locale.US)
            sdf.format(Date(preferences.lastSyncTimestampMillis))
        }
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
            // Header
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
                            imageVector = when (syncState) {
                                SyncState.SUCCESS -> Icons.Default.CloudDone
                                SyncState.SYNCING -> Icons.Default.CloudSync
                                SyncState.ERROR -> Icons.Default.ErrorOutline
                                else -> Icons.Default.CloudSync
                            },
                            contentDescription = null,
                            tint = when (syncState) {
                                SyncState.SUCCESS -> IncomeGreen
                                SyncState.ERROR -> ExpenseRed
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Supabase Real-Time Cloud Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Multi-device synchronization & conflict engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Status Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cloud Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (syncState) {
                                SyncState.SUCCESS -> "Connected & Synced"
                                SyncState.SYNCING -> "Synchronizing..."
                                SyncState.ERROR -> "Sync Warning"
                                else -> if (isSandbox) "Sandbox Mode Active" else "Ready to Sync"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    StatusPill(
                        label = when (syncState) {
                            SyncState.SUCCESS -> "ONLINE"
                            SyncState.SYNCING -> "SYNCING"
                            SyncState.ERROR -> "ERROR"
                            else -> if (isSandbox) "SANDBOX" else "STANDBY"
                        },
                        color = when (syncState) {
                            SyncState.SUCCESS -> IncomeGreen
                            SyncState.ERROR -> ExpenseRed
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Local Vault Records",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${transactions.size} Items",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Last Successful Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastSyncFormatted,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Sync Now Action Button
            Button(
                onClick = {
                    haptics.confirm()
                    scope.launch {
                        val result = SupabaseSyncEngine.performSync(
                            supabaseUrl = urlInput,
                            supabaseAnonKey = keyInput,
                            isSandboxMode = isSandbox,
                            localTransactions = transactions,
                            strategy = selectedStrategy
                        )
                        if (result.success) {
                            onUpdatePreferences(
                                preferences.copy(
                                    supabaseUrl = urlInput,
                                    supabaseAnonKey = keyInput,
                                    supabaseSandboxMode = isSandbox,
                                    supabaseConflictStrategy = selectedStrategy.name,
                                    lastSyncTimestampMillis = System.currentTimeMillis()
                                )
                            )
                            onShowMessage(result.message)
                        } else {
                            onShowMessage(result.message)
                        }
                    }
                },
                enabled = syncState != SyncState.SYNCING,
                shape = ButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (syncState == SyncState.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Syncing with Cloud...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Sync Now", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sandbox Mode Toggle Row
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Developer Demo Sandbox",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Test instant cloud sync and conflict flows without live Supabase keys",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isSandbox,
                        onCheckedChange = { checked ->
                            haptics.selection()
                            isSandbox = checked
                            onUpdatePreferences(preferences.copy(supabaseSandboxMode = checked))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real Supabase Credentials Inputs (Visible when Sandbox is OFF)
            AnimatedVisibility(visible = !isSandbox) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Supabase Project Credentials",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://your-project-id.supabase.co") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Supabase Anon Public API Key") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            haptics.confirm()
                            isTestingConnection = true
                            connectionTestMessage = null
                            scope.launch {
                                val res = SupabaseSyncEngine.testConnection(urlInput, keyInput)
                                isTestingConnection = false
                                res.onSuccess {
                                    connectionTestSuccess = true
                                    connectionTestMessage = "Connected to Supabase project successfully!"
                                }.onFailure { err ->
                                    connectionTestSuccess = false
                                    connectionTestMessage = err.message
                                }
                            }
                        },
                        shape = ButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Connection...")
                        } else {
                            Text("Test Supabase Connection")
                        }
                    }

                    connectionTestMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (connectionTestSuccess) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Conflict Resolution Strategy Selector
            Text(
                text = "Conflict Resolution Strategy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            ConflictResolutionStrategy.entries.forEach { strat ->
                val isSelected = selectedStrategy == strat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .bouncyClickable {
                            haptics.selection()
                            selectedStrategy = strat
                            onUpdatePreferences(preferences.copy(supabaseConflictStrategy = strat.name))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .then(
                                    if (!isSelected) Modifier.clip(CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strat.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strat.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recent Sync Activity & Audit Log
            Text(
                text = "Recent Cloud Sync Activity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            recentLogs.take(5).forEach { log ->
                val logTime = SimpleDateFormat("h:mm:ss a", Locale.US).format(Date(log.timestampMillis))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (log.isSuccess) IncomeGreen else ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = logTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
