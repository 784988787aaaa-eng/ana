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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.platform.security.BiometricAuthHelper
import com.smartledger.aldaftar.ui.screens.security.lock.LockHapticHelper
import com.smartledger.aldaftar.ui.screens.security.lock.LockHapticType
import com.smartledger.aldaftar.ui.screens.security.lock.PasscodeKeypadContent
import com.smartledger.aldaftar.ui.screens.security.lock.RecoveryPhraseContent
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val lifecycleOwner = LocalLifecycleOwner.current
    val mizanColors = MaterialTheme.mizanColors
    val vibrator = remember(context) { LockHapticHelper.getVibrator(context) }

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val lockoutUntil by viewModel.lockoutUntilState.collectAsStateWithLifecycle()
    val isBiometricSupported = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }
    val canUseBiometric = isBiometricSupported && isBiometricEnabled

    var enteredPasscode by remember { mutableStateOf("") }
    var isCheckingPasscode by remember { mutableStateOf(false) }
    var isVerifyingRecovery by remember { mutableStateOf(false) }
    var hasUnlocked by remember { mutableStateOf(false) }
    var hasAutoTriggeredBiometric by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recoveryErrorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryView by remember { mutableStateOf(false) }
    var recoveryPhraseInput by remember { mutableStateOf("") }
    var showHintText by remember { mutableStateOf(false) }
    val recoveryHint = settings.recoveryHint

    val shakeOffset = remember { Animatable(0f) }

    val currentEnteredPasscode by rememberUpdatedState(enteredPasscode)
    val currentIsCheckingPasscode by rememberUpdatedState(isCheckingPasscode)
    val currentHasUnlocked by rememberUpdatedState(hasUnlocked)

    var lockoutTimeRemainingSec by remember { mutableStateOf(0L) }

    // Reactive countdown timer: ONLY runs when an active lockout timestamp exists
    LaunchedEffect(lockoutUntil) {
        while (true) {
            val now = System.currentTimeMillis()
            val remainingMs = if (lockoutUntil > now) lockoutUntil - now else viewModel.getLockoutTimeRemainingMs()
            if (remainingMs <= 0L) {
                lockoutTimeRemainingSec = 0L
                break
            }
            lockoutTimeRemainingSec = (remainingMs + 999L) / 1000L
            delay(250L)
        }
    }

    // Clear partial sensitive input and cancel biometric prompt if app moves to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                BiometricAuthHelper.cancelActiveAuthentication()
                if (!currentIsCheckingPasscode) {
                    enteredPasscode = ""
                }
                hasAutoTriggeredBiometric = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val triggerErrorAnimationAndHaptic = {
        scope.launch {
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
            shakeOffset.animateTo(12f, tween(35))
            shakeOffset.animateTo(-12f, tween(35))
            shakeOffset.animateTo(6f, tween(35))
            shakeOffset.animateTo(0f, tween(35))
        }
        Unit
    }

    val completeUnlockOnce = {
        if (!hasUnlocked) {
            hasUnlocked = true
            BiometricAuthHelper.cancelActiveAuthentication()
            viewModel.resetFailedAttempts()
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
            onUnlockSuccess()
        }
    }

    val triggerBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (
            activity != null &&
            canUseBiometric &&
            !showRecoveryView &&
            !isCheckingPasscode &&
            !hasUnlocked &&
            lockoutTimeRemainingSec <= 0L &&
            !BiometricAuthHelper.isAuthenticationInProgress()
        ) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = context.getString(R.string.lock_ledger_locked),
                subtitle = context.getString(R.string.lock_enter_pin_prompt),
                negativeButtonText = context.getString(R.string.lock_cancel_btn),
                onSuccess = {
                    completeUnlockOnce()
                },
                onError = { _, _ -> },
                onFailed = {
                    LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                }
            )
        }
    }

    LaunchedEffect(canUseBiometric, showRecoveryView, lockoutTimeRemainingSec) {
        if (canUseBiometric && !showRecoveryView && lockoutTimeRemainingSec <= 0L && !hasAutoTriggeredBiometric && !hasUnlocked) {
            hasAutoTriggeredBiometric = true
            triggerBiometricPrompt()
        }
    }

    val onKeyPress = remember(vibrator) {
        { key: String ->
            if (lockoutTimeRemainingSec > 0L) {
                triggerErrorAnimationAndHaptic()
            } else if (!currentIsCheckingPasscode && !currentHasUnlocked && currentEnteredPasscode.length < 4) {
                errorMessage = null
                LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
                val nextPasscode = currentEnteredPasscode + key
                enteredPasscode = nextPasscode

                if (nextPasscode.length == 4) {
                    isCheckingPasscode = true
                    scope.launch {
                        val isMatch = try {
                            viewModel.verifyPinOffMainThread(nextPasscode)
                        } catch (_: Exception) {
                            false
                        }
                        if (isMatch) {
                            isCheckingPasscode = false
                            completeUnlockOnce()
                        } else {
                            viewModel.handleFailedAttempt()
                            val attempts = viewModel.getFailedAttemptsCount()
                            val newMs = viewModel.getLockoutTimeRemainingMs()
                            if (newMs > 0L) {
                                lockoutTimeRemainingSec = (newMs + 999L) / 1000L
                            }
                            triggerErrorAnimationAndHaptic()
                            errorMessage = if (attempts in 3..4) {
                                context.getString(R.string.lock_attempts_remaining_warning, 5 - attempts)
                            } else {
                                context.getString(R.string.lock_incorrect_pin)
                            }
                            enteredPasscode = ""
                            isCheckingPasscode = false
                        }
                    }
                }
            }
        }
    }

    val onDeleteClick = {
        if (!isCheckingPasscode && !hasUnlocked) {
            errorMessage = null
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
            if (enteredPasscode.isNotEmpty()) {
                enteredPasscode = enteredPasscode.dropLast(1)
            }
        }
    }

    val onForgotClick = {
        if (!isCheckingPasscode && !hasUnlocked) {
            BiometricAuthHelper.cancelActiveAuthentication()
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
            errorMessage = null
            recoveryErrorMessage = null
            enteredPasscode = ""
            showRecoveryView = true
        }
    }

    val onVerifyRecoveryPhrase = {
        if (!isVerifyingRecovery && !hasUnlocked && recoveryPhraseInput.isNotBlank()) {
            isVerifyingRecovery = true
            recoveryErrorMessage = null
            scope.launch {
                val isCorrect = try {
                    viewModel.verifyRecoveryPhraseOffMainThread(recoveryPhraseInput)
                } catch (_: Exception) {
                    false
                }
                if (isCorrect) {
                    try {
                        viewModel.disablePasscodeAfterRecovery(settings)
                        hasUnlocked = true
                        isVerifyingRecovery = false
                        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.SUCCESS)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        Toast.makeText(context, context.getString(R.string.lock_recovery_matched), Toast.LENGTH_SHORT).show()
                        onUnlockBypassedAndDisabled()
                    } catch (e: Exception) {
                        isVerifyingRecovery = false
                        LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                        recoveryErrorMessage = e.message ?: context.getString(R.string.toast_operation_failed)
                    }
                } else {
                    isVerifyingRecovery = false
                    LockHapticHelper.performLockHaptic(vibrator, LockHapticType.ERROR)
                    recoveryErrorMessage = context.getString(R.string.lock_recovery_wrong)
                }
            }
        }
        Unit
    }

    val onReturnToKeypadClick = {
        if (!isVerifyingRecovery) {
            LockHapticHelper.performLockHaptic(vibrator, LockHapticType.KEYPRESS)
            keyboardController?.hide()
            focusManager.clearFocus()
            recoveryPhraseInput = ""
            recoveryErrorMessage = null
            showRecoveryView = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = mizanColors.securityBackground
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
                    onRecoveryPhraseChange = {
                        recoveryPhraseInput = it
                        recoveryErrorMessage = null
                    },
                    recoveryHint = recoveryHint,
                    showHintText = showHintText,
                    onToggleHint = { showHintText = !showHintText },
                    onVerifyClick = onVerifyRecoveryPhrase,
                    onReturnToKeypadClick = onReturnToKeypadClick,
                    isVerifying = isVerifyingRecovery,
                    errorMessage = recoveryErrorMessage
                )
            } else {
                PasscodeKeypadContent(
                    enteredPasscode = enteredPasscode,
                    isCheckingPasscode = isCheckingPasscode,
                    shakeOffsetPx = shakeOffset.value,
                    isBiometricSupported = canUseBiometric,
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

