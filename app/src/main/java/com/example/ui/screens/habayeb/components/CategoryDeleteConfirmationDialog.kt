package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة نافذة تأكيد حذف التصنيف المالي (Category Delete Confirmation Dialog Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على نافذة الحوار المخصصة لتأكيد حذف تصنيف عملاء محدد:
 * - تتيح خيارين للحذف: إما حذف التصنيف فقط (مع الإبقاء على العملاء)، أو حذف التصنيف والعملاء المرتبطين به.
 * - زر التراجع الإلغائي الملون بسمة التطبيق لمنع الحذف العرضي أو غير المقصود.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

/*
 * =====================================================================================
 * نافذة تأكيد حذف التصنيف (CategoryDeleteConfirmationDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية تأكيدية لإزالة تصنيف معين توفر ثلاث خيارات واضحة:
 * 1. إلغاء العملية والعودة.
 * 2. حذف التصنيف فقط وفك ارتباط الحسابات به دون حذف بيانات العملاء.
 * 3. حذف التصنيف وجميع الحسابات المرتبطة به كلياً (إجراء تحذيري مميز بلون الخطأ).
 *
 * [المُدخلات]:
 * - categoryName: اسم التصنيف المراد حذفه لعرضه في نص التأكيد.
 * - activeThemeColor: لون السمة النشط لزر الإلغاء الافتراضي.
 * - onDismiss: رد نداء لإغلاق النافذة عند الإلغاء.
 * - onConfirmDelete: رد نداء يحمل مؤشر حذف الحسابات المرتبطة (true إذا كان شاملاً للحسابات).
 * =====================================================================================
 */
@Composable
fun CategoryDeleteConfirmationDialog(
    categoryName: String,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onConfirmDelete: (deleteLinkedAccounts: Boolean) -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error
    val buttonShape = remember { RoundedCornerShape(8.dp) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.88f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.habayeb_category_delete_confirm, categoryName),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // زر الإلغاء (Cancel button)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeThemeColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_cancel),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // زر حذف التصنيف فقط (Delete Category Only)
                OutlinedButton(
                    onClick = {
                        onConfirmDelete(false)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_only),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // زر حذف التصنيف مع الحسابات المرتبطة (Delete Category and Linked Accounts)
                OutlinedButton(
                    onClick = {
                        onConfirmDelete(true)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, errorColor.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = errorColor
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_all_accounts),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

