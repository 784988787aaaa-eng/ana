package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =====================================================================================
// نظام الألوان المالي الفاخر - الدفتر الذكي (Smart Ledger Executive Design System)
// تصميم احترافي عالمي مستوحى من كبرى التطبيقات المالية (Apple Wallet, Revolut, Linear)
// =====================================================================================

// 1. التوكنز الأساسية المطلقة (Absolute Base Tokens)
val TokenWhite = Color(0xFFFFFFFF)
val TokenBlack = Color(0xFF000000)
val TokenTransparent = Color(0x00000000)
val TokenScrim = Color(0xFF000000)

// 2. هوية العلامة الأساسية - النيلي الملكي والياقوت المالي (Royal Indigo & Electric Sapphire)
val EmeraldPrimary = Color(0xFF4338CA)      // Royal Indigo 700 - لون قيادي فاخر وموثوق
val EmeraldLight = Color(0xFF6366F1)        // Indigo 500 - تدرج إشعاعي حديث
val CoralAccent = Color(0xFF0284C7)         // Sky Blue 600 - لمسة إلكترونية حيوية

// Dark-mode Brand Accents - مضاءة ومصممة بدقة لشاشات OLED
val EmeraldDark = Color(0xFF818CF8)         // Indigo 400 - فائق الوضوح والراحة البصرية في الظلام
val CoralDark = Color(0xFF38BDF8)           // Sky 400 - تباين ناعم متناسق
val IvoryBackground = Color(0xFFF8FAFC)     // Slate 50 - مساحة بيضاء كريستالية نقية وعصرية

// 3. درجات الحاويات والأسطح - الوضع النهاري (Light Mode Architecture)
val PrimaryContainerLight = Color(0xFFEEF2FF)     // Indigo 50
val OnPrimaryContainerLight = Color(0xFF312E81)   // Indigo 900
val SecondaryContainerLight = Color(0xFFE0F2FE)   // Sky 50
val OnSecondaryContainerLight = Color(0xFF0369A1) // Sky 700
val OnTertiaryContainerLight = Color(0xFF064E3B)  // Emerald 900
val OnErrorContainerLight = Color(0xFF881337)     // Rose 900
val SurfaceVariantLight = Color(0xFFF1F5F9)       // Slate 100
val OutlineVariantLight = Color(0xFFE2E8F0)       // Slate 200 - حدود دقيقة جداً
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF8FAFC)
val SurfaceContainerLight = Color(0xFFFFFFFF)     // بطاقات بيضاء ناصعة وفخمة
val SurfaceContainerHighLight = Color(0xFFF1F5F9)
val SurfaceContainerHighestLight = Color(0xFFE2E8F0)
val SurfaceDimLight = Color(0xFFE2E8F0)
val SurfaceBrightLight = Color(0xFFFFFFFF)

// 4. درجات الحاويات والأسطح - الوضع الليلي الفاخر (Midnight Obsidian & Dark Slate)
val PrimaryContainerDark = Color(0xFF1E1B4B)      // Deep Indigo Charcoal
val OnPrimaryContainerDark = Color(0xFFE0E7FF)
val SecondaryContainerDark = Color(0xFF0C4A6E)
val OnSecondaryContainerDark = Color(0xFFBAE6FD)
val OnTertiaryContainerDark = Color(0xFFA7F3D0)
val OnErrorContainerDark = Color(0xFFFECDD3)
val SurfaceVariantDark = Color(0xFF1E293B)        // Slate 800
val OutlineVariantDark = Color(0xFF334155)        // Slate 700
val SurfaceContainerLowestDark = Color(0xFF070A10)
val SurfaceContainerLowDark = Color(0xFF0B101B)
val SurfaceContainerDark = Color(0xFF111827)       // Gray 900 - سطح البطاقات الداكنة
val SurfaceContainerHighDark = Color(0xFF1E293B)   // Slate 800
val SurfaceContainerHighestDark = Color(0xFF334155)// Slate 700
val SurfaceDimDark = Color(0xFF0B0F17)
val SurfaceBrightDark = Color(0xFF1E293B)

// 5. تأثيرات التموج اللمسي (Ripple Tokens)
val RippleLight = Color(0x1A4338CA)
val RippleDark = Color(0x26818CF8)

// 6. الألوان المالية الدلالية المعتمدة (Financial Semantic Tokens - Credit & Debt)
// الأخضر المالي (علينا / إيراد / دائن)
val SoftGreen = Color(0xFF059669)                 // Emerald 600 - ناصع وواضح
val CreditGreen = Color(0xFF059669)
val CreditGreenDark = Color(0xFF34D399)             // Emerald 400 - متألق في الوضع الداكن
val CreditContainerLight = Color(0xFFECFDF5)        // Emerald 50
val CreditContainerDark = Color(0xFF064E3B)         // Emerald 900
val CreditBorderLight = Color(0xFFA7F3D0)           // Emerald 200
val CreditBorderDark = Color(0xFF047857)            // Emerald 700

