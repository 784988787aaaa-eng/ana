package com.smartledger.aldaftar.ui.helper

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.platform.contacts.FormatUtils
import com.smartledger.aldaftar.ui.theme.AvatarPastelPalette
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import java.math.BigDecimal
import kotlin.math.abs

fun getInitialColor(name: String): Color {
    val hash = (name.hashCode() and Int.MAX_VALUE)
    return AvatarPastelPalette[hash % AvatarPastelPalette.size]
}

fun formatCurrency(amount: BigDecimal, currencySymbol: String): String {
    return FormatUtils.formatCurrency(amount.abs(), currencySymbol, null)
}

@Composable
fun AutoScaleText(
    text: String,
    baseFontSize: TextUnit,
    color: Color,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 11.sp,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1
) {
    val initialSize = remember(text, baseFontSize, minFontSize) {
        val estimated = when {
            text.length > 22 -> (baseFontSize.value * 0.68f)
            text.length > 16 -> (baseFontSize.value * 0.78f)
            text.length > 12 -> (baseFontSize.value * 0.88f)
            else -> baseFontSize.value
        }
        estimated.coerceAtLeast(minFontSize.value).sp
    }
    var fontSizeState by remember(text, baseFontSize, minFontSize) { mutableStateOf(initialSize) }
    var hasAdjusted by remember(text, baseFontSize, minFontSize) { mutableStateOf(false) }
    var readyToDraw by remember(text, baseFontSize, minFontSize) { mutableStateOf(false) }

    val mergedStyle = LocalTextStyle.current.merge(
        TextStyle(
            fontFamily = CairoFontFamily,
            fontSize = fontSizeState,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        )
    )

    Text(
        text = text,
        color = color,
        style = mergedStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        overflow = if (readyToDraw) TextOverflow.Ellipsis else TextOverflow.Clip,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && !hasAdjusted) {
                hasAdjusted = true
                val currentSize = fontSizeState.value
                val minSizeVal = minFontSize.value
                if (currentSize > minSizeVal) {
                    val containerWidth = textLayoutResult.size.width.toFloat()
                    val actualWidth = if (textLayoutResult.lineCount > 0) {
                        abs(textLayoutResult.getLineRight(0) - textLayoutResult.getLineLeft(0))
                    } else {
                        0f
                    }
                    val ratio = if (containerWidth > 0f && actualWidth > containerWidth) {
                        (containerWidth / actualWidth) * 0.95f
                    } else {
                        0.82f
                    }
                    val nextSize = (currentSize * ratio).coerceAtLeast(minSizeVal)
                    if (nextSize < currentSize - 0.1f) {
                        fontSizeState = nextSize.sp
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}
