package com.smartledger.aldaftar.ui.screens.habayeb.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.Calendar
import java.util.UUID

/**
 * مساعد حفظ بيانات العميل والرصيد الافتتاحي (Add Customer Save & Calculation Helper)
 *
 * المسؤوليات المعمارية:
 * 1. معالجة وإنشاء الكيانات: يتم إنشاء معرف فريد للعميل (UUID) مستقل تماماً عن اسمه القابل للتعديل، للحفاظ على استقرار العلاقات مع المعاملات والنسخ الاحتياطية.
 * 2. التحقق من المدخلات: إزالة المسافات الزائدة، التحقق من عدم تكرار الاسم، والتأكد من إيجابية الرصيد الافتتاحي ونوع تصنيف العميل.
 * 3. معالجة العملات الأجنبية وأسعار الصرف وتحويل الرصيد بدقة مالية (BigDecimal).
 * 4. إسناد الحفظ إلى ViewModel وإدارة حالات التقدم والإشعارات دون تضخم كود الواجهة الرسومية (Composable).
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
                        viewModel.triggerActivationRequired()
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

                    viewModel.saveHabayebCustomer(
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
                        Toast.makeText(context, context.getString(R.string.habayeb_toast_save_success), Toast.LENGTH_SHORT).show()
                        onSuccess(newCustomerId)
                        onDismiss()
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
