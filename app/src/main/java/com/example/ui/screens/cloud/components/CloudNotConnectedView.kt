package com.example.ui.screens.cloud.components

/*
 * =====================================================================================
 * حزمة واجهة عدم الاتصال بالسحابة (Cloud Not Connected View Component Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المكون البصري الإرشادي المعروض عند عدم ربط التطبيق بحساب Google:
 * - أيقونة توضيحية لعدم توفر الاتصال السحابي.
 * - نصوص إرشادية وتثقيفية تشرح أهمية ربط الحساب السحابي لحفظ البيانات وأمانها.
 * - زر تفاعلي لبدء عملية الربط وتسجيل الدخول عبر Google.
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary

/*
 * =====================================================================================
 * واجهة حالة عدم الاتصال بالسحابة (CloudNotConnectedView)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * شاشة إرشادية تظهر للمستخدم عندما لا يكون التطبيق مربوطاً بحساب Google Drive:
 * 1. عرض أيقونة وعنوان ورسالة توجيهية تشرح مميزات النسخ الاحتياطي السحابي.
 * 2. توفير زر ربط الحساب المباشر لبدء مصادقة Google Sign-In.
 *
 * [المُدخلات]:
 * - onConnectClick: رد نداء عند الضغط على زر ربط الحساب، أو null لإخفاء الزر.
 * - modifier: مغيرات الحجم والتموضع الخارجي.
 * =====================================================================================
 */
@Composable
fun CloudNotConnectedView(
    onConnectClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = stringResource(R.string.cloud_not_linked_title),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.cloud_not_linked_desc),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (onConnectClick != null) {
            Button(
                onClick = onConnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.backup_cloud_linking_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

