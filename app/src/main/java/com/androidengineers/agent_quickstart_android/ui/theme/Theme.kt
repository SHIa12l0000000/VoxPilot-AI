package com.androidengineers.agent_quickstart_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VoxIndigo,
    onPrimary = Color.White,
    primaryContainer = VoxNavySurface,
    onPrimaryContainer = VoxTextPrimary,
    secondary = VoxCyan,
    onSecondary = VoxNavyDark,
    secondaryContainer = VoxNavyCard,
    onSecondaryContainer = VoxTextPrimary,
    tertiary = VoxIndigoLight,
    onTertiary = VoxNavyDark,
    tertiaryContainer = VoxNavySurface,
    onTertiaryContainer = VoxTextPrimary,
    background = VoxNavyDark,
    onBackground = VoxTextPrimary,
    surface = VoxNavyCard,
    onSurface = VoxTextPrimary,
    surfaceVariant = VoxNavySurface,
    onSurfaceVariant = VoxTextSecondary,
    surfaceContainer = VoxNavyCard,
    surfaceContainerHigh = VoxNavySurface,
    surfaceContainerHighest = VoxNavyBorder,
    outline = VoxNavyBorder,
    outlineVariant = VoxNavyBorder,
    error = VoxCoral,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = VoxIndigo,
    onPrimary = Color.White,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightTextPrimary,
    secondary = VoxCyan,
    onSecondary = Color.White,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightTextPrimary,
    tertiary = VoxIndigoLight,
    onTertiary = Color.White,
    tertiaryContainer = LightSurface,
    onTertiaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightCard,
    surfaceContainerHigh = LightSurface,
    surfaceContainerHighest = LightBorder,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = VoxCoral,
    onError = Color.White,
)

@Composable
fun VoxPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        content = content,
    )
}

// Backward-compatible alias for existing code/tests
@Composable
fun AgentquickstartandroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = VoxPilotTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
