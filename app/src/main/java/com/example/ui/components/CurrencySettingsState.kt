package com.example.ui.components

/*
 * =====================================================================================
 * حزمة المكونات وحالة واجهة المستخدم (UI Components & State Management)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على كائنات إدارة الحالة (State Holders) التي تفصل منطق الأعمال
 * والحسابات المالية والتحقق من المدخلات عن طبقة الرسم المرئي في Jetpack Compose.
 * =====================================================================================
 */

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

/*
 * =====================================================================================
 * كلاس إدارة حالة إعدادات العملات وأسعار الصرف (CurrencySettingsState)
 * -------------------------------------------------------------------------------------
 * [الهدف والمسؤولية]:
 * إدارة الحالة الديناميكية التفاعلية داخل نافذة إعدادات العملة:
 * 1. متابعة العملة الافتراضية للتطبيق ونقل أسعار الصرف السابقة عند تغيير العملة الأساسية.
 * 2. تتبع العملة الثانوية المختارة وسعر الصرف المقابل لها.
 * 3. التحقق من صحة الأرقام المدخلة وتحويل الأرقام المشرقية/العربية إلى صيغة قياسية.
 * 4. مقارنة السعر الجديد بالسعر المخزن سابقاً لتحديد ما إذا كان يلزم فتح نافذة تأكيد إعادة التقييم.
 * 5. إدارة حفظ التعديلات وإرسالها عبر دوال رد النداء (Callbacks).
 *
 * [المُدخلات]:
 * - initialSettings: كائن إعدادات التطبيق الأولي من قاعدة البيانات.
 * - currencyYer: مسمى الريال اليمني المترجم.
 * - currencySar: مسمى الريال السعودي المترجم.
 * - currencyUsd: مسمى الدولار الأمريكي المترجم.
 * =====================================================================================
 */
