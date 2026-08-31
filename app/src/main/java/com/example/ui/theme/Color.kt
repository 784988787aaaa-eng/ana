/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/theme/Color.kt
 * المسؤولية: تعريف ألوان واجهة التطبيق وقيمها المرجعية.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 107: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 265: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 716: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 724: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// 1. Primitive Palette (Raw Color Definitions Only)
// ============================================================================

// --- Brand Primitives ---
val BrandPrimary = Color(0xFF4B36A2)          // Premium Glowing Violet/Purple (#4B36A2)
val BrandPrimaryLight = Color(0xFF8C7CFF)     // Lighter Purple/Lavender (#8C7CFF)
val BrandPrimaryDark = Color(0xFF4B36A2)      // Glowing Violet for dark mode
val BrandPrimaryContainerLight = Color(0xFFEADBFF)
val BrandPrimaryContainerDark = Color(0xFF352478)
val BrandOnPrimaryContainerLight = Color(0xFF24005A)
val BrandOnPrimaryContainerDark = Color(0xFFEADBFF)

val BrandSecondary = Color(0xFF00B2FE)        // Neon Cyan / Electric Blue (#00B2FE)
val BrandSecondaryDark = Color(0xFF00B2FE)
val BrandSecondaryContainerLight = Color(0xFFD3E4FF)
val BrandSecondaryContainerDark = Color(0xFF004881)
val BrandOnSecondaryContainerLight = Color(0xFF001C38)
val BrandOnSecondaryContainerDark = Color(0xFFD3E4FF)

// --- Neutral Primitives ---
val NeutralBackgroundLight = Color(0xFFF8F9FA)
val NeutralBackgroundDark = Color(0xFF121212)
val NeutralSurfaceLight = Color(0xFFFFFFFF)
val NeutralSurfaceDark = Color(0xFF1E1E1E)
val NeutralSurfaceVariantLight = Color(0xFFF1F3F5)
val NeutralSurfaceVariantDark = Color(0xFF262626)
val NeutralSurfaceContainerLowDark = Color(0xFF181818)

val NeutralTextPrimaryLight = Color(0xFF1A1A1A)
val NeutralTextSecondaryLight = Color(0xFF555555)
val NeutralTextTertiaryLight = Color(0xFF888888)
val NeutralTextDisabledLight = Color(0xFFBDBDBD)

val NeutralTextPrimaryDark = Color(0xFFF5F5F5)
val NeutralTextSecondaryDark = Color(0xFFAAAAAA)
val NeutralTextTertiaryDark = Color(0xFF777777)
val NeutralTextDisabledDark = Color(0xFF555555)

val NeutralBorderLight = Color(0xFFE0E0E0)
val NeutralBorderVariantLight = Color(0xFFEAEAEA)
val NeutralBorderDark = Color(0xFF333333)
val NeutralBorderVariantDark = Color(0xFF2A2A2A)

// --- Financial Primitives ---
val CreditGreen = Color(0xFF2E7D32)            // Deep Emerald Green Light (#2E7D32)
val CreditGreenDark = Color(0xFF51CF66)        // Soft Neon Green Dark (#51CF66)
val CreditContainerLight = Color(0xFFF0FDF4)   // Light mode "علينا" card background (#F0FDF4)
val CreditContainerDark = Color(0xFF16281E)    // Dark mode "علينا" card background (#16281E)
val CreditBorderLight = Color(0xFFA7F3D0)
val CreditBorderDark = Color(0xFF1B4D2E)

val DebtRed = Color(0xFFD32F2F)                // Vivid Solid Red Light (#D32F2F)
val DebtRedDark = Color(0xFFFF6B6B)            // Soft High-Contrast Red Dark (#FF6B6B)
val DebtContainerLight = Color(0xFFFDF2F2)     // Light mode "لنا" card background (#FDF2F2)
val DebtContainerDark = Color(0xFF2C1A1D)      // Dark mode "لنا" card background (#2C1A1D)
val DebtBorderLight = Color(0xFFFECDD3)
val DebtBorderDark = Color(0xFF531A21)

