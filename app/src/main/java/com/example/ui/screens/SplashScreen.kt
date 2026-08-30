package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة شاشة البداية والتحميل المتحركة (Splash Screen Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على شاشة الانطلاق الترحيبية المتحركة (TheMasterSplashScreen):
 * تأثيرات النبض التنفسي، دوران التدرجات اللونية الحلقية، التلاشي والتوهج المحيطي.
 * =====================================================================================
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

import com.example.ui.theme.Slate900
import com.example.ui.theme.SplashRadialGlow
import com.example.ui.theme.SplashSweepGradient

/*
 * =====================================================================================
 * شاشة البداية المتحركة (TheMasterSplashScreen)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * شاشة تشغيل ترحيبية بتصميم بصري تجريدي حديث:
 * 1. انتقال سلس تلقائي بعد مهلة زمنية قدرها 2500 مللي ثانية.
 * 2. تأثير نبض حلقي دائري مع دوران طيفي مستمر متوافق مع شاشات 120Hz.
 * 3. استخدام لوحات الفرش المحسوبة مسبقاً (Pre-allocated Brushes) لمنع إعادة التركيب غير الضرورية.
 *
 * [المُدخلات]:
 * - onSplashFinished: رد نداء إنهاء عرض شاشة البداية والانتقال للواجهة الرئيسية أو القفل.
 * =====================================================================================
 */
@Composable
fun TheMasterSplashScreen(
    onSplashFinished: () -> Unit
) {
    // مهلة زمنية أنيقة لمدة 2.5 ثانية
    LaunchedEffect(Unit) {
        delay(2500)
        onSplashFinished()
    }

    // خلفية داكنة غير لامعة بلون كحلي هادئ
    val deepMatteDark = Slate900

    // حلقة الحركة التكرارية المستمرة
    val infiniteTransition = rememberInfiniteTransition(label = "PortalAnimation")

    // نبض التكبير والتصغير المستمر
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // نبض الشفافية والتوهج
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // دوران طيف الألوان
    val rotateDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorRotation"
    )

    // حاوية الشاشة الكاملة
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepMatteDark),
        contentAlignment = Alignment.Center
    ) {
        // حلقة الضوء والتوهج التجريدي
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    rotationZ = rotateDegrees
                }
                .scale(scalePulse)
                .alpha(alphaPulse)
        ) {
            // التوهج الشعاعي المركزي (Aura)
            drawCircle(
                brush = SplashRadialGlow,
                radius = size.minDimension * 0.48f
            )

            // التدرج الدوار المحيطي للحلقة الضوئية
            val strokePx = 12.dp.toPx()
            drawCircle(
                brush = SplashSweepGradient,
                radius = size.minDimension * 0.40f,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
