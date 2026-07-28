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
    primary = DarkPurplePrimary,
    secondary = PurpleContainer,
    tertiary = MinimalSurfaceContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = PurpleOnContainer,
    onBackground = MinimalBackground,
    onSurface = MinimalBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MinimalSurfaceContainer
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleOnContainer,
    secondary = PurpleContainer,
    onSecondary = PurpleOnContainer,
    tertiary = MinimalSurfaceContainer,
    background = MinimalBackground,
    surface = MinimalSurface,
    onBackground = TextMain,
    onSurface = TextMain,
    surfaceVariant = MinimalSurfaceVariant,
    onSurfaceVariant = TextMuted
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to ensure Clean Minimalism design is consistently displayed
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

