package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة أدوات واجهة المستخدم لشاشات الحبايب (Habayeb UI Helpers Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال ومكونات مساعدة لواجهة المستخدم الخاصة بشاشات الحسابات والعملاء،
 * مثل توليد ألوان الصور الرمزية، تنسيق العملات، والمكون النصي متكيف الحجم (AutoScaleText).
 * =====================================================================================
 */

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
import com.example.domain.FormatUtils
import com.example.ui.theme.AvatarDarkPalette
import com.example.ui.theme.AvatarPastelPalette
import java.math.BigDecimal

/*
 * =====================================================================================
 * دالة توليد لون الصورة الرمزية ثنائي التوليد (getInitialColor)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تعتمد على ترميز الاسم بتجزئة رياضية (Hash Code) للحصول على نفس اللون الهادئ والمريح
 * للعين دائماً لنفس الشخص من لوحة ألوان الباستيل المعتمدة (AvatarPastelPalette).
 * =====================================================================================
 */
fun getInitialColor(name: String, isDark: Boolean = false): Color {
    val hash = (name.hashCode() and Int.MAX_VALUE)
    val palette = if (isDark) AvatarDarkPalette else AvatarPastelPalette
    return palette[hash % palette.size]
}

/*
 * =====================================================================================
 * دالة تنسيق المبالغ المالية مع رمز العملة (formatCurrency)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * دالة مركزية لتنسيق المبالغ المالية بالقيمة المطلقة مع رمز العملة بالاعتماد حصراً
 * على BigDecimal لمنع أخطاء التقريب والفاصلة العائمة.
 * =====================================================================================
 */
fun formatCurrency(amount: BigDecimal, currencySymbol: String): String {
    return FormatUtils.formatCurrency(amount.abs(), currencySymbol, null)
}

/*
 * =====================================================================================
 * مكون النص المتكيف مع المساحة المتاحة (AutoScaleText)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون نصي ذكي يقوم بتصغير حجم الخط تلقائياً (Auto-Shrink) إذا كان النص طويلاً ويتجاوز
 * عرض الشاشة أو الحاوية المتاحة (مثل أرقام الحسابات الضخمة أو الأسماء الطويلة):
 * 1. حساب حجم خط ابتدائي بناءً على عدد الحروف لتسريع الاستجابة.
 * 2. قياس التدفق البصري للنص (onTextLayout) وتصغير الحجم تدريجياً حتى يتناسب تماماً
 *    أو يصل إلى الحد الأدنى المسموح به (9sp).
 * 3. منع الرسم حتى يتأكد المكون من استقرار الحجم لتفادي وميض النص (Flicker Prevention).
 * =====================================================================================
 */
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
    /*
     * تقدير مبدئي لحجم الخط وفق طول النص لتفادي تكرار دورات الرسم (Re-layout optimization)
     */
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

    /*
     * رسم النص مع التحكم في حجم الخط والتدفق البصري
     */
    Text(
        text = text,
        color = color,
        style = TextStyle(
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
                if (currentSize > 9f) {
                    fontSizeState = (currentSize - 0.5f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

