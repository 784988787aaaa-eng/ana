package com.smartledger.aldaftar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// A. Primitive colors
// ============================================================================

// Brand primitives
val BrandPrimary = Color(0xFF4B36A2)
val BrandPrimaryLight = Color(0xFF8C7CFF)
val BrandPrimaryDark = Color(0xFF4B36A2)
val BrandPrimaryContainerLight = Color(0xFFEADBFF)
val BrandPrimaryContainerDark = Color(0xFF352478)
val BrandOnPrimaryContainerLight = Color(0xFF24005A)
val BrandOnPrimaryContainerDark = Color(0xFFEADBFF)

val BrandSecondary = Color(0xFF00B2FE)
val BrandSecondaryDark = Color(0xFF00B2FE)
val BrandSecondaryContainerLight = Color(0xFFD3E4FF)
val BrandSecondaryContainerDark = Color(0xFF004881)
val BrandOnSecondaryContainerLight = Color(0xFF001C38)
val BrandOnSecondaryContainerDark = Color(0xFFD3E4FF)

// Neutral primitives
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
val NeutralBlack = Color(0xFF000000)

// Slate primitives
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

// External/domain primitives
val WhatsAppGreen = Color(0xFF128C7E)
val WhatsAppLightGreen = Color(0xFF25D366)

// Category palette: domain-specific category badges; keep ordering stable.
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

// Avatar pastel palette: ordering is part of the user experience.
val AvatarPastelPalette = listOf(
    Color(0xFFFCA5A5), Color(0xFFFDBA74), Color(0xFFFDE047),
    Color(0xFF86EFAC), Color(0xFF93C5FD), Color(0xFFC4B5FD),
    Color(0xFFF472B6), Color(0xFF2DD4BF)
)


// ============================================================================
// B. Semantic colors
// ============================================================================

// Financial semantics
val CreditGreen = Color(0xFF2E7D32)
val CreditGreenDark = Color(0xFF51CF66)
val CreditContainerLight = Color(0xFFF0FDF4)
val CreditContainerDark = Color(0xFF16281E)
val CreditBorderLight = Color(0xFFA7F3D0)
val CreditBorderDark = Color(0xFF1B4D2E)

val DebtRed = Color(0xFFD32F2F)
val DebtRedDark = Color(0xFFFF6B6B)
val DebtContainerLight = Color(0xFFFDF2F2)
val DebtContainerDark = Color(0xFF2C1A1D)
val DebtBorderLight = Color(0xFFFECDD3)
val DebtBorderDark = Color(0xFF531A21)

val FinancialSelectionGreen = Color(0xFF10B981)
val FinancialSelectionContainerLight = Color(0xFFE6F4EA)
val FinancialSelectionContainerDark = Color(0xFF152D1F)

// Status semantics
val StatusWarningAmber = Color(0xFFF59E0B)
val StatusWarningAmberBg = Color(0xFFFFF8E1)
val StatusWarningAmberBorder = Color(0xFFFFB300)
val StatusWarningDarkRedText = Color(0xFFB71C1C)
val StatusWarningOrangeButton = Color(0xFFE65100)

val StatusErrorRed = DebtRed
val StatusErrorRedDark = DebtRedDark
val StatusErrorContainerLight = DebtContainerLight
val StatusErrorContainerDark = DebtContainerDark

val StatusInfoBlue = Color(0xFF3B82F6)
val StatusInfoBlueBgLight = Color(0xFFEFF6FF)
val StatusInfoBlueBgDark = Color(0xFF1E293B)
val StatusInfoBlueTextLight = Color(0xFF1D4ED8)
val StatusInfoBlueTextDark = Color(0xFF60A5FA)

// Supporting semantic tokens
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
val SuccessGreenBgLight = FinancialSelectionContainerLight
val SuccessGreenBorderDark = FinancialSelectionGreen
val SuccessGreenBorderLight = Color(0xFF137333)

