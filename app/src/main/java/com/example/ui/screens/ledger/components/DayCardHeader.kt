package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * ترويسة بطاقة اليوم في دفتر الأستاذ (Day Card Header Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * شريط العنوان العلوي لكل بطاقة يوم في شاشة دفتر الأستاذ:
 * 1. يعرض في الطرف الأيمن (RTL): مربع اختيار اليوم الدائري (عند تفعيل وضع التحديد) + اسم اليوم والتاريخ (مثال: الأربعاء 19/08).
 * 2. يعرض في الطرف الأيسر: صافي الحركة اليومية ملوناً ومسبوقاً بمؤشر الصعود أو الهبوط (▲ + أو ▼).
 * 3. يحتوي على سهم المؤشر (Chevron) الذي يوضح حالة الطي أو التوسيع للبطاقة.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SelectionGreen

/**
 * لون أيقونة علامة الصح داخل مربع اختيار اليوم.
 */

/*
 * =====================================================================================
 * دالة العرض لترويسة بطاقة اليوم (DayCardHeader Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - formattedDateHeader: النص المنسق لاسم اليوم وتاريخه.
 * - formattedNetAmount: النص المنسق لمبلغ الصافي مع إشارة الاتجاه.
 * - netHeaderColor: لون نص الصافي (أخضر للفائض، أحمر للعجز).
 * - isExpanded: راية توضح هل البطاقة مفتوحة حالياً.
 * - isDaySelected: راية توضح هل هذا اليوم محدد ضمن مجموعة الأيام المختارة.
 * - isDaySelectionMode: راية تفعيل وضع تحديد الأيام المتعددة.
 * - modifier: مغير التنسيق والمحاذاة.
 * =====================================================================================
 */
@Composable
fun DayCardHeader(
    formattedDateHeader: String,
    formattedNetAmount: String,
    netHeaderColor: Color,
    isExpanded: Boolean,
    isDaySelected: Boolean,
    isDaySelectionMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // اليمين في RTL: اسم اليوم والتاريخ + دائرة الاختيار عند التحديد
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isDaySelectionMode) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isDaySelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDaySelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Text(
                text = formattedDateHeader,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // اليسار في RTL: مبلغ الصافي + سهم التوسيع/الطي
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formattedNetAmount,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = netHeaderColor
            )

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

