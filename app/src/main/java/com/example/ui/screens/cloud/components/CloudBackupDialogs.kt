package com.example.ui.screens.cloud.components

/*
 * =====================================================================================
 * حزمة نوافذ حوارات النسخ الاحتياطي السحابي (Cloud Backup Dialogs Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على مجموعة النوافذ المنبثقة التفاعلية لإدارة السحابة (Google Drive):
 * - مؤشر التقدم الدائري للعمليات الجارية (CloudOngoingActionDialog).
 * - حوار تأكيد استعادة نسخة احتياطية محددة (CloudRestoreConfirmDialog).
 * - حوار تأكيد حذف نسخة احتياطية مفردة (CloudDeleteConfirmDialog).
 * - حوار تأكيد الحذف المتعدد لعدة نسخ احتياطية (CloudMultiDeleteConfirmDialog).
 * =====================================================================================
 */

import androidx.compose.material3.MaterialTheme
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.CloudBackupFile
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.BackupSyncViewModel

/*
 * =====================================================================================
 * نافذة العمليات السحابية الجارية (CloudOngoingActionDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة تقدم غير قابلة للإلغاء تظهر أثناء الرفع أو التنزيل أو الحذف لمنع تداخل العمليات:
 * 1. مؤشر تقدم دائري متحرك بلون مميز.
 * 2. نص توضيحي يصف طبيعة العملية السحابية الحالية.
 * =====================================================================================
 */
@Composable
fun CloudOngoingActionDialog(ongoingActionMessage: String) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Text(
                    text = ongoingActionMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * حوار تأكيد استعادة النسخة السحابية (CloudRestoreConfirmDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة تحذيرية لتأكيد استبدال واسترجاع قاعدة البيانات من Google Drive:
 * 1. إظهار اسم وتاريخ النسخة المحددة للتحذير من استبدال البيانات الحالية.
 * 2. تنفيذ التنزيل والاستعادة عبر ViewModel مع إشعار بنتيجة العملية عبر Toast.
 * =====================================================================================
 */
@Composable
fun CloudRestoreConfirmDialog(
    context: Context,
    targetId: String,
    cloudBackups: List<CloudBackupFile>,
    viewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onStartAction: (String) -> Unit,
    onCompleteAction: () -> Unit,
    onSheetDismiss: () -> Unit
) {
    val fileItem = cloudBackups.find { it.id == targetId }
    val displayName = fileItem?.name ?: stringResource(R.string.cloud_default_selected_backup)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.cloud_restore_confirm_title),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.cloud_restore_confirm_desc, displayName),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartAction(context.getString(R.string.cloud_progress_restoring))
                    viewModel.restoreFromGoogleDriveById(context, targetId) { success ->
                        onCompleteAction()
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_restore_success), Toast.LENGTH_LONG).show()
                            onSheetDismiss()
                        } else {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_restore_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.cloud_btn_restore_confirm),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cloud_btn_cancel_action),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    )
}

/*
 * =====================================================================================
 * حوار تأكيد حذف نسخة احتياطية مفردة (CloudDeleteConfirmDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة تحذيرية لتأكيد حذف ملف نسخة احتياطية من التخزين السحابي Google Drive:
 * 1. عرض اسم الملف المستهدف.
 * 2. تنفيذ الحذف عبر المعرف (ID) وتحديث القائمة وإشعار المستخدم.
 * =====================================================================================
 */
@Composable
fun CloudDeleteConfirmDialog(
    context: Context,
    targetId: String,
    cloudBackups: List<CloudBackupFile>,
    viewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onStartAction: (String) -> Unit,
    onCompleteAction: () -> Unit
) {
    val fileItem = cloudBackups.find { it.id == targetId }
    val displayName = fileItem?.name ?: stringResource(R.string.cloud_default_selected_backup)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.cloud_delete_confirm_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.cloud_delete_confirm_desc, displayName),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartAction(context.getString(R.string.cloud_progress_deleting))
                    viewModel.deleteCloudBackupById(targetId) { success ->
                        onCompleteAction()
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_delete_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_delete_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.cloud_btn_delete_confirm),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cloud_btn_generic_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    )
}

/*
 * =====================================================================================
 * حوار تأكيد الحذف المتعدد للنسخ السحابية (CloudMultiDeleteConfirmDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة لتأكيد الحذف الجماعي لمجموعة من النسخ السحابية المحددة:
 * 1. عرض عدد العناصر المحددة للحذف.
 * 2. تنفيذ الحذف المتوازي السريع عبر السحابة وتحديث واجهة المستخدم.
 * =====================================================================================
 */
@Composable
fun CloudMultiDeleteConfirmDialog(
    context: Context,
    selectedFileIds: List<String>,
    viewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onStartAction: (String) -> Unit,
    onCompleteAction: (Boolean) -> Unit
) {
    val selectedCount = selectedFileIds.size
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.cloud_multi_delete_confirm_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.cloud_multi_delete_confirm_desc, selectedCount),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onStartAction(context.getString(R.string.cloud_progress_multi_deleting))
                    viewModel.deleteMultipleCloudBackupsByIds(selectedFileIds.toList()) { success ->
                        onCompleteAction(success)
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_multi_delete_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.cloud_toast_multi_delete_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.cloud_btn_multi_delete_confirm),
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cloud_btn_generic_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    )
}

