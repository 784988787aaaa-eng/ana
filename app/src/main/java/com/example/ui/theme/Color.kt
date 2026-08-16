package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mizan Al-Dar Color Palette - Styled with the premium Violet & Neon Cyan "الدفتر الذكي" Palette
val EmeraldPrimary = Color(0xFF4B36A2)      // Premium Glowing Violet/Purple (#4B36A2)
val EmeraldLight = Color(0xFF8C7CFF)        // Lighter Purple/Lavender for beautiful gradients (#8C7CFF)
val CoralAccent = Color(0xFF00B2FE)         // Neon Cyan / Electric Blue Accent (#00B2FE)

val EmeraldDark = Color(0xFF4B36A2)         // Rich Glowing Violet/Purple directly in dark mode
val CoralDark = Color(0xFF00B2FE)           // Rich Glowing Neon Cyan directly in dark mode
val IvoryBackground = Color(0xFFF0F3FC)     // Soft modern lavender-tinted background (Eye safe)

val SoftRed = Color(0xFFDC2626)            // Vibrant Deep Red (Expense / Debt)
val SoftGreen = Color(0xFF059669)          // Rich Emerald Green (Income / Owed)

// Financial semantic colors
val CreditGreen = SoftGreen
val DebtRed = SoftRed
val CreditGreenDark = Color(0xFF34D399)
val DebtRedDark = Color(0xFFFF5252)

// Financial Card / Container backgrounds and borders
val CreditContainerLight = Color(0xFFE8F5E9)
val CreditContainerDark = Color(0xFF14241B)
val CreditBorderLight = Color(0xFF81C784)
val CreditBorderDark = Color(0xFF2E7D32)

val DebtContainerLight = Color(0xFFFFEBEE)
val DebtContainerDark = Color(0xFF2D1A1A)
val DebtBorderLight = Color(0xFFE57373)
val DebtBorderDark = Color(0xFFC62828)

// Selection colors
val SelectionGreen = Color(0xFF10B981)
val SelectionGreenContainerLight = Color(0xFFE6F4EA)
val SelectionGreenContainerDark = Color(0xFF152D1F)

val DarkBackground = Color(0xFF0C0B14)     // Rich Premium Deep Indigo-Black
val DarkSurface = Color(0xFF161525)        // Rich Deep Violet-Charcoal Card surface
val LightSurface = Color(0xFFFFFFFF)       // Clean white card surface

val TextPrimaryDark = Color(0xFFF1F0F7)     // Bright Crisp White for deep indigo contrast
val TextSecondaryDark = Color(0xFF9E9BAC)   // Soft purple-slate secondary text
val TextPrimaryLight = Color(0xFF1E1A3E)    // Deep indigo-slate primary text
val TextSecondaryLight = Color(0xFF5C58A5)  // Muted purple-slate secondary text

val BorderDark = Color(0xFF24223B)          // Elegant Indigo-Slate Border
val BorderLight = Color(0xFFE2E8F0)         // Light border

// Modern Pre-allocated Static Gradients (Zero-allocation during recomposition)
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(EmeraldPrimary, EmeraldLight)
)

val CoralGradient = Brush.linearGradient(
    colors = listOf(CoralAccent, Color(0xFF0284C7))
)

val IncomeGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFF3FAF5), LightSurface)
)

val IncomeGradientDark = Brush.linearGradient(
    colors = listOf(CreditContainerDark, DarkSurface)
)

val ExpenseGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFFFF7F7), LightSurface)
)

val ExpenseGradientDark = Brush.linearGradient(
    colors = listOf(DebtContainerDark, DarkSurface)
)

val SelectedItemGradientLight = Brush.linearGradient(
    colors = listOf(SelectionGreenContainerLight, Color(0xFFD1FAE5))
)

val SelectedItemGradientDark = Brush.linearGradient(
    colors = listOf(SelectionGreenContainerDark, Color(0xFF121F17))
)

val NeonGreenCyanGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF))
)

val VioletHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4B36A2), Color(0xFF7C3AED), Color(0xFF8C7CFF))
)

val HeaderCardGradientDark = Brush.linearGradient(
    colors = listOf(Color(0xFF1E1B4B), Color(0xFF161525))
)

val HeaderCardGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFF5F3FF), Color(0xFFFFFFFF))
)

val GoldLicenseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
)

val WarningGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
)

val SplashSweepGradient = Brush.sweepGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF2563EB),
        0.5f to Color(0xFF10B981),
        1.0f to Color(0xFF2563EB)
    )
)

val SplashRadialGlow = Brush.radialGradient(
    colors = listOf(
        Color(0xFF2563EB).copy(alpha = 0.22f),
        Color(0xFF10B981).copy(alpha = 0.15f),
        Color.Transparent
    )
)

// Specific Functional Palette Colors
val WhatsAppGreen = Color(0xFF128C7E)
val WhatsAppLightGreen = Color(0xFF25D366)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberBg = Color(0xFFFFF8E1)
val WarningAmberBorder = Color(0xFFFFB300)
val WarningDarkRedText = Color(0xFFB71C1C)
val WarningOrangeButton = Color(0xFFE65100)
val LicenseGreenBg = Color(0xFFE8F5E9)
val LicenseGreenText = Color(0xFF2E7D32)
val LicenseBadgeGreenText = Color(0xFF1B5E20)
val InfoBlue = Color(0xFF3B82F6)
val InfoBlueBgLight = Color(0xFFEFF6FF)
val InfoBlueBgDark = Color(0xFF1E293B)
val InfoBlueTextLight = Color(0xFF1D4ED8)
val InfoBlueTextDark = Color(0xFF60A5FA)

// Avatar Pastel Palette
val AvatarPastelPalette = listOf(
    Color(0xFFFCA5A5), Color(0xFFFDBA74), Color(0xFFFDE047),
    Color(0xFF86EFAC), Color(0xFF93C5FD), Color(0xFFC4B5FD),
    Color(0xFFF472B6), Color(0xFF2DD4BF)
)


