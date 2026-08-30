/**
 * =====================================================================
 * ملف: SecurityActivePanel.kt
 * الحزمة: com.example.ui.screens.security.components
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الملف لوحة التحكم الأمنية النشطة التي تظهر عندما يكون القفل الرقمي مفعلاً.
 * يعرض هذا المكون حالة الحماية الحالية للتطبيق، ويتيح للمستخدم إدارة خيارات الأمان
 * المتقدمة مثل: تفعيل أو تعطيل البصمة الحيوية (Biometrics)، تغيير رمز المرور (PIN)،
 * أو إلغاء تفعيل قفل التطبيق بالكامل بعد التحقق الأمني الإلزامي.
 * 
 * [الأمان والتحقق المسبق]:
 * - لحماية بيانات المستخدم من التعديل غير المصرح به، لا يمكن تغيير الرمز أو إلغاء القفل
 *   إلا بعد اجتياز نافذة التحقق الأمني (`VerifyOldPinDialog`) عبر إدخال الرمز القديم
 *   أو استخدام عبارة الاسترداد الاحتياطية.
 * =====================================================================
 */
package com.example.ui.screens.security.components

// ---------------------------------------------------------------------
// استيراد أدوات أندرويد وواجهة Jetpack Compose ومكونات الأمان
// ---------------------------------------------------------------------
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.domain.StringUtils.toEnglishDigits
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.SecurityAndLicenseViewModel

/**
 * أنواع الإجراءات الأمنية الحساسة التي تتطلب تحققاً مسبقاً قبل تنفيذها:
 * - [CHANGE_PIN]: تغيير الرمز السري الحالي إلى رمز جديد.
 * - [DEACTIVATE]: إيقاف وتعطيل نظام قفل التطبيق نهائياً.
 */
enum class SecurityActiveAction {
    CHANGE_PIN,
    DEACTIVATE
}

/**
 * لون المقبض الدائري لمفتاح التبديل (Switch).
 */

/**
 * =====================================================================
 * [لوحة الأمان النشطة - SecurityActivePanel]:
 * 
 * [الهدف والغرض]:
 * عرض الحالة الأمنية النشطة وتوفير أزرار التحكم في البصمة وتغيير أو تعطيل القفل.
 * 
 * [البيانات المستلمة]:
 * @param currentSettings كائن إعدادات التطبيق الحالية المحفوظة في قاعدة البيانات.
 * @param viewModel نموذج العرض المسؤول عن إدارة وتحديث خيارات الأمان والتحقق من الرموز.
 * @param onCopyRecoveryPhrase دالة استدعاء لنسخ عبارة الاسترداد (اختيارية).
 * @param onChangePasscode دالة بدء شاشة إنشاء رمز مرور جديد بعد اجتياز التحقق.
 * @param onDeactivateSecurity دالة تنفيذ إيقاف القفل بعد اجتياز التحقق.
 * @param modifier مخصصات الأبعاد والمحاذاة.
 * =====================================================================
 */
