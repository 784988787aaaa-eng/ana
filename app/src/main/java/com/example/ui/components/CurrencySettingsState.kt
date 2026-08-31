/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/components/CurrencySettingsState.kt
 * المسؤولية: نموذج حالة Compose المستخدم لإدارة بيانات وإجراءات إعدادات العملات.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 31: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 38: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 46: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 85: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 105: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 174: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.

package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.entities.AppSettings
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import java.math.BigDecimal

// تم فصل حالة العرض عن مكونات الواجهة للحفاظ على مسؤولية واحدة دون تغيير تجربة المستخدم.

/**
 * فئة إدارة وتخزين حالة إعدادات العملة وأسعار الصرف، منفصلة عن عناصر بناء الواجهة.
 */
class CurrencySettingsState(
    initialSettings: AppSettings,
    val currencyYer: String,
    val currencySar: String,
    val currencyUsd: String
) {
    var localDefaultCurrency by mutableStateOf(initialSettings.currencySymbol)
    var localExchangeRatesJson by mutableStateOf(initialSettings.exchangeRatesJson)

    val currenciesToDisplay = listOf(currencyYer, currencySar, currencyUsd)

    var selectedTargetCurrency by mutableStateOf(
        if (localDefaultCurrency == currencyYer) currencyUsd else currencyYer
    )

    val currentRateValue: Double
        get() = ExchangeRateHelper.getRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency)

    var rateInputStr by mutableStateOf(
        if (currentRateValue > 0.0 && currentRateValue != 1.0) HabayebMathHelper.formatRate(currentRateValue) else ""
    )

    var activeDialogState by mutableStateOf<CurrencyDialogState>(CurrencyDialogState.None)

    fun onDefaultCurrencyChange(newDefault: String) {
        val oldDefault = localDefaultCurrency
        localDefaultCurrency = newDefault
        if (selectedTargetCurrency == newDefault) {
            selectedTargetCurrency = if (newDefault == currencyYer) currencyUsd else currencyYer
        }
        localExchangeRatesJson = ExchangeRateHelper.migrateRates(
            localExchangeRatesJson,
            oldDefault,
            newDefault
        )
        refreshRateInput()
    }

    fun onTargetCurrencyChange(newTarget: String) {
        selectedTargetCurrency = newTarget
        refreshRateInput()
    }

    fun onRateInputChange(newInput: String) {
        val cleaned = CurrencyConfig.normalizeDigits(newInput)
        rateInputStr = cleaned
        val parsed = cleaned.toDoubleOrNull() ?: 1.0
        localExchangeRatesJson = ExchangeRateHelper.setRate(
            localExchangeRatesJson,
            localDefaultCurrency,
            selectedTargetCurrency,
            parsed
        )
    }

    private fun refreshRateInput() {
        val rate = currentRateValue
        rateInputStr = if (rate > 0.0 && rate != 1.0) HabayebMathHelper.formatRate(rate) else ""
    }

    fun handleSave(
        settings: AppSettings,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val finalRate = rateInputStr.trim().toDoubleOrNull() ?: currentRateValue
        if (finalRate > 0.0) {
            val migratedOriginalJson = ExchangeRateHelper.migrateRates(
                settings.exchangeRatesJson,
                settings.currencySymbol,
                localDefaultCurrency
            )
            val alreadyHasRate = ExchangeRateHelper.hasRate(
                migratedOriginalJson,
                localDefaultCurrency,
                selectedTargetCurrency
            )
            val existingRate = ExchangeRateHelper.getRate(
                migratedOriginalJson,
                localDefaultCurrency,
                selectedTargetCurrency
            )
            val oldRateBD = BigDecimal.valueOf(existingRate)
            val newRateBD = BigDecimal.valueOf(finalRate)
            val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0

            if (alreadyHasRate && rateChanged) {
                activeDialogState = CurrencyDialogState.RevalueConfirm(selectedTargetCurrency, newRateBD)
            } else {
                val updatedExchangeRatesJson = ExchangeRateHelper.setRate(
                    localExchangeRatesJson,
                    localDefaultCurrency,
                    selectedTargetCurrency,
                    finalRate
                )
                val updatedSettings = settings.copy(
                    currencySymbol = localDefaultCurrency,
                    exchangeRatesJson = updatedExchangeRatesJson
                )
                onSaveSettings(updatedSettings, selectedTargetCurrency, finalRate, false)
                onDismiss()
            }
        } else {
            val updatedSettings = settings.copy(
                currencySymbol = localDefaultCurrency,
                exchangeRatesJson = localExchangeRatesJson
            )
            onSaveSettings(updatedSettings, "", 0.0, false)
            onDismiss()
        }
    }

    fun handleConfirmHistoricalAndFuture(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                localDefaultCurrency,
                targetCurrency,
                newRate
            )
        )
        onSaveSettings(updatedSettings, targetCurrency, newRate.toDouble(), true)
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }

    fun handleConfirmFutureOnly(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                localDefaultCurrency,
                targetCurrency,
                newRate
            )
        )
        onSaveSettings(updatedSettings, targetCurrency, newRate.toDouble(), false)
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }
}

@Composable
fun rememberCurrencySettingsState(
    settings: AppSettings,
    currencyYer: String,
    currencySar: String,
    currencyUsd: String
): CurrencySettingsState {
    return remember(settings, currencyYer, currencySar, currencyUsd) {
        CurrencySettingsState(
            initialSettings = settings,
            currencyYer = currencyYer,
            currencySar = currencySar,
            currencyUsd = currencyUsd
        )
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
