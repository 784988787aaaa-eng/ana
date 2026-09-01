/**
 * =====================================================================
 * ملف: معدل حركة الكشف الدائري (CircularReveal.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الملف معدل تنسيق مخصص (Custom Modifier) في Jetpack Compose لتنفيذ
 * حركة "الكشف الدائري" (Circular Reveal Animation). يُستخدم هذا التأثير البصري
 * لإظهار أو إخفاء الشاشات والمكونات بحركة دائرية تنطلق من نقطة محددة (مثل زر النقر).
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. كفاءة الأداء وتفادي تدوير الذاكرة (Memory Optimization & Zero GC Churn):
 *    - استخدام [drawWithCache] لإعادة استخدام كائن [Path] دون إعادة إنشائه في كل إطار حركي (Frame).
 * 2. الحساب الهندسي الدقيق لنصف القطر (Hypotenuse Maximum Radius):
 *    - حساب أقصى نصف قطر ممكن باستخدام نظرية فيثاغورس [hypot] لتغطية كامل أبعاد المكون مهما كان موضع المركز.
 * 3. دعم الإحداثيات النسبية والمطلقة (Relative & Absolute Coordinate Offsets):
 *    - دعم تمرير مركز الحركة كنسبة مئوية من العرض أو كإحداثيات نقطية مطلقة بدقة.
 */
package com.example.ui.components

// ---------------------------------------------------------------------
// استيراد أدوات الرسم والهندسة الرياضية من حزم Jetpack Compose
// ---------------------------------------------------------------------
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot

/**
 * [معدل الكشف الدائري المحسن - circularReveal]:
 * يقتطع محتوى المكون داخل مسار بيضاوي/دائري يتسع تدريجياً حسب نسبة التقدم [progress].
 * 
 * @param progress نسبة تقدم الحركة من 0.0f (مخفي بالكامل) إلى 1.0f (مكشوف بالكامل).
 * @param centerOffset إحداثيات نقطة انطلاق الدائرة (المركز).
 * @param isRelative تحديد ما إذا كانت إحداثيات X مئوية نسبية من عرض المكون.
 * @return معدل التنسيق المعدل والمدمج مع ذاكرة الرسم التخزينية.
 */
fun Modifier.circularReveal(
    progress: Float,
    centerOffset: Offset,
    isRelative: Boolean = false
): Modifier = this.drawWithCache {
    val path = Path()
    onDrawWithContent {
        val maxRadius = hypot(size.width, size.height)
        val radius = maxRadius * progress
        val cx = if (isRelative) size.width * centerOffset.x else centerOffset.x
        val cy = centerOffset.y
        path.reset()
        path.addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius))
        clipPath(path) {
            this@onDrawWithContent.drawContent()
        }
    }
}

