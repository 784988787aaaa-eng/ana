package com.example.ui.components

/*
 * =====================================================================================
 * حزمة عناصر القائمة الجانبية (Drawer Components Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على العناصر والمكونات الفرعية المستخدمة في بناء القائمة الجانبية (Navigation Drawer)،
 * مثل أزرار التنقل الرئيسية، مؤشرات التحديد، وأيقونات التواصل.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.sp

/*
 * =====================================================================================
 * المكون الفرعي: عنصر القائمة الجانبية (DrawerItem)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يمثل صفاً تفاعلياً واحداً داخل القائمة الجانبية للتنقل بين شاشات التطبيق:
 * 1. يعرض أيقونة القسم وعنوانه النصي بتنسيق متناسق.
 * 2. يبرز العنصر النشط (المحدد حالياً) بتغيير لون الخلفية وسماكة الخط وإظهار شريط عمودي جانبي.
 * 3. يستجيب للنقر عبر تنفيذ دالة رد النداء (onClick) لتغيير الشاشة وإغلاق القائمة.
 *
 * [البيانات والمُدخلات]:
 * - selected: قيمة منطقية تشير إلى ما إذا كانت هذه الشاشة هي المعروضة حالياً.
 * - icon: الأيقونة الشعاعية المعبرة عن القسم.
 * - label: نص عنوان القسم باللغة العربية.
 * - onClick: الإجراء المنفذ عند النقر على العنصر.
 * =====================================================================================
 */
@Composable
fun DrawerItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    /*
     * ---------------------------------------------------------------------------------
     * استخراج الألوان والسمات البصرية وفق حالة التحديد (Visual States & Colors)
     * ---------------------------------------------------------------------------------
     * يتم تكييف لون الحاوية وسماكة النص وفق حالة التحديد (selected) لتمييز العنصر النشط.
     * ---------------------------------------------------------------------------------
     */
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    /*
     * ---------------------------------------------------------------------------------
     * بناء سطح العنصر التفاعلي (Surface Container)
     * ---------------------------------------------------------------------------------
     * يتم استخدام Surface لتوفير تفاعل سلس عند اللمس مع حواف دائرية أنيقة.
     * ---------------------------------------------------------------------------------
     */
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // أيقونة القسم التوضيحية
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // عنوان القسم النصي
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = fontWeight,
                    color = textPrimary
                )
            }

            /*
             * مؤشر بصري عمودي يظهر على طرف العنصر عند تحديده لتأكيد النشاط الحالي
             */
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight(0.6f)
                        .align(Alignment.CenterEnd)
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * المكون الفرعي: أيقونة التواصل الدائرية (ContactIcon)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * زر أيقونة دائري مدمج يُستخدم لروابط منصات التواصل الاجتماعي ووسائل الاتصال:
 * 1. يوفر مساحة لمس مريحة متوافقة مع معايير إمكانية الوصول (Accessibility).
 * 2. يقدم خلفية دائرية ملونة خفيفة تتفاعل مع لمسات المستخدم.
 *
 * [المُدخلات]:
 * - icon: الأيقونة الشعاعية لمنصة التواصل أو وسيلة الاتصال.
 * - onClick: الحدث المنفذ عند النقر.
 * =====================================================================================
 */
@Composable
fun ContactIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundLight = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundLight)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

