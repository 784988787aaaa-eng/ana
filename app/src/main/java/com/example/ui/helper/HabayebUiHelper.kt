package com.example.ui.helper

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.example.domain.FormatUtils
import com.example.ui.theme.AvatarPastelPalette

// Pastel Colors for Initials
val PastelColors = AvatarPastelPalette

fun getInitialColor(name: String): Color {
    val hash = (name.hashCode() and Int.MAX_VALUE)
    return PastelColors[hash % PastelColors.size]
}

fun formatCurrency(amount: Double, currencySymbol: String): String {
    return FormatUtils.formatDouble(kotlin.math.abs(amount), currencySymbol)
}

@Composable
fun AutoScaleText(
    text: String,
    baseFontSize: TextUnit,
    color: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1
) {
    val adjustedFontSize = when {
        text.length > 18 -> baseFontSize * 0.70f
        text.length > 13 -> baseFontSize * 0.82f
        else -> baseFontSize
    }
    Text(
        text = text,
        color = color,
        fontSize = adjustedFontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
