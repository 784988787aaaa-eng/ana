package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun PasscodeDotIndicators(
    enteredLength: Int,
    shakeOffsetPx: Float,
    modifier: Modifier = Modifier,
    totalDots: Int = 4,
    hasError: Boolean = false,
    isChecking: Boolean = false
) {
    val mizanColors = MaterialTheme.mizanColors

    Row(
        modifier = modifier.offset(x = shakeOffsetPx.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalDots) {
            val filled = enteredLength > i
            val dotScale by animateFloatAsState(
                targetValue = when {
                    isChecking && filled -> 1.15f
                    filled -> 1.22f
                    else -> 1.0f
                },
                animationSpec = spring(
                    stiffness = Spring.StiffnessHigh,
                    dampingRatio = Spring.DampingRatioNoBouncy
                ),
                label = "dotScale_$i"
            )
            val fillColor = when {
                hasError -> mizanColors.error.copy(alpha = if (filled) 0.85f else 0.20f)
                filled -> mizanColors.securityIndicatorFilled
                else -> mizanColors.securityIndicatorEmpty
            }
            val borderColor = when {
                hasError -> mizanColors.error
                filled -> mizanColors.securityIndicatorFilled
                else -> mizanColors.securityKeyBorder
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(fillColor)
                    .border(
                        width = 1.4.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