val FinancialSelectionGreen = Color(0xFF10B981)
val FinancialSelectionContainerLight = Color(0xFFE6F4EA)
val FinancialSelectionContainerDark = Color(0xFF152D1F)

// --- Status & Functional Primitives ---
val StatusWarningAmber = Color(0xFFF59E0B)
val StatusWarningAmberBg = Color(0xFFFFF8E1)
val StatusWarningAmberBorder = Color(0xFFFFB300)
val StatusWarningDarkRedText = Color(0xFFB71C1C)
val StatusWarningOrangeButton = Color(0xFFE65100)

val StatusErrorRed = Color(0xFFD32F2F)
val StatusErrorRedDark = Color(0xFFFF6B6B)
val StatusErrorContainerLight = Color(0xFFFDF2F2)
val StatusErrorContainerDark = Color(0xFF2C1A1D)

val StatusInfoBlue = Color(0xFF3B82F6)
val StatusInfoBlueBgLight = Color(0xFFEFF6FF)
val StatusInfoBlueBgDark = Color(0xFF1E293B)
val StatusInfoBlueTextLight = Color(0xFF1D4ED8)
val StatusInfoBlueTextDark = Color(0xFF60A5FA)

val WhatsAppGreen = Color(0xFF128C7E)
val WhatsAppLightGreen = Color(0xFF25D366)

// --- Slate Palette Primitives ---
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

// --- Category Palette Primitives (Strictly for Domain Category Badges) ---
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
    val BLUE_SOFT_DARK = Color(0xFF172554)
    val BLUE_SOFT_LIGHT = Color(0xFFDBEAFE)
    val SKY_DARK = Color(0xFF0C4A6E)
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

// --- Avatar Pastel Palette Primitives ---
val AvatarPastelPalette = listOf(
    Color(0xFFFCA5A5), Color(0xFFFDBA74), Color(0xFFFDE047),
    Color(0xFF86EFAC), Color(0xFF93C5FD), Color(0xFFC4B5FD),
    Color(0xFFF472B6), Color(0xFF2DD4BF)
)

// Legacy Aliases during migration phase
val EmeraldPrimary = BrandPrimary
val EmeraldLight = BrandPrimaryLight
val CoralAccent = BrandSecondary
val EmeraldDark = BrandPrimaryDark
val CoralDark = BrandSecondaryDark
val IvoryBackground = NeutralBackgroundLight
val SoftRed = DebtRed
val SoftGreen = CreditGreen
val SelectionGreen = FinancialSelectionGreen
val SelectionGreenContainerLight = FinancialSelectionContainerLight
val SelectionGreenContainerDark = FinancialSelectionContainerDark
val DarkBackground = NeutralBackgroundDark
val DarkSurface = NeutralSurfaceDark
val LightSurface = NeutralSurfaceLight
val TextPrimaryDark = NeutralTextPrimaryDark
val TextSecondaryDark = NeutralTextSecondaryDark
val TextPrimaryLight = NeutralTextPrimaryLight
val TextSecondaryLight = NeutralTextSecondaryLight
val BorderDark = NeutralBorderDark
val BorderLight = NeutralBorderLight
val WarningAmber = StatusWarningAmber
val WarningAmberBg = StatusWarningAmberBg
val WarningAmberBorder = StatusWarningAmberBorder
val WarningDarkRedText = StatusWarningDarkRedText
val WarningOrangeButton = StatusWarningOrangeButton
val LicenseGreenBg = Color(0xFFE8F5E9)
val LicenseGreenText = Color(0xFF2E7D32)
val LicenseBadgeGreenText = Color(0xFF1B5E20)
val InfoBlue = StatusInfoBlue
val InfoBlueBgLight = StatusInfoBlueBgLight
val InfoBlueBgDark = StatusInfoBlueBgDark
val InfoBlueTextLight = StatusInfoBlueTextLight
val InfoBlueTextDark = StatusInfoBlueTextDark
val NeonGreen = Color(0xFF00E676)
val NeonCyan = Color(0xFF00B0FF)
val IndigoAccent = Color(0xFF6366F1)
val PurpleAccent = Color(0xFF8B5CF6)
val SoftLavender = Color(0xFFC4B5FD)
val WarningRed = Color(0xFFE53935)
val WarningRedBorder = Color(0xFFB91C1C)
val WarningRedBorderLight = Color(0xFFD93025)
val LightRedTint = Color(0xFFFF8A80)
val AlertGoldBgDark = Color(0xFF451A03)
val AlertGoldBorderDark = Color(0xFF92400E)
val AlertGoldTextDark = Color(0xFFFBBF24)
val AlertGoldTextLight = Color(0xFFB45309)
val SuccessGreenBgDark = Color(0xFF064E3B)
val SuccessGreenBgLight = Color(0xFFE6F4EA)
val SuccessGreenBorderDark = Color(0xFF10B981)
val SuccessGreenBorderLight = Color(0xFF137333)
val MutedTextDark = NeutralTextSecondaryDark
val MutedTextLight = NeutralTextSecondaryLight
val DarkNeutralTrack = Color(0xFF2D2D2D)
val LightNeutralTrack = Color(0xFFEEEEEE)

