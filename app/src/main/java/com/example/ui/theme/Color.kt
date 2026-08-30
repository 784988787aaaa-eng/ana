package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mizan Al-Dar Color Palette - Styled with the premium Violet & Neon Cyan "الدفتر الذكي" Palette
val EmeraldPrimary = Color(0xFF5B46B8)      // Premium violet tuned to the app icon, with reduced blue intensity
val EmeraldLight = Color(0xFF8F82E6)        // Soft lavender companion for accessible gradients
val CoralAccent = Color(0xFF247E9D)         // Filtered cyan-blue accent: calmer and less visually aggressive

// Dark-mode accents intentionally use lower luminance and lower saturation to avoid neon/glow fatigue.
val EmeraldDark = Color(0xFF6F63B4)      // Quiet violet aligned with the app icon without neon intensity
val CoralDark = Color(0xFF4B8290)        // Muted cyan-teal companion for dark mode
val IvoryBackground = Color(0xFFF8F7FB)     // Solid high-contrast background (#F8F9FA)

// Financial semantic colors - High Contrast Display Tokens
val SoftRed = Color(0xFFD32F2F)            // Debt (لنا / مدين) - Vivid Solid Red Light (#D32F2F)
val SoftGreen = Color(0xFF2E7D32)          // Credit (علينا / دائن) - Deep Emerald Green Light (#2E7D32)

val CreditGreen = Color(0xFF2E7D32)        // Credit Green Light (#2E7D32)
val DebtRed = Color(0xFFD32F2F)            // Debt Red Light (#D32F2F)
val CreditGreenDark = Color(0xFF4E9C68)    // Muted financial green for dark mode
val DebtRedDark = Color(0xFFD06B74)        // Muted financial red for dark mode

// Financial Card / Container backgrounds (Solid Safe Colors for replacement & low-contrast screens)
val CreditContainerLight = Color(0xFFF0FDF4) // خلفية بطاقة "علينا" الخضراء في الوضع النهاري (#F0FDF4)
val CreditContainerDark = Color(0xFF16281E)  // خلفية بطاقة "علينا" الخضراء في الوضع الليلي (#16281E)
val CreditBorderLight = Color(0xFFA7F3D0)
val CreditBorderDark = Color(0xFF1B4D2E)

val DebtContainerLight = Color(0xFFFDF2F2)   // خلفية بطاقة "لنا" الحمراء في الوضع النهاري (#FDF2F2)
val DebtContainerDark = Color(0xFF2C1A1D)    // خلفية بطاقة "لنا" الحمراء في الوضع الليلي (#2C1A1D)
val DebtBorderLight = Color(0xFFFECDD3)
val DebtBorderDark = Color(0xFF531A21)

// Selection colors
val SelectionGreen = Color(0xFF10B981)
val SelectionGreenContainerLight = Color(0xFFE6F4EA)
val SelectionGreenContainerDark = Color(0xFF152D1F)

val DarkBackground = Color(0xFF0E0D15)     // Deep violet-black background to reduce visual glare
val DarkSurface = Color(0xFF171522)        // Elevated violet-charcoal surface
val LightSurface = Color(0xFFFFFFFF)       // Clean light surface

val TextPrimaryDark = Color(0xFFE9E6EE)     // Soft near-white for comfortable dark-mode reading
val TextSecondaryDark = Color(0xFFA8A3B0)   // Calm secondary text for dark mode
val TextPrimaryLight = Color(0xFF211D2B)    // Deep dark crisp primary text light
val TextSecondaryLight = Color(0xFF5F586B)  // Clear legible secondary text light

val BorderDark = Color(0xFF312C3A)          // Quiet, low-glare dark border
val BorderLight = Color(0xFFE1DDE9)         // Clean light border

// Modern Pre-allocated Static Gradients (Zero-allocation during recomposition)
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(EmeraldPrimary, EmeraldLight)
)

val CoralGradient = Brush.linearGradient(
    colors = listOf(CoralAccent, Color(0xFF1D607B))
)

val IncomeGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFF0FDF4), LightSurface)
)

val IncomeGradientDark = Brush.linearGradient(
    colors = listOf(CreditContainerDark, DarkSurface)
)

val ExpenseGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFFDF2F2), LightSurface)
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
    colors = listOf(Color(0xFF32B77E), Color(0xFF2B9FC2))
)

