package com.prajwalprince94.expensetracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAppLockEnabled by remember { mutableStateOf(false) }
    var isTrueBlackEnabled by remember { mutableStateOf(false) }
    var isDynamicColorEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Preferences & Privacy",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance & Theming Group
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.Palette,
                        title = "Dynamic Wallpaper Color",
                        subtitle = "Adapts to phone wallpaper (Android 12+)",
                        checked = isDynamicColorEnabled,
                        onCheckedChange = {
                            isDynamicColorEnabled = it
                            onShowMessage(if (it) "Dynamic color enabled" else "Default theme active")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "True Black (AMOLED)",
                        subtitle = "Pure black background to save battery",
                        checked = isTrueBlackEnabled,
                        onCheckedChange = {
                            isTrueBlackEnabled = it
                            onShowMessage(if (it) "True Black OLED mode enabled" else "Standard dark theme active")
                        }
                    )
                }
            }
        }

        // Financial & Regional Settings
        item {
            Text(
                text = "Currency & Locale",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onShowMessage("Active Currency: Indian Rupee (INR ₹). Stored as integer paise.")
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Base Currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Indian Rupee (INR ₹) • Zero floating point (Paise)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Security Group
        item {
            Text(
                text = "Security",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.Fingerprint,
                        title = "App Lock",
                        subtitle = "Require Biometric or PIN on app launch",
                        checked = isAppLockEnabled,
                        onCheckedChange = {
                            isAppLockEnabled = it
                            onShowMessage(if (it) "🔒 App Lock enabled! Will require fingerprint/PIN." else "App Lock disabled.")
                        }
                    )
                }
            }
        }

        // Backup & Export Group
        item {
            Text(
                text = "Data & Backup",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onShowMessage("📄 Exporting transactions to CSV... File ready in Downloads.")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Export to CSV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Export all transactions to Excel or Sheets (Tap to test)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onShowMessage("☁️ Local database is primary source of truth. Cloud sync connects in Level 8.")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cloud Synchronization",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Offline-first Room database. Supabase connects in Level 8",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // About / Build Info
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowMessage("Expense Tracker v1.0.0 • Pixel Expressive Architecture") }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Expense Tracker v1.0.0 (Interactive Preview)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Bottom nav spacer
        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
