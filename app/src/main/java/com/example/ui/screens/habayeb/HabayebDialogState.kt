package com.example.ui.screens.habayeb

/*
 * =====================================================================================
 * حزمة حالات حوارات شاشة الحبايب (Habayeb Dialog State Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على التسلسل الهرمي المختوم (Sealed Interface) لتمثيل حالات الحوارات
 * والورقات السفلية والنوافذ المنبثقة التابعة لشاشة الحبايب وإدارة العملاء.
 * =====================================================================================
 */

import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.state.CustomerUiState

/*
 * =====================================================================================
 * واجهة حالات الحوارات المختومة (HabayebDialogState)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تمثل جميع الحالات الممكنة للنوافذ المنبثقة والورقات السفلية في شاشة الحبايب لضمان
 * إدارة حالة أحادية الاتجاه (UDF) وخالية من التعارض:
 * - None: لا يوجد أي حوار معروض.
 * - AddCustomer: حوار إضافة عميل جديد.
 * - AddTransaction: حوار إضافة قيد مالي جديد لعميل محدد أو تعديل قيد موجود.
 * - EditCustomer: حوار تعديل بيانات العميل الأساسية.
 * - DeleteConfirm: حوار تأكيد حذف عميل أو مجموعة عملاء محددين.
 * - AddCategory: حوار إنشاء فئة تصنيف جديدة.
 * - BulkAssignCategory: حوار إسناد وتعيين فئة لعدة عملاء محددين دفعة واحدة.
 * - ContextMenu: الورقة السفلية للخيارات السريعة لعميل محدد.
 * - DeviceActivation: حوار تفعيل ترخيص الجهاز عند طلب مزايا تتطلب التفعيل.
 * =====================================================================================
 */
sealed interface HabayebDialogState {
    object None : HabayebDialogState
    object AddCustomer : HabayebDialogState
    data class AddTransaction(
        val customer: HabayebCustomer,
        val defaultType: String = TransactionType.OWED_BY_THEM.value,
        val editingTx: HabayebTransaction? = null
    ) : HabayebDialogState
    data class EditCustomer(val customer: HabayebCustomer) : HabayebDialogState
    object DeleteConfirm : HabayebDialogState
    object AddCategory : HabayebDialogState
    object BulkAssignCategory : HabayebDialogState
    data class ContextMenu(val customer: CustomerUiState) : HabayebDialogState
    object DeviceActivation : HabayebDialogState
}