// Chip Filter Legacy Tokens
val ChipRedBgDarkSelected = Color(0xFF3B2025)
val ChipRedBgDarkUnselected = Color(0xFF2C1A1D)
val ChipRedBgLightSelected = Color(0xFFFCE8E8)
val ChipRedBgLightUnselected = Color(0xFFFDF2F2)
val ChipRedBorderDarkSelected = Color(0xFFFF6B6B)
val ChipRedBorderDarkUnselected = Color(0xFF531A21)
val ChipRedBorderLightSelected = Color(0xFFD32F2F)
val ChipRedBorderLightUnselected = Color(0xFFFECDD3)
val ChipRedTextDark = Color(0xFFFF6B6B)
val ChipRedTextLight = Color(0xFFD32F2F)
val ChipRedHeaderDark = Color(0xFFFFA3A3)
val ChipRedHeaderLight = Color(0xFFB71C1C)

val ChipGreenBgDarkSelected = Color(0xFF1D3528)
val ChipGreenBgDarkUnselected = Color(0xFF16281E)
val ChipGreenBgLightSelected = Color(0xFFE6F9ED)
val ChipGreenBgLightUnselected = Color(0xFFF0FDF4)
val ChipGreenBorderDarkSelected = Color(0xFF51CF66)
val ChipGreenBorderDarkUnselected = Color(0xFF1B4D2E)
val ChipGreenBorderLightSelected = Color(0xFF2E7D32)
val ChipGreenBorderLightUnselected = Color(0xFFA7F3D0)
val ChipGreenTextDark = Color(0xFF51CF66)
val ChipGreenTextLight = Color(0xFF2E7D32)
val ChipGreenHeaderDark = Color(0xFF86EFAC)
val ChipGreenHeaderLight = Color(0xFF1B5E20)

// Legacy gradients
val PrimaryGradient = Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryLight))
val CoralGradient = Brush.linearGradient(listOf(BrandSecondary, Color(0xFF0284C7)))
val IncomeGradientLight = Brush.linearGradient(listOf(CreditContainerLight, NeutralSurfaceLight))
val IncomeGradientDark = Brush.linearGradient(listOf(CreditContainerDark, NeutralSurfaceDark))
val ExpenseGradientLight = Brush.linearGradient(listOf(DebtContainerLight, NeutralSurfaceLight))
val ExpenseGradientDark = Brush.linearGradient(listOf(DebtContainerDark, NeutralSurfaceDark))
val SelectedItemGradientLight = Brush.linearGradient(listOf(FinancialSelectionContainerLight, Color(0xFFD1FAE5)))
val SelectedItemGradientDark = Brush.linearGradient(listOf(FinancialSelectionContainerDark, Color(0xFF121F17)))
val NeonGreenCyanGradient = Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
val VioletHeroGradient = Brush.linearGradient(listOf(BrandPrimary, Color(0xFF7C3AED), BrandPrimaryLight))
val HeaderCardGradientDark = Brush.linearGradient(listOf(Color(0xFF1E1E1E), Color(0xFF262626)))
val HeaderCardGradientLight = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA)))
val GoldLicenseGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309)))
val WarningGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))
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

