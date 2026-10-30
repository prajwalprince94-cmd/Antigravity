package com.prajwalprince94.expensetracker.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import com.prajwalprince94.expensetracker.security.BiometricSecurityManager
import com.prajwalprince94.expensetracker.ui.security.PinSetupBottomSheet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.prajwalprince94.expensetracker.BuildConfig
import com.prajwalprince94.expensetracker.data.export.CsvExporter
import com.prajwalprince94.expensetracker.data.model.AccountItem
import com.prajwalprince94.expensetracker.data.model.CategoryItem
import com.prajwalprince94.expensetracker.data.model.SubscriptionItem
import com.prajwalprince94.expensetracker.data.update.AppUpdateInfo
import com.prajwalprince94.expensetracker.data.update.AppUpdateManager
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.UpdateBottomSheet
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.components.rememberAppHaptics
import com.prajwalprince94.expensetracker.ui.components.responsiveHorizontalPadding
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ColorStyle
import com.prajwalprince94.expensetracker.ui.model.ThemeMode
import com.prajwalprince94.expensetracker.ui.model.TransactionItem
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed
import com.prajwalprince94.expensetracker.ui.theme.PillShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    transactions: List<TransactionItem> = emptyList(),
    accounts: List<AccountItem> = emptyList(),
    categories: List<CategoryItem> = emptyList(),
    subscriptions: List<SubscriptionItem> = emptyList(),
    onNavigateToAccounts: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSplit: () -> Unit = {},
    onUpdatePreferences: (AppPreferences) -> Unit,
    onResetDatabase: () -> Unit,
    onClearTransactions: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberAppHaptics()
    var showPinSetupSheet by remember { mutableStateOf(false) }
    val isBiometricAvailable = remember(context) { BiometricSecurityManager.isBiometricAvailable(context) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateInfoToDisplay by remember { mutableStateOf<AppUpdateInfo?>(null) }
    val updateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val horizontalPadding = responsiveHorizontalPadding()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.entrance(0)
            )
            Text(
                text = "Preferences, Theme & Offline Database",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.entrance(0)
            )
        }

        // 1. Theme Mode (System / Light / Dark)
        item {
            SectionHeader(title = "Theme Mode", modifier = Modifier.entrance(1))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(1),
                contentPadding = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = preferences.themeMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bouncyClickable(scaleDown = 0.92f) {
                                    haptics.selection()
                                    onUpdatePreferences(preferences.copy(themeMode = mode))
                                    onShowMessage("Theme set to ${mode.name.lowercase()}")
                                },
                            shape = PillShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Color Palette & Styles (Stride Inspiration)
        item {
            SectionHeader(title = "App Color Accent", modifier = Modifier.entrance(2))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(2),
                contentPadding = 12.dp
            ) {
                Text(
                    text = "Accent Palette",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(AppPalette.values()) { palette ->
                        val isSelected = preferences.palette == palette
                        val previewColor = when (palette) {
                            AppPalette.EMERALD -> Color(0xFF10B981)
                            AppPalette.OCEAN -> Color(0xFF0284C7)
                            AppPalette.CRIMSON -> Color(0xFFE11D48)
                            AppPalette.AMETHYST -> Color(0xFF8B5CF6)
                        }

                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .bouncyClickable(scaleDown = 0.92f) {
                                    haptics.selection()
                                    onUpdatePreferences(preferences.copy(palette = palette))
                                    onShowMessage("${palette.displayName} palette applied")
                                },
                            shape = ChipShape,
                            color = if (isSelected) previewColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, previewColor) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(previewColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = palette.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Color Harmony Style",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ColorStyle.values()) { style ->
                        val isSelected = preferences.colorStyle == style
                        Surface(
                            shape = ChipShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                                haptics.selection()
                                onUpdatePreferences(preferences.copy(colorStyle = style))
                                onShowMessage("${style.displayName} style applied")
                            }
                        ) {
                            Text(
                                text = style.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. AMOLED & Visual Effects
        item {
            SectionHeader(title = "Display & Surfaces", modifier = Modifier.entrance(3))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(3),
                contentPadding = 12.dp
            ) {
                SettingToggleRow(
                    icon = Icons.Default.Brightness4,
                    title = "Pure Black AMOLED",
                    subtitle = "Sets surfaces to #000000 on dark theme for OLED displays",
                    checked = preferences.amoled,
                    onCheckedChange = {
                        onUpdatePreferences(preferences.copy(amoled = it))
                        onShowMessage(if (it) "Pure Black AMOLED enabled" else "Standard dark theme enabled")
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                SettingToggleRow(
                    icon = Icons.Default.MotionPhotosOff,
                    title = "Reduce Motion",
                    subtitle = "Replaces spring physics with instant transitions",
                    checked = preferences.reduceMotion,
                    onCheckedChange = {
                        onUpdatePreferences(preferences.copy(reduceMotion = it))
                        onShowMessage(if (it) "Motion reduced" else "Full animations enabled")
                    }
                )
            }
        }

        // 4. Security & Biometrics
        item {
            SectionHeader(title = "Security & Privacy", modifier = Modifier.entrance(4))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(4),
                contentPadding = 12.dp
            ) {
                // App Lock Master Toggle
                SettingToggleRow(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = if (preferences.appLockEnabled) "App requires 4-digit PIN or Biometrics to open" else "Secure your transactions and account records",
                    checked = preferences.appLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (preferences.appLockPinHash.isBlank()) {
                                showPinSetupSheet = true
                            } else {
                                onUpdatePreferences(preferences.copy(appLockEnabled = true))
                                onShowMessage("App Lock enabled")
                            }
                        } else {
                            onUpdatePreferences(preferences.copy(appLockEnabled = false))
                            onShowMessage("App Lock disabled")
                        }
                    }
                )

                if (preferences.appLockEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Change PIN Row
                    SettingActionRow(
                        icon = Icons.Default.Password,
                        title = if (preferences.appLockPinHash.isBlank()) "Set 4-Digit PIN" else "Change 4-Digit PIN",
                        subtitle = if (preferences.appLockPinHash.isBlank()) "PIN required to complete lock setup" else "Update your 4-digit passcode",
                        onClick = {
                            haptics.selection()
                            showPinSetupSheet = true
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Biometrics Toggle
                    SettingToggleRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Unlock",
                        subtitle = if (isBiometricAvailable) "Use Fingerprint or Face recognition" else "Biometrics not enrolled or supported on this device",
                        checked = preferences.biometricEnabled && isBiometricAvailable,
                        onCheckedChange = { enabled ->
                            if (!isBiometricAvailable) {
                                onShowMessage("Biometrics unavailable on device")
                            } else {
                                onUpdatePreferences(preferences.copy(biometricEnabled = enabled))
                                onShowMessage(if (enabled) "Biometric Unlock enabled" else "Biometric Unlock disabled")
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Auto-Lock Timeout Row
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Lock Delay",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lock when app is minimized or backgrounded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val timeoutOptions = listOf(
                                0 to "Immediately",
                                60 to "1 Minute",
                                300 to "5 Minutes"
                            )
                            timeoutOptions.forEach { (seconds, label) ->
                                val selected = preferences.autoLockTimeoutSeconds == seconds
                                Surface(
                                    shape = ChipShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .bouncyClickable(scaleDown = 0.94f) {
                                            haptics.selection()
                                            onUpdatePreferences(preferences.copy(autoLockTimeoutSeconds = seconds))
                                            onShowMessage("Lock delay: $label")
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Recents Privacy Shield
                    SettingToggleRow(
                        icon = Icons.Default.Security,
                        title = "Recents Privacy Shield",
                        subtitle = "Prevents previews in Android Recent Apps switcher",
                        checked = preferences.recentsPrivacyBlur,
                        onCheckedChange = { blur ->
                            onUpdatePreferences(preferences.copy(recentsPrivacyBlur = blur))
                            onShowMessage(if (blur) "Recents Privacy Shield enabled" else "Recents Privacy Shield disabled")
                        }
                    )
                }
            }
        }

        // 5. In-App Update Engine
        item {
            SectionHeader(title = "App Updates & Releases", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Check for Updates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current: v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = ButtonShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.bouncyClickable(scaleDown = 0.9f) {
                            if (!isCheckingUpdate) {
                                haptics.light()
                                isCheckingUpdate = true
                                onShowMessage("Checking for latest release...")
                                scope.launch {
                                    val info = AppUpdateManager.checkForUpdates(BuildConfig.VERSION_NAME)
                                    isCheckingUpdate = false
                                    updateInfoToDisplay = info
                                }
                            }
                        }
                    ) {
                        Text(
                            text = "Check",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 5.5 Accounts & Tagging
        item {
            SectionHeader(title = "Accounts & Tagging", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 12.dp
            ) {
                SettingActionRow(
                    icon = Icons.Default.AccountBalance,
                    title = "Accounts & Custom Categories",
                    subtitle = "${accounts.size} Wallets/Banks • ${categories.size} Category Tags",
                    onClick = {
                        haptics.confirm()
                        onNavigateToAccounts()
                    }
                )
            }
        }

        // 5.6 Recurring Bills & Subscriptions
        item {
            SectionHeader(title = "Subscriptions & Commitments", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 12.dp
            ) {
                SettingActionRow(
                    icon = Icons.Default.CalendarMonth,
                    title = "Subscriptions & Recurring Bills",
                    subtitle = "${subscriptions.size} tracked • ${subscriptions.count { it.isActive }} active",
                    onClick = {
                        haptics.confirm()
                        onNavigateToSubscriptions()
                    }
                )
            }
        }

        // 5.7 Financial Health & Analytics
        item {
            SectionHeader(title = "Financial Health & Analytics", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 12.dp
            ) {
                SettingActionRow(
                    icon = Icons.Default.Speed,
                    title = "Financial Health & Projections",
                    subtitle = "0-100 Score • Burn Rate Forecast • MoM Trends",
                    onClick = {
                        haptics.confirm()
                        onNavigateToAnalytics()
                    }
                )
            }
        }

        // 5.8 Split Expenses & Shared Groups
        item {
            SectionHeader(title = "Split Bills & Group Expenses", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 12.dp
            ) {
                SettingActionRow(
                    icon = Icons.Default.Group,
                    title = "Split Expenses with Friends",
                    subtitle = "Track group balances, debt simplification & one-tap settlements",
                    onClick = {
                        haptics.confirm()
                        onNavigateToSplit()
                    }
                )
            }
        }

        // 6. Offline Vault & Database Management
        item {
            SectionHeader(title = "Offline Database Vault", modifier = Modifier.entrance(6))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(6),
                contentPadding = 12.dp
            ) {
                Column {
                    SettingActionRow(
                        icon = Icons.Default.FileDownload,
                        title = "Export Statement (CSV)",
                        subtitle = if (transactions.isNotEmpty()) {
                            "Share statement containing ${transactions.size} transactions"
                        } else {
                            "No transactions in database to export"
                        },
                        onClick = {
                            if (transactions.isEmpty()) {
                                onShowMessage("No transactions found to export.")
                            } else {
                                haptics.confirm()
                                val result = CsvExporter.exportAndShare(context, transactions)
                                result.onSuccess {
                                    onShowMessage("CSV statement ready! Choose an app to share.")
                                }.onFailure { err ->
                                    onShowMessage("Export failed: ${err.message}")
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingActionRow(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Sample Data",
                        subtitle = "Restore default transactions & budget allocations",
                        onClick = {
                            haptics.confirm()
                            onResetDatabase()
                            onShowMessage("Database reset to factory sample data")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingActionRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Transactions",
                        subtitle = "Remove all expense and income records from SQLite storage",
                        onClick = {
                            haptics.confirm()
                            onClearTransactions()
                            onShowMessage("All transactions cleared from database")
                        }
                    )
                }
            }
        }

        // 7. About App
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(7),
                contentPadding = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Expense Tracker v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stride UI/UX • Vyxel Splash • Offline SQLite Vault",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Modal Bottom Sheet showing in-app update information & installer
    updateInfoToDisplay?.let { info ->
        UpdateBottomSheet(
            updateInfo = info,
            sheetState = updateSheetState,
            onDismiss = { updateInfoToDisplay = null },
            onShowMessage = onShowMessage
        )
    }

    if (showPinSetupSheet) {
        PinSetupBottomSheet(
            onDismiss = { showPinSetupSheet = false },
            onPinConfigured = { hash ->
                onUpdatePreferences(
                    preferences.copy(
                        appLockPinHash = hash,
                        appLockEnabled = true
                    )
                )
                showPinSetupSheet = false
                onShowMessage("4-Digit PIN configured successfully")
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(scaleDown = 0.98f) { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(scaleDown = 0.96f, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