val VioletHeroGradient = Brush.linearGradient(
    colors = listOf(EmeraldPrimary, Color(0xFF7258C9), EmeraldLight)
)

val HeaderCardGradientDark = Brush.linearGradient(
    colors = listOf(DarkSurface, Color(0xFF1D1A25))
)

val HeaderCardGradientLight = Brush.linearGradient(
    colors = listOf(LightSurface, IvoryBackground)
)

val GoldLicenseGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
)

val WarningGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
)

val SplashSweepGradient = Brush.sweepGradient(
    colorStops = arrayOf(
        0.0f to EmeraldPrimary,
        0.5f to CoralAccent,
        1.0f to EmeraldPrimary
    )
)

val SplashRadialGlow = Brush.radialGradient(
    colors = listOf(
        EmeraldPrimary.copy(alpha = 0.20f),
        CoralAccent.copy(alpha = 0.13f),
        Color.Transparent
    )
)

// Specific Functional Palette Colors
val WhatsAppGreen = Color(0xFF128C7E)
val WhatsAppLightGreen = Color(0xFF25D366)
val WhatsAppDarkGreen = Color(0xFF3D805C)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberDark = Color(0xFFC9953D)
val WarningAmberBg = Color(0xFFFFF8E1)
val WarningAmberBgDark = Color(0xFF332A1A)
val WarningAmberBorder = Color(0xFFFFB300)
val WarningAmberBorderDark = Color(0xFF70582B)
val WarningDarkRedText = Color(0xFFB71C1C)
val WarningOrangeButton = Color(0xFFB96F36)
val LicenseGreenBg = Color(0xFFE8F5E9)
val LicenseGreenText = Color(0xFF2E7D32)
val LicenseBadgeGreenText = Color(0xFF1B5E20)
val InfoBlue = Color(0xFF2F83A1)
val InfoBlueBgLight = Color(0xFFEAF5F7)
val InfoBlueBgDark = Color(0xFF1B2D34)
val InfoBlueTextLight = Color(0xFF1D607B)
val InfoBlueTextDark = Color(0xFF68A8B7)

// Slate & UI Accents
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// Status & Action tokens
val NeonGreen = Color(0xFF32A978)
val NeonCyan = Color(0xFF247E9D)
val IndigoAccent = Color(0xFF5F579E)
val PurpleAccent = Color(0xFF775CCB)
val SoftLavender = Color(0xFFA79DD0)
val WarningRed = Color(0xFFE53935)
val WarningRedBorder = Color(0xFF9E5B63)
val WarningRedBorderLight = Color(0xFFD93025)
val LightRedTint = Color(0xFFD99AA0)

// Alert Gold (Yellow/Amber badge tokens)
val AlertGoldBgDark = Color(0xFF451A03)
val AlertGoldBorderDark = Color(0xFF92400E)
val AlertGoldTextDark = Color(0xFFD8AA4B)
val AlertGoldTextLight = Color(0xFFB45309)

// Success Green badge tokens
val SuccessGreenBgDark = Color(0xFF064E3B)
val SuccessGreenBgLight = Color(0xFFE6F4EA)
val SuccessGreenBorderDark = Color(0xFF3D8558)
val SuccessGreenBorderLight = Color(0xFF137333)
val MutedTextDark = Color(0xFF9AA0A6)
val MutedTextLight = Color(0xFF5F6368)
val DarkNeutralTrack = Color(0xFF27242D)
val LightNeutralTrack = Color(0xFFEEEEEE)

// Chip Filter Color Tokens (Solid High-Contrast Safe Values)
val ChipRedBgDarkSelected = Color(0xFF3B2025)
val ChipRedBgDarkUnselected = Color(0xFF2C1A1D)
val ChipRedBgLightSelected = Color(0xFFFCE8E8)
val ChipRedBgLightUnselected = Color(0xFFFDF2F2)

val ChipRedBorderDarkSelected = DebtRedDark
val ChipRedBorderDarkUnselected = Color(0xFF531A21)
val ChipRedBorderLightSelected = Color(0xFFD32F2F)
val ChipRedBorderLightUnselected = Color(0xFFFECDD3)

val ChipRedTextDark = DebtRedDark
val ChipRedTextLight = Color(0xFFD32F2F)
val ChipRedHeaderDark = Color(0xFFD99AA0)
val ChipRedHeaderLight = Color(0xFFB71C1C)

