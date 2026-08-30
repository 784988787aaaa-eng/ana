package com.example.ui.components

/*
 * =====================================================================================
 * حزمة المكونات المرئية لواجهة المستخدم (UI Components Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على المكونات والعناصر الرسومية المشتركة في التطبيق مثل
 * التذييلات، التنبيهات، القوائم الجانبية، ومربعات الحوار.
 * =====================================================================================
 */

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.whatsappColor
import androidx.compose.ui.graphics.luminance

/*
 * =====================================================================================
 * مكون تذييل معلومات المطور والدعم الفني (DeveloperSealFooter)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة مرئية أنيقة تُعرض في أسفل شاشات الإعدادات أو القائمة الجانبية:
 * 1. إبراز حقوق وتوقيع مطور التطبيق لتعزيز الموثوقية والهوية المهنية.
 * 2. عرض رقم الدعم الفني المباشر للتواصل والاستفسارات.
 * 3. توفير زر تفاعلي سريع يفتح محادثة واتساب (WhatsApp) مباشرة مع فريق الدعم الفني.
 *
 * [البيانات والمُدخلات]:
 * - modifier: مغير الخصائص لتحديد الهوامش والأبعاد والموضع داخل الشاشة الحاضنة.
 *
 * [التدفق والترابط]:
 * يستخرج أرقام التواصل ونصوص الشكر من موارد النصوص (strings.xml)، ويستخدم LocalContext
 * لإطلاق نية النظام (Intent) وفتح تطبيق واتساب الخارجي.
 * =====================================================================================
 */
@Composable
fun DeveloperSealFooter(modifier: Modifier = Modifier) {
    /*
     * ---------------------------------------------------------------------------------
     * استخراج سياق التطبيق وبيانات الاتصال من الموارد (Context & Contact Info)
     * ---------------------------------------------------------------------------------
     * يتم جلب سياق الأندرويد الحالي لبدء الأنشطة الخارجية (Start Activity)،
     * واستخراج نصوص الدعم الفني مع تصفية رقم الواتساب لإبقاء الأرقام فقط وتجهيز رابط URL.
     * ---------------------------------------------------------------------------------
     */
    val context = LocalContext.current
    val supportPhone = stringResource(R.string.support_phone_number)
    val whatsappNumber = stringResource(R.string.support_whatsapp_number).filter { it.isDigit() }
    
    /*
     * ---------------------------------------------------------------------------------
     * بناء بطاقة التذييل الجمالية (Card Container)
     * ---------------------------------------------------------------------------------
     * يتم رسم بطاقة ذات حواف دائرية وظلال ناعمة بخلفية شبه شفافة تتناغم مع سمة التطبيق.
     * ---------------------------------------------------------------------------------
     */
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // نص اسم المطور وحقوق الملكية البرمجية
            Text(
                text = stringResource(R.string.developer_credit),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // رقم هاتف الدعم الفني والاستفسارات
            Text(
                text = supportPhone,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            /*
             * -------------------------------------------------------------------------
             * زر التواصل المباشر عبر واتساب (WhatsApp Contact Button)
             * -------------------------------------------------------------------------
             * عند النقر، يتم تكوين نية عرض (ACTION_VIEW) برابط "https://wa.me/NUMBER"
             * وتمريرها لنظام التشغيل لفتح محادثة فورية داخل تطبيق الواتساب.
             * -------------------------------------------------------------------------
             */
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/$whatsappNumber")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = whatsappColor(MaterialTheme.colorScheme.background.luminance() < 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.contact_whatsapp_direct),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

