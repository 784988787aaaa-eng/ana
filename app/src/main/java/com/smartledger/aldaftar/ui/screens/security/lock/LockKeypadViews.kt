package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun KeypadButton(
    text: String,
    isFunctional: Boolean,
    onClick: () -> Unit
) {
    val mizanColors = MaterialTheme.mizanColors
    val haptic = LocalHapticFeedback.current
    val normalizedText = remember(text) { text.toEnglishDigits() }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing),
        label = "keypadBtnScale"
    )
    val bg = when {
        isPressed -> mizanColors.securityIndicatorFilled.copy(alpha = 0.28f)
        isFunctional -> mizanColors.securityKeyBackground.copy(alpha = 0.55f)
        else -> mizanColors.securityKeyBackground
    }
    val borderColor = when {
        isPressed -> mizanColors.securityIndicatorFilled.copy(alpha = 0.75f)
        else -> mizanColors.securityKeyBorder
    }
    val textCol = remember(isFunctional, mizanColors) {
        if (isFunctional) mizanColors.securityForegroundMuted else mizanColors.securityKeyContent
    }
    val textSize = remember(isFunctional) { if (isFunctional) 13.5.sp else 25.sp }
    val fontWeight = remember(isFunctional) { if (isFunctional) FontWeight.Bold else FontWeight.ExtraBold }

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = if (isPressed) 1.5.dp else 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            )
            .testTag("keypad_btn_$normalizedText"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = normalizedText,
            color = textCol,
            fontFamily = CairoFontFamily,
            fontSize = textSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
                fontFamily = CairoFontFamily,
                fontFeatureSettings = if (isFunctional) null else "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                )
            )
        )
    }
}

@Composable
fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val mizanColors = MaterialTheme.mizanColors
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing),
        label = "keypadIconScale"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(buttonScale)
            .clip(CircleShape)
            .background(
                if (isPressed) mizanColors.securityIndicatorFilled.copy(alpha = 0.28f)
                else mizanColors.securityIndicatorFilled.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                color = mizanColors.securityIndicatorFilled.copy(alpha = if (isPressed) 0.7f else 0.35f),
                shape = CircleShape
            )
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            )
            .testTag("keypad_biometric_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = mizanColors.securityIndicatorFilled,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun KeypadRow(
    row: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.forEach { digit ->
            KeypadButton(text = digit, isFunctional = false, onClick = { onKeyClick(digit) })
        }
    }
}
