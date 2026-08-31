/*
 * ================================================================
 * التوثيق الهندسي العربي الفائق — QuadBackupItem.kt
 * ================================================================
 * المسؤولية المعمارية:
 * عنصر مفرد داخل QuadBackupCard، يمثل مساراً أو وظيفة محددة للنسخ الاحتياطي.
 *
 * المشهد التعليمي والبصري:
 * تخيل شاشة «الإعدادات» على الهاتف: كل بطاقة هنا تمثل منطقة قرار واضحة؛ يقرأ المستخدم
 * الحالة أولاً، ثم يختار الإجراء، ثم يظهر الحوار المناسب عند الحاجة. هذا الملف يشرح
 * كيف تتحول حالة النظام إلى عناصر مرئية دون نقل مسؤوليات التخزين أو الأمن إلى Compose.
 *
 * فهرس العناصر التنفيذية المكتشفة:
 * - `fun QuadBackupItem(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 *
 * قاعدة الثبات المطلقة:
 * النص التنفيذي الأصلي محفوظ ككتلة متصلة أدناه دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuadBackupItem(
    title: String,
    description: String,
    accentColor: Color,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 13.sp
                )
            }
            content()
        }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * 1. الحفاظ على هذا المكوّن في طبقة العرض وعدم نقل قواعد العمل الحساسة إليه؛ القرار النهائي يجب أن يبقى في ViewModel/Domain.
 * 2. إضافة اختبارات UI للحالات الأساسية وحالات الخطأ والحدود دون تغيير السلوك الحالي.
 * 3. مراجعة الوصولية واتساق Material 3 عند اختلاف أحجام الشاشات والوضعين الفاتح والداكن.
 */
