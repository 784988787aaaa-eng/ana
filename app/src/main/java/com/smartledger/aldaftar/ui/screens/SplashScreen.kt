package com.smartledger.aldaftar.ui.screens

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

import com.smartledger.aldaftar.ui.theme.Slate900
import com.smartledger.aldaftar.ui.theme.SplashRadialGlow
import com.smartledger.aldaftar.ui.theme.SplashSweepGradient

@Composable
fun TheMasterSplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onSplashFinished()
    }

    val deepMatteDark = Slate900 // slate-900 security look

    val infiniteTransition = rememberInfiniteTransition(label = "PortalAnimation")

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val rotateDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepMatteDark),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    rotationZ = rotateDegrees
                }
                .scale(scalePulse)
                .alpha(alphaPulse)
        ) {
            drawCircle(
                brush = SplashRadialGlow,
                radius = size.minDimension * 0.48f
            )

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