val DarkNeutralTrack = Color(0xFF2D2D2D)
val LightNeutralTrack = Color(0xFFEEEEEE)

// Chip filter semantic tokens
val ChipRedBgDarkSelected = Color(0xFF3B2025)
val ChipRedBgDarkUnselected = DebtContainerDark
val ChipRedBgLightSelected = Color(0xFFFCE8E8)
val ChipRedBgLightUnselected = DebtContainerLight
val ChipRedBorderDarkSelected = DebtRedDark
val ChipRedBorderDarkUnselected = DebtBorderDark
val ChipRedBorderLightSelected = DebtRed
val ChipRedBorderLightUnselected = DebtBorderLight
val ChipRedTextDark = DebtRedDark
val ChipRedTextLight = DebtRed
val ChipRedHeaderDark = Color(0xFFFFA3A3)
val ChipRedHeaderLight = StatusWarningDarkRedText

val ChipGreenBgDarkSelected = Color(0xFF1D3528)
val ChipGreenBgDarkUnselected = CreditContainerDark
val ChipGreenBgLightSelected = Color(0xFFE6F9ED)
val ChipGreenBgLightUnselected = CreditContainerLight
val ChipGreenBorderDarkSelected = CreditGreenDark
val ChipGreenBorderDarkUnselected = CreditBorderDark
val ChipGreenBorderLightSelected = CreditGreen
val ChipGreenBorderLightUnselected = CreditBorderLight
val ChipGreenTextDark = CreditGreenDark
val ChipGreenTextLight = CreditGreen
val ChipGreenHeaderDark = Color(0xFF86EFAC)
val ChipGreenHeaderLight = Color(0xFF1B5E20)

// ============================================================================
// C. Theme color model
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
    val splashGradient: Brush,
    val splashGlow: Brush
)


// ============================================================================
// D. Light theme tokens
// ============================================================================

