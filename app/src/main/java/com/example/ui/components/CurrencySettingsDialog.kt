package com.example.ui.components

/*
 * =====================================================================================
 * حزمة المكونات المرئية لواجهة المستخدم (UI Components Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على عناصر الواجهة القابلة لإعادة الاستخدام في مختلف شاشات التطبيق،
 * بما في ذلك مربعات الحوار، القوائم الجانبية، الأزرار المتخصصة، والمؤشرات المرئية.
 * =====================================================================================
 */

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

/*
 * =====================================================================================
 * وسوم التعريف والتوثيق التشخيصي (Diagnostic Log Tags)
 * -------------------------------------------------------------------------------------
 * يُستخدم هذا الثابت كوسم (Tag) عند تسجيل الأحداث أو التحذيرات في سجلات النظام (Logcat)،
 * مما يسهل تتبع الأخطاء البرمجية الخاصة بنافذة إعدادات العملات.
 * =====================================================================================
 */
private const val TAG = "CurrencySettingsDialog"

/*
 * =====================================================================================
 * المكون الرئيسي: نافذة ضبط إعدادات وأسعار صرف العملات (CurrencySettingsDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية منبثقة (Dialog) تتيح للمستخدم:
 * 1. تعيين العملة الافتراضية للتطبيق (مثل: ريال يمني، ريال سعودي، دولار أمريكي).
 * 2. تحديد أسعار الصرف بين العملات المختلفة بدقة رياضية عالية.
 * 3. تطبيق تغييرات أسعار الصرف إما على المعاملات المستقبلية فقط، أو إعادة تقييم
 *    المعاملات السابقة والتاريخية بناءً على رغبة المستخدم.
 *
 * [البيانات والمُدخلات]:
 * - settings: كائن إعدادات التطبيق الحالية (AppSettings) المحمل من قاعدة البيانات.
 * - onSaveSettings: دالة رد النداء (Callback) تُستدعى لحفظ الإعدادات الجديدة وتطبيق خيارات إعادة التقييم.
 * - onDismiss: دالة إغلاق مربع الحوار عند الإلغاء أو الضغط خارج الإطار.
 *
 * [التدفق والترابط]:
 * يرتبط هذا المكون بـ rememberCurrencySettingsState لإدارة المنطق الداخلي والحالة،
 * كما يفتح مربع الحوار الفرعي CurrencyRevalueConfirmDialog عند تعديل سعر الصرف لتأكيد آلية التقييم.
 * =====================================================================================
 */
@Composable
fun CurrencySettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    /*
     * ---------------------------------------------------------------------------------
     * تهيئة الاستجابة اللمسية والنصوص المترجمة (Haptics & String Resources)
     * ---------------------------------------------------------------------------------
     * يتم جلب كائن التغذية اللمسية لتقديم رد فعل حسي عند النقر،
     * واستخراج مسميات العملات الرسمية المعرفة في ملف الموارد strings.xml لدعم تعدد اللغات.
     * ---------------------------------------------------------------------------------
     */
    val haptic = LocalHapticFeedback.current

    val currencyYer = stringResource(id = R.string.currency_yer)
    val currencySar = stringResource(id = R.string.currency_sar)
    val currencyUsd = stringResource(id = R.string.currency_usd)

    /*
     * ---------------------------------------------------------------------------------
     * ربط الحالة التشغيلية لنافذة إعدادات العملة (Currency Settings State)
     * ---------------------------------------------------------------------------------
     * يتم إنشاء وتذكر كائن الحالة المتخصص (CurrencySettingsState) الذي يعزل المنطق
     * الحسابي والتحقق من صحة المدخلات عن الرسم المباشر لواجهة المستخدم.
     * ---------------------------------------------------------------------------------
     */
    val state = rememberCurrencySettingsState(
        settings = settings,
        currencyYer = currencyYer,
        currencySar = currencySar,
        currencyUsd = currencyUsd
    )

    /*
     * ---------------------------------------------------------------------------------
     * إدارة التركيز ولوحة المفاتيح التلقائية (Focus & Virtual Keyboard Management)
     * ---------------------------------------------------------------------------------
     * يتم إعداد ملقم التركيز ومتحكم لوحة المفاتيح البرمجية ليتم تفعيل حقل إدخال سعر الصرف
     * فور فتح النافذة أو تبديل العملة المستهدفة لتحسين سرعة الإدخال وتجربة المستخدم.
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * بناء هيكل مربع الحوار المنبثق (Dialog & Card Container)
     * ---------------------------------------------------------------------------------
     * يتم إنشاء الحاوية الخارجية المنبثقة مع ضبط خصائص النافذة لضمان بقاء لوحة المفاتيح
     * ظاهرة ومراعاة الحواف وتعديل حجم الإطار بسلاسة وفق محتوى الحقول.
     * ---------------------------------------------------------------------------------
     */
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
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
                // شريط العنوان العلوي مع زر الإغلاق
                CurrencyDialogHeader(onDismiss = onDismiss)

                // خط فاصل رفيع لتنسيق الأقسام البصرية
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // أعمدة اختيار العملات وحقل إدخال سعر الصرف
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

                // أزرار التحكم السفلية (حفظ التعديلات أو الإلغاء)
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

    /*
     * ---------------------------------------------------------------------------------
     * مربع حوار تأكيد إعادة التقييم المالي (Currency Revaluation Confirmation Dialog)
     * ---------------------------------------------------------------------------------
     * إذا قام المستخدم بتعديل سعر الصرف لعملة مستهدفة، يتحول المتغير activeDialogState
     * إلى RevalueConfirm لعرض خيارات إعادة تقييم الحسابات السابقة أو المستقبلية فقط.
     * ---------------------------------------------------------------------------------
     */
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

