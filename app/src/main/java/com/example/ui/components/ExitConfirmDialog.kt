package com.example.ui.components

/*
 * =====================================================================================
 * حزمة المكونات المرئية لواجهة المستخدم (UI Components Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على مربعات الحوار التفاعلية ونوافذ التأكيد التي تحمي المستخدم
 * من الإجراءات غير المقصودة (مثل الخروج غير المتعمد أو حذف البيانات).
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary

/*
 * =====================================================================================
 * نافذة تأكيد الخروج من التطبيق (ExitConfirmDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية منبثقة تظهر عند ضغط المستخدم على زر الرجوع في الشاشة الرئيسية:
 * 1. تمنع الخروج المفاجئ وغير المقصود من التطبيق أثناء العمل.
 * 2. تعرض رسالة توضيحية تسأل المستخدم عما إذا كان يرغب في إغلاق التطبيق.
 * 3. تتيح خيار "عدم الإظهار مجدداً" (Don't show again) عبر مربع اختيار (Checkbox)
 *    لتخزين تفضيل المستخدم في التفضيلات الدائمة وتجاوز هذه النافذة مستقبلاً.
 * 4. توفر زرين متناسقين: أحدهما للبقاء في التطبيق (إلغاء) والآخر لتأكيد الخروج.
 *
 * [البيانات والمُدخلات]:
 * - show: قيمة منطقية لتحديد ما إذا كان مربع الحوار معروضاً على الشاشة أم مخفياً.
 * - onDismiss: دالة تُستدعى عند الإلغاء أو الضغط على الخلفية للبقاء في التطبيق.
 * - onConfirm: دالة تُستدعى عند تأكيد الخروج، وتمرر قيمة خيار (dontShowAgain).
 * =====================================================================================
 */
@Composable
fun ExitConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    // التحقق من حالة الظهور؛ إذا كانت false يتم الخروج دون رسم أي عنصر
    if (!show) return

    /*
     * ---------------------------------------------------------------------------------
     * حالة مربع خيار "عدم الإظهار مجدداً" (Don't show again State)
     * ---------------------------------------------------------------------------------
     * يتم تذكر حالة التحديد محلياً، وتمريرها إلى رد النداء onConfirm لحفظها في الإعدادات.
     * ---------------------------------------------------------------------------------
     */
    var dontShowAgain by remember { mutableStateOf(false) }

    /*
     * ---------------------------------------------------------------------------------
     * بناء غطاء الخلفية المعتم (Modal Scrim Overlay)
     * ---------------------------------------------------------------------------------
     * طبقة معتمة تغطي الشاشة بالكامل مع إمكانية النقر عليها لإلغاء الحوار.
     * ---------------------------------------------------------------------------------
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        /*
         * -----------------------------------------------------------------------------
         * بطاقة مربع الحوار الرئيسية (Dialog Card Container)
         * -----------------------------------------------------------------------------
         * بطاقة بارزة بحواف دائرية وظلال ناعمة تمنع تسرب أحداث النقر إلى الخلفية المعتمة.
         * -----------------------------------------------------------------------------
         */
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .widthIn(max = 310.dp)
                .fillMaxWidth(0.80f)
                .clickable(enabled = false) { } // منع تسرب أحداث النقر
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // عنوان نافذة التأكيد
                Text(
                    text = stringResource(id = R.string.dialog_exit_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // نص الرسالة التوضيحية لتأكيد الرغبة في الخروج
                Text(
                    text = stringResource(id = R.string.dialog_exit_message),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                /*
                 * ---------------------------------------------------------------------
                 * صف خيار "عدم الإظهار مرة أخرى" (Checkbox Row)
                 * ---------------------------------------------------------------------
                 */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dontShowAgain = !dontShowAgain }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.dialog_exit_dont_show_again),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                /*
                 * ---------------------------------------------------------------------
                 * أزرار اتخاذ القرار (Action Buttons: Cancel vs Exit)
                 * ---------------------------------------------------------------------
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر الإلغاء والبقاء في التطبيق
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.common_cancel),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // زر تأكيد الخروج من التطبيق
                    Button(
                        onClick = { onConfirm(dontShowAgain) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_exit_confirm),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