// Legacy Financial Semantic Color Resolvers
fun financialCreditColor(isDark: Boolean): Color = if (isDark) CreditGreenDark else CreditGreen
fun financialDebtColor(isDark: Boolean): Color = if (isDark) DebtRedDark else DebtRed
fun financialCreditBg(isDark: Boolean): Color = if (isDark) CreditContainerDark else CreditContainerLight
fun financialDebtBg(isDark: Boolean): Color = if (isDark) DebtContainerDark else DebtContainerLight
fun financialCreditBorder(isDark: Boolean): Color = if (isDark) CreditBorderDark else CreditBorderLight
fun financialDebtBorder(isDark: Boolean): Color = if (isDark) DebtBorderDark else DebtBorderLight


// ============================================================================
// 2. MizanColors Central Semantic Design Tokens
// ============================================================================

data class MizanColors(
    // Brand
    val brandPrimary: Color,
    val onBrandPrimary: Color,
    val brandPrimaryContainer: Color,
    val onBrandPrimaryContainer: Color,
    val brandSecondary: Color,
    val onBrandSecondary: Color,
    val brandSecondaryContainer: Color,
    val onBrandSecondaryContainer: Color,

    // Surface
    val appBackground: Color,
    val appSurface: Color,
    val appSurfaceContainer: Color,
    val appSurfaceContainerLow: Color,
    val appSurfaceContainerHigh: Color,
    val appSurfaceVariant: Color,

    // Text
    val contentPrimary: Color,
    val contentSecondary: Color,
    val contentTertiary: Color,
    val contentDisabled: Color,
    val contentOnBrand: Color,

    // Borders
    val border: Color,
    val borderVariant: Color,
    val borderStrong: Color,

    // Credit — علينا / دائن
    val credit: Color,
    val onCredit: Color,
    val creditContainer: Color,
    val onCreditContainer: Color,
    val creditBorder: Color,
    val creditGradientStart: Color,
    val creditGradientEnd: Color,

    // Debt — لنا / مدين
    val debt: Color,
    val onDebt: Color,
    val debtContainer: Color,
    val onDebtContainer: Color,
    val debtBorder: Color,
    val debtGradientStart: Color,
    val debtGradientEnd: Color,

    // Selection
    val selection: Color,
    val onSelection: Color,
    val selectionContainer: Color,
    val selectionBorder: Color,

    // Status
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val warningBorder: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,

    // Header
    val headerForeground: Color,
    val headerForegroundMuted: Color,
    val headerControlContainer: Color,
    val headerControlBorder: Color,
    val headerControlContent: Color,
    val headerControlContentMuted: Color,

    // Floating UI
    val floatingControlBackground: Color,
    val floatingControlBorder: Color,
    val floatingControlContent: Color,
    val floatingControlContentMuted: Color,

    // Dialogs
    val dialogScrim: Color,
    val dialogActionContent: Color,
    val dialogDestructiveContent: Color,

    // Inputs
    val inputBorder: Color,
    val inputBorderFocused: Color,
    val inputContent: Color,
    val inputLabel: Color,
    val inputPlaceholder: Color,

    // Security
    val securityBackground: Color,
    val securityForeground: Color,
    val securityForegroundMuted: Color,
    val securityKeyBackground: Color,
    val securityKeyContent: Color,
    val securityKeyBorder: Color,
    val securityIndicatorEmpty: Color,
    val securityIndicatorFilled: Color,
    val securityInputBorder: Color,

    // Miscellaneous
    val separator: Color,
    val shadowTint: Color,
    val ripple: Color,
    val disabledTrack: Color,

    // Metric & Filter Chips
    val chipDebtSelectedBackground: Color,
    val chipDebtUnselectedBackground: Color,
    val chipDebtSelectedBorder: Color,
    val chipDebtUnselectedBorder: Color,
    val chipDebtText: Color,
    val chipCreditSelectedBackground: Color,
    val chipCreditUnselectedBackground: Color,
    val chipCreditSelectedBorder: Color,
    val chipCreditUnselectedBorder: Color,
    val chipCreditText: Color,

    // Row Indicators & Badges
    val alertGoldBackground: Color,
    val alertGoldBorder: Color,
    val alertGoldText: Color,
    val infoBlueBackground: Color,
    val infoBlueBorder: Color,
    val infoBlueText: Color,
    val successGreenBackground: Color,
    val successGreenBorder: Color,
    val successGreenText: Color,

    // Semantic Gradients
    val brandGradient: Brush,
    val brandSecondaryGradient: Brush,
    val heroGradient: Brush,
    val headerGradient: Brush,
    val creditGradient: Brush,
    val debtGradient: Brush,
    val selectionGradient: Brush,
    val warningGradient: Brush,
    val licenseGradient: Brush,
    val splashGradient: Brush,
    val splashGlow: Brush
)

