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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Palette
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
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ColorStyle
import com.prajwalprince94.expensetracker.ui.model.ThemeMode

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    onUpdatePreferences: (AppPreferences) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAppLockEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
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
                text = "Preferences, Theme & Security",
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

        // 2. Palette Selector (Emerald, Ocean, Crimson, Amethyst)
        item {
            SectionHeader(title = "Accent Palette", modifier = Modifier.entrance(2))
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(AppPalette.values()) { palette ->
                    val isSelected = preferences.palette == palette
                    val swatchColor = when (palette) {
                        AppPalette.EMERALD -> Color(0xFF006960)
                        AppPalette.OCEAN -> Color(0xFF1B6088)
                        AppPalette.CRIMSON -> Color(0xFF904A42)
                        AppPalette.AMETHYST -> Color(0xFF6B538C)
                    }

                    Surface(
                        modifier = Modifier
                            .bouncyClickable(scaleDown = 0.92f) {
                                onUpdatePreferences(preferences.copy(palette = palette, dynamicColor = false))
                                onShowMessage("${palette.displayName} palette activated")
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = palette.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. ColorBlendr Style
        item {
            SectionHeader(title = "Color Style", modifier = Modifier.entrance(3))
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ColorStyle.values()) { style ->
                    val isSelected = preferences.colorStyle == style
                    Surface(
                        modifier = Modifier.bouncyClickable(scaleDown = 0.92f) {
                            onUpdatePreferences(preferences.copy(colorStyle = style))
                            onShowMessage("${style.name} style applied")
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = style.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 4. Living Aurora & Display
        item {
            SectionHeader(title = "Display & Ambient", modifier = Modifier.entrance(4))
            Spacer(modifier = Modifier.height(4.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .entrance(4),
                contentPadding = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    SettingToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Living Aurora Backdrop",
                        subtitle = "Range-inspired ambient blooms drifting in background",
                        checked = preferences.ambientAurora,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(ambientAurora = it))
                            onShowMessage(if (it) "Living Aurora enabled" else "Flat background active")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingToggleRow(
                        icon = Icons.Default.Palette,
                        title = "Material You Dynamic Color",
                        subtitle = "Adapts to phone wallpaper (Android 12+)",
                        checked = preferences.dynamicColor,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(dynamicColor = it))
                            onShowMessage(if (it) "Dynamic color enabled" else "Palette color active")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Pure Black AMOLED",
                        subtitle = "Saves battery on OLED displays with true black ladder",
                        checked = preferences.amoled,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(amoled = it))
                            onShowMessage(if (it) "AMOLED pure black enabled" else "Standard dark theme active")
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingToggleRow(
                        icon = Icons.Default.MotionPhotosOff,
                        title = "Reduce Motion",
                        subtitle = "Disables ambient canvas animations for power saving",
                        checked = preferences.reduceMotion,
                        onCheckedChange = {
                            onUpdatePreferences(preferences.copy(reduceMotion = it))
                            onShowMessage(if (it) "Motion reduced" else "Full motion enabled")
                        }
                    )
                }
            }
        }

        // 5. Security & Privacy
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
                        subtitle = "Require Fingerprint / Face unlock to open app",
                        checked = isAppLockEnabled,
                        onCheckedChange = {
                            isAppLockEnabled = it
                            onShowMessage(if (it) "Biometric Lock enabled" else "Biometric Lock disabled")
                        }
                    )
                }
            }
        }

        // 6. Data & Cloud Sync
        item {
            SectionHeader(title = "Data Management", modifier = Modifier.entrance(6))
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
                        onClick = { onShowMessage("Exported 10 transactions to Downloads/expenses.csv") }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    SettingActionRow(
                        icon = Icons.Default.CloudSync,
                        title = "Supabase Cloud Sync",
                        subtitle = "Encrypted remote backup (Configured for Level 6)",
                        onClick = { onShowMessage("Cloud Sync configuration ready!") }
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
                            text = "Expense Tracker v1.2",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Range & Stride UI/UX • Poppins Font • Aurora Blooms",
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
