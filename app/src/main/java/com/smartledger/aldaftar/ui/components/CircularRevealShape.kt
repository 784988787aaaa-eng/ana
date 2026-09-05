/**
 * =====================================================================
 * ملف: شكل الكشف الدائري للقص والتحول البصري (CircularRevealShape.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الملف فئة شكل مخصصة (Custom [Shape]) لتطبيق حركة الكشف والتحول السائل
 * (Liquid Morphing Reveal) عبر واجهات Compose القياسية مثل `Modifier.clip(shape)`.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. تطبيق واجهة الأشكال في Compose (Shape Interface Implementation):
 *    - تنفيذ دالة [createOutline] لإنتاج حدود مسار عام [Outline.Generic] بالاستناد إلى حجم المكون وكثافة الشاشة.
 * 2. التوسع الدائري المحسوب (Dynamic Radius Calculation):
 *    - احتساب نصف القطر الأقصى باستخدام وتر الشاشة [hypot] لتغطية أركان العنصر بالكامل عند اكتمال التقدم.
 * 3. التكيف مع الإحداثيات النسبية (Relative Coordinate Support):
 *    - دعم تمرير مركز الكشف كنقطة عائمة مئوية من عرض الحاوية لتلائم مختلف أبعاد الشاشات.
 */
package com.smartledger.aldaftar.ui.components

// ---------------------------------------------------------------------
// استيراد أدوات تحديد الأشكال والمسارات الهندسية في Jetpack Compose
// ---------------------------------------------------------------------
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.hypot

/**
 * [شكل الكشف الدائري التفاعلي - CircularRevealShape]:
 * 
 * @property progress نسبة تقدم الحركة من 0.0f إلى 1.0f.
 * @property centerOffset نقطة مركز انطلاق الدائرة.
 * @property isRelative هل إحداثي X نسبي مئوي من العرض أم مطلق بالبكسل.
 */
class CircularRevealShape(
    val progress: Float,
    val centerOffset: Offset,
    val isRelative: Boolean = false
) : Shape {

    /**
     * [توليد الحدود الهندسية للمسار - createOutline]:
     * تُستدعى بواسطة إطار عمل Compose لاقتصاص العنصر وفق المسار البيضاوي المتوسع.
     */
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val maxRadius = hypot(size.width, size.height)
        val radius = maxRadius * progress
        val cx = if (isRelative) size.width * centerOffset.x else centerOffset.x
        val cy = centerOffset.y
        val path = Path().apply {
            addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius))
        }
        return Outline.Generic(path)
    }
}

