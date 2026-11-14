package com.smartledger.aldaftar.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R

// Local static Cairo font family bundled in res/font/cairo.ttf
val CairoFontFamily = FontFamily(
    Font(resId = R.font.cairo, weight = FontWeight.Normal),
    Font(resId = R.font.cairo, weight = FontWeight.Medium),
    Font(resId = R.font.cairo, weight = FontWeight.SemiBold),
    Font(resId = R.font.cairo, weight = FontWeight.Bold),
    Font(resId = R.font.cairo, weight = FontWeight.ExtraBold)
)

private val ArabicPlatformTextStyle = PlatformTextStyle(
    includeFontPadding = false
)

private val ArabicLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    displayMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    displaySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    titleSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    bodySmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )
)

/**
 * Domain-specific semantic typography tokens for financial and core application UI.
 */
object MizanTypographyTokens {
    val metricHero = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val metricValue = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val metricLabel = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val cardTitle = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.5.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val cardSubtitle = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val dialogTitle = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val dialogSubtitle = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val badge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )

    val button = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        platformStyle = ArabicPlatformTextStyle,
        lineHeightStyle = ArabicLineHeightStyle
    )
}