val LightMizanColors by lazy {
    MizanColors(
    brandPrimary = BrandPrimary,
    onBrandPrimary = NeutralSurfaceLight,
    brandPrimaryContainer = BrandPrimaryContainerLight,
    onBrandPrimaryContainer = BrandOnPrimaryContainerLight,
    brandSecondary = BrandSecondary,
    onBrandSecondary = NeutralSurfaceLight,
    brandSecondaryContainer = BrandSecondaryContainerLight,
    onBrandSecondaryContainer = BrandOnSecondaryContainerLight,

    appBackground = NeutralBackgroundLight,
    appSurface = NeutralSurfaceLight,
    appSurfaceContainer = NeutralSurfaceLight,
    appSurfaceContainerLow = NeutralBackgroundLight,
    appSurfaceContainerHigh = NeutralBackgroundLight,
    appSurfaceVariant = NeutralSurfaceVariantLight,

    contentPrimary = NeutralTextPrimaryLight,
    contentSecondary = NeutralTextSecondaryLight,
    contentTertiary = NeutralTextTertiaryLight,
    contentDisabled = NeutralTextDisabledLight,
    contentOnBrand = NeutralSurfaceLight,

    border = NeutralBorderLight,
    borderVariant = NeutralBorderVariantLight,
    borderStrong = Slate300,

    credit = CreditGreen,
    onCredit = NeutralSurfaceLight,
    creditContainer = CreditContainerLight,
    onCreditContainer = Color(0xFF14532D),
    creditBorder = CreditBorderLight,
    creditGradientStart = CreditContainerLight,
    creditGradientEnd = NeutralSurfaceLight,

    debt = DebtRed,
    onDebt = NeutralSurfaceLight,
    debtContainer = DebtContainerLight,
    onDebtContainer = Color(0xFF7F1D1D),
    debtBorder = DebtBorderLight,
    debtGradientStart = DebtContainerLight,
    debtGradientEnd = NeutralSurfaceLight,

    selection = FinancialSelectionGreen,
    onSelection = NeutralSurfaceLight,
    selectionContainer = FinancialSelectionContainerLight,
    selectionBorder = CreditBorderLight,

    success = CreditGreen,
    onSuccess = NeutralSurfaceLight,
    successContainer = FinancialSelectionContainerLight,
    warning = StatusWarningAmber,
    onWarning = NeutralSurfaceLight,
    warningContainer = StatusWarningAmberBg,
    warningBorder = StatusWarningAmberBorder,
    error = StatusErrorRed,
    onError = NeutralSurfaceLight,
    errorContainer = StatusErrorContainerLight,
    info = StatusInfoBlue,
    onInfo = NeutralSurfaceLight,
    infoContainer = StatusInfoBlueBgLight,

    headerForeground = NeutralSurfaceLight,
    headerForegroundMuted = Color(0xCCFFFFFF),
    headerControlContainer = Color(0x2BFFFFFF),
    headerControlBorder = Color(0x40FFFFFF),
    headerControlContent = NeutralSurfaceLight,
    headerControlContentMuted = Color(0xB3FFFFFF),

    floatingControlBackground = Color(0x33FFFFFF),
    floatingControlBorder = Color(0x59FFFFFF),
    floatingControlContent = NeutralSurfaceLight,
    floatingControlContentMuted = Color(0xCCFFFFFF),

    dialogScrim = Color(0x80000000),
    dialogActionContent = NeutralSurfaceLight,
    dialogDestructiveContent = NeutralSurfaceLight,

    inputBorder = NeutralBorderLight,
    inputBorderFocused = BrandPrimary,
    inputContent = NeutralTextPrimaryLight,
    inputLabel = NeutralTextSecondaryLight,
    inputPlaceholder = NeutralTextTertiaryLight,

    securityBackground = Color(0xFF1B133E),
    securityForeground = NeutralSurfaceLight,
    securityForegroundMuted = Color(0xB3FFFFFF),
    securityKeyBackground = Color(0x1AFFFFFF),
    securityKeyContent = NeutralSurfaceLight,
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

    alertGoldBackground = StatusWarningAmberBg,
    alertGoldBorder = StatusWarningAmberBorder,
    alertGoldText = AlertGoldTextLight,
    infoBlueBackground = StatusInfoBlueBgLight,
    infoBlueBorder = StatusInfoBlue,
    infoBlueText = StatusInfoBlueTextLight,
    successGreenBackground = SuccessGreenBgLight,
    successGreenBorder = SuccessGreenBorderLight,
    successGreenText = CreditGreen,

    brandGradient = PrimaryGradient,
    brandSecondaryGradient = CoralGradient,
    heroGradient = VioletHeroGradient,
    headerGradient = HeaderCardGradientLight,
    creditGradient = IncomeGradientLight,
    debtGradient = ExpenseGradientLight,
    selectionGradient = SelectedItemGradientLight,
    warningGradient = WarningGradient,
    splashGradient = SplashSweepGradient,
    splashGlow = SplashRadialGlow
    )
}

