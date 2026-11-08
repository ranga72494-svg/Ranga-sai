package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisDarkBackground,
    primaryContainer = JarvisDarkSurfaceVariant,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisGold,
    onSecondary = JarvisDarkBackground,
    secondaryContainer = JarvisDarkSurfaceVariant,
    onSecondaryContainer = JarvisGold,
    tertiary = JarvisEmerald,
    onTertiary = JarvisDarkBackground,
    background = JarvisDarkBackground,
    onBackground = TextPrimary,
    surface = JarvisDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = JarvisDarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = JarvisCardBorder,
    outlineVariant = JarvisDarkSurfaceVariant,
    error = JarvisCrimson,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = JarvisColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = JarvisDarkBackground.toArgb()
            window.navigationBarColor = JarvisDarkBackground.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
