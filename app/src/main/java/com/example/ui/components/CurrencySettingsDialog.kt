/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/components/CurrencySettingsDialog.kt
 * المسؤولية: واجهة إعدادات العملات التي تعرض وتجمع مدخلات ومعلومات إعداد العملة.
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
// توثيق السطر 13: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 15: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 18: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 20: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 21: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 22: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 23: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 24: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 25: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 26: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 27: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 28: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 29: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 30: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 31: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 32: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 33: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 34: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 35: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 36: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 37: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 38: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 39: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 40: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 41: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 42: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 43: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 44: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 45: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 46: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 47: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 48: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 49: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 50: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 51: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 52: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 53: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 54: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 55: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 56: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 57: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 58: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 59: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 60: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 61: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 62: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 63: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 64: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 65: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 66: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 67: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 68: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 69: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 70: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 71: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 72: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 73: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 81: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 104: الأثر الجانبي التالي مدار بواسطة Compose وفق مفاتيحه الأصلية.
// توثيق السطر 184: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 219: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 252: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 413: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 441: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.

package com.example.ui.components

import android.util.Log
import android.view.WindowManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import kotlinx.coroutines.android.awaitFrame
import java.math.BigDecimal

private const val TAG = "CurrencySettingsDialog"

/**
 * نافذة ضبط وتعديل أسعار صرف العملات والعملة الافتراضية للتطبيق
 * توفر تجربة مدمجة وسلسة لضبط أزواج الصرف وإعادة تقييم العمليات السابقة أو اللاحقة بدقة متناهية.
 */
@Composable
fun CurrencySettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val currencyYer = stringResource(id = R.string.currency_yer)
    val currencySar = stringResource(id = R.string.currency_sar)
    val currencyUsd = stringResource(id = R.string.currency_usd)

    // تم فصل حالة العرض عن مكونات الواجهة للحفاظ على مسؤولية واحدة دون تغيير تجربة المستخدم.
    val state = rememberCurrencySettingsState(
        settings = settings,
        currencyYer = currencyYer,
        currencySar = currencySar,
        currencyUsd = currencyUsd
    )

    val rateFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.localDefaultCurrency, state.selectedTargetCurrency) {
        try {
            awaitFrame()
            rateFocusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to request focus or show keyboard: ${e.message}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val view = LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? DialogWindowProvider)?.window
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            onDispose {}
        }
        Card(
            modifier = Modifier
                .width(280.dp)
                .padding(4.dp)
                .imePadding()
                .animateContentSize(animationSpec = tween(200)),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                bottomEnd = 28.dp,
                topEnd = 6.dp,
                bottomStart = 6.dp
            ),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyDialogHeader(onDismiss = onDismiss)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                CurrencySelectorColumns(
                    currenciesToDisplay = state.currenciesToDisplay,
                    localDefaultCurrency = state.localDefaultCurrency,
                    selectedTargetCurrency = state.selectedTargetCurrency,
                    rateInputStr = state.rateInputStr,
                    rateFocusRequester = rateFocusRequester,
                    haptic = haptic,
                    currencyYer = currencyYer,
                    currencyUsd = currencyUsd,
                    onDefaultCurrencyChange = { newDefault -> state.onDefaultCurrencyChange(newDefault) },
                    onTargetCurrencyChange = { newTarget -> state.onTargetCurrencyChange(newTarget) },
                    onRateInputChange = { newInput -> state.onRateInputChange(newInput) }
                )

                Spacer(modifier = Modifier.height(2.dp))

                CurrencyActionButtons(
                    haptic = haptic,
                    onDismiss = onDismiss,
                    onSave = {
                        state.handleSave(
                            settings = settings,
                            onSaveSettings = onSaveSettings,
                            onDismiss = onDismiss
                        )
                    }
                )
            }
        }
    }

    val revalueState = state.activeDialogState as? CurrencyDialogState.RevalueConfirm
    if (revalueState != null) {
        val targetCurrency = revalueState.targetCurrency
        val newRate = revalueState.newRate

        CurrencyRevalueConfirmDialog(
            targetCurrency = targetCurrency,
            newRate = newRate,
            onConfirmHistoricalAndFuture = {
                state.handleConfirmHistoricalAndFuture(
                    settings = settings,
                    targetCurrency = targetCurrency,
                    newRate = newRate,
                    onSaveSettings = onSaveSettings,
                    onDismiss = onDismiss
                )
            },
            onConfirmFutureOnly = {
                state.handleConfirmFutureOnly(
                    settings = settings,
                    targetCurrency = targetCurrency,
                    newRate = newRate,
                    onSaveSettings = onSaveSettings,
                    onDismiss = onDismiss
                )
            },
            onDismiss = {
                state.activeDialogState = CurrencyDialogState.None
            }
        )
    }
}

/**
 * شريط العنوان وزر الإغلاق المصغر لنافذة إعدادات العملة
 */
@Composable
private fun CurrencyDialogHeader(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.currency_settings_dialog_title),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.currency_settings_dialog_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * أعمدة الاختيار الثنائي بين العملة الافتراضية للتطبيق وأزواج الصرف المستهدفة
 */
@Composable
private fun CurrencySelectorColumns(
    currenciesToDisplay: List<String>,
    localDefaultCurrency: String,
    selectedTargetCurrency: String,
    rateInputStr: String,
    rateFocusRequester: FocusRequester,
    haptic: HapticFeedback,
    currencyYer: String,
    currencyUsd: String,
    onDefaultCurrencyChange: (String) -> Unit,
    onTargetCurrencyChange: (String) -> Unit,
    onRateInputChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // العمود الأيمن: العملة الافتراضية للتطبيق
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_default),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                currenciesToDisplay.forEach { symbol ->
                    val isSelected = localDefaultCurrency == symbol
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDefaultCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // العمود الأيسر: عملات الصرف وحقل إدخال المعادلة
        Column(
            modifier = Modifier.weight(1.3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_target),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            val availableTargets = remember(currenciesToDisplay, localDefaultCurrency) {
                currenciesToDisplay.filter { it != localDefaultCurrency }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(1.5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                availableTargets.forEach { symbol ->
                    val isSelected = selectedTargetCurrency == symbol
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(topStart = 5.dp, bottomEnd = 5.dp, topEnd = 1.5.dp, bottomStart = 1.5.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTargetCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // حقل إدخال معادلة الصرف بدقة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.currency_settings_dialog_unit_rate_prefix, selectedTargetCurrency),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = rateInputStr,
                        onValueChange = onRateInputChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(rateFocusRequester),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (rateInputStr.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.currency_settings_dialog_price),
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Text(
                    text = localDefaultCurrency,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * أزرار الحفظ والإلغاء لنافذة إعدادات العملة
 */
@Composable
private fun CurrencyActionButtons(
    haptic: HapticFeedback,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp),
            modifier = Modifier
                .weight(1.3f)
                .height(24.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_save),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp),
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