val DarkMizanColors by lazy {
    MizanColors(
    brandPrimary = BrandPrimaryDark,
    onBrandPrimary = NeutralSurfaceLight,
    brandPrimaryContainer = BrandPrimaryContainerDark,
    onBrandPrimaryContainer = BrandOnPrimaryContainerDark,
    brandSecondary = BrandSecondaryDark,
    onBrandSecondary = NeutralSurfaceLight,
    brandSecondaryContainer = BrandSecondaryContainerDark,
    onBrandSecondaryContainer = BrandOnSecondaryContainerDark,

    appBackground = NeutralBackgroundDark,
    appSurface = NeutralSurfaceDark,
    appSurfaceContainer = NeutralSurfaceDark,
    appSurfaceContainerLow = NeutralSurfaceContainerLowDark,
    appSurfaceContainerHigh = NeutralSurfaceVariantDark,
    appSurfaceVariant = NeutralSurfaceVariantDark,

    contentPrimary = NeutralTextPrimaryDark,
    contentSecondary = NeutralTextSecondaryDark,
    contentTertiary = NeutralTextTertiaryDark,
    contentDisabled = NeutralTextDisabledDark,
    contentOnBrand = NeutralSurfaceLight,

    border = NeutralBorderDark,
    borderVariant = NeutralBorderVariantDark,
    borderStrong = Slate700,

    credit = CreditGreenDark,
    onCredit = NeutralBlack,
    creditContainer = CreditContainerDark,
    onCreditContainer = CreditBorderLight,
    creditBorder = CreditBorderDark,
    creditGradientStart = CreditContainerDark,
    creditGradientEnd = NeutralSurfaceDark,

    debt = DebtRedDark,
    onDebt = NeutralBlack,
    debtContainer = DebtContainerDark,
    onDebtContainer = DebtBorderLight,
    debtBorder = DebtBorderDark,
    debtGradientStart = DebtContainerDark,
    debtGradientEnd = NeutralSurfaceDark,

    selection = FinancialSelectionGreen,
    onSelection = NeutralSurfaceLight,
    selectionContainer = FinancialSelectionContainerDark,
    selectionBorder = CreditBorderDark,

    success = CreditGreenDark,
    onSuccess = NeutralBlack,
    successContainer = FinancialSelectionContainerDark,
    warning = StatusWarningAmber,
    onWarning = NeutralBlack,
    warningContainer = AlertGoldBgDark,
    warningBorder = AlertGoldBorderDark,
    error = StatusErrorRedDark,
    onError = NeutralBlack,
    errorContainer = StatusErrorContainerDark,
    info = StatusInfoBlue,
    onInfo = NeutralSurfaceLight,
    infoContainer = StatusInfoBlueBgDark,

    headerForeground = NeutralSurfaceLight,
    headerForegroundMuted = Color(0xCCFFFFFF),
    headerControlContainer = Color(0x2BFFFFFF),
    headerControlBorder = Color(0x40FFFFFF),
    headerControlContent = NeutralSurfaceLight,
    headerControlContentMuted = Color(0xB3FFFFFF),

    floatingControlBackground = Color(0x33FFFFFF),
    floatingControlBorder = Color(0x59FFFFFF),
    floatingControlContent = NeutralSurfaceLight,
    floatingControlContentMuted = Color(0xCCFFFFFF),

    dialogScrim = Color(0x99000000),
    dialogActionContent = NeutralSurfaceLight,
    dialogDestructiveContent = NeutralSurfaceLight,

    inputBorder = NeutralBorderDark,
    inputBorderFocused = BrandPrimaryLight,
    inputContent = NeutralTextPrimaryDark,
    inputLabel = NeutralTextSecondaryDark,
    inputPlaceholder = NeutralTextTertiaryDark,

    securityBackground = Color(0xFF0E0B1F),
    securityForeground = NeutralSurfaceLight,
    securityForegroundMuted = Color(0xB3FFFFFF),
    securityKeyBackground = Color(0x1AFFFFFF),
    securityKeyContent = NeutralSurfaceLight,
    securityKeyBorder = Color(0x26FFFFFF),
    securityIndicatorEmpty = Color(0x33FFFFFF),
    securityIndicatorFilled = BrandSecondary,
    securityInputBorder = Color(0x4DFFFFFF),

    separator = NeutralBorderVariantDark,
    shadowTint = Color(0x33000000),
    ripple = NeutralSurfaceLight,
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
    infoBlueBackground = StatusInfoBlueBgDark,
    infoBlueBorder = StatusInfoBlue,
    infoBlueText = StatusInfoBlueTextDark,
    successGreenBackground = SuccessGreenBgDark,
    successGreenBorder = SuccessGreenBorderDark,
    successGreenText = CreditGreenDark,

    brandGradient = PrimaryGradient,
    brandSecondaryGradient = CoralGradient,
    heroGradient = VioletHeroGradient,
    headerGradient = HeaderCardGradientDark,
    creditGradient = IncomeGradientDark,
    debtGradient = ExpenseGradientDark,
    selectionGradient = SelectedItemGradientDark,
    warningGradient = WarningGradient,
    splashGradient = SplashSweepGradient,
    splashGlow = SplashRadialGlow
    )
}

