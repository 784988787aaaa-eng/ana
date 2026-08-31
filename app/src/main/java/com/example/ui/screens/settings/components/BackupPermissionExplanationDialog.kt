/*
 * ================================================================
 * التوثيق الهندسي العربي الفائق — BackupPermissionExplanationDialog.kt
 * ================================================================
 * المسؤولية المعمارية:
 * حوار يشرح للمستخدم سبب طلب صلاحيات أو وصول مرتبط بالنسخ الاحتياطي قبل الانتقال إلى الإجراء الفعلي.
 *
 * المشهد التعليمي والبصري:
 * تخيل شاشة «الإعدادات» على الهاتف: كل بطاقة هنا تمثل منطقة قرار واضحة؛ يقرأ المستخدم
 * الحالة أولاً، ثم يختار الإجراء، ثم يظهر الحوار المناسب عند الحاجة. هذا الملف يشرح
 * كيف تتحول حالة النظام إلى عناصر مرئية دون نقل مسؤوليات التخزين أو الأمن إلى Compose.
 *
 * فهرس العناصر التنفيذية المكتشفة:
 * - `fun BackupPermissionExplanationDialog(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 *
 * قاعدة الثبات المطلقة:
 * النص التنفيذي الأصلي محفوظ ككتلة متصلة أدناه دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.settings.components

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsSuggest
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
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.WarningAmber

@Composable
fun BackupPermissionExplanationDialog(
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit,
    onUseInternalStorage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.settings_permissions_dialog_title),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    stringResource(R.string.settings_permissions_dialog_desc),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_permissions_storage_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Right)
                    Icon(Icons.Default.Folder, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_permissions_manage_files_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Right)
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                    }
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_permissions_notifications_label), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Right)
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.settings_permissions_fallback_note),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGrantPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(stringResource(R.string.settings_permissions_grant_btn), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUseInternalStorage
            ) {
                Text(stringResource(R.string.settings_permissions_internal_storage_btn), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * 1. الحفاظ على هذا المكوّن في طبقة العرض وعدم نقل قواعد العمل الحساسة إليه؛ القرار النهائي يجب أن يبقى في ViewModel/Domain.
 * 2. إضافة اختبارات UI للحالات الأساسية وحالات الخطأ والحدود دون تغيير السلوك الحالي.
 * 3. مراجعة الوصولية واتساق Material 3 عند اختلاف أحجام الشاشات والوضعين الفاتح والداكن.
 */
