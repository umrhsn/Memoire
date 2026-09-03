package com.umrhsn.mmoire.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = MemoirePrimaryDark,
    onPrimary = Color(0xFF000033),
    primaryContainer = MemoirePrimaryContainerDark,
    onPrimaryContainer = Color(0xFFE0E0FF),

    secondary = MemoireSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF003730),
    onSecondaryContainer = Color(0xFF6FFFD9),

    tertiary = MemoireAccent,
    onTertiary = Color.Black,

    background = BackgroundDark,
    onBackground = Color(0xFFE4E1E6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE4E1E6),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC4C6D0),

    error = MemoireError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MemoirePrimary,
    onPrimary = Color.White,
    primaryContainer = MemoirePrimaryContainer,
    onPrimaryContainer = Color(0xFF000066),

    secondary = MemoireSecondary,
    onSecondary = Color.White,
    secondaryContainer = MemoireSecondaryContainer,
    onSecondaryContainer = Color(0xFF003730),

    tertiary = MemoireAccent,
    onTertiary = Color.Black,

    background = BackgroundLight,
    onBackground = Color(0xFF1B1B1F),
    surface = SurfaceLight,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),

    error = MemoireError,
    onError = Color.White
)

@Composable
fun MemoireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