// ============================================================================
// F. Semantic gradients
// ============================================================================

// Shared semantic gradients. MizanColors references these directly so each
// gradient has one source of truth.
val PrimaryGradient = Brush.linearGradient(listOf(BrandPrimary, BrandPrimaryLight))
val CoralGradient = Brush.linearGradient(listOf(BrandSecondary, Color(0xFF0284C7)))

val IncomeGradientLight = Brush.linearGradient(
    listOf(CreditContainerLight, NeutralSurfaceLight)
)
val IncomeGradientDark = Brush.linearGradient(
    listOf(CreditContainerDark, NeutralSurfaceDark)
)
val ExpenseGradientLight = Brush.linearGradient(
    listOf(DebtContainerLight, NeutralSurfaceLight)
)
val ExpenseGradientDark = Brush.linearGradient(
    listOf(DebtContainerDark, NeutralSurfaceDark)
)
val SelectedItemGradientLight = Brush.linearGradient(
    listOf(FinancialSelectionContainerLight, Color(0xFFD1FAE5))
)
val SelectedItemGradientDark = Brush.linearGradient(
    listOf(FinancialSelectionContainerDark, Color(0xFF121F17))
)
val NeonGreenCyanGradient = Brush.horizontalGradient(
    listOf(NeonGreen, NeonCyan)
)
val VioletHeroGradient = Brush.linearGradient(
    listOf(BrandPrimary, Color(0xFF7C3AED), BrandPrimaryLight)
)
val HeaderCardGradientDark = Brush.linearGradient(
    listOf(NeutralSurfaceDark, NeutralSurfaceVariantDark)
)
val HeaderCardGradientLight = Brush.linearGradient(
    listOf(NeutralSurfaceLight, NeutralBackgroundLight)
)
val WarningGradient = Brush.linearGradient(
    listOf(StatusWarningAmber, Color(0xFFEF4444))
)
val SplashSweepGradient = Brush.sweepGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF2563EB),
        0.5f to FinancialSelectionGreen,
        1.0f to Color(0xFF2563EB)
    )
)
val SplashRadialGlow = Brush.radialGradient(
    colors = listOf(
        Color(0xFF2563EB).copy(alpha = 0.22f),
        FinancialSelectionGreen.copy(alpha = 0.15f),
        Color.Transparent
    )
)

// ============================================================================
// G. Theme accessors / resolvers
// ============================================================================

// Financial semantic color resolvers
fun financialCreditColor(isDark: Boolean): Color =
    if (isDark) CreditGreenDark else CreditGreen

fun financialDebtColor(isDark: Boolean): Color =
    if (isDark) DebtRedDark else DebtRed

fun financialCreditBg(isDark: Boolean): Color =
    if (isDark) CreditContainerDark else CreditContainerLight

fun financialDebtBg(isDark: Boolean): Color =
    if (isDark) DebtContainerDark else DebtContainerLight

fun financialCreditBorder(isDark: Boolean): Color =
    if (isDark) CreditBorderDark else CreditBorderLight

fun financialDebtBorder(isDark: Boolean): Color =
    if (isDark) DebtBorderDark else DebtBorderLight

val LocalMizanColors = compositionLocalOf { LightMizanColors }
val LocalIsDarkTheme = compositionLocalOf { false }

val MaterialTheme.mizanColors: MizanColors
    @Composable
    @ReadOnlyComposable
    get() = LocalMizanColors.current

val MaterialTheme.isDark: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalIsDarkTheme.current

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