// ============================================================================
// 3. Light and Dark Theme Instances
// ============================================================================

val LightMizanColors = MizanColors(
    brandPrimary = BrandPrimary,
    onBrandPrimary = Color(0xFFFFFFFF),
    brandPrimaryContainer = BrandPrimaryContainerLight,
    onBrandPrimaryContainer = BrandOnPrimaryContainerLight,
    brandSecondary = BrandSecondary,
    onBrandSecondary = Color(0xFFFFFFFF),
    brandSecondaryContainer = BrandSecondaryContainerLight,
    onBrandSecondaryContainer = BrandOnSecondaryContainerLight,

    appBackground = NeutralBackgroundLight,
    appSurface = NeutralSurfaceLight,
    appSurfaceContainer = NeutralSurfaceLight,
    appSurfaceContainerLow = NeutralBackgroundLight,
    appSurfaceContainerHigh = Color(0xFFF8F9FA),
    appSurfaceVariant = NeutralSurfaceVariantLight,

    contentPrimary = NeutralTextPrimaryLight,
    contentSecondary = NeutralTextSecondaryLight,
    contentTertiary = NeutralTextTertiaryLight,
    contentDisabled = NeutralTextDisabledLight,
    contentOnBrand = Color(0xFFFFFFFF),

    border = NeutralBorderLight,
    borderVariant = NeutralBorderVariantLight,
    borderStrong = Slate300,

    credit = CreditGreen,
    onCredit = Color(0xFFFFFFFF),
    creditContainer = CreditContainerLight,
    onCreditContainer = Color(0xFF14532D),
    creditBorder = CreditBorderLight,
    creditGradientStart = CreditContainerLight,
    creditGradientEnd = NeutralSurfaceLight,

    debt = DebtRed,
    onDebt = Color(0xFFFFFFFF),
    debtContainer = DebtContainerLight,
    onDebtContainer = Color(0xFF7F1D1D),
    debtBorder = DebtBorderLight,
    debtGradientStart = DebtContainerLight,
    debtGradientEnd = NeutralSurfaceLight,

    selection = FinancialSelectionGreen,
    onSelection = Color(0xFFFFFFFF),
    selectionContainer = FinancialSelectionContainerLight,
    selectionBorder = Color(0xFFA7F3D0),

    success = CreditGreen,
    onSuccess = Color(0xFFFFFFFF),
    successContainer = FinancialSelectionContainerLight,
    warning = StatusWarningAmber,
    onWarning = Color(0xFFFFFFFF),
    warningContainer = StatusWarningAmberBg,
    warningBorder = StatusWarningAmberBorder,
    error = StatusErrorRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = StatusErrorContainerLight,
    info = StatusInfoBlue,
    onInfo = Color(0xFFFFFFFF),
    infoContainer = StatusInfoBlueBgLight,

    headerForeground = Color(0xFFFFFFFF),
    headerForegroundMuted = Color(0xCCFFFFFF),
    headerControlContainer = Color(0x2BFFFFFF),
    headerControlBorder = Color(0x40FFFFFF),
    headerControlContent = Color(0xFFFFFFFF),
    headerControlContentMuted = Color(0xB3FFFFFF),

    floatingControlBackground = Color(0x33FFFFFF),
    floatingControlBorder = Color(0x59FFFFFF),
    floatingControlContent = Color(0xFFFFFFFF),
    floatingControlContentMuted = Color(0xCCFFFFFF),

    dialogScrim = Color(0x80000000),
    dialogActionContent = Color(0xFFFFFFFF),
    dialogDestructiveContent = Color(0xFFFFFFFF),

    inputBorder = NeutralBorderLight,
    inputBorderFocused = BrandPrimary,
    inputContent = NeutralTextPrimaryLight,
    inputLabel = NeutralTextSecondaryLight,
    inputPlaceholder = NeutralTextTertiaryLight,

    securityBackground = Color(0xFF1B133E),
    securityForeground = Color(0xFFFFFFFF),
    securityForegroundMuted = Color(0xB3FFFFFF),
    securityKeyBackground = Color(0x1AFFFFFF),
    securityKeyContent = Color(0xFFFFFFFF),
    securityKeyBorder = Color(0x26FFFFFF),
    securityIndicatorEmpty = Color(0x33FFFFFF),
    securityIndicatorFilled = BrandSecondary,
    securityInputBorder = Color(0x4DFFFFFF),

    separator = NeutralBorderVariantLight,
    shadowTint = Color(0x1A000000),
    ripple = Color(0xFF6B21A8),
    disabledTrack = LightNeutralTrack,

    chipDebtSelectedBackground = ChipRedBgLightSelected,
    chipDebtUnselectedBackground = DebtContainerLight,
    chipDebtSelectedBorder = DebtRed,
    chipDebtUnselectedBorder = DebtBorderLight,
    chipDebtText = DebtRed,
    chipCreditSelectedBackground = ChipGreenBgLightSelected,
    chipCreditUnselectedBackground = CreditContainerLight,
    chipCreditSelectedBorder = CreditGreen,
    chipCreditUnselectedBorder = CreditBorderLight,
    chipCreditText = CreditGreen,

    alertGoldBackground = WarningAmberBg,
    alertGoldBorder = WarningAmberBorder,
    alertGoldText = AlertGoldTextLight,
    infoBlueBackground = InfoBlueBgLight,
    infoBlueBorder = InfoBlue,
    infoBlueText = InfoBlueTextLight,
    successGreenBackground = SuccessGreenBgLight,
    successGreenBorder = SuccessGreenBorderLight,
    successGreenText = CreditGreen,

    brandGradient = Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryLight)),
    brandSecondaryGradient = Brush.linearGradient(listOf(BrandSecondary, Color(0xFF0284C7))),
    heroGradient = Brush.linearGradient(listOf(BrandPrimary, Color(0xFF7C3AED), BrandPrimaryLight)),
    headerGradient = Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA))),
    creditGradient = Brush.linearGradient(listOf(CreditContainerLight, NeutralSurfaceLight)),
    debtGradient = Brush.linearGradient(listOf(DebtContainerLight, NeutralSurfaceLight)),
    selectionGradient = Brush.linearGradient(listOf(FinancialSelectionContainerLight, Color(0xFFD1FAE5))),
    warningGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
    licenseGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))),
    splashGradient = Brush.sweepGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF2563EB),
            0.5f to Color(0xFF10B981),
            1.0f to Color(0xFF2563EB)
        )
    ),
    splashGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2563EB).copy(alpha = 0.22f),
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color.Transparent
        )
    )
)

