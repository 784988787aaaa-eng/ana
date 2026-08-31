/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/theme/Theme.kt
 * المسؤولية: تجميع وتطبيق Theme الخاص بـ Compose، بما يشمل الألوان والأشكال والطباعة وسياق الواجهة.
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
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 13: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 15: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 18: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 126: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 127: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 159: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.

package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

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

/**
 * دالة استيفاء خطي (Linear Interpolation) لكامل عناصر لوحة ألوان Material 3 ككتلة متزامنة واحدة.
 * تضمن انتقالاً ناعماً ومتناسقاً (260ms) دون تشتت الحالات أو إعادة رسم غير متزامنة.
 */
fun lerpColorScheme(start: ColorScheme, stop: ColorScheme, fraction: Float): ColorScheme {
    return ColorScheme(
        primary = lerp(start.primary, stop.primary, fraction),
        onPrimary = lerp(start.onPrimary, stop.onPrimary, fraction),
        primaryContainer = lerp(start.primaryContainer, stop.primaryContainer, fraction),
        onPrimaryContainer = lerp(start.onPrimaryContainer, stop.onPrimaryContainer, fraction),
        inversePrimary = lerp(start.inversePrimary, stop.inversePrimary, fraction),
        secondary = lerp(start.secondary, stop.secondary, fraction),
        onSecondary = lerp(start.onSecondary, stop.onSecondary, fraction),
        secondaryContainer = lerp(start.secondaryContainer, stop.secondaryContainer, fraction),
        onSecondaryContainer = lerp(start.onSecondaryContainer, stop.onSecondaryContainer, fraction),
        tertiary = lerp(start.tertiary, stop.tertiary, fraction),
        onTertiary = lerp(start.onTertiary, stop.onTertiary, fraction),
        tertiaryContainer = lerp(start.tertiaryContainer, stop.tertiaryContainer, fraction),
        onTertiaryContainer = lerp(start.onTertiaryContainer, stop.onTertiaryContainer, fraction),
        background = lerp(start.background, stop.background, fraction),
        onBackground = lerp(start.onBackground, stop.onBackground, fraction),
        surface = lerp(start.surface, stop.surface, fraction),
        onSurface = lerp(start.onSurface, stop.onSurface, fraction),
        surfaceVariant = lerp(start.surfaceVariant, stop.surfaceVariant, fraction),
        onSurfaceVariant = lerp(start.onSurfaceVariant, stop.onSurfaceVariant, fraction),
        surfaceTint = lerp(start.surfaceTint, stop.surfaceTint, fraction),
        inverseSurface = lerp(start.inverseSurface, stop.inverseSurface, fraction),
        inverseOnSurface = lerp(start.inverseOnSurface, stop.inverseOnSurface, fraction),
        error = lerp(start.error, stop.error, fraction),
        onError = lerp(start.onError, stop.onError, fraction),
        errorContainer = lerp(start.errorContainer, stop.errorContainer, fraction),
        onErrorContainer = lerp(start.onErrorContainer, stop.onErrorContainer, fraction),
        outline = lerp(start.outline, stop.outline, fraction),
        outlineVariant = lerp(start.outlineVariant, stop.outlineVariant, fraction),
        scrim = lerp(start.scrim, stop.scrim, fraction),
        surfaceBright = lerp(start.surfaceBright, stop.surfaceBright, fraction),
        surfaceDim = lerp(start.surfaceDim, stop.surfaceDim, fraction),
        surfaceContainer = lerp(start.surfaceContainer, stop.surfaceContainer, fraction),
        surfaceContainerHigh = lerp(start.surfaceContainerHigh, stop.surfaceContainerHigh, fraction),
        surfaceContainerHighest = lerp(start.surfaceContainerHighest, stop.surfaceContainerHighest, fraction),
        surfaceContainerLow = lerp(start.surfaceContainerLow, stop.surfaceContainerLow, fraction),
        surfaceContainerLowest = lerp(start.surfaceContainerLowest, stop.surfaceContainerLowest, fraction),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MizanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeTransitionFraction by animateFloatAsState(
        targetValue = if (darkTheme) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "mizan_theme_transition"
    )

    val animatedColorScheme = remember(themeTransitionFraction) {
        lerpColorScheme(MizanLightColorScheme, MizanDarkColorScheme, themeTransitionFraction)
    }
    val mizanColors = if (darkTheme) DarkMizanColors else LightMizanColors

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

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
