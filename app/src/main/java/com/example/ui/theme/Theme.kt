package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ClaroRedPrimary,
    onPrimary = Color.White,
    primaryContainer = ClaroRedLight,
    onPrimaryContainer = ClaroRedDark,
    secondary = ClaroRedAccent,
    onSecondary = Color.White,
    secondaryContainer = ClaroRedPale,
    onSecondaryContainer = ClaroRedDark,
    tertiary = CorporateOrangeAccent,
    onTertiary = Color.White,
    tertiaryContainer = CorporateOrangeLight,
    onTertiaryContainer = CorporateOrangeAccent,
    background = CorporateBackground,
    onBackground = CorporateTextPrimary,
    surface = CorporateSurface,
    onSurface = CorporateTextPrimary,
    surfaceVariant = CorporateSurfaceVariant,
    onSurfaceVariant = CorporateTextSecondary,
    outline = CorporateBorder,
    error = StatusDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = ClaroRedLight,
    onPrimary = ClaroRedDark,
    primaryContainer = ClaroRedDark,
    onPrimaryContainer = ClaroRedLight,
    secondary = ClaroRedAccent,
    onSecondary = Color.White,
    background = CorporateTextPrimary,
    onBackground = CorporateBackground,
    surface = Color(0xFF1E293B),
    onSurface = CorporateBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep clean light Claro corporate theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
