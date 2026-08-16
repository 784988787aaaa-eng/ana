package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MizanLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    secondary = CoralAccent,
    onSecondary = Color.White,
    tertiary = SoftGreen,
    error = SoftRed,
    background = IvoryBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFF7F9FC),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLight
)

private val MizanDarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    secondary = CoralDark,
    onSecondary = Color.White,
    tertiary = Color(0xFF34D399),
    error = Color(0xFFFF5252),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF211E35), // Beautiful slightly lighter violet-charcoal for deep theme cohesion
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = BorderDark
)

@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = 280, easing = FastOutSlowInEasing)

    val primary = animateColorAsState(targetColorScheme.primary, animationSpec, label = "primary").value
    val onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec, label = "onPrimary").value
    val primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec, label = "primaryContainer").value
    val onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec, label = "onPrimaryContainer").value
    val secondary = animateColorAsState(targetColorScheme.secondary, animationSpec, label = "secondary").value
    val onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec, label = "onSecondary").value
    val secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, animationSpec, label = "secondaryContainer").value
    val onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, animationSpec, label = "onSecondaryContainer").value
    val tertiary = animateColorAsState(targetColorScheme.tertiary, animationSpec, label = "tertiary").value
    val onTertiary = animateColorAsState(targetColorScheme.onTertiary, animationSpec, label = "onTertiary").value
    val tertiaryContainer = animateColorAsState(targetColorScheme.tertiaryContainer, animationSpec, label = "tertiaryContainer").value
    val onTertiaryContainer = animateColorAsState(targetColorScheme.onTertiaryContainer, animationSpec, label = "onTertiaryContainer").value
    val error = animateColorAsState(targetColorScheme.error, animationSpec, label = "error").value
    val onError = animateColorAsState(targetColorScheme.onError, animationSpec, label = "onError").value
    val errorContainer = animateColorAsState(targetColorScheme.errorContainer, animationSpec, label = "errorContainer").value
    val onErrorContainer = animateColorAsState(targetColorScheme.onErrorContainer, animationSpec, label = "onErrorContainer").value
    val background = animateColorAsState(targetColorScheme.background, animationSpec, label = "background").value
    val onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec, label = "onBackground").value
    val surface = animateColorAsState(targetColorScheme.surface, animationSpec, label = "surface").value
    val onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec, label = "onSurface").value
    val surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec, label = "surfaceVariant").value
    val onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec, label = "onSurfaceVariant").value
    val outline = animateColorAsState(targetColorScheme.outline, animationSpec, label = "outline").value
    val outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, animationSpec, label = "outlineVariant").value
    val scrim = animateColorAsState(targetColorScheme.scrim, animationSpec, label = "scrim").value
    val inverseSurface = animateColorAsState(targetColorScheme.inverseSurface, animationSpec, label = "inverseSurface").value
    val inverseOnSurface = animateColorAsState(targetColorScheme.inverseOnSurface, animationSpec, label = "inverseOnSurface").value
    val inversePrimary = animateColorAsState(targetColorScheme.inversePrimary, animationSpec, label = "inversePrimary").value

    return targetColorScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary
    )
}

@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val rawColorScheme = if (darkTheme) MizanDarkColorScheme else MizanLightColorScheme
    val animatedColorScheme = animateColorScheme(rawColorScheme)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MizanTheme(darkTheme = darkTheme, content = content)
}
