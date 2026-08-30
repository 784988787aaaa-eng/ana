package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * حوار تأكيد حذف المعاملة المالية (Day Card Delete Dialog Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة تحذيرية تنبثق عند طلب المستخدم حذف معاملة مالية من سجل اليوم:
 * 1. تمنع الحذف العرضي أو غير المقصود للبيانات المالية وتطلب تأكيداً صريحاً.
 * 2. تبرز زر الحذف باللون الأحمر التحذيري الهادئ (SoftRed) للتنبيه على خطورة الإجراء.
 * 3. تدعم اتجاه الكتابة من اليمين لليسار (RTL) بالكامل وتلتزم بإرشادات تصميم Material 3.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.R

/*
 * =====================================================================================
 * دالة العرض لحوار تأكيد الحذف (DayCardDeleteDialog Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - txId: المعرف الفريد للمعاملة المراد حذفها.
 * - onConfirm: رد النداء لتنفيذ عملية الحذف مع تمرير معرف المعاملة.
 * - onDismiss: رد النداء لإلغاء وإغلاق نافذة الحوار دون حذف.
 * =====================================================================================
 */
@Composable
fun DayCardDeleteDialog(
    txId: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.ledger_confirm_delete_tx_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.ledger_confirm_delete_tx_msg),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(txId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.ledger_confirm_delete_btn),
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.common_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.90f)
        )
    }
}