val ChipGreenBgDarkSelected = Color(0xFF1D3528)
val ChipGreenBgDarkUnselected = Color(0xFF16281E)
val ChipGreenBgLightSelected = Color(0xFFE6F9ED)
val ChipGreenBgLightUnselected = Color(0xFFF0FDF4)

val ChipGreenBorderDarkSelected = CreditGreenDark
val ChipGreenBorderDarkUnselected = Color(0xFF1B4D2E)
val ChipGreenBorderLightSelected = Color(0xFF2E7D32)
val ChipGreenBorderLightUnselected = Color(0xFFA7F3D0)

val ChipGreenTextDark = CreditGreenDark
val ChipGreenTextLight = Color(0xFF2E7D32)
val ChipGreenHeaderDark = Color(0xFFA7D4B6)
val ChipGreenHeaderLight = Color(0xFF1B5E20)

// Financial Semantic Color Resolvers
fun financialCreditColor(isDark: Boolean): Color = if (isDark) CreditGreenDark else CreditGreen
fun financialDebtColor(isDark: Boolean): Color = if (isDark) DebtRedDark else DebtRed
fun financialCreditBg(isDark: Boolean): Color = if (isDark) CreditContainerDark else CreditContainerLight
fun financialDebtBg(isDark: Boolean): Color = if (isDark) DebtContainerDark else DebtContainerLight
fun financialCreditBorder(isDark: Boolean): Color = if (isDark) CreditBorderDark else CreditBorderLight
fun financialDebtBorder(isDark: Boolean): Color = if (isDark) DebtBorderDark else DebtBorderLight

fun whatsappColor(isDark: Boolean): Color = if (isDark) WhatsAppDarkGreen else WhatsAppLightGreen
fun warningColor(isDark: Boolean): Color = if (isDark) WarningAmberDark else WarningAmber
fun warningBg(isDark: Boolean): Color = if (isDark) WarningAmberBgDark else WarningAmberBg
fun warningBorder(isDark: Boolean): Color = if (isDark) WarningAmberBorderDark else WarningAmberBorder
fun shareSecondaryColor(isDark: Boolean): Color = if (isDark) IndigoAccent else IndigoAccent

// Category Palette Tokens for Domain Utils
object CategoryPalette {
    val AMBER_DARK = Color(0xFF451A03)
    val AMBER_LIGHT = Color(0xFFFEF3C7)
    val PINK_DARK = Color(0xFF4D1222)
    val PINK_LIGHT = Color(0xFFFCE7F3)
    val GRAY_LIGHT_DARK = Color(0xFF262626)
    val GRAY_LIGHT_LIGHT = Color(0xFFEFEFEF)
    val RED_SOFT_DARK = Color(0xFF3E1F1F)
    val RED_SOFT_LIGHT = Color(0xFFFEE2E2)
    val YELLOW_DARK = Color(0xFF3F3701)
    val YELLOW_LIGHT = Color(0xFFFEF9C3)
    val BLUE_SOFT_DARK = Color(0xFF173039)
    val BLUE_SOFT_LIGHT = Color(0xFFDCEFF3)
    val SKY_DARK = Color(0xFF0E4250)
    val SKY_LIGHT = Color(0xFFE3F4F7)
    val PURPLE_DARK = Color(0xFF3B0764)
    val PURPLE_LIGHT = Color(0xFFF3E8FF)
    val EMERALD_SOFT_DARK = Color(0xFF064E3B)
    val EMERALD_SOFT_LIGHT = Color(0xFFD1FAE5)
    val GREEN_FIFTY_DARK = Color(0xFF022C22)
    val GREEN_FIFTY_LIGHT = Color(0xFFECFDF5)
    val SLATE_DEFAULT_DARK = Slate900
    val SLATE_DEFAULT_LIGHT = Slate100
}

// Avatar Pastel Palette
val AvatarPastelPalette = listOf(
    Color(0xFFFCA5A5), Color(0xFFFDBA74), Color(0xFFFDE047),
    Color(0xFF86EFAC), Color(0xFF93C5FD), Color(0xFFC4B5FD),
    Color(0xFFF472B6), Color(0xFF2DD4BF)
)

val AvatarDarkPalette = listOf(
    Color(0xFF7F3D4A), Color(0xFF7A4D2A), Color(0xFF6B5D1A),
    Color(0xFF2F6B45), Color(0xFF315A7A), Color(0xFF55407A),
    Color(0xFF7A315B), Color(0xFF236D66)
)



