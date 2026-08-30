package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة مساعد حفظ بيانات العميل (Add Customer Save Helper Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على كائن المعالجة والحفظ المحاسبي الآمن لبيانات العميل والرصيد الافتتاحي:
 * - التحقق من صحة واكتمال المدخلات (الاسم، عدم التكرار، إيجابية المبلغ، وتحديد نوع الحساب).
 * - معالجة العملات الأجنبية وأسعار الصرف وتحويل الرصيد بدقة مالية متناهية (BigDecimal).
 * - توليد المعرفات الفريدة (UUID) المستقلة عن أسماء العملاء لضمان سلامة العلاقات وقواعد البيانات.
 * - حماية الترخيص وفحص انتهاء الفترة التجريبية وتفويض الحفظ إلى قاعدة البيانات المحلية عبر ViewModel.
 * =====================================================================================
 */

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.domain.model.SaveTransactionResult
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import com.example.ui.viewmodel.HabayebFinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Calendar
import java.util.UUID

/*
 * =====================================================================================
 * نموذج بيانات نموذج إضافة العميل (AddCustomerFormData)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * كائن بيانات تجميعي يحمل كافة قيم النموذج من واجهة المستخدم لتمريرها بشكل منظم لمعالج الحفظ:
 * - nameStr / phoneStr / notesStr / initialAmountStr: نصوص الحقول الأربعة.
 * - initialType: نوع الحساب الافتتاحي (له/عليه).
 * - selectedTransactionCurrency / currencySymbol: العملة المختارة والعملة الافتراضية.
 * - applyExchangeRate / settingsRate: إعدادات تحويل العملة وسعر الصرف.
 * - selectedCalendar: تاريخ ووقت العملية المسجل.
 * - isDuplicateName: مؤشر التحقق من تكرار الاسم.
 * =====================================================================================
 */
data class AddCustomerFormData(
    val nameStr: String,
    val phoneStr: String,
    val notesStr: String,
    val initialAmountStr: String,
    val initialType: String?,
    val selectedTransactionCurrency: String,
    val currencySymbol: String,
    val applyExchangeRate: Boolean,
    val selectedCalendar: Calendar,
    val settingsRate: Double,
    val isDuplicateName: Boolean
)

/*
 * =====================================================================================
 * كائن مساعد حفظ بيانات العميل (AddCustomerSaveHelper)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * ينفذ دورة التحقق والحفظ المحاسبية الكاملة:
 * 1. التحقق من الحقول الإلزامية ومنع تكرار الأسماء وتأكيد إيجابية المبلغ.
 * 2. التحقق من وجود سعر صرف فعال عند اختيار عملة أجنبية مع تفعيل التحويل.
 * 3. فحص صلاحية ترخيص التطبيق.
 * 4. إنشاء كيان العميل والقيد الافتتاحي وتخزينهما بأمان عبر Coroutines.
 * =====================================================================================
 */
object AddCustomerSaveHelper {
    fun handleSave(
        context: Context,
        viewModel: HabayebFinanceViewModel,
        formData: AddCustomerFormData,
        onIsSavingChange: (Boolean) -> Unit,
        onShowRateSetup: (String) -> Unit,
        onSuccess: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        val nameStr = formData.nameStr
        val phoneStr = formData.phoneStr
        val notesStr = formData.notesStr
        val initialAmountStr = formData.initialAmountStr
        val currentType = formData.initialType
        val selectedTransactionCurrency = formData.selectedTransactionCurrency
        val currencySymbol = formData.currencySymbol
        val applyExchangeRate = formData.applyExchangeRate
        val selectedCalendar = formData.selectedCalendar
        val settingsRate = formData.settingsRate
        val isDuplicateName = formData.isDuplicateName

        // التحقق من الاسم وعدم التكرار والنوع
        if (nameStr.trim().isBlank()) {
            Toast.makeText(context, context.getString(R.string.habayeb_toast_enter_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (isDuplicateName) {
            Toast.makeText(context, context.getString(R.string.habayeb_error_duplicate_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (currentType == null) {
            Toast.makeText(context, context.getString(R.string.habayeb_toast_select_type), Toast.LENGTH_SHORT).show()
            return
        }
        val cleanAmountStr = CurrencyConfig.normalizeDigits(initialAmountStr).trim()
        if (cleanAmountStr.isBlank()) {
            Toast.makeText(context, context.getString(R.string.habayeb_required_field), Toast.LENGTH_SHORT).show()
            return
        }
        val actualInitialAmountBd = CurrencyConfig.parseBigDecimal(cleanAmountStr)
        if (actualInitialAmountBd < BigDecimal.ZERO) {
            Toast.makeText(context, context.getString(R.string.habayeb_toast_initial_amount_negative), Toast.LENGTH_SHORT).show()
            return
        }

        // فحص العملات الأجنبية وسعر الصرف
        val isForeignSelected = selectedTransactionCurrency != currencySymbol
        if (isForeignSelected && applyExchangeRate) {
            val settings = viewModel.settingsState.value
            val hasStoredRate = ExchangeRateHelper.hasRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency)
            val currentRateVal = ExchangeRateHelper.getRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency)
            if (!hasStoredRate || currentRateVal == 1.0) {
                onShowRateSetup("")
                return
            }
        }

        onIsSavingChange(true)
        val transactionTimestamp = selectedCalendar.timeInMillis / 1000
        val newCustomerId = "cust_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"

        viewModel.viewModelScope.launch(Dispatchers.IO) {
            try {
                if (viewModel.isTrialExpiredDirect()) {
                    withContext(Dispatchers.Main) {
                        onIsSavingChange(false)
                        Toast.makeText(context, context.getString(R.string.licensing_trial_expired_toast), Toast.LENGTH_LONG).show()
                        viewModel.triggerActivationRequired()
                        onDismiss()
                    }
                } else {
                    val newCustomer = HabayebCustomer(
                        id = newCustomerId,
                        name = nameStr.trim(),
                        phone = phoneStr.trim(),
                        notes = notesStr.trim(),
                        createdAt = transactionTimestamp,
                        initialType = currentType
                    )
                    val exchangeRateBd = if (isForeignSelected && applyExchangeRate) {
                        BigDecimal.valueOf(settingsRate)
                    } else {
                        BigDecimal.ONE
                    }
                    val finalEquivalentAmountBd = if (isForeignSelected && applyExchangeRate) {
                        CurrencyConfig.convertAmountBigDecimal(actualInitialAmountBd, currencySymbol, selectedTransactionCurrency, exchangeRateBd)
                    } else {
                        BigDecimal.ZERO
                    }
                    val finalAmountBd = if (isForeignSelected && applyExchangeRate) finalEquivalentAmountBd else actualInitialAmountBd
                    val finalDetails = if (notesStr.trim().isBlank()) context.getString(R.string.habayeb_opening_balance_default_desc) else notesStr.trim()

                    val result = viewModel.saveHabayebCustomer(
                        customer = newCustomer,
                        initialAmount = finalAmountBd,
                        initialType = currentType,
                        customTimestamp = transactionTimestamp,
                        initialDetails = CurrencyConfig.formatDescriptionWithCurrency(finalDetails, selectedTransactionCurrency),
                        isForeign = isForeignSelected,
                        currencyCode = selectedTransactionCurrency,
                        foreignAmount = actualInitialAmountBd,
                        exchangeRate = exchangeRateBd,
                        isRateCalculated = isForeignSelected && applyExchangeRate,
                        equivalentAmount = finalEquivalentAmountBd
                    )

                    withContext(Dispatchers.Main) {
                        onIsSavingChange(false)
                        when (result) {
                            is SaveTransactionResult.Success -> {
                                Toast.makeText(context, context.getString(R.string.habayeb_toast_save_success), Toast.LENGTH_SHORT).show()
                                onSuccess(newCustomerId)
                                onDismiss()
                            }
                            is SaveTransactionResult.TrialExpired -> {
                                Toast.makeText(context, context.getString(R.string.licensing_trial_expired_toast), Toast.LENGTH_LONG).show()
                                viewModel.triggerActivationRequired()
                                onDismiss()
                            }
                            is SaveTransactionResult.Error -> {
                                Toast.makeText(context, context.getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AddCustomerSaveHelper", "Failed to save customer safely", e)
                withContext(Dispatchers.Main) {
                    onIsSavingChange(false)
                }
            }
        }
    }
}