val DarkMizanColors = MizanColors(
    brandPrimary = BrandPrimaryDark,
    onBrandPrimary = Color(0xFFFFFFFF),
    brandPrimaryContainer = BrandPrimaryContainerDark,
    onBrandPrimaryContainer = BrandOnPrimaryContainerDark,
    brandSecondary = BrandSecondaryDark,
    onBrandSecondary = Color(0xFFFFFFFF),
    brandSecondaryContainer = BrandSecondaryContainerDark,
    onBrandSecondaryContainer = BrandOnSecondaryContainerDark,

    appBackground = NeutralBackgroundDark,
    appSurface = NeutralSurfaceDark,
    appSurfaceContainer = NeutralSurfaceDark,
    appSurfaceContainerLow = NeutralSurfaceContainerLowDark,
    appSurfaceContainerHigh = Color(0xFF262626),
    appSurfaceVariant = NeutralSurfaceVariantDark,

    contentPrimary = NeutralTextPrimaryDark,
    contentSecondary = NeutralTextSecondaryDark,
    contentTertiary = NeutralTextTertiaryDark,
    contentDisabled = NeutralTextDisabledDark,
    contentOnBrand = Color(0xFFFFFFFF),

    border = NeutralBorderDark,
    borderVariant = NeutralBorderVariantDark,
    borderStrong = Slate700,

    credit = CreditGreenDark,
    onCredit = Color(0xFF000000),
    creditContainer = CreditContainerDark,
    onCreditContainer = Color(0xFFA7F3D0),
    creditBorder = CreditBorderDark,
    creditGradientStart = CreditContainerDark,
    creditGradientEnd = NeutralSurfaceDark,

    debt = DebtRedDark,
    onDebt = Color(0xFF000000),
    debtContainer = DebtContainerDark,
    onDebtContainer = Color(0xFFFECDD3),
    debtBorder = DebtBorderDark,
    debtGradientStart = DebtContainerDark,
    debtGradientEnd = NeutralSurfaceDark,

    selection = FinancialSelectionGreen,
    onSelection = Color(0xFFFFFFFF),
    selectionContainer = FinancialSelectionContainerDark,
    selectionBorder = Color(0xFF1B4D2E),

    success = CreditGreenDark,
    onSuccess = Color(0xFF000000),
    successContainer = FinancialSelectionContainerDark,
    warning = StatusWarningAmber,
    onWarning = Color(0xFF000000),
    warningContainer = Color(0xFF451A03),
    warningBorder = Color(0xFF92400E),
    error = StatusErrorRedDark,
    onError = Color(0xFF000000),
    errorContainer = StatusErrorContainerDark,
    info = StatusInfoBlue,
    onInfo = Color(0xFFFFFFFF),
    infoContainer = StatusInfoBlueBgDark,

    headerForeground = Color(0xFFFFFFFF),
    headerForegroundMuted = Color(0xCCFFFFFF),
    headerControlContainer = Color(0x2BFFFFFF),
    headerControlBorder = Color(0x40FFFFFF),
    headerControlContent = Color(0xFFFFFFFF),
    headerControlContentMuted = Color(0xB3FFFFFF),

    floatingControlBackground = Color(0x33FFFFFF),
    floatingControlBorder = Color(0x59FFFFFF),
    floatingControlContent = Color(0xFFFFFFFF),
    floatingControlContentMuted = Color(0xCCFFFFFF),

    dialogScrim = Color(0x99000000),
    dialogActionContent = Color(0xFFFFFFFF),
    dialogDestructiveContent = Color(0xFFFFFFFF),

    inputBorder = NeutralBorderDark,
    inputBorderFocused = BrandPrimaryLight,
    inputContent = NeutralTextPrimaryDark,
    inputLabel = NeutralTextSecondaryDark,
    inputPlaceholder = NeutralTextTertiaryDark,

    securityBackground = Color(0xFF0E0B1F),
    securityForeground = Color(0xFFFFFFFF),
    securityForegroundMuted = Color(0xB3FFFFFF),
    securityKeyBackground = Color(0x1AFFFFFF),
    securityKeyContent = Color(0xFFFFFFFF),
    securityKeyBorder = Color(0x26FFFFFF),
    securityIndicatorEmpty = Color(0x33FFFFFF),
    securityIndicatorFilled = BrandSecondary,
    securityInputBorder = Color(0x4DFFFFFF),

    separator = NeutralBorderVariantDark,
    shadowTint = Color(0x33000000),
    ripple = Color(0xFFFFFFFF),
    disabledTrack = DarkNeutralTrack,

    chipDebtSelectedBackground = ChipRedBgDarkSelected,
    chipDebtUnselectedBackground = DebtContainerDark,
    chipDebtSelectedBorder = DebtRedDark,
    chipDebtUnselectedBorder = DebtBorderDark,
    chipDebtText = DebtRedDark,
    chipCreditSelectedBackground = ChipGreenBgDarkSelected,
    chipCreditUnselectedBackground = CreditContainerDark,
    chipCreditSelectedBorder = CreditGreenDark,
    chipCreditUnselectedBorder = CreditBorderDark,
    chipCreditText = CreditGreenDark,

    alertGoldBackground = AlertGoldBgDark,
    alertGoldBorder = AlertGoldBorderDark,
    alertGoldText = AlertGoldTextDark,
    infoBlueBackground = InfoBlueBgDark,
    infoBlueBorder = InfoBlue,
    infoBlueText = InfoBlueTextDark,
    successGreenBackground = SuccessGreenBgDark,
    successGreenBorder = SuccessGreenBorderDark,
    successGreenText = CreditGreenDark,

    brandGradient = Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryLight)),
    brandSecondaryGradient = Brush.linearGradient(listOf(BrandSecondary, Color(0xFF0284C7))),
    heroGradient = Brush.linearGradient(listOf(BrandPrimary, Color(0xFF7C3AED), BrandPrimaryLight)),
    headerGradient = Brush.linearGradient(listOf(Color(0xFF1E1E1E), Color(0xFF262626))),
    creditGradient = Brush.linearGradient(listOf(CreditContainerDark, NeutralSurfaceDark)),
    debtGradient = Brush.linearGradient(listOf(DebtContainerDark, NeutralSurfaceDark)),
    selectionGradient = Brush.linearGradient(listOf(FinancialSelectionContainerDark, Color(0xFF121F17))),
    warningGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
    licenseGradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))),
    splashGradient = Brush.sweepGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF2563EB),
            0.5f to Color(0xFF10B981),
            1.0f to Color(0xFF2563EB)
        )
    ),
    splashGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2563EB).copy(alpha = 0.22f),
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color.Transparent
        )
    )
)

