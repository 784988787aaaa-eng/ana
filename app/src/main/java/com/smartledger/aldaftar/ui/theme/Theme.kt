package com.smartledger.aldaftar.ui.theme

import android.app.Activity
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MizanLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = LightMizanColors.onBrandPrimary,
    primaryContainer = BrandPrimaryContainerLight,
    onPrimaryContainer = BrandOnPrimaryContainerLight,
    secondary = BrandSecondary,
    onSecondary = LightMizanColors.onBrandSecondary,
    secondaryContainer = BrandSecondaryContainerLight,
    onSecondaryContainer = BrandOnSecondaryContainerLight,
    tertiary = CreditGreen,
    onTertiary = LightMizanColors.onCredit,
    tertiaryContainer = CreditContainerLight,
    onTertiaryContainer = LightMizanColors.onCreditContainer,
    error = DebtRed,
    onError = LightMizanColors.onError,
    errorContainer = DebtContainerLight,
    onErrorContainer = LightMizanColors.onDebtContainer,
    background = NeutralBackgroundLight,
    surface = NeutralSurfaceLight,
    surfaceVariant = NeutralSurfaceVariantLight,
    onBackground = NeutralTextPrimaryLight,
    onSurface = NeutralTextPrimaryLight,
    onSurfaceVariant = NeutralTextSecondaryLight,
    outline = NeutralBorderLight,
    outlineVariant = NeutralBorderVariantLight,
    surfaceContainer = NeutralSurfaceLight,
    surfaceContainerHigh = LightMizanColors.appSurfaceContainerHigh,
    surfaceContainerLow = NeutralBackgroundLight
)

private val MizanDarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = DarkMizanColors.onBrandPrimary,
    primaryContainer = BrandPrimaryContainerDark,
    onPrimaryContainer = BrandOnPrimaryContainerDark,
    secondary = BrandSecondaryDark,
    onSecondary = DarkMizanColors.onBrandSecondary,
    secondaryContainer = BrandSecondaryContainerDark,
    onSecondaryContainer = BrandOnSecondaryContainerDark,
    tertiary = CreditGreenDark,
    onTertiary = DarkMizanColors.onCredit,
    tertiaryContainer = CreditContainerDark,
    onTertiaryContainer = DarkMizanColors.onCreditContainer,
    error = DebtRedDark,
    onError = DarkMizanColors.onError,
    errorContainer = DebtContainerDark,
    onErrorContainer = DarkMizanColors.onDebtContainer,
    background = NeutralBackgroundDark,
    surface = NeutralSurfaceDark,
    surfaceVariant = NeutralSurfaceVariantDark,
    onBackground = NeutralTextPrimaryDark,
    onSurface = NeutralTextPrimaryDark,
    onSurfaceVariant = NeutralTextSecondaryDark,
    outline = NeutralBorderDark,
    outlineVariant = NeutralBorderVariantDark,
    surfaceContainer = NeutralSurfaceDark,
    surfaceContainerHigh = DarkMizanColors.appSurfaceContainerHigh,
    surfaceContainerLow = NeutralSurfaceContainerLowDark
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MizanDarkColorScheme else MizanLightColorScheme
    val mizanColors = if (darkTheme) DarkMizanColors else LightMizanColors

    val customRippleConfiguration = RippleConfiguration(
        color = mizanColors.ripple.copy(alpha = if (darkTheme) 0.15f else 0.12f)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalMizanColors provides mizanColors,
        LocalRippleConfiguration provides customRippleConfiguration
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
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
