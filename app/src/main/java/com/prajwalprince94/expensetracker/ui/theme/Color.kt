package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.prajwalprince94.expensetracker.ui.model.AppPalette
import com.prajwalprince94.expensetracker.ui.model.TransactionType

// Strict Financial Semantic Colors for Expense Tracker
// High-Contrast Vibrant Emerald, Coral, Royal Azure
val IncomeGreen = Color(0xFF10B981)      // Vibrant Emerald (Credits / Positive cash flow)
val IncomeGreenLight = Color(0xFFECFDF5)
val IncomeGreenDark = Color(0xFF34D399)

val ExpenseRed = Color(0xFFEF4444)        // Vibrant Coral (Debits / Liabilities / Purchases)
val ExpenseRedLight = Color(0xFFFEF2F2)
val ExpenseRedDark = Color(0xFFF87171)

val TransferBlue = Color(0xFF3B82F6)      // Royal Azure (Internal account movements)
val TransferBlueLight = Color(0xFFEFF6FF)
val TransferBlueDark = Color(0xFF60A5FA)

// Pure OLED Black
val TrueBlack = Color(0xFF000000)
val SurfaceDarkOled = Color(0xFF12161A)

fun getFinancialColor(type: TransactionType, dark: Boolean): Color = when (type) {
    TransactionType.INCOME -> if (dark) IncomeGreenDark else Color(0xFF059669)
    TransactionType.EXPENSE -> if (dark) ExpenseRedDark else Color(0xFFDC2626)
    TransactionType.TRANSFER -> if (dark) TransferBlueDark else Color(0xFF2563EB)
}

fun getFinancialContainerColor(type: TransactionType, dark: Boolean): Color = when (type) {
    TransactionType.INCOME -> if (dark) Color(0xFF064E3B).copy(alpha = 0.40f) else IncomeGreenLight
    TransactionType.EXPENSE -> if (dark) Color(0xFF7F1D1D).copy(alpha = 0.40f) else ExpenseRedLight
    TransactionType.TRANSFER -> if (dark) Color(0xFF1E3A8A).copy(alpha = 0.40f) else TransferBlueLight
}

// ==========================================
// Palettes inspired by NikhilKain/stride
// High-Contrast Obsidian Dark / Crisp Slate Light
// ==========================================

// 1. Emerald (Prosperity & Mint - Default Financial Theme)
private val EmeraldLight = lightColorScheme(
    primary = Color(0xFF006960),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9EF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF051F1C),
    tertiary = Color(0xFF9A4600),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC6),
    onTertiaryContainer = Color(0xFF321300),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE8ECEF),
    surfaceContainerHighest = Color(0xFFDEE3E7),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

private val EmeraldDark = darkColorScheme(
    primary = Color(0xFF82D5C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9EF2E4),
    secondary = Color(0xFFB1CCC6),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = Color(0xFFFFB877),
    onTertiary = Color(0xFF522300),
    tertiaryContainer = Color(0xFF743500),
    onTertiaryContainer = Color(0xFFFFDCC6),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B0F12),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF212830),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF070A0D),
    surfaceContainerLow = Color(0xFF101418),
    surfaceContainer = Color(0xFF161B20),
    surfaceContainerHigh = Color(0xFF212830),
    surfaceContainerHighest = Color(0xFF2A3440),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D3748)
)

// 2. Ocean (Zen Blue - Trustworthy Banking Feel)
private val OceanLight = lightColorScheme(
    primary = Color(0xFF1B6088),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC4E7FF),
    onPrimaryContainer = Color(0xFF001E2E),
    secondary = Color(0xFF4E616D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E5F4),
    onSecondaryContainer = Color(0xFF0A1E28),
    tertiary = Color(0xFF615A7D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE7DEFF),
    onTertiaryContainer = Color(0xFF1D1736),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE8ECEF),
    surfaceContainerHighest = Color(0xFFDEE3E7),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

