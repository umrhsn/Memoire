package com.umrhsn.mmoire.ui.theme

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.umrhsn.mmoire.models.AppColorTheme
import com.umrhsn.mmoire.models.AppTheme

@Composable
fun MemoireTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    appColorTheme: AppColorTheme = AppColorTheme.DEFAULT,
    isTintEnabled: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val context = LocalContext.current

    // Smoothly update system bars without activity recreation
    LaunchedEffect(darkTheme) {
        (context as? ComponentActivity)?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.argb(0xe6, 0xff, 0xff, 0xff),
                android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b),
            ) { darkTheme }
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> getColorScheme(darkTheme, appColorTheme, isTintEnabled)
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

private fun getColorScheme(
    darkTheme: Boolean,
    colorTheme: AppColorTheme,
    isTintEnabled: Boolean
): ColorScheme {
    return if (darkTheme) {
        val primary = when (colorTheme) {
            AppColorTheme.DEFAULT -> MemoirePrimaryDark
            AppColorTheme.RED -> RedPrimaryDark
            AppColorTheme.BLUE -> BluePrimaryDark
            AppColorTheme.GREEN -> GreenPrimaryDark
            AppColorTheme.ORANGE -> OrangePrimaryDark
        }
        val primaryContainer = when (colorTheme) {
            AppColorTheme.DEFAULT -> MemoirePrimaryContainerDark
            AppColorTheme.RED -> RedPrimaryContainerDark
            AppColorTheme.BLUE -> BluePrimaryContainerDark
            AppColorTheme.GREEN -> GreenPrimaryContainerDark
            AppColorTheme.ORANGE -> OrangePrimaryContainerDark
        }

        // Subtle background tint logic
        val background = if (isTintEnabled) {
            primary.copy(alpha = 0.15f).compositeOver(BackgroundDark)
        } else BackgroundDark

        val surface = if (isTintEnabled) {
            primary.copy(alpha = 0.22f).compositeOver(SurfaceDark)
        } else SurfaceDark

        darkColorScheme(
            primary = primary,
            onPrimary = Color(0xFF000033),
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color(0xFFE0E0FF),
            secondary = MemoireSecondary,
            onSecondary = Color.Black,
            background = background,
            surface = surface,
            surfaceVariant = SurfaceVariantDark,
            error = MemoireError,
            onError = Color.White
        )
    } else {
        val primary = when (colorTheme) {
            AppColorTheme.DEFAULT -> MemoirePrimary
            AppColorTheme.RED -> RedPrimary
            AppColorTheme.BLUE -> BluePrimary
            AppColorTheme.GREEN -> GreenPrimary
            AppColorTheme.ORANGE -> OrangePrimary
        }
        val primaryContainer = when (colorTheme) {
            AppColorTheme.DEFAULT -> MemoirePrimaryContainer
            AppColorTheme.RED -> RedPrimaryContainer
            AppColorTheme.BLUE -> BluePrimaryContainer
            AppColorTheme.GREEN -> GreenPrimaryContainer
            AppColorTheme.ORANGE -> OrangePrimaryContainer
        }

        val background = if (isTintEnabled) {
            primary.copy(alpha = 0.10f).compositeOver(BackgroundLight)
        } else BackgroundLight

        val surface = if (isTintEnabled) {
            primary.copy(alpha = 0.16f).compositeOver(SurfaceLight)
        } else SurfaceLight

        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = Color(0xFF000066),
            secondary = MemoireSecondary,
            onSecondary = Color.White,
            background = background,
            surface = surface,
            error = MemoireError,
            onError = Color.White
        )
    }
}

// Helper to composite colors
private fun Color.compositeOver(background: Color): Color {
    val a = this.alpha
    val r = this.red * a + background.red * (1 - a)
    val g = this.green * a + background.green * (1 - a)
    val b = this.blue * a + background.blue * (1 - a)
    return Color(r, g, b, 1f)
}
