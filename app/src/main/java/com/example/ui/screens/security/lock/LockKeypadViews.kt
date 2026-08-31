package com.example.ui.screens.security.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.mizanColors

/**
 * Standard PIN Keypad Digit / Action Button with custom styling and haptics support.
 */
@Composable
fun KeypadButton(
    text: String,
    isFunctional: Boolean,
    onClick: () -> Unit
) {
    val mizanColors = MaterialTheme.mizanColors
    val bg = remember(isFunctional, mizanColors) { if (isFunctional) mizanColors.securityKeyBackground.copy(alpha = 0.5f) else mizanColors.securityKeyBackground }
    val textCol = remember(isFunctional, mizanColors) { if (isFunctional) mizanColors.securityForegroundMuted else mizanColors.securityKeyContent }
    val textSize = remember(isFunctional) { if (isFunctional) 13.sp else 24.sp }
    val fontWeight = remember(isFunctional) { if (isFunctional) FontWeight.Medium else FontWeight.ExtraBold }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = 1.dp,
                color = mizanColors.securityKeyBorder,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .testTag("keypad_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textCol,
            fontSize = textSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Biometric / Action Icon Button on Keypad.
 */
@Composable
fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val mizanColors = MaterialTheme.mizanColors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(mizanColors.securityIndicatorFilled.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = mizanColors.securityIndicatorFilled.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
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

/**
 * Single Row of 3 Keypad Digit Buttons.
 */
@Composable
fun KeypadRow(
    row: List<String>,
    onKeyClick: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.forEach { digit ->
            KeypadButton(text = digit, isFunctional = false, onClick = { onKeyClick(digit) })
        }
    }
}