// الأحمر المالي (لنا / مصروف / مدين)
val SoftRed = Color(0xFFE11D48)                   // Rose 600 - راقٍ وغير مؤذٍ للعين
val DebtRed = Color(0xFFE11D48)
val DebtRedDark = Color(0xFFFB7185)               // Rose 400 - مضيء ومتناسق في الظلام
val DebtContainerLight = Color(0xFFFFF1F2)          // Rose 50
val DebtContainerDark = Color(0xFF4C0519)           // Rose 950
val DebtBorderLight = Color(0xFFFECDD3)             // Rose 200
val DebtBorderDark = Color(0xFF9F1239)              // Rose 800

// 7. ألوان التحديد النشط (Selection colors)
val SelectionGreen = Color(0xFF10B981)
val SelectionGreenContainerLight = Color(0xFFECFDF5)
val SelectionGreenContainerDark = Color(0xFF064E3B)

val DarkBackground = Color(0xFF080C14)             // Midnight Obsidian Background
val DarkSurface = Color(0xFF111827)                // Deep Slate Card Surface
val LightSurface = Color(0xFFFFFFFF)               // Pure White Surface

val TextPrimaryDark = Color(0xFFF8FAFC)            // Slate 50 - قراءة مريحة وعالية التباين
val TextSecondaryDark = Color(0xFF94A3B8)          // Slate 400
val TextPrimaryLight = Color(0xFF0F172A)           // Slate 900 - أسود كحلي عميق وفخم
val TextSecondaryLight = Color(0xFF64748B)         // Slate 500

val BorderDark = Color(0xFF1E293B)                 // Slate 800
val BorderLight = Color(0xFFE2E8F0)                // Slate 200

// 8. تدرجات لونية تنفيذية جاهزة (Pre-allocated Static Gradients)
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4338CA), Color(0xFF6366F1))
)

val PrimaryHeaderGradientLight = Brush.verticalGradient(
    colors = listOf(Color(0xFF3730A3), Color(0xFF4338CA), Color(0xFF4F46E5))
)

val PrimaryHeaderGradientDark = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF172554))
)

val CoralGradient = Brush.linearGradient(
    colors = listOf(CoralAccent, Color(0xFF0369A1))
)

val IncomeGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFECFDF5), LightSurface)
)

val IncomeGradientDark = Brush.linearGradient(
    colors = listOf(CreditContainerDark, DarkSurface)
)

val ExpenseGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFFFFF1F2), LightSurface)
)

val ExpenseGradientDark = Brush.linearGradient(
    colors = listOf(DebtContainerDark, DarkSurface)
)

val SelectedItemGradientLight = Brush.linearGradient(
    colors = listOf(SelectionGreenContainerLight, Color(0xFFD1FAE5))
)

val SelectedItemGradientDark = Brush.linearGradient(
    colors = listOf(SelectionGreenContainerDark, Color(0xFF022C22))
)

val NeonGreenCyanGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
)

val VioletHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF6366F1))
)

val HeaderCardGradientDark = Brush.linearGradient(
    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)

