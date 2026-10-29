package com.prajwalprince94.expensetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.prajwalprince94.expensetracker.ui.model.AppPreferences
import com.prajwalprince94.expensetracker.ui.model.ThemeMode

@Composable
fun ExpenseTrackerTheme(
    preferences: AppPreferences = AppPreferences(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (preferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val baseScheme = if (preferences.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val dyn = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dyn.expressiveSurfaces(darkTheme)
    } else {
        val paletteScheme = getPaletteColorScheme(preferences.palette, darkTheme)
        paletteScheme.expressiveSurfaces(darkTheme)
    }

    val finalColorScheme = baseScheme
        .applyStyle(preferences.colorStyle)
        .let { if (preferences.amoled && darkTheme) it.applyAmoled() else it }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides finalColorScheme.onBackground
        ) {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = finalColorScheme.background,
                contentColor = finalColorScheme.onBackground,
                content = content
            )
        }
    }
}
