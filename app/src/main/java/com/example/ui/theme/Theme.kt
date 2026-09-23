package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = PureWhite,
    primaryContainer = Blue900,
    onPrimaryContainer = Blue100,
    secondary = Amber400,
    onSecondary = Navy900,
    secondaryContainer = Amber600,
    onSecondaryContainer = Amber50,
    tertiary = Emerald500,
    onTertiary = PureWhite,
    background = Navy900,
    onBackground = Slate50,
    surface = Navy800,
    onSurface = Slate50,
    surfaceVariant = Navy700,
    onSurfaceVariant = Slate300,
    outline = Navy600
)

private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = PureWhite,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Amber600,
    onSecondary = PureWhite,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber600,
    tertiary = Emerald600,
    onTertiary = PureWhite,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep branded high-contrast theme across all system modes
    dynamicColor: Boolean = false, // Keep branded navy & gold palette
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