// ============================================================================
// 4. CompositionLocal & MaterialTheme Accessor
// ============================================================================

val LocalMizanColors = staticCompositionLocalOf { LightMizanColors }

val MaterialTheme.mizanColors: MizanColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMizanColors.current

// ============================================================================
// 5. Document Palette (Print-Safe Adapter Mapping for PDF / Canvas)
// ============================================================================

object MizanDocumentColors {
    val brandPrimary = Color(0xFF0F4C43)
    val headerBackground = Color(0xFF2C3E50)
    val headerText = Color(0xFFFFFFFF)
    val headerBorder = Color(0xFFCBD5E1)

    val contentPrimary = Color(0xFF1E293B)
    val contentSecondary = Color(0xFF64748B)
    val contentTertiary = Color(0xFF0F172A)
    val contentMedium = Color(0xFF334155)
    val contentLight = Color(0xFF475569)

    val netDebtBlue = Color(0xFF1E3A8A)
    val debt = Color(0xFFB91C1C)
    val debtContainer = Color(0xFFFEF2F2)
    val debtBorder = Color(0xFFFECDD3)
    val credit = Color(0xFF156534)
    val creditContainer = Color(0xFFF0FDF4)
    val creditBorder = Color(0xFFA7F3D0)

    val surface = Color(0xFFFFFFFF)
    val surfaceVariant = Color(0xFFF8FAFC)
    val surfaceContainer = Color(0xFFF8FAFC)
    val rowDivider = Color(0xFFE2E8F0)
    val altRowBackground = Color(0xFFF8FAFC)
    val totalsRowBackground = Color(0xFFF1F5F9)
    val bannerDebtBg = Color(0xFFFEE2E2)
    val bannerCreditBg = Color(0xFFDCFCE7)
    val borderStrong = Color(0xFFCBD5E1)
    val borderVariant = Color(0xFFE2E8F0)
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
