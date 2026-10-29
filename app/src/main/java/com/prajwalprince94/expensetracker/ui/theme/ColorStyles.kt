package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.prajwalprince94.expensetracker.ui.model.ColorStyle

/*
 * ColorBlendr-style palette treatments inspired by NikhilKain/stride.
 * Saturation/hue remaps for accent roles, monochrome desaturation,
 * expressive surface styling, and pure-black AMOLED elevation ladders.
 */

private fun Color.mapHsv(block: (FloatArray) -> Unit): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    block(hsv)
    hsv[1] = hsv[1].coerceIn(0f, 1f)
    hsv[2] = hsv[2].coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor((alpha * 255).toInt(), hsv))
}

private fun Color.saturate(factor: Float) = mapHsv { it[1] = it[1] * factor }
private fun Color.hueShift(degrees: Float) = mapHsv { it[0] = (it[0] + degrees + 360f) % 360f }

private fun ColorScheme.mapAccents(transform: (Color) -> Color): ColorScheme = copy(
    primary = transform(primary),
    onPrimary = transform(onPrimary),
    primaryContainer = transform(primaryContainer),
    onPrimaryContainer = transform(onPrimaryContainer),
    secondary = transform(secondary),
    onSecondary = transform(onSecondary),
    secondaryContainer = transform(secondaryContainer),
    onSecondaryContainer = transform(onSecondaryContainer),
    tertiary = transform(tertiary),
    onTertiary = transform(onTertiary),
    tertiaryContainer = transform(tertiaryContainer),
    onTertiaryContainer = transform(onTertiaryContainer),
    inversePrimary = transform(inversePrimary)
)

private fun ColorScheme.mapTertiary(transform: (Color) -> Color): ColorScheme = copy(
    tertiary = transform(tertiary),
    tertiaryContainer = transform(tertiaryContainer)
)

private fun ColorScheme.mapAll(transform: (Color) -> Color): ColorScheme =
    mapAccents(transform).copy(
        background = transform(background),
        onBackground = transform(onBackground),
        surface = transform(surface),
        onSurface = transform(onSurface),
        surfaceVariant = transform(surfaceVariant),
        onSurfaceVariant = transform(onSurfaceVariant),
        surfaceContainerLowest = transform(surfaceContainerLowest),
        surfaceContainerLow = transform(surfaceContainerLow),
        surfaceContainer = transform(surfaceContainer),
        surfaceContainerHigh = transform(surfaceContainerHigh),
        surfaceContainerHighest = transform(surfaceContainerHighest),
        outline = transform(outline),
        outlineVariant = transform(outlineVariant),
        inverseSurface = transform(inverseSurface),
        inverseOnSurface = transform(inverseOnSurface)
    )

fun ColorScheme.applyStyle(style: ColorStyle): ColorScheme = when (style) {
    ColorStyle.TONAL_SPOT -> this
    ColorStyle.MONOCHROME -> mapAll { it.saturate(0f) }
    ColorStyle.VIBRANT -> mapAccents { it.saturate(1.4f) }
    ColorStyle.EXPRESSIVE -> mapAccents { it.saturate(1.2f) }
        .mapTertiary { it.hueShift(-40f) }
        .copy(
            secondary = secondary.hueShift(25f).saturate(1.2f),
            secondaryContainer = secondaryContainer.hueShift(25f).saturate(1.15f)
        )
}

/** Pure-black window with obsidian surface ladder for AMOLED displays */
fun ColorScheme.applyAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF090D10),
    surfaceContainer = Color(0xFF12161A),
    surfaceContainerHigh = Color(0xFF1B2127),
    surfaceContainerHighest = Color(0xFF252D35),
    surfaceVariant = Color(0xFF1E252D),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8)
)

/** Stride & Zenith surface elevation hierarchy */
fun ColorScheme.expressiveSurfaces(dark: Boolean): ColorScheme =
    if (dark) copy(
        background = Color(0xFF0B0F12),
        surface = Color(0xFF0B0F12),
        surfaceContainerLowest = Color(0xFF070A0D),
        surfaceContainerLow = Color(0xFF101418),
        surfaceContainer = Color(0xFF161B20),
        surfaceContainerHigh = Color(0xFF212830),
        surfaceContainerHighest = Color(0xFF2A3440),
        onSurface = Color(0xFFF8FAFC),
        onSurfaceVariant = Color(0xFF94A3B8),
        outlineVariant = Color(0xFF2D3748)
    ) else copy(
        background = Color(0xFFF4F6F8),
        surface = Color(0xFFF4F6F8),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFE8ECEF),
        surfaceContainerHighest = Color(0xFFDEE3E7),
        onSurface = Color(0xFF0F172A),
        onSurfaceVariant = Color(0xFF475569),
        outlineVariant = Color(0xFFE2E8F0)
    )
