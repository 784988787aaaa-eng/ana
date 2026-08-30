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
import androidx.compose.ui.graphics.Color

private val ThemeWhite = Color.White
private val ThemeDarkContent = Color(0xFF171522)

private val MizanLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = ThemeWhite,
    primaryContainer = Color(0xFFEDE9FF),
    onPrimaryContainer = Color(0xFF28155F),
    secondary = CoralAccent,
    onSecondary = ThemeWhite,
    secondaryContainer = Color(0xFFDDF3F7),
    onSecondaryContainer = Color(0xFF063542),
    tertiary = CreditGreen,
    onTertiary = ThemeWhite,
    tertiaryContainer = CreditContainerLight,
    onTertiaryContainer = Color(0xFF14532D),
    error = DebtRed,
    onError = ThemeWhite,
    errorContainer = DebtContainerLight,
    onErrorContainer = Color(0xFF7F1D1D),
    background = IvoryBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFF1EFF5),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFE9E5EF),
    surfaceContainer = LightSurface,
    surfaceContainerHigh = Color(0xFFF4F1F8),
    surfaceContainerLow = LightSurface
)

private val MizanDarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = ThemeWhite,
    primaryContainer = Color(0xFF2A2540),
    onPrimaryContainer = Color(0xFFE9E3F7),
    secondary = CoralDark,
    onSecondary = ThemeDarkContent,
    secondaryContainer = Color(0xFF1A3036),
    onSecondaryContainer = Color(0xFFD0E9EE),
    tertiary = CreditGreenDark,
    onTertiary = ThemeDarkContent,
    tertiaryContainer = CreditContainerDark,
    onTertiaryContainer = Color(0xFFB6E4C8),
    error = DebtRedDark,
    onError = ThemeDarkContent,
    errorContainer = DebtContainerDark,
    onErrorContainer = Color(0xFFF0C3C8),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF1D1A25),
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF2E2937),
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = Color(0xFF1E1B27),
    surfaceContainerLow = Color(0xFF121117)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) MizanDarkColorScheme else MizanLightColorScheme
    val customRippleConfiguration = RippleConfiguration(
        color = if (darkTheme) ThemeWhite.copy(alpha = 0.12f) else EmeraldPrimary.copy(alpha = 0.12f)
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
