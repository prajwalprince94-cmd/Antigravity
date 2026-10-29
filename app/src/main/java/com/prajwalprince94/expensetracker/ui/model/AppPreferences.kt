package com.prajwalprince94.expensetracker.ui.model

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorStyle {
    TONAL_SPOT,
    VIBRANT,
    EXPRESSIVE,
    MONOCHROME
}

enum class AppPalette(val displayName: String) {
    EMERALD("Emerald"),
    OCEAN("Ocean"),
    CRIMSON("Crimson"),
    AMETHYST("Amethyst")
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val amoled: Boolean = false,
    val colorStyle: ColorStyle = ColorStyle.EXPRESSIVE,
    val palette: AppPalette = AppPalette.EMERALD
)
