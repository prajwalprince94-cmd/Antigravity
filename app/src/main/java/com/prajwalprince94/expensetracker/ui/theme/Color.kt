package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.prajwalprince94.expensetracker.ui.model.AppPalette

// Strict Financial Semantic Colors for Expense Tracker
// Rule: Never change these semantic colors across screens
val IncomeGreen = Color(0xFF2E7D32)      // Credits / Positive cash flow
val IncomeGreenLight = Color(0xFFE8F5E9)

val ExpenseRed = Color(0xFFC62828)        // Debits / Liabilities / Purchases
val ExpenseRedLight = Color(0xFFFFEBEE)

val TransferBlue = Color(0xFF1565C0)      // Internal account movements
val TransferBlueLight = Color(0xFFE3F2FD)

// Pure OLED Black
val TrueBlack = Color(0xFF000000)
val SurfaceDarkOled = Color(0xFF121212)

// ==========================================
// Palettes inspired by NikhilKain/stride
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
    background = Color(0xFFEBF3F0),
    onBackground = Color(0xFF171D1B),
    surface = Color(0xFFEBF3F0),
    onSurface = Color(0xFF171D1B),
    surfaceVariant = Color(0xFFDBE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFDFFFD),
    surfaceContainerHigh = Color(0xFFF3F9F6),
    surfaceContainerHighest = Color(0xFFE7EEEB),
    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFBEC9C5)
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
    background = Color(0xFF0D1513),
    onBackground = Color(0xFFDDE4E1),
    surface = Color(0xFF0D1513),
    onSurface = Color(0xFFDDE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    surfaceContainerLowest = Color(0xFF081110),
    surfaceContainerLow = Color(0xFF151E1C),
    surfaceContainer = Color(0xFF1A2422),
    surfaceContainerHigh = Color(0xFF242F2C),
    surfaceContainerHighest = Color(0xFF2F3A37),
    outline = Color(0xFF89938F),
    outlineVariant = Color(0xFF3F4946)
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
    background = Color(0xFFF6FAFD),
    onBackground = Color(0xFF171C1F),
    surface = Color(0xFFF6FAFD),
    onSurface = Color(0xFF171C1F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF0F5F9),
    surfaceContainerHigh = Color(0xFFEAEFF3),
    surfaceContainerHighest = Color(0xFFE1E7EB)
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
    background = Color(0xFF0F1417),
    onBackground = Color(0xFFDFE3E7),
    surface = Color(0xFF0F1417),
    onSurface = Color(0xFFDFE3E7),
    surfaceContainerLowest = Color(0xFF0A0F12),
    surfaceContainerLow = Color(0xFF171C20),
    surfaceContainer = Color(0xFF1B2024),
    surfaceContainerHigh = Color(0xFF252B2F),
    surfaceContainerHighest = Color(0xFF30363A)
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
    background = Color(0xFFFCF8F7),
    onBackground = Color(0xFF201A19),
    surface = Color(0xFFFCF8F7),
    onSurface = Color(0xFF201A19)
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
    background = Color(0xFF181211),
    onBackground = Color(0xFFEDE0DE),
    surface = Color(0xFF181211),
    onSurface = Color(0xFFEDE0DE)
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
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1D1B20),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1D1B20)
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
    background = Color(0xFF151218),
    onBackground = Color(0xFFE7E0E8),
    surface = Color(0xFF151218),
    onSurface = Color(0xFFE7E0E8)
)

fun getPaletteColorScheme(palette: AppPalette, darkTheme: Boolean): ColorScheme = when (palette) {
    AppPalette.EMERALD -> if (darkTheme) EmeraldDark else EmeraldLight
    AppPalette.OCEAN -> if (darkTheme) OceanDark else OceanLight
    AppPalette.CRIMSON -> if (darkTheme) CrimsonDark else CrimsonLight
    AppPalette.AMETHYST -> if (darkTheme) AmethystDark else AmethystLight
}
