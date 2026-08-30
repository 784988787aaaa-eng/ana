package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة متحكم واجهة دفتر الأستاذ اليومي (Main Ledger UI Controller Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على وحدة إدارة الحالة التشغيلية ومتحكم واجهة المستخدم لشاشة اليومية الرئيسية،
 * بما في ذلك حالات الحوارات، التحديد المتعدد للأيام والمعاملات، وتوسيع وطي الشهور والأيام.
 * =====================================================================================
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.TransactionDb

/*
 * =====================================================================================
 * الحالات المغلقة لحوارات دفتر اليومية (MainLedgerDialogState)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * واجهة مختومة (Sealed Interface) تمثل الحالة المنبثقة النشطة حالياً في دفتر الأستاذ:
 * - None: لا توجد نوافذ مفتوحة.
 * - AddTransaction: نافذة إضافة أو تعديل قيد مالي (مدفوعات/مقبوضات).
 * - Search: شاشة أو حوار البحث في القيود.
 * - CommitmentsList: قائمة الالتزامات والأقساط الدورية.
 * - AddCommitment: إضافة أو تعديل التزام ثابت.
 * - ReorderCommitment: حوار إعادة ترتيب الالتزامات.
 * - DeleteDaysConfirm: حوار تأكيد حذف أيام كاملة بقيودها.
 * - DeviceActivation: حوار طلب تفعيل ترخيص الجهاز.
 * =====================================================================================
 */
sealed interface MainLedgerDialogState {
    object None : MainLedgerDialogState
    data class AddTransaction(val type: String = "EXPENSE", val editingTx: TransactionDb? = null) : MainLedgerDialogState
    object Search : MainLedgerDialogState
    object CommitmentsList : MainLedgerDialogState
    data class AddCommitment(val editingCommitment: FixedCommitment? = null) : MainLedgerDialogState
    data class ReorderCommitment(val target: FixedCommitment) : MainLedgerDialogState
    object DeleteDaysConfirm : MainLedgerDialogState
    object DeviceActivation : MainLedgerDialogState
}

/*
 * =====================================================================================
 * متحكم واجهة دفتر الأستاذ (MainLedgerUiController)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * كلاس إدارة حالة الواجهة (State Holder) يفصل منطق إدارة الحالة البصرية عن شجرة Compose:
 * 1. التحكم بفتح وإغلاق النوافذ المنبثقة.
 * 2. إدارة الأيام الموسعة والمطوية والشهور المنهارة.
 * 3. إدارة وضع التحديد المتعدد للمعاملات (Transaction Selection Mode).
 * 4. إدارة وضع التحديد المتعدد للأيام (Day Selection Mode) لحذف أو ترحيل أيام كاملة.
 * =====================================================================================
 */
class MainLedgerUiController internal constructor(
    private val habayebActiveState: MutableState<Boolean>
) {
    // حالة الحوار النشط حالياً
    var activeDialogState by mutableStateOf<MainLedgerDialogState>(MainLedgerDialogState.None)
    
    // مفاتيح الأيام الموسعة حالياً لعرض قيودها
    var expandedDayKeys by mutableStateOf(setOf<String>())
    
    // وضع تحديد المعاملات المفردة
    var isSelectionMode by mutableStateOf(false)
    val selectedTxIds = mutableStateListOf<String>()
    
    // الشهور المطوية في العرض الزمني
    var collapsedMonths by mutableStateOf(setOf<String>())

    // تتبع حالة تنشيط شاشة الحبايب المدمجة
    var isHabayebActive: Boolean
        get() = habayebActiveState.value
        set(value) {
            habayebActiveState.value = value
        }

    // وضع تحديد الأيام الكاملة
    var isDaySelectionMode by mutableStateOf(false)
    val selectedDayKeys = mutableStateListOf<String>()

    /*
     * إلغاء وتفريغ كافة التحديدات للأيام والمعاملات
     */
    fun clearSelection() {
        selectedTxIds.clear()
        selectedDayKeys.clear()
        isSelectionMode = false
        isDaySelectionMode = false
    }

    /*
     * تبديل حالة طي أو فتح شهر زمني محدد
     */
    fun toggleMonthCollapsed(mKey: String) {
        collapsedMonths = if (collapsedMonths.contains(mKey)) {
            collapsedMonths - mKey
        } else {
            collapsedMonths + mKey
        }
    }

    /*
     * معالجة النقر على رأس اليوم (توسيع/طي أو تحديد في وضع التحديد)
     */
    fun handleDayClick(key: String) {
        if (isDaySelectionMode) {
            if (selectedDayKeys.contains(key)) {
                selectedDayKeys.remove(key)
                if (selectedDayKeys.isEmpty()) isDaySelectionMode = false
            } else {
                selectedDayKeys.add(key)
            }
        } else {
            expandedDayKeys = if (expandedDayKeys.contains(key)) expandedDayKeys - key else expandedDayKeys + key
        }
    }

    /*
     * معالجة النقر المطول على رأس اليوم لبدء وضع تحديد الأيام
     */
    fun handleDayLongClick(key: String) {
        if (!isDaySelectionMode && !isSelectionMode) {
            isDaySelectionMode = true
            selectedDayKeys.add(key)
        } else if (isDaySelectionMode) {
            if (selectedDayKeys.contains(key)) {
                selectedDayKeys.remove(key)
                if (selectedDayKeys.isEmpty()) isDaySelectionMode = false
            } else {
                selectedDayKeys.add(key)
            }
        }
    }

    /*
     * تبديل تحديد معاملة مالية معينة
     */
    fun handleTransactionSelectToggle(txId: String) {
        if (selectedTxIds.contains(txId)) {
            selectedTxIds.remove(txId)
            if (selectedTxIds.isEmpty()) isSelectionMode = false
        } else {
            if (!isSelectionMode) isSelectionMode = true
            selectedTxIds.add(txId)
        }
    }

    /*
     * إلغاء وضع تحديد الأيام وتفريغ القائمة
     */
    fun cancelDaySelection() {
        isDaySelectionMode = false
        selectedDayKeys.clear()
    }

    /*
     * تحديد أو إلغاء تحديد كافة الأيام المعروضة
     */
    fun selectAllDays(allKeys: List<String>) {
        if (selectedDayKeys.size == allKeys.size) {
            selectedDayKeys.clear()
        } else {
            selectedDayKeys.clear()
            selectedDayKeys.addAll(allKeys)
        }
    }

    /*
     * إغلاق أي حوار منبثق حالي
     */
    fun dismissDialog() {
        activeDialogState = MainLedgerDialogState.None
    }
}

/*
 * =====================================================================================
 * دالة إنشاء وتذكر متحكم واجهة دفتر الأستاذ (rememberMainLedgerUiController)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * دالة قابلة للتركيب (Composable) تنشئ نسخة مستقرة ومحفوظة من المتحكم داخل نطاق الشاشة.
 * =====================================================================================
 */
@Composable
fun rememberMainLedgerUiController(): MainLedgerUiController {
    val habayebActiveState = rememberSaveable { mutableStateOf(false) }
    return remember {
        MainLedgerUiController(habayebActiveState = habayebActiveState)
    }
}

