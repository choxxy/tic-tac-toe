package com.jna.tictactoe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ZenithPrimary,
    onPrimary = ZenithOnPrimary,
    primaryContainer = ZenithPrimaryContainer,
    onPrimaryContainer = ZenithOnPrimaryContainer,
    inversePrimary = ZenithInversePrimary,
    secondary = ZenithSecondary,
    onSecondary = ZenithOnSecondary,
    secondaryContainer = ZenithSecondaryContainer,
    onSecondaryContainer = ZenithOnSecondaryContainer,
    tertiary = ZenithTertiary,
    onTertiary = ZenithOnTertiary,
    tertiaryContainer = ZenithTertiaryContainer,
    onTertiaryContainer = ZenithOnTertiaryContainer,
    error = ZenithError,
    onError = ZenithOnError,
    errorContainer = ZenithErrorContainer,
    onErrorContainer = ZenithOnErrorContainer,
    background = ZenithBackground,
    onBackground = ZenithOnBackground,
    surface = ZenithSurface,
    onSurface = ZenithOnSurface,
    surfaceVariant = ZenithSurfaceVariant,
    onSurfaceVariant = ZenithOnSurfaceVariant,
    inverseSurface = ZenithInverseSurface,
    inverseOnSurface = ZenithInverseOnSurface,
    outline = ZenithOutline,
    outlineVariant = ZenithOutlineVariant,
    surfaceTint = ZenithSurfaceTint,
    surfaceContainerLowest = ZenithSurfaceContainerLowest,
    surfaceContainerLow = ZenithSurfaceContainerLow,
    surfaceContainer = ZenithSurfaceContainer,
    surfaceContainerHigh = ZenithSurfaceContainerHigh,
    surfaceContainerHighest = ZenithSurfaceContainerHighest,
)

// Dark scheme: approximate inverses — not fully specified in DESIGN.md
private val DarkColorScheme = darkColorScheme(
    primary = ZenithInversePrimary,
    onPrimary = Color(0xFF002D6B),
    primaryContainer = Color(0xFF00419B),
    onPrimaryContainer = ZenithPrimaryContainer,
    secondary = Color(0xFFFFB3AC),
    onSecondary = Color(0xFF680002),
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = Color(0xFFFFDAD5),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFBCC5D6),
    inverseSurface = ZenithSurface,
    inverseOnSurface = ZenithOnBackground,
    inversePrimary = ZenithPrimary,
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF272A2F),
    surfaceContainerHighest = Color(0xFF32353A),
)

@Composable
fun TictactoeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
