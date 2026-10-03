package com.smartledger.aldaftar.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.platform.security.BiometricAuthHelper
import com.smartledger.aldaftar.ui.screens.security.lock.LockHapticHelper
import com.smartledger.aldaftar.ui.screens.security.lock.LockHapticType
import com.smartledger.aldaftar.ui.screens.security.lock.PasscodeKeypadContent
import com.smartledger.aldaftar.ui.screens.security.lock.RecoveryPhraseContent
import com.smartledger.aldaftar.ui.theme.NeutralBackgroundDark
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppLockScreen(
    viewModel: SecurityViewModel,
    onUnlockSuccess: () -> Unit,
    onUnlockBypassedAndDisabled: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val vibrator = remember(context) { LockHapticHelper.getVibrator(context) }

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isBiometricSupported = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }

    var enteredPasscode by remember { mutableStateOf("") }
    var isCheckingPasscode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryView by remember { mutableStateOf(false) }
    var recoveryPhraseInput by remember { mutableStateOf("") }
    var showHintText by remember { mutableStateOf(false) }
    val recoveryHint = settings.recoveryHint

    val shakeOffset = remember { Animatable(0f) }

    val currentEnteredPasscode by rememberUpdatedState(enteredPasscode)
    val currentIsCheckingPasscode by rememberUpdatedState(isCheckingPasscode)
    val currentPasscodeHash by rememberUpdatedState(settings.passcodeHash.orEmpty())

    var lockoutTimeRemainingSec by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val ms = viewModel.getLockoutTimeRemainingMs()
            lockoutTimeRemainingSec = if (ms > 0) (ms + 999) / 1000 else 0L
            delay(1000L)
        }
    }

    val triggerErrorAnimationAndHaptic = {
        scope.launch {
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
            shakeOffset.animateTo(12f, tween(40))
            shakeOffset.animateTo(-12f, tween(40))
            shakeOffset.animateTo(6f, tween(40))
            shakeOffset.animateTo(0f, tween(40))
        }
        Unit
    }

    val triggerBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (activity != null && isBiometricSupported && lockoutTimeRemainingSec <= 0L) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = context.getString(R.string.lock_ledger_locked),
                subtitle = context.getString(R.string.lock_enter_pin_prompt),
                negativeButtonText = context.getString(R.string.lock_cancel_btn),
                onSuccess = {
                    viewModel.resetFailedAttempts()
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

    LaunchedEffect(isBiometricSupported, isBiometricEnabled, lockoutTimeRemainingSec) {
        if (isBiometricSupported && isBiometricEnabled && !showRecoveryView && lockoutTimeRemainingSec <= 0L) {
            triggerBiometricPrompt()
        }
    }

    val onKeyPress = remember(vibrator) {
        { key: String ->
            if (lockoutTimeRemainingSec > 0L) {
                triggerErrorAnimationAndHaptic()
            } else if (!currentIsCheckingPasscode && currentEnteredPasscode.length < 4) {
                errorMessage = null
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
                val nextPasscode = currentEnteredPasscode + key
                enteredPasscode = nextPasscode

                if (nextPasscode.length == 4) {
                    isCheckingPasscode = true
                    scope.launch {
                        val passChars = nextPasscode.toCharArray()
                        val isMatch = withContext(Dispatchers.Default) {
                            try {
                                HashUtils.verifyPin(String(passChars), currentPasscodeHash)
                            } finally {
                                HashUtils.wipeCharArray(passChars)
                            }
                        }
                        if (isMatch) {
                            viewModel.resetFailedAttempts()
                            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                            onUnlockSuccess()
                        } else {
                            viewModel.handleFailedAttempt()
                            val newMs = viewModel.getLockoutTimeRemainingMs()
                            if (newMs > 0) {
                                lockoutTimeRemainingSec = (newMs + 999) / 1000
                            }
                            triggerErrorAnimationAndHaptic()
                            errorMessage = context.getString(R.string.lock_incorrect_pin)
                            enteredPasscode = ""
                            isCheckingPasscode = false
                        }
                    }
                }
            }
        }
    }

    val onDeleteClick = {
        if (!isCheckingPasscode) {
            errorMessage = null
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
            if (enteredPasscode.isNotEmpty()) {
                enteredPasscode = enteredPasscode.dropLast(1)
            }
        }
    }

    val onForgotClick = {
        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
        showRecoveryView = true
    }

    val onVerifyRecoveryPhrase = {
        scope.launch {
            val recoveryChars = recoveryPhraseInput.trim().toCharArray()
            val isCorrect = withContext(Dispatchers.Default) {
                try {
                    HashUtils.verifyPin(String(recoveryChars), settings.recoveryPhraseHash)
                } finally {
                    HashUtils.wipeCharArray(recoveryChars)
                }
            }
            if (isCorrect) {
                viewModel.resetFailedAttempts()
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                keyboardController?.hide()
                focusManager.clearFocus()
                Toast.makeText(context, context.getString(R.string.lock_recovery_matched), Toast.LENGTH_SHORT).show()
                onUnlockBypassedAndDisabled()
            } else {
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                Toast.makeText(context, context.getString(R.string.lock_recovery_wrong), Toast.LENGTH_SHORT).show()
            }
        }
        Unit
    }

    val onReturnToKeypadClick = {
        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
        keyboardController?.hide()
        focusManager.clearFocus()
        recoveryPhraseInput = ""
        showRecoveryView = false
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = NeutralBackgroundDark
    ) {
        AnimatedContent(
            targetState = showRecoveryView,
            transitionSpec = {
                fadeIn(animationSpec = tween(120)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            label = "ScreenType"
        ) { isRecovery ->
            if (isRecovery) {
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
                PasscodeKeypadContent(
                    enteredPasscode = enteredPasscode,
                    isCheckingPasscode = isCheckingPasscode,
                    shakeOffsetPx = shakeOffset.value,
                    isBiometricSupported = isBiometricSupported,
                    errorMessage = errorMessage,
                    onKeyPress = onKeyPress,
                    onDeleteClick = onDeleteClick,
                    onForgotClick = onForgotClick,
                    onBiometricClick = {
                        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
                        triggerBiometricPrompt()
                    },
                    lockoutTimeRemainingSec = lockoutTimeRemainingSec
                )
            }
        }
    }
}