private val OceanDark = darkColorScheme(
    primary = Color(0xFF8DCDFF),
    onPrimary = Color(0xFF00344E),
    primaryContainer = Color(0xFF004B6F),
    onPrimaryContainer = Color(0xFFC4E7FF),
    secondary = Color(0xFFB5C9D7),
    onSecondary = Color(0xFF20333E),
    secondaryContainer = Color(0xFF364955),
    onSecondaryContainer = Color(0xFFD1E5F4),
    tertiary = Color(0xFFCBC1EA),
    onTertiary = Color(0xFF322C4C),
    tertiaryContainer = Color(0xFF494264),
    onTertiaryContainer = Color(0xFFE7DEFF),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B0F12),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF212830),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF070A0D),
    surfaceContainerLow = Color(0xFF101418),
    surfaceContainer = Color(0xFF161B20),
    surfaceContainerHigh = Color(0xFF212830),
    surfaceContainerHighest = Color(0xFF2A3440),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D3748)
)

// 3. Crimson (Warm Coral Sunset)
private val CrimsonLight = lightColorScheme(
    primary = Color(0xFF904A42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD5),
    onPrimaryContainer = Color(0xFF3B0906),
    secondary = Color(0xFF775652),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD5),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF715B2E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEDFA6),
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE8ECEF),
    surfaceContainerHighest = Color(0xFFDEE3E7),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

private val CrimsonDark = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF561E18),
    primaryContainer = Color(0xFF73342D),
    onPrimaryContainer = Color(0xFFFFDAD5),
    secondary = Color(0xFFE7BDB7),
    onSecondary = Color(0xFF442926),
    secondaryContainer = Color(0xFF5D3F3B),
    onSecondaryContainer = Color(0xFFFFDAD5),
    tertiary = Color(0xFFE0C38C),
    onTertiary = Color(0xFF3F2E05),
    tertiaryContainer = Color(0xFF584419),
    onTertiaryContainer = Color(0xFFFEDFA6),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B0F12),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF212830),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF070A0D),
    surfaceContainerLow = Color(0xFF101418),
    surfaceContainer = Color(0xFF161B20),
    surfaceContainerHigh = Color(0xFF212830),
    surfaceContainerHighest = Color(0xFF2A3440),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D3748)
)

// 4. Amethyst (Premium Violet)
private val AmethystLight = lightColorScheme(
    primary = Color(0xFF6B538C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEDCFF),
    onPrimaryContainer = Color(0xFF260D44),
    secondary = Color(0xFF655A6F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEBDDF6),
    onSecondaryContainer = Color(0xFF20182A),
    background = Color(0xFFF4F6F8),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF4F6F8),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE8ECEF),
    surfaceContainerHighest = Color(0xFFDEE3E7),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

private val AmethystDark = darkColorScheme(
    primary = Color(0xFFD7BAFB),
    onPrimary = Color(0xFF3B245A),
    primaryContainer = Color(0xFF533B72),
    onPrimaryContainer = Color(0xFFEEDCFF),
    secondary = Color(0xFFCFC2DA),
    onSecondary = Color(0xFF352D40),
    secondaryContainer = Color(0xFF4C4357),
    onSecondaryContainer = Color(0xFFEBDDF6),
    background = Color(0xFF0B0F12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B0F12),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF212830),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF070A0D),
    surfaceContainerLow = Color(0xFF101418),
    surfaceContainer = Color(0xFF161B20),
    surfaceContainerHigh = Color(0xFF212830),
    surfaceContainerHighest = Color(0xFF2A3440),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D3748)
)

fun getPaletteColorScheme(palette: AppPalette, darkTheme: Boolean): ColorScheme = when (palette) {
    AppPalette.EMERALD -> if (darkTheme) EmeraldDark else EmeraldLight
    AppPalette.OCEAN -> if (darkTheme) OceanDark else OceanLight
    AppPalette.CRIMSON -> if (darkTheme) CrimsonDark else CrimsonLight
    AppPalette.AMETHYST -> if (darkTheme) AmethystDark else AmethystLight
}
