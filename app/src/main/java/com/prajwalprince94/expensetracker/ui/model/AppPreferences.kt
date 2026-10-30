package com.prajwalprince94.expensetracker.ui.model

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorStyle(val displayName: String) {
    TONAL_SPOT("Tonal Spot"),
    VIBRANT("Vibrant"),
    EXPRESSIVE("Expressive"),
    MONOCHROME("Monochrome")
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
    val palette: AppPalette = AppPalette.EMERALD,
    val ambientAurora: Boolean = true,
    val reduceMotion: Boolean = false,
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = true,
    val appLockPinHash: String = "",
    val autoLockTimeoutSeconds: Int = 0, // 0: Immediately, 60: 1 min, 300: 5 min
    val recentsPrivacyBlur: Boolean = true,
    val baseCurrency: String = "INR",
    val supabaseSyncEnabled: Boolean = false,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val supabaseSandboxMode: Boolean = true,
    val supabaseConflictStrategy: String = "LATEST_TIMESTAMP",
    val lastSyncTimestampMillis: Long = 0L
)
