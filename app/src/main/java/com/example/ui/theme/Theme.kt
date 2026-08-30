package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Mizan Light Color Scheme - Standardized M3 Roles
private val MizanLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = TokenWhite,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    inversePrimary = EmeraldLight,
    secondary = CoralAccent,
    onSecondary = TokenWhite,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = CreditGreen,
    onTertiary = TokenWhite,
    tertiaryContainer = CreditContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = DebtRed,
    onError = TokenWhite,
    errorContainer = DebtContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = IvoryBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = EmeraldPrimary,
    inverseSurface = DarkSurface,
    inverseOnSurface = TextPrimaryDark,
    outline = BorderLight,
    outlineVariant = OutlineVariantLight,
    scrim = TokenScrim,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight
)

// Mizan Dark Color Scheme - High-Contrast Distinguishable Surfaces
private val MizanDarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = TokenWhite,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = EmeraldPrimary,
    secondary = CoralDark,
    onSecondary = TokenWhite,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = CreditGreenDark,
    onTertiary = TokenWhite,
    tertiaryContainer = CreditContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = DebtRedDark,
    onError = TokenWhite,
    errorContainer = DebtContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = EmeraldDark,
    inverseSurface = LightSurface,
    inverseOnSurface = TextPrimaryLight,
    outline = BorderDark,
    outlineVariant = OutlineVariantDark,
    scrim = TokenScrim,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) MizanDarkColorScheme else MizanLightColorScheme
    val customRippleConfiguration = RippleConfiguration(
        color = if (darkTheme) RippleDark else RippleLight
    )

    CompositionLocalProvider(
        LocalRippleConfiguration provides customRippleConfiguration
    ) {
        MaterialTheme(
            colorScheme = targetColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MizanTheme(darkTheme = darkTheme, content = content)
}