@Composable
fun SecurityActivePanel(
    currentSettings: AppSettings,
    viewModel: SecurityAndLicenseViewModel,
    onCopyRecoveryPhrase: (() -> Unit)? = null,
    onChangePasscode: () -> Unit,
    onDeactivateSecurity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.run { red < 0.5f }

    // تتبع الإجراء الحساس المعلق بانتظار التحقق من هوية المستخدم
    var pendingAction by remember { mutableStateOf<SecurityActiveAction?>(null) }

    // بطاقة الحاوية الرئيسية للوحة الأمان
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // تهيئة درجات ألوان الأمان والتحذير المتوافقة مع السمة
            val shieldBg = if (isDark) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.CreditContainerLight
            val shieldBorder = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
            val shieldTint = if (isDark) com.example.ui.theme.CreditGreenDark else com.example.ui.theme.CreditGreen
            val activeText = if (isDark) com.example.ui.theme.CreditGreenDark else com.example.ui.theme.CreditGreen
            val deactivateContent = if (isDark) com.example.ui.theme.DebtRedDark else com.example.ui.theme.DebtRed
            val deactivateBorder = if (isDark) com.example.ui.theme.DebtBorderDark else com.example.ui.theme.DebtBorderLight

            // -------------------------------------------------------------
            // شارة درع الحماية المعتمدة (Shield Icon)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(shieldBg, CircleShape)
                    .border(width = 1.dp, color = shieldBorder, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = shieldTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // نص تأكيد الحماية النشطة
            Text(
                text = stringResource(id = R.string.sec_toast_active_success),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = activeText
            )

            // نص الإرشادات والتحذيرات التوضيحية
            Text(
                text = stringResource(id = R.string.sec_card_desc_warning),
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // -------------------------------------------------------------
            // قسم المصادقة البيومترية (البصمة الحيوية) إذا كان الجهاز يدعمها
            // -------------------------------------------------------------
            if (viewModel.isBiometricSupported) {
                val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_title),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.sec_biometric_desc),
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // مفتاح تبديل تفعيل أو تعطيل فتح القفل بالبصمة
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometric(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)

            // -------------------------------------------------------------
            // زر طلب تغيير رمز المرور (PIN)
            // -------------------------------------------------------------
            OutlinedButton(
                onClick = { pendingAction = SecurityActiveAction.CHANGE_PIN },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.sec_btn_change_pin),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            // -------------------------------------------------------------
            // زر طلب تعطيل وإلغاء قفل التطبيق بالكامل
            // -------------------------------------------------------------
            OutlinedButton(
                onClick = { pendingAction = SecurityActiveAction.DEACTIVATE },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = deactivateContent),
                border = androidx.compose.foundation.BorderStroke(1.dp, deactivateBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.sec_deactivate_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // حوار التحقق الأمني المشروط: يتطلب إدخال الرمز القديم أو عبارة الاسترداد
    // -----------------------------------------------------------------
    val activePendingAction = pendingAction
    if (activePendingAction != null) {
        VerifyOldPinDialog(
            action = activePendingAction,
            recoveryHint = currentSettings.recoveryHint,
            onVerify = { input ->
                viewModel.verifyCredentials(input)
            },
            onSuccess = {
                val action = pendingAction
                pendingAction = null
                if (action == SecurityActiveAction.CHANGE_PIN) {
                    onChangePasscode()
                } else if (action == SecurityActiveAction.DEACTIVATE) {
                    onDeactivateSecurity()
                }
            },
            onDismiss = { pendingAction = null }
        )
    }
}

/**
 * =====================================================================
 * [نافذة التحقق الأمني - VerifyOldPinDialog]:
 * 
 * [الهدف والغرض]:
 * نافذة حوار تحقق تلزم المستخدم بإثبات ملكيته للتطبيق عبر إدخال الرمز السري الحالي
 * أو استخدام عبارة الاسترداد وتلميحها قبل السماح بتغيير الرمز أو تعطيل الحماية.
 * 
 * [المعاملات المستلمة]:
 * @param action الإجراء الأمني المراد تنفيذه بعد نجاح التحقق.
 * @param recoveryHint تلميح عبارة الاسترداد المسجل للمساعدة في التذكر.
 * @param onVerify دالة فحص مطابقة المدخل مع الرمز أو العبارة المخزنة في الـ ViewModel.
 * @param onSuccess استدعاء عند نجاح عملية التحقق لمتابعة تنفيذ الإجراء المطلوب.
 * @param onDismiss استدعاء عند إلغاء أو إغلاق الحوار.
 * =====================================================================
 */
@Composable
fun VerifyOldPinDialog(
    action: SecurityActiveAction,
    recoveryHint: String?,
    onVerify: (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    var pinInput by remember { mutableStateOf("") }
    var recoveryInput by remember { mutableStateOf("") }
    var showRecoveryMode by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 340.dp)
                .heightIn(max = 580.dp)
                .padding(8.dp)
                .imePadding(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // شريط العنوان وزر الإغلاق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = if (action == SecurityActiveAction.CHANGE_PIN) stringResource(id = R.string.sec_verify_change_pin) else stringResource(id = R.string.sec_verify_disable_lock),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // نص الإرشاد التوضيحي وفقاً للنمط النشط (رمز المرور أو الاسترداد)
                Text(
                    text = if (!showRecoveryMode) stringResource(id = R.string.sec_verify_pin_prompt) else stringResource(id = R.string.sec_verify_recovery_prompt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // -------------------------------------------------------------
                // نمط التحقق برمز المرور الحالي (PIN)
                // -------------------------------------------------------------
                if (!showRecoveryMode) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            val clean = it.toEnglishDigits()
                            if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                                pinInput = clean
                            }
                        },
                        label = { Text(stringResource(id = R.string.sec_current_pin_label), fontSize = 12.sp) },
                        placeholder = { Text(stringResource(id = R.string.sec_placeholder_code), fontSize = 12.sp) },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (onVerify(pinInput)) {
                                onSuccess()
                            } else {
                                Toast.makeText(context, context.getString(R.string.sec_toast_incorrect_current_pin), Toast.LENGTH_SHORT).show()
                                pinInput = ""
                            }
                        }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // رابط التحويل إلى نمط الاسترداد في حال نسيان الرمز
                    TextButton(
                        onClick = { showRecoveryMode = true },
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.sec_forgot_pin_recovery_link),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // ---------------------------------------------------------
                    // نمط التحقق عبر عبارة الاسترداد وتلميح الأمان
                    // ---------------------------------------------------------
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it.toEnglishDigits() },
                        label = { Text(stringResource(id = R.string.sec_recovery_phrase_label), fontSize = 12.sp) },
                        placeholder = { Text(stringResource(id = R.string.sec_recovery_phrase_placeholder), fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // إظهار تلميح الأمان إن وُجد للمساعدة في تذكر عبارة الاسترداد
                    if (!recoveryHint.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showHint = !showHint }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = com.example.ui.theme.warningColor(isDark), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showHint) stringResource(id = R.string.sec_hint_display_pattern, recoveryHint) else stringResource(id = R.string.sec_hint_toggle_show),
                                fontSize = 12.sp,
                                color = com.example.ui.theme.warningColor(isDark),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // زر العودة إلى نمط إدخال رمز المرور
                    TextButton(onClick = { showRecoveryMode = false }) {
                        Text(stringResource(id = R.string.sec_back_to_passcode_btn), fontSize = 12.sp)
                    }
                }

                // -------------------------------------------------------------
                // أزرار التحكم في الحوار (إلغاء / تأكيد التحقق)
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(stringResource(id = R.string.sec_btn_cancel), fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val targetInput = if (!showRecoveryMode) pinInput else recoveryInput
                            if (targetInput.isNotBlank() && onVerify(targetInput)) {
                                onSuccess()
                            } else {
                                val msg = if (!showRecoveryMode) context.getString(R.string.sec_toast_incorrect_current_pin) else context.getString(R.string.sec_toast_incorrect_recovery)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (!showRecoveryMode) pinInput = "" else recoveryInput = ""
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(id = R.string.sec_btn_confirm), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

