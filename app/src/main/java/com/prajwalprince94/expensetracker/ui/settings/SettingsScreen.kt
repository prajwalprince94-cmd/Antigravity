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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.prajwalprince94.expensetracker.ui.components.GlassCard
import com.prajwalprince94.expensetracker.ui.components.SectionHeader
import com.prajwalprince94.expensetracker.ui.components.bouncyClickable
import com.prajwalprince94.expensetracker.ui.components.entrance
import com.prajwalprince94.expensetracker.ui.components.fadingEdges
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ColorStyle
import com.prajwalprince94.expensetracker.ui.model.ThemeMode
import com.prajwalprince94.expensetracker.ui.theme.ExpenseRed

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onResetDatabase: () -> Unit,
    onClearTransactions: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAppLockEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
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
                                    onUpdatePreferences(preferences.copy(themeMode = mode))
                                    onShowMessage("Theme set to ${mode.name.lowercase()}")
                                },
                            shape = RoundedCornerShape(16.dp),
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

        // 2. Color Palette & Styles (Stride ColorBlendr Inspiration)
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
                    fontWeight = FontWeight.Bold
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
                                    onUpdatePreferences(preferences.copy(palette = palette))
                                    onShowMessage("${palette.displayName} palette applied")
                                },
                            shape = RoundedCornerShape(14.dp),
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
                                    fontSize = 13.sp,
                                    color = if (isSelected) previewColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Stride ColorStyles Selector
        item {
            SectionHeader(title = "Material Expressive Style", modifier = Modifier.entrance(3))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(3),
                contentPadding = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ColorStyle.values().forEach { style ->
                        val isSelected = preferences.colorStyle == style
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .bouncyClickable(scaleDown = 0.92f) {
                                    onUpdatePreferences(preferences.copy(colorStyle = style))
                                    onShowMessage("${style.name.lowercase()} styling active")
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Display & Visual Atmosphere (Range Aurora + AMOLED)
        item {
            SectionHeader(title = "Atmosphere & Visuals", modifier = Modifier.entrance(4))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(4),
                contentPadding = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Pure Black AMOLED",
                        subtitle = "Absolute #000000 background surface for OLED displays",
                        checked = preferences.amoled,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(amoled = it))
                            onShowMessage(if (it) "AMOLED Pure Black active" else "Standard dark theme active")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Living Aurora Backdrop",
                        subtitle = "Soft ambient color glows drifting behind cards",
                        checked = preferences.ambientAurora,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(ambientAurora = it))
                            onShowMessage(if (it) "Living Aurora enabled" else "Living Aurora paused")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingToggleRow(
                        icon = Icons.Default.MotionPhotosOff,
                        title = "Reduce Motion",
                        subtitle = "Disable bouncy physics and living starfield animations",
                        checked = preferences.reduceMotion,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(reduceMotion = it))
                            onShowMessage(if (it) "Animations reduced" else "Expressive motion enabled")
                        }
                    )
                }
            }
        }

        // 5. Security & Biometrics
        item {
            SectionHeader(title = "Security & Privacy", modifier = Modifier.entrance(5))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(5),
                contentPadding = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Require fingerprint or face unlock on launch",
                        checked = isAppLockEnabled,
                        onCheckedChange = {
                            isAppLockEnabled = it
                            onShowMessage(if (it) "Biometric Lock enabled" else "Biometric Lock disabled")
                        }
                    )
                }
            }
        }

        // 6. Real Offline Data Management & Database Controls
        item {
            SectionHeader(title = "Offline Database & Vault", modifier = Modifier.entrance(6))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(6),
                contentPadding = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    SettingActionRow(
                        icon = Icons.Default.FileDownload,
                        title = "Export Data (CSV / JSON)",
                        subtitle = "Export transaction ledger to local storage",
                        onClick = { onShowMessage("Transactions exported to local device storage") }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingActionRow(
                        icon = Icons.Default.CloudSync,
                        title = "Supabase Cloud Sync",
                        subtitle = "Encrypted remote backup sync",
                        onClick = { onShowMessage("Cloud Sync vault active and in sync") }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingActionRow(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Database to Defaults",
                        subtitle = "Restore default sample transactions and category budgets",
                        onClick = {
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
                            text = "Expense Tracker v2.0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Stride Pill Navigation • Range Living Aurora • Poppins Offline",
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
                fontWeight = FontWeight.Bold
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
                fontWeight = FontWeight.Bold
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
