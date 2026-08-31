package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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

@Composable
fun ColorScheme.animated(
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 280, easing = FastOutSlowInEasing)
): ColorScheme {
    return copy(
        primary = animateColorAsState(primary, animationSpec, label = "th_primary").value,
        onPrimary = animateColorAsState(onPrimary, animationSpec, label = "th_onPrimary").value,
        primaryContainer = animateColorAsState(primaryContainer, animationSpec, label = "th_primaryContainer").value,
        onPrimaryContainer = animateColorAsState(onPrimaryContainer, animationSpec, label = "th_onPrimaryContainer").value,
        inversePrimary = animateColorAsState(inversePrimary, animationSpec, label = "th_inversePrimary").value,
        secondary = animateColorAsState(secondary, animationSpec, label = "th_secondary").value,
        onSecondary = animateColorAsState(onSecondary, animationSpec, label = "th_onSecondary").value,
        secondaryContainer = animateColorAsState(secondaryContainer, animationSpec, label = "th_secondaryContainer").value,
        onSecondaryContainer = animateColorAsState(onSecondaryContainer, animationSpec, label = "th_onSecondaryContainer").value,
        tertiary = animateColorAsState(tertiary, animationSpec, label = "th_tertiary").value,
        onTertiary = animateColorAsState(onTertiary, animationSpec, label = "th_onTertiary").value,
        tertiaryContainer = animateColorAsState(tertiaryContainer, animationSpec, label = "th_tertiaryContainer").value,
        onTertiaryContainer = animateColorAsState(onTertiaryContainer, animationSpec, label = "th_onTertiaryContainer").value,
        background = animateColorAsState(background, animationSpec, label = "th_background").value,
        onBackground = animateColorAsState(onBackground, animationSpec, label = "th_onBackground").value,
        surface = animateColorAsState(surface, animationSpec, label = "th_surface").value,
        onSurface = animateColorAsState(onSurface, animationSpec, label = "th_onSurface").value,
        surfaceVariant = animateColorAsState(surfaceVariant, animationSpec, label = "th_surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(onSurfaceVariant, animationSpec, label = "th_onSurfaceVariant").value,
        surfaceTint = animateColorAsState(surfaceTint, animationSpec, label = "th_surfaceTint").value,
        inverseSurface = animateColorAsState(inverseSurface, animationSpec, label = "th_inverseSurface").value,
        inverseOnSurface = animateColorAsState(inverseOnSurface, animationSpec, label = "th_inverseOnSurface").value,
        error = animateColorAsState(error, animationSpec, label = "th_error").value,
        onError = animateColorAsState(onError, animationSpec, label = "th_onError").value,
        errorContainer = animateColorAsState(errorContainer, animationSpec, label = "th_errorContainer").value,
        onErrorContainer = animateColorAsState(onErrorContainer, animationSpec, label = "th_onErrorContainer").value,
        outline = animateColorAsState(outline, animationSpec, label = "th_outline").value,
        outlineVariant = animateColorAsState(outlineVariant, animationSpec, label = "th_outlineVariant").value,
        scrim = animateColorAsState(scrim, animationSpec, label = "th_scrim").value,
        surfaceBright = animateColorAsState(surfaceBright, animationSpec, label = "th_surfaceBright").value,
        surfaceDim = animateColorAsState(surfaceDim, animationSpec, label = "th_surfaceDim").value,
        surfaceContainer = animateColorAsState(surfaceContainer, animationSpec, label = "th_surfaceContainer").value,
        surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, animationSpec, label = "th_surfaceContainerHigh").value,
        surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, animationSpec, label = "th_surfaceContainerHighest").value,
        surfaceContainerLow = animateColorAsState(surfaceContainerLow, animationSpec, label = "th_surfaceContainerLow").value,
        surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, animationSpec, label = "th_surfaceContainerLowest").value,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColorScheme = if (darkTheme) MizanDarkColorScheme else MizanLightColorScheme
    val mizanColors = if (darkTheme) DarkMizanColors else LightMizanColors
    val animatedColorScheme = targetColorScheme.animated()

    val customRippleConfiguration = RippleConfiguration(
        color = mizanColors.ripple.copy(alpha = if (darkTheme) 0.15f else 0.12f)
    )

    CompositionLocalProvider(
        LocalMizanColors provides mizanColors,
        LocalRippleConfiguration provides customRippleConfiguration
    ) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
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
