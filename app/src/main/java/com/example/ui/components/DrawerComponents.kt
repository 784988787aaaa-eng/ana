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
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        Color.Transparent
    }
    val iconColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val textPrimary = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = fontWeight,
                color = textPrimary
            )
        }
    }
}

@Composable
fun ContactIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundLight = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)

    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(42.dp)
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

