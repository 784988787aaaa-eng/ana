package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ============================================================================
 * Design Tokens — Mizan Al Dar Design System (نظام التصميم الموحد)
 * ============================================================================
 * يحتوي هذا الملف على الثوابت التصميمية الموحدة والمعتمدة لكامل الواجهات:
 * - شبكة المسافات (Spacing Grid)
 * - أنصاف أقطار الحواف (Corner Radii)
 * - أبعاد العناصر التفاعلية والأهداف اللمسية (Dimensions & Touch Targets)
 * - الارتفاعات البصرية (Elevations)
 * - رموز الحركة والتلاشي (Animation Tokens)
 */

object MizanSpacing {
    val none: Dp = 0.dp
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val hero: Dp = 48.dp
}

object MizanRadii {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val pill: Dp = 999.dp

    // Shapes
    val shapeXs = RoundedCornerShape(xs)
    val shapeSm = RoundedCornerShape(sm)
    val shapeMd = RoundedCornerShape(md)
    val shapeLg = RoundedCornerShape(lg)
    val shapeXl = RoundedCornerShape(xl)
    val shapeXxl = RoundedCornerShape(xxl)
    val shapePill = RoundedCornerShape(pill)
}

object MizanTouchTarget {
    val minimum: Dp = 48.dp
    val standardButtonHeight: Dp = 48.dp
    val largeButtonHeight: Dp = 56.dp
    val compactButtonHeight: Dp = 40.dp
    val inputHeight: Dp = 56.dp
    val listItemMinHeight: Dp = 56.dp
    val iconButtonSize: Dp = 48.dp
    val smallIconButtonSize: Dp = 40.dp
    val fabStandardSize: Dp = 56.dp
    val fabLargeSize: Dp = 64.dp
}

object MizanElevation {
    val flat: Dp = 0.dp
    val cardResting: Dp = 1.dp
    val cardElevated: Dp = 3.dp
    val dialog: Dp = 6.dp
    val bottomSheet: Dp = 8.dp
    val floatingControl: Dp = 4.dp
    val fab: Dp = 6.dp
}

object MizanIconSizes {
    val xs: Dp = 14.dp
    val sm: Dp = 18.dp
    val md: Dp = 24.dp
    val lg: Dp = 32.dp
    val xl: Dp = 48.dp
}

object MizanAnimationTokens {
    const val DURATION_FAST = 150
    const val DURATION_STANDARD = 260
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 400

    val easeInOut = FastOutSlowInEasing
    val easeOut = LinearOutSlowInEasing
    val standardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    fun <T> springDefault() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> springGentle() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> tweenStandard(duration: Int = DURATION_STANDARD) = tween<T>(
        durationMillis = duration,
        easing = easeInOut
    )
}
