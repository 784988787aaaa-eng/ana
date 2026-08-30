package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة نوافذ إدارة عملاء الحبايب (Habayeb Dialogs Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال Compose وسيطة تربط بين واجهات الحوارات ونموذج العرض (ViewModel):
 * 1. نافذة تأكيد الحذف (DeleteConfirmDialog): معالجة حذف عميل فردي أو مجموعة عملاء محددين جماعياً.
 * 2. نافذة تعديل بيانات العميل (EditCustomerDialog): تعديل الاسم ورقم الهاتف مع التحقق من عدم التكرار.
 * =====================================================================================
 */

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.ui.viewmodel.HabayebFinanceViewModel

/*
 * =====================================================================================
 * نافذة تأكيد حذف العملاء (DeleteConfirmDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون وسيط يربط حوار تأكيد الحذف بنموذج العرض لحذف العملاء المحدد فردياً أو جماعياً.
 *
 * [المُدخلات]:
 * - selectedCustomerIds: قائمة معرفات العملاء المطلوب حذفهم.
 * - viewModel: نموذج العرض المالي الخاص بالحبايب.
 * - onDismiss: رد نداء عند إغلاق أو إلغاء الحوار.
 * - onSuccessBulkDelete: رد نداء ينفذ بعد اكتمال الحذف بنجاح.
 * =====================================================================================
 */
@Composable
fun DeleteConfirmDialog(
    selectedCustomerIds: List<String>,
    viewModel: HabayebFinanceViewModel,
    onDismiss: () -> Unit,
    onSuccessBulkDelete: () -> Unit
) {
    val context = LocalContext.current
    val customersState by viewModel.habayebCustomersState.collectAsStateWithLifecycle()
    val singleCustomer = remember(selectedCustomerIds, customersState) {
        val targetId = selectedCustomerIds.singleOrNull()
        if (targetId != null) customersState.find { it.id == targetId } else null
    }

    CustomerDeleteConfirmationDialog(
        customer = singleCustomer,
        selectedCustomerIds = selectedCustomerIds,
        onConfirm = {
            viewModel.deleteMultipleHabayebCustomers(selectedCustomerIds)
            Toast.makeText(context, context.getString(R.string.habayeb_toast_delete_success), Toast.LENGTH_SHORT).show()
            onSuccessBulkDelete()
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun EditCustomerDialog(
    customer: HabayebCustomer,
    viewModel: HabayebFinanceViewModel,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onCustomerUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val existingCustomers by viewModel.habayebCustomersState.collectAsStateWithLifecycle()

    CustomerEditDialog(
        customer = customer,
        activeThemeColor = activeThemeColor,
        existingCustomers = existingCustomers,
        onConfirm = { newName, newPhone ->
            if (newName.isNotBlank()) {
                viewModel.updateHabayebCustomer(
                    customer.copy(
                        name = newName.trim(),
                        phone = newPhone.trim()
                    )
                )
                Toast.makeText(context, context.getString(R.string.habayeb_toast_update_success), Toast.LENGTH_SHORT).show()
                onCustomerUpdated()
            }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}
