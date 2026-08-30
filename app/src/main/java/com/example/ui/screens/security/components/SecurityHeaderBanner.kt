/**
 * =====================================================================
 * ملف: SecurityHeaderBanner.kt
 * الحزمة: com.example.ui.screens.security.components
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الملف شريط الإشعار العلوي (Header Banner) في شاشة إعدادات الأمان والترخيص.
 * يقدم هذا المكون ملخصاً بصرياً سريعاً ومباشراً يوضح للمستخدم ما إذا كان نظام
 * حماية التطبيق بكلمة المرور مفعلاً حالياً أو معطلاً، مع تغيير الأيقونة
 * ونصوص الحالة والألوان المصاحبة ديناميكياً وفقاً للوضع الأمني الحالي.
 * 
 * [التكامل وتجربة المستخدم]:
 * - يتغير لون وشكل الشارة البصرية بين اللون الأخضر (أيقونة الحساب الموثق VerifiedUser)
 *   عند التفعيل، واللون الأزرق المحايد (أيقونة درع الأمان Security) عند التعطيل.
 * - يراعي التوافق الكامل مع الثيمين النهاري والليلي ودعم القراءة باللغة العربية.
 * =====================================================================
 */
package com.example.ui.screens.security.components

// ---------------------------------------------------------------------
// استيراد أدوات واجهة Jetpack Compose ومكونات Material Design 3
// ---------------------------------------------------------------------
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary

/**
 * =====================================================================
 * [شريط حالة الأمان العلوي - SecurityHeaderBanner]:
 * 
 * [الهدف والغرض]:
 * عرض بطاقة علوية توضح حالة تأمين التطبيق الحالية (مفعل / غير مفعل) مع شارة أيقونة معبرة.
 * 
 * [البيانات المستلمة]:
 * @param isAlreadyPasscodeEnabled قيمة منطقية تحدد ما إذا كان قفل رمز المرور مفعلاً حالياً.
 * @param modifier مخصصات الأبعاد والمحاذاة للبطاقة.
 * =====================================================================
 */
@Composable
fun SecurityHeaderBanner(
    isAlreadyPasscodeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // -----------------------------------------------------------------
    // بطاقة الحاوية الرئيسية مع إطار خارجي رفيع متناسق
    // -----------------------------------------------------------------
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp, 
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // تحديد الألوان الديناميكية للأيقونة وخلفيتها وفقاً لحالة القفل والسمة النشطة
        val isDark = MaterialTheme.colorScheme.background.run { red < 0.5f }
        val iconBg = if (isAlreadyPasscodeEnabled) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
        val iconTint = if (isAlreadyPasscodeEnabled) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

        // -------------------------------------------------------------
        // توزيع عناصر الشريط: النصوص التوضيحية على اليمين وشارة الأيقونة
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // عمود النصوص: العنوان التلخيصي والشرح المفصل للحالة
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isAlreadyPasscodeEnabled) stringResource(id = R.string.sec_status_active) else stringResource(id = R.string.sec_status_inactive),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isAlreadyPasscodeEnabled) stringResource(id = R.string.sec_desc_active) else stringResource(id = R.string.sec_desc_inactive),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // شارة الأمان البصرية الدائرية (VerifiedUser عند التفعيل أو Security عند التعطيل)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconBg,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlreadyPasscodeEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

