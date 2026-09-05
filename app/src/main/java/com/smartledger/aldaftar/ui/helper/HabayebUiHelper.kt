package com.smartledger.aldaftar.ui.helper

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
import com.smartledger.aldaftar.domain.FormatUtils
import com.smartledger.aldaftar.ui.theme.AvatarPastelPalette
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import java.math.BigDecimal

// جلب لون الصورة الرمزية بالاعتماد على لوحة الألوان المركزية المعتمدة
fun getInitialColor(name: String): Color {
    val hash = (name.hashCode() and Int.MAX_VALUE)
    return AvatarPastelPalette[hash % AvatarPastelPalette.size]
}

/**
 * دالة مركزية لتنسيق المبالغ المالية مع رمز العملة بالاعتماد حصراً على BigDecimal لمنع أخطاء التقريب.
 */
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
    minFontSize: TextUnit = 9.sp,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = 1
) {
    val initialSize = remember(text, baseFontSize) {
        when {
            text.length > 22 -> (baseFontSize.value * 0.68f).sp
            text.length > 16 -> (baseFontSize.value * 0.78f).sp
            text.length > 12 -> (baseFontSize.value * 0.88f).sp
            else -> baseFontSize
        }
    }
    var fontSizeState by remember(text, baseFontSize) { mutableStateOf(initialSize) }
    var readyToDraw by remember(text, baseFontSize) { mutableStateOf(true) }

    Text(
        text = text,
        color = color,
        style = TextStyle(
            fontFamily = CairoFontFamily,
            fontSize = fontSizeState,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        ),
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                val currentSize = fontSizeState.value
                val minSizeVal = minFontSize.value
                if (currentSize > minSizeVal) {
                    val nextSize = (currentSize - 1.0f).coerceAtLeast(minSizeVal)
                    if (nextSize != currentSize) {
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
