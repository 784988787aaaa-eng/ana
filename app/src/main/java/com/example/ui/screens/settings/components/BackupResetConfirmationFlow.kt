/*
 * ================================================================
 * التوثيق الهندسي العربي الفائق — BackupResetConfirmationFlow.kt
 * ================================================================
 * المسؤولية المعمارية:
 * ينسق مسار تأكيد إعادة ضبط بيانات النسخ الاحتياطي ويمنع الانتقال المباشر من النية إلى العملية الحساسة دون تأكيد.
 *
 * المشهد التعليمي والبصري:
 * تخيل شاشة «الإعدادات» على الهاتف: كل بطاقة هنا تمثل منطقة قرار واضحة؛ يقرأ المستخدم
 * الحالة أولاً، ثم يختار الإجراء، ثم يظهر الحوار المناسب عند الحاجة. هذا الملف يشرح
 * كيف تتحول حالة النظام إلى عناصر مرئية دون نقل مسؤوليات التخزين أو الأمن إلى Compose.
 *
 * فهرس العناصر التنفيذية المكتشفة:
 * - `fun BackupResetConfirmationFlow(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val context = LocalContext.current`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `var currentStep by remember { mutableStateOf(1) }`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 *
 * قاعدة الثبات المطلقة:
 * النص التنفيذي الأصلي محفوظ ككتلة متصلة أدناه دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.settings.components

import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.BackupSyncViewModel

@Composable
fun BackupResetConfirmationFlow(
    viewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onSuccessReset: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    if (currentStep == 1) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = stringResource(R.string.backup_reset1_title),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.backup_reset1_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = { currentStep = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_reset_confirm_btn),
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.backup_reset_cancel_btn),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        )
    } else if (currentStep == 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = stringResource(R.string.backup_reset2_title),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.backup_reset2_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLocalCopyAndWipeMemory(context)
                        Toast.makeText(context, context.getString(R.string.backup_toast_reset_success), Toast.LENGTH_LONG).show()
                        onSuccessReset()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_reset_final_btn),
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.backup_reset_final_cancel_btn),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * 1. الحفاظ على هذا المكوّن في طبقة العرض وعدم نقل قواعد العمل الحساسة إليه؛ القرار النهائي يجب أن يبقى في ViewModel/Domain.
 * 2. إضافة اختبارات UI للحالات الأساسية وحالات الخطأ والحدود دون تغيير السلوك الحالي.
 * 3. مراجعة الوصولية واتساق Material 3 عند اختلاف أحجام الشاشات والوضعين الفاتح والداكن.
 */