/*
 * =====================================================================================
 * المكون الفرعي: ترويسة نافذة إعدادات العملة (CurrencyDialogHeader)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يعرض العنوان الرئيسي للنافذة في المنتصف مع زر إغلاق مصغر على الجانب لإتاحة الخروج السريع.
 *
 * [المُدخلات]:
 * - onDismiss: حدث إغلاق النافذة عند النقر على زر الإغلاق (X).
 * =====================================================================================
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

/*
 * =====================================================================================
 * المكون الفرعي: أعمدة اختيار العملات ومعدل الصرف (CurrencySelectorColumns)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يُنظم عملية اختيار العملات في عمودين متجاورين:
 * 1. العمود الأيمن: قائمة اختيار العملة الافتراضية الرئيسية لكافة حسابات التطبيق.
 * 2. العمود الأيسر: أزرار التبديل السريع بين العملات الثانوية وحقل إدخال سعر الصرف المقابل.
 *
 * [المُدخلات]:
 * - currenciesToDisplay: قائمة بكافة رموز العملات المتاحة للاختيار.
 * - localDefaultCurrency: رمز العملة الافتراضية المحددة حالياً.
 * - selectedTargetCurrency: رمز العملة الثانوية المختارة لضبط سعر صرفها.
 * - rateInputStr: النص المكتوب حالياً داخل حقل سعر الصرف.
 * - rateFocusRequester: كائن توجيه التركيز إلى حقل الإدخال.
 * - haptic: كائن التغذية اللمسية.
 * - currencyYer & currencyUsd: مسميات العملات المترجمة.
 * - دوال المعالجة (onDefaultCurrencyChange, onTargetCurrencyChange, onRateInputChange).
 * =====================================================================================
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
        /*
         * -----------------------------------------------------------------------------
         * العمود الأيمن: اختيار العملة الافتراضية للتطبيق (Default Currency)
         * -----------------------------------------------------------------------------
         */
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

        /*
         * -----------------------------------------------------------------------------
         * العمود الأيسر: العملات المقابلة وحقل إدخال معادلة الصرف (Target Currency & Rate)
         * -----------------------------------------------------------------------------
         */
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

            // استبعاد العملة الافتراضية من قائمة الأهداف لتجنب مقارنة العملة بنفسها
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

            // حقل إدخال سعر الصرف مع لاحقة وبادئة توضح العملة الأساسية والمستهدفة
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

/*
 * =====================================================================================
 * المكون الفرعي: أزرار إجراءات الحفظ والإلغاء (CurrencyActionButtons)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * يعرض زري "حفظ" و "إلغاء" بأسلوب بصري متميز وأبعاد مناسبة للمس السريع مع استجابة اهتزازية.
 *
 * [المُدخلات]:
 * - haptic: كائن التغذية اللمسية لتأكيد ضغط الزر.
 * - onDismiss: حدث إلغاء وإغلاق النافذة.
 * - onSave: حدث بدء فحص وتخزين إعدادات العملة.
 * =====================================================================================
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

