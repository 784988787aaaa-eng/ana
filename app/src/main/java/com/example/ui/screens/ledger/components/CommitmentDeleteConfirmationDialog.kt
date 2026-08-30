package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * حوار تأكيد حذف الالتزام المالي (Commitment Delete Confirmation Dialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية منبثقة تطلب تأكيداً صريحاً من المستخدم قبل حذف سجل التزام مالي:
 * 1. تمنع الحذف غير المقصود أو العرضي للالتزامات والديون المجدولة.
 * 2. تعرض رسالة تحذيرية واضحة باللغة العربية مع محاذاة منسقة لليمين.
 * 3. توفر زراً باللون الأحمر التحذيري (SoftRed) لتأكيد الحذف وزراً ملغياً للعودة الآمنة.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.R

/*
 * =====================================================================================
 * دالة العرض لحوار تأكيد الحذف (CommitmentDeleteConfirmationDialog Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - commitmentName: اسم الالتزام المراد حذفه (إذا كان null لا يتم عرض الحوار).
 * - onConfirmDelete: رد النداء لتنفيذ الحذف بالاسم المحدد.
 * - onDismiss: رد النداء لإغلاق الحوار وإلغاء العملية.
 * =====================================================================================
 */
@Composable
fun CommitmentDeleteConfirmationDialog(
    commitmentName: String?,
    onConfirmDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (commitmentName == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.ledger_confirm_delete_commitment_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.ledger_confirm_delete_commitment_msg),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete(commitmentName)
                    onDismiss()
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
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.90f)
    )
}

