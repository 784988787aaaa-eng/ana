/**
 * =====================================================================
 * ملف: LockKeypadViews.kt
 * الحزمة: com.example.ui.screens.security.lock
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يحتوي هذا الملف على مكونات واجهة المستخدم التأسيسية الخاصة بلوحة مفاتيح
 * شاشة القفل الرقمية (PIN Lock Keypad). يوفر أزراراً دائرية أنيقة ذات طابع
 * زجاجي عصري (Frosted / Glassmorphism) مصممة خصيصاً للشاشات الداكنة وقفل الأمان،
 * تشمل أزرار الأرقام، الأزرار الوظيفية (مثل الحذف أو المسح)، وأزرار المصادقة البيومترية.
 * 
 * [المكونات المتاحة]:
 * 1. [KeypadButton]: زر رقمي أو وظيفي دائري مع تخصيص الشفافية وحجم الخط.
 * 2. [KeypadIconButton]: زر أيقونة مخصص للبصمة الحيوية بلون الزمرد المميز.
 * 3. [KeypadRow]: صف أفقي منظم يحتوي على 3 أزرار أرقام بمسافات متوازنة.
 * =====================================================================
 */
package com.example.ui.screens.security.lock

// ---------------------------------------------------------------------
// استيراد حزم Jetpack Compose للواجهات الرسومية والتصميم الدائري
// ---------------------------------------------------------------------
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

/**
 * =====================================================================
 * [زر لوحة المفاتيح الرقمي/الوظيفي - KeypadButton]:
 * 
 * [الهدف والغرض]:
 * رسم زر دائري بقطر 72dp يعرض رقماً أو نصاً وظيفياً مع تدرجات لونية نصف شفافة.
 * 
 * [المعاملات المستلمة]:
 * @param text النص أو الرقم المعروض داخل الزر.
 * @param isFunctional يحدد ما إذا كان الزر وظيفياً (مثل مسح/حذف) لتقليل حجم الخط وشفافيته مقارنة بالأرقام.
 * @param onClick دالة الاستدعاء عند النقر على الزر.
 * =====================================================================
 */
@Composable
fun KeypadButton(
    text: String,
    isFunctional: Boolean,
    onClick: () -> Unit
) {
    // حساب الألوان والأحجام بصورة تذكارية (remember) بناءً على نوع الزر
    val bg = if (isFunctional) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)
    val textCol = if (isFunctional) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null, // إخفاء وميض التحديد الافتراضي للاعتماد على الهزاز اللمسي
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
 * =====================================================================
 * [زر الأيقونة البيومترية للوحة المفاتيح - KeypadIconButton]:
 * 
 * [الهدف والغرض]:
 * رسم زر دائري لعرض أيقونة البصمة الحيوية مع إطار وخلفية بلون الزمرد الأخضر.
 * 
 * [المعاملات المستلمة]:
 * @param icon متجه الأيقونة المراد رسمها (مثل أيقونة بصمة الإصبع).
 * @param contentDescription الوصف النصي المساعد لخدمات إمكانية الوصول.
 * @param onClick دالة الاستدعاء عند النقر على الزر.
 * =====================================================================
 */
@Composable
fun KeypadIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
    }
}

/**
 * =====================================================================
 * [صف أزرار لوحة المفاتيح - KeypadRow]:
 * 
 * [الهدف والغرض]:
 * تنظيم 3 أزرار أرقام في صف أفقي مع تباعد متناسق (28.dp) لتشكيل شبكة اللوحة.
 * 
 * [المعاملات المستلمة]:
 * @param row قائمة النصوص أو الأرقام الثلاثة في هذا الصف.
 * @param onKeyClick دالة الاستدعاء عند النقر على أي رقم مع تمرير قيمته.
 * =====================================================================
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

