package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة شاشات قفل وحماية التطبيق (Application Security & Lock Screens Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على واجهة قفل التطبيق الرئيسية (AppLockScreen)، والتي تجمع بين
 * التحقق برمز المرور (PIN)، البصمة الحيوية (Biometrics)، وعبارة الاسترداد الآمنة (Recovery Phrase).
 * =====================================================================================
 */

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.BiometricAuthHelper
import com.example.domain.DatabaseSecurityGuard
import com.example.domain.HashUtils
import com.example.ui.screens.security.lock.LockHapticHelper
import com.example.ui.screens.security.lock.LockHapticType
import com.example.ui.screens.security.lock.PasscodeKeypadContent
import com.example.ui.screens.security.lock.RecoveryPhraseContent
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * =====================================================================================
 * واجهة قفل التطبيق الموحدة (AppLockScreen)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * شاشة الحماية المركزية التي تعمل كبوابة أمنية صارمة تمنع الوصول إلى البيانات المالية دون توثيق:
 * 1. لوحة إدخال رمز المرور (PIN) مع اهتزازات حسية عند النقر وحركات اهتزازية (Shake) عند الخطأ.
 * 2. التحقق البيومتري الفوري (بصمة الإصبع أو الوجه) التلقائي عند فتح التطبيق.
 * 3. آلية استرداد الحساب بكلمة السر الاحتياطية (Recovery Phrase) مع تلميح الأمان.
 * 4. إدارة الذاكرة الآمنة (Zero-Leakage Memory): مسح مصفوفات الحروف `CharArray` فور انتهاء المقارنة.
 *
 * [المُدخلات]:
 * - viewModel: نموذج بيانات الأمان وإعدادات التشفير والترخيص.
 * - onUnlockSuccess: دالة إلغاء القفل بنجاح والدخول لشاشات التطبيق.
 * - onUnlockBypassedAndDisabled: دالة الاسترداد وتجاوز القفل بعد التحقق من عبارة الاسترداد.
 * =====================================================================================
 */
@Composable
fun AppLockScreen(
    viewModel: SecurityAndLicenseViewModel,
    onUnlockSuccess: () -> Unit,
    onUnlockBypassedAndDisabled: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val vibrator = remember(context) { LockHapticHelper.getVibrator(context) }

    /*
     * ---------------------------------------------------------------------------------
     * جلب إعدادات الأمان وحالة دعم البصمة
     * ---------------------------------------------------------------------------------
     */
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isBiometricSupported = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }

    /*
     * ---------------------------------------------------------------------------------
     * متغيرات الحالة المحلية للإدخال والتحقق
     * ---------------------------------------------------------------------------------
     */
    var enteredPasscode by remember { mutableStateOf("") }
    var isCheckingPasscode by remember { mutableStateOf(false) }
    var showRecoveryView by remember { mutableStateOf(false) }
    var recoveryPhraseInput by remember { mutableStateOf("") }
    var showHintText by remember { mutableStateOf(false) }
    val recoveryHint = settings.recoveryHint

    // محرك الرسوم المتحركة للاهتزاز الأفقي عند إدخال رمز خاطئ (Error Shake)
    val shakeOffset = remember { Animatable(0f) }

    val currentEnteredPasscode by rememberUpdatedState(enteredPasscode)
    val currentIsCheckingPasscode by rememberUpdatedState(isCheckingPasscode)
    val currentPasscodeHash by rememberUpdatedState(settings.passcodeHash.orEmpty())

    /*
     * ---------------------------------------------------------------------------------
     * دالة تشغيل حركة الاهتزاز والتغذية اللمسية عند الخطأ
     * ---------------------------------------------------------------------------------
     */
    val triggerErrorAnimationAndHaptic = {
        scope.launch {
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
            shakeOffset.animateTo(24f, tween(40))
            shakeOffset.animateTo(-24f, tween(40))
            shakeOffset.animateTo(16f, tween(35))
            shakeOffset.animateTo(-16f, tween(35))
            shakeOffset.animateTo(8f, tween(30))
            shakeOffset.animateTo(0f, tween(30))
        }
        Unit
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة إطلاق موجه البصمة الحيوية (Biometric Prompt)
     * ---------------------------------------------------------------------------------
     */
    val triggerBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (activity != null && isBiometricSupported) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = context.getString(R.string.lock_ledger_locked),
                subtitle = context.getString(R.string.lock_enter_pin_prompt),
                negativeButtonText = context.getString(R.string.lock_cancel_btn),
                onSuccess = {
                    LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                    onUnlockSuccess()
                },
                onError = { _, _ -> },
                onFailed = {
                    LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                }
            )
        }
    }

    /*
     * إطلاق موجه البصمة تلقائياً بمجرد فتح الشاشة إذا كانت الميزة مفعلة ومدعومة
     */
    LaunchedEffect(isBiometricSupported, isBiometricEnabled) {
        if (isBiometricSupported && isBiometricEnabled && !showRecoveryView) {
            delay(200)
            triggerBiometricPrompt()
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * معالجة النقر على أزرار لوحة الأرقام (Passcode Keypad)
     * ---------------------------------------------------------------------------------
     */
    val onKeyPress = remember(vibrator) {
        { key: String ->
            if (!currentIsCheckingPasscode && currentEnteredPasscode.length < 4) {
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
                val nextPasscode = currentEnteredPasscode + key
                enteredPasscode = nextPasscode
                if (nextPasscode.length == 4) {
                    isCheckingPasscode = true
                    scope.launch {
                        val passChars = nextPasscode.toCharArray()
                        val isMatch = withContext(Dispatchers.Default) {
                            try {
                                val hashed = HashUtils.hashString(String(passChars))
                                DatabaseSecurityGuard.secureEqual(hashed, currentPasscodeHash)
                            } finally {
                                // مسح الذاكرة الحساسة فوراً بعد المقارنة
                                HashUtils.wipeCharArray(passChars)
                            }
                        }
                        if (isMatch) {
                            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                            onUnlockSuccess()
                        } else {
                            triggerErrorAnimationAndHaptic()
                            val fallbackMsg = context.getString(R.string.lock_incorrect_pin)
                            Toast.makeText(context, fallbackMsg, Toast.LENGTH_SHORT).show()
                            enteredPasscode = ""
                            isCheckingPasscode = false
                        }
                    }
                }
            }
        }
    }

    // زر مسح الرقم الأخير
    val onDeleteClick = {
        if (!isCheckingPasscode) {
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
            if (enteredPasscode.isNotEmpty()) {
                enteredPasscode = enteredPasscode.dropLast(1)
            }
        }
    }

    // زر الانتقال لواجهة استرداد الحساب
    val onForgotClick = {
        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
        showRecoveryView = true
    }

    /*
     * ---------------------------------------------------------------------------------
     * معالجة التحقق من عبارة الاسترداد (Recovery Phrase Verification)
     * ---------------------------------------------------------------------------------
     */
    val onVerifyRecoveryPhrase = {
        scope.launch {
            val recoveryChars = recoveryPhraseInput.trim().toCharArray()
            val isCorrect = withContext(Dispatchers.Default) {
                try {
                    val hashed = HashUtils.hashString(String(recoveryChars))
                    DatabaseSecurityGuard.secureEqual(hashed, settings.recoveryPhraseHash)
                } finally {
                    HashUtils.wipeCharArray(recoveryChars)
                }
            }
            if (isCorrect) {
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                keyboardController?.hide()
                focusManager.clearFocus()
                val successMsg = context.getString(R.string.lock_recovery_matched)
                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                onUnlockBypassedAndDisabled()
            } else {
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                val errorMsg = context.getString(R.string.lock_recovery_wrong)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
        Unit
    }

    // العودة من واجهة الاسترداد إلى لوحة الأرقام
    val onReturnToKeypadClick = {
        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
        keyboardController?.hide()
        focusManager.clearFocus()
        recoveryPhraseInput = ""
        showRecoveryView = false
    }

    /*
     * ---------------------------------------------------------------------------------
     * رسم واجهة القفل مع انتقال انسيابي بين لوحة الأرقام وشاشة الاسترداد
     * ---------------------------------------------------------------------------------
     */
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = showRecoveryView,
            transitionSpec = {
                slideInHorizontally { width -> if (targetState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState) -width else width } + fadeOut()
            },
            label = "ScreenType"
        ) { isRecovery ->
            if (isRecovery) {
                // شاشة إدخال عبارة الاسترداد والتلميح
                RecoveryPhraseContent(
                    recoveryPhraseInput = recoveryPhraseInput,
                    onRecoveryPhraseChange = { recoveryPhraseInput = it },
                    recoveryHint = recoveryHint,
                    showHintText = showHintText,
                    onToggleHint = { showHintText = !showHintText },
                    onVerifyClick = onVerifyRecoveryPhrase,
                    onReturnToKeypadClick = onReturnToKeypadClick
                )
            } else {
                // شاشة لوحة إدخال رمز المرور والأزرار البيومترية
                PasscodeKeypadContent(
                    enteredPasscode = enteredPasscode,
                    isCheckingPasscode = isCheckingPasscode,
                    shakeOffsetPx = shakeOffset.value,
                    isBiometricSupported = isBiometricSupported,
                    onKeyPress = onKeyPress,
                    onDeleteClick = onDeleteClick,
                    onForgotClick = onForgotClick,
                    onBiometricClick = {
                        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
                        triggerBiometricPrompt()
                    }
                )
            }
        }
    }
}