class CurrencySettingsState(
    initialSettings: AppSettings,
    val currencyYer: String,
    val currencySar: String,
    val currencyUsd: String
) {
    /*
     * ---------------------------------------------------------------------------------
     * متغيرات الحالة القابلة للتفاعل (Mutable Reactive States)
     * ---------------------------------------------------------------------------------
     * - localDefaultCurrency: العملة الأساسية للتطبيق المختارة محلياً في النافذة.
     * - localExchangeRatesJson: النص بتنسيق JSON الذي يخزن مصفوفة أسعار الصرف بين العملات.
     * ---------------------------------------------------------------------------------
     */
    var localDefaultCurrency by mutableStateOf(initialSettings.currencySymbol)
    var localExchangeRatesJson by mutableStateOf(initialSettings.exchangeRatesJson)

    /*
     * قائمة العملات المدعومة والمتاحة للعرض والاختيار في النافذة
     */
    val currenciesToDisplay = listOf(currencyYer, currencySar, currencyUsd)

    /*
     * العملة الثانوية المستهدفة لضبط سعر صرفها مقابل العملة الافتراضية
     */
    var selectedTargetCurrency by mutableStateOf(
        if (localDefaultCurrency == currencyYer) currencyUsd else currencyYer
    )

    /*
     * خاصية محسوبة (Computed Property) لاستخراج قيمة سعر الصرف الحالي من JSON
     */
    val currentRateValue: Double
        get() = ExchangeRateHelper.getRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency)

    /*
     * النص المعروض داخل حقل إدخال سعر الصرف مع تنسيقه عند التحميل الأولي
     */
    var rateInputStr by mutableStateOf(
        if (currentRateValue > 0.0 && currentRateValue != 1.0) HabayebMathHelper.formatRate(currentRateValue) else ""
    )

    /*
     * الحالة الحالية للنوافذ الفرعية المنبثقة (مثل نافذة تأكيد إعادة التقييم)
     */
    var activeDialogState by mutableStateOf<CurrencyDialogState>(CurrencyDialogState.None)

    /*
     * ---------------------------------------------------------------------------------
     * دالة تبديل العملة الافتراضية للتطبيق (onDefaultCurrencyChange)
     * ---------------------------------------------------------------------------------
     * [الهدف]:
     * تُستدعى عند اختيار عملة افتراضية جديدة للتطبيق:
     * 1. تحديث قيمة العملة الافتراضية المحلية.
     * 2. في حال تطابق العملة المستهدفة مع العملة الافتراضية الجديدة، يتم تحويل الهدف تلقائياً إلى عملة أخرى.
     * 3. إعادة حساب ومهاجرة (Migrate) أسعار الصرف القديمة لتنسب إلى العملة الأساسية الجديدة.
     * 4. تحديث حقل الإدخال ليعكس السعر الجديد.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة تبديل العملة المستهدفة (onTargetCurrencyChange)
     * ---------------------------------------------------------------------------------
     * تُستدعى عند اختيار المستخدم لعملة ثانوية أخرى لضبط سعر صرفها مقابل العملة الافتراضية،
     * وتقوم بتحديث الهدف وإعادة تحميل السعر المقابل في حقل الإدخال.
     * ---------------------------------------------------------------------------------
     */
    fun onTargetCurrencyChange(newTarget: String) {
        selectedTargetCurrency = newTarget
        refreshRateInput()
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة معالجة إدخال سعر الصرف (onRateInputChange)
     * ---------------------------------------------------------------------------------
     * [الهدف]:
     * 1. تنظيف وتوحيد الأرقام المكتوبة (تحويل الأرقام العربية/الهندية إلى أرقام لاتينية قياسية).
     * 2. تحويل النص إلى رقم عشري (Double) مع قيمة افتراضية آمنة (1.0).
     * 3. تحديث مصفوفة أسعار الصرف داخل نص JSON المحلي.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة مساعدة لتحديث نص حقل سعر الصرف (refreshRateInput)
     * ---------------------------------------------------------------------------------
     * تستخرج السعر الحالي وتنسقه للعرض، أو تفرغ الحقل إذا كان السعر غير معرف أو مساوياً لـ 1.
     * ---------------------------------------------------------------------------------
     */
    private fun refreshRateInput() {
        val rate = currentRateValue
        rateInputStr = if (rate > 0.0 && rate != 1.0) HabayebMathHelper.formatRate(rate) else ""
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة معالجة الحفظ النهائي لإعدادات العملة (handleSave)
     * ---------------------------------------------------------------------------------
     * [الهدف والمنطق]:
     * 1. فحص القيمة النهائية لسعر الصرف المدخل.
     * 2. في حال وجود سعر صرف سابق وتغيرت قيمته، تفتح نافذة تأكيد إعادة التقييم (RevalueConfirm)
     *    لتخيير المستخدم بين إعادة تقييم المعاملات التاريخية أو المستقبلية فقط.
     * 3. إذا لم يتغير السعر أو لم يكن هناك سعر سابق، يتم حفظ الإعدادات مباشرة وإغلاق النافذة.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة تأكيد إعادة تقييم المعاملات السابقة والمستقبلية (handleConfirmHistoricalAndFuture)
     * ---------------------------------------------------------------------------------
     * تحفظ سعر الصرف الجديد مع تمرير القيمة المنطقية (true) لمعامل revalueHistorical
     * لتحديث كافة السجلات المحاسبية القديمة في قاعدة البيانات بالسعر الجديد.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة تأكيد تطبيق السعر على المعاملات المستقبلية فقط (handleConfirmFutureOnly)
     * ---------------------------------------------------------------------------------
     * تحفظ سعر الصرف الجديد مع تمرير القيمة المنطقية (false) لمعامل revalueHistorical
     * للإبقاء على القيم التاريخية للمعاملات السابقة دون تعديل أرقامها المحاسبية.
     * ---------------------------------------------------------------------------------
     */
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

/*
 * =====================================================================================
 * دالة مساعدة لتذكر وإنشاء كائن الحالة (rememberCurrencySettingsState)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * دالة Composable تقوم بإنشاء وتذكر نسخة من CurrencySettingsState طوال دورة حياة النافذة
 * وتضمن عدم إعادة التهيئة إلا عند تغير الإعدادات الأولية أو مسميات العملات الممررة.
 * =====================================================================================
 */
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

