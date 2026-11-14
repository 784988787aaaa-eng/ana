package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun PasscodeDotIndicators(
    enteredLength: Int,
    shakeOffsetPx: Float,
    modifier: Modifier = Modifier,
    totalDots: Int = 4
) {
    val mizanColors = MaterialTheme.mizanColors

    Row(
        modifier = modifier
            .offset(x = shakeOffsetPx.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = CircleShape
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalDots) {
            val filled = enteredLength > i
            val dotScale by animateFloatAsState(
                targetValue = if (filled) 1.22f else 1.0f,
                animationSpec = tween(
                    durationMillis = 70,
                    easing = FastOutSlowInEasing
                ),
                label = "dotScale_$i"
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(if (filled) mizanColors.securityIndicatorFilled else mizanColors.securityIndicatorEmpty)
                    .border(
                        width = 1.5.dp,
                        color = if (filled) mizanColors.securityIndicatorFilled else mizanColors.securityKeyBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}
