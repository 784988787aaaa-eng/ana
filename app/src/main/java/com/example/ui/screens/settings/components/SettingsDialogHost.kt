/*
 * ================================================================
 * التوثيق الهندسي العربي الفائق — SettingsDialogHost.kt
 * ================================================================
 * المسؤولية المعمارية:
 * مضيف مركزي للحورات المنبثقة من شاشة الإعدادات، يفصل اختيار الحوار عن محتوى كل حوار.
 *
 * المشهد التعليمي والبصري:
 * تخيل شاشة «الإعدادات» على الهاتف: كل بطاقة هنا تمثل منطقة قرار واضحة؛ يقرأ المستخدم
 * الحالة أولاً، ثم يختار الإجراء، ثم يظهر الحوار المناسب عند الحاجة. هذا الملف يشرح
 * كيف تتحول حالة النظام إلى عناصر مرئية دون نقل مسؤوليات التخزين أو الأمن إلى Compose.
 *
 * فهرس العناصر التنفيذية المكتشفة:
 * - `fun SettingsDialogHost(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val targetCurrency = currenciesToSetup[currentSetupIndex]`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val migratedOriginalJson = ExchangeRateHelper.migrateRates(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val alreadyHasRate = ExchangeRateHelper.hasRate(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val existingRate = ExchangeRateHelper.getRate(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val oldRateBD = java.math.BigDecimal.valueOf(existingRate)`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val newRateBD = newRate`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val currentSettings = settings`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val updatedSettings = currentSettings.copy(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val revalueState = activeDialogState as? SettingsDialogState.RevalueConfirm`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val targetCurrency = revalueState.targetCurrency`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val newRate = revalueState.newRate`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val currentSettings = settings`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val updatedSettings = currentSettings.copy(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val currentSettings = settings`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val updatedSettings = currentSettings.copy(`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 *
 * قاعدة الثبات المطلقة:
 * النص التنفيذي الأصلي محفوظ ككتلة متصلة أدناه دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.data.local.entities.AppSettings
import com.example.ui.screens.SettingsDialogState
import com.example.ui.screens.habayeb.components.ExchangeRateSetupDialog
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel

@Composable
fun SettingsDialogHost(
    activeDialogState: SettingsDialogState,
    onDismissDialog: () -> Unit,
    onStateChange: (SettingsDialogState) -> Unit,
    settings: AppSettings,
    currencySymbol: String,
    currenciesToSetup: List<String>,
    currentSetupIndex: Int,
    onSetupIndexChange: (Int) -> Unit,
    onCurrenciesToSetupChange: (List<String>) -> Unit,
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    onLaunchPermissions: () -> Unit,
    onPermissionGrantedCallback: (() -> Unit)?
) {
    if (activeDialogState is SettingsDialogState.PermissionExplanation) {
        BackupPermissionExplanationDialog(
            onDismiss = onDismissDialog,
            onGrantPermissions = onLaunchPermissions,
            onUseInternalStorage = {
                onPermissionGrantedCallback?.invoke()
            }
        )
    }

    if (activeDialogState is SettingsDialogState.ResetDataTrap) {
        ResetTrapDialog(
            onDismiss = onDismissDialog,
            onConfirmDelete = {
                viewModel.deleteAllData()
                onDismissDialog()
            }
        )
    }

    if (activeDialogState is SettingsDialogState.CurrencySetup && currentSetupIndex < currenciesToSetup.size) {
        val targetCurrency = currenciesToSetup[currentSetupIndex]
        ExchangeRateSetupDialog(
            selectedCurrency = targetCurrency,
            initialRateStr = "",
            activeThemeColor = MaterialTheme.colorScheme.primary,
            onDismiss = {
                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    onSetupIndexChange(currentSetupIndex + 1)
                } else {
                    onDismissDialog()
                    onCurrenciesToSetupChange(emptyList())
                }
            },
            onConfirm = { newRate ->
                val migratedOriginalJson = ExchangeRateHelper.migrateRates(
                    settings.exchangeRatesJson,
                    settings.currencySymbol,
                    currencySymbol
                )
                val alreadyHasRate = ExchangeRateHelper.hasRate(
                    migratedOriginalJson,
                    currencySymbol,
                    targetCurrency
                )
                val existingRate = ExchangeRateHelper.getRate(
                    migratedOriginalJson,
                    currencySymbol,
                    targetCurrency
                )
                val oldRateBD = java.math.BigDecimal.valueOf(existingRate)
                val newRateBD = newRate
                val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0

                if (alreadyHasRate && rateChanged) {
                    onStateChange(SettingsDialogState.RevalueConfirm(targetCurrency, newRateBD))
                } else {
                    val currentSettings = settings
                    val updatedSettings = currentSettings.copy(
                        exchangeRatesJson = ExchangeRateHelper.setRate(
                            currentSettings.exchangeRatesJson,
                            currencySymbol,
                            targetCurrency,
                            newRate
                        )
                    )
                    viewModel.saveSettings(updatedSettings)

                    if (currentSetupIndex + 1 < currenciesToSetup.size) {
                        onSetupIndexChange(currentSetupIndex + 1)
                    } else {
                        onDismissDialog()
                        onCurrenciesToSetupChange(emptyList())
                    }
                }
            }
        )
    }

    val revalueState = activeDialogState as? SettingsDialogState.RevalueConfirm
    if (revalueState != null) {
        val targetCurrency = revalueState.targetCurrency
        val newRate = revalueState.newRate

        RevalueConfirmDialog(
            targetCurrency = targetCurrency,
            onConfirmAll = {
                habayebViewModel.revalueHistoricalTransactions(currencySymbol, targetCurrency, newRate)
                val currentSettings = settings
                val updatedSettings = currentSettings.copy(
                    exchangeRatesJson = ExchangeRateHelper.setRate(
                        currentSettings.exchangeRatesJson,
                        currencySymbol,
                        targetCurrency,
                        newRate
                    )
                )
                viewModel.saveSettings(updatedSettings)

                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    onSetupIndexChange(currentSetupIndex + 1)
                    onStateChange(SettingsDialogState.CurrencySetup)
                } else {
                    onDismissDialog()
                    onCurrenciesToSetupChange(emptyList())
                }
            },
            onConfirmFutureOnly = {
                val currentSettings = settings
                val updatedSettings = currentSettings.copy(
                    exchangeRatesJson = ExchangeRateHelper.setRate(
                        currentSettings.exchangeRatesJson,
                        currencySymbol,
                        targetCurrency,
                        newRate
                    )
                )
                viewModel.saveSettings(updatedSettings)

                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    onSetupIndexChange(currentSetupIndex + 1)
                    onStateChange(SettingsDialogState.CurrencySetup)
                } else {
                    onDismissDialog()
                    onCurrenciesToSetupChange(emptyList())
                }
            },
            onDismiss = onDismissDialog
        )
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * 1. الحفاظ على هذا المكوّن في طبقة العرض وعدم نقل قواعد العمل الحساسة إليه؛ القرار النهائي يجب أن يبقى في ViewModel/Domain.
 * 2. إضافة اختبارات UI للحالات الأساسية وحالات الخطأ والحدود دون تغيير السلوك الحالي.
 * 3. مراجعة الوصولية واتساق Material 3 عند اختلاف أحجام الشاشات والوضعين الفاتح والداكن.
 */