val HeaderCardGradientLight = Brush.linearGradient(
    colors = listOf(LightSurface, Color(0xFFF8FAFC))
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

// 9. ألوان وظيفية متخصصة (Specific Functional Palette Colors)
val WhatsAppGreen = Color(0xFF128C7E)
val WhatsAppLightGreen = Color(0xFF25D366)
val WhatsAppDarkGreen = Color(0xFF059669)
val WarningAmber = Color(0xFFD97706)
val WarningAmberDark = Color(0xFFF59E0B)
val WarningAmberBg = Color(0xFFFFFBEB)
val WarningAmberBgDark = Color(0xFF451A03)
val WarningAmberBorder = Color(0xFFFDE68A)
val WarningAmberBorderDark = Color(0xFF78350F)
val WarningDarkRedText = Color(0xFF991B1B)
val WarningOrangeButton = Color(0xFFEA580C)
val LicenseGreenBg = Color(0xFFECFDF5)
val LicenseGreenText = Color(0xFF059669)
val LicenseBadgeGreenText = Color(0xFF047857)
val InfoBlue = Color(0xFF0284C7)
val InfoBlueBgLight = Color(0xFFF0F9FF)
val InfoBlueBgDark = Color(0xFF0C4A6E)
val InfoBlueTextLight = Color(0xFF0369A1)
val InfoBlueTextDark = Color(0xFF38BDF8)

// 10. سلسلة درجات السليت (Slate Series)
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

// 11. عناصر الحالة والأزرار (Status & Action tokens)
val NeonGreen = Color(0xFF10B981)
val NeonCyan = Color(0xFF06B6D4)
val IndigoAccent = Color(0xFF6366F1)
val PurpleAccent = Color(0xFF8B5CF6)
val SoftLavender = Color(0xFFA5B4FC)
val WarningRed = Color(0xFFEF4444)
val WarningRedBorder = Color(0xFFF87171)
val WarningRedBorderLight = Color(0xFFFCA5A5)
val LightRedTint = Color(0xFFFECDD3)

// Alert Gold Tokens
val AlertGoldBgDark = Color(0xFF451A03)
val AlertGoldBorderDark = Color(0xFF92400E)
val AlertGoldTextDark = Color(0xFFFCD34D)
val AlertGoldTextLight = Color(0xFFB45309)

// Success Green badge tokens
val SuccessGreenBgDark = Color(0xFF064E3B)
val SuccessGreenBgLight = Color(0xFFECFDF5)
val SuccessGreenBorderDark = Color(0xFF059669)
val SuccessGreenBorderLight = Color(0xFFA7F3D0)
val MutedTextDark = Color(0xFF94A3B8)
val MutedTextLight = Color(0xFF64748B)
val DarkNeutralTrack = Color(0xFF1E293B)
val LightNeutralTrack = Color(0xFFE2E8F0)

// 12. بطاقات وأزرار التصفية (Chip Filter Color Tokens)
val ChipRedBgDarkSelected = Color(0xFF881337)
val ChipRedBgDarkUnselected = Color(0xFF4C0519)
val ChipRedBgLightSelected = Color(0xFFFFE4E6)
val ChipRedBgLightUnselected = Color(0xFFFFF1F2)

val ChipRedBorderDarkSelected = DebtRedDark
val ChipRedBorderDarkUnselected = Color(0xFF9F1239)
val ChipRedBorderLightSelected = Color(0xFFE11D48)
val ChipRedBorderLightUnselected = Color(0xFFFECDD3)

val ChipRedTextDark = DebtRedDark
val ChipRedTextLight = Color(0xFFE11D48)
val ChipRedHeaderDark = Color(0xFFFDA4AF)
val ChipRedHeaderLight = Color(0xFF9F1239)

val ChipGreenBgDarkSelected = Color(0xFF064E3B)
val ChipGreenBgDarkUnselected = Color(0xFF022C22)
val ChipGreenBgLightSelected = Color(0xFFD1FAE5)
val ChipGreenBgLightUnselected = Color(0xFFECFDF5)

val ChipGreenBorderDarkSelected = CreditGreenDark
val ChipGreenBorderDarkUnselected = Color(0xFF047857)
val ChipGreenBorderLightSelected = Color(0xFF059669)
val ChipGreenBorderLightUnselected = Color(0xFFA7F3D0)

val ChipGreenTextDark = CreditGreenDark
val ChipGreenTextLight = Color(0xFF059669)
val ChipGreenHeaderDark = Color(0xFF6EE7B7)
val ChipGreenHeaderLight = Color(0xFF065F46)

// 13. دوال حل الألوان الدلالية حسب الوضع (Financial Semantic Color Resolvers)
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
fun shareSecondaryColor(isDark: Boolean): Color = if (isDark) IndigoAccent else EmeraldPrimary

// 14. ألوان التصنيفات (Category Palette Tokens)
object CategoryPalette {
    val AMBER_DARK = Color(0xFF451A03)
    val AMBER_LIGHT = Color(0xFFFEF3C7)
    val PINK_DARK = Color(0xFF4C0519)
    val PINK_LIGHT = Color(0xFFFCE7F3)
    val GRAY_LIGHT_DARK = Color(0xFF1E293B)
    val GRAY_LIGHT_LIGHT = Color(0xFFF1F5F9)
    val RED_SOFT_DARK = Color(0xFF450A0A)
    val RED_SOFT_LIGHT = Color(0xFFFEE2E2)
    val YELLOW_DARK = Color(0xFF422006)
    val YELLOW_LIGHT = Color(0xFFFEF9C3)
    val BLUE_SOFT_DARK = Color(0xFF0C4A6E)
    val BLUE_SOFT_LIGHT = Color(0xFFE0F2FE)
    val SKY_DARK = Color(0xFF082F49)
    val SKY_LIGHT = Color(0xFFE0F2FE)
    val PURPLE_DARK = Color(0xFF3B0764)
    val PURPLE_LIGHT = Color(0xFFF3E8FF)
    val EMERALD_SOFT_DARK = Color(0xFF064E3B)
    val EMERALD_SOFT_LIGHT = Color(0xFFD1FAE5)
    val GREEN_FIFTY_DARK = Color(0xFF022C22)
    val GREEN_FIFTY_LIGHT = Color(0xFFECFDF5)
    val SLATE_DEFAULT_DARK = Slate900
    val SLATE_DEFAULT_LIGHT = Slate100
}

// 15. لوحة صور رمزية عصرية (Modern Avatar Pastel Palette)
val AvatarPastelPalette = listOf(
    Color(0xFFFCA5A5), Color(0xFFFDBA74), Color(0xFFFDE047),
    Color(0xFF86EFAC), Color(0xFF93C5FD), Color(0xFFC4B5FD),
    Color(0xFFF472B6), Color(0xFF2DD4BF)
)

val AvatarDarkPalette = listOf(
    Color(0xFF881337), Color(0xFF7C2D12), Color(0xFF713F12),
    Color(0xFF065F46), Color(0xFF1E40AF), Color(0xFF5B21B6),
    Color(0xFF831843), Color(0xFF115E59)
)



