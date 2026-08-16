package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.BiometricAuthHelper
import com.example.domain.DatabaseSecurityGuard
import com.example.domain.HashUtils
import com.example.domain.StringUtils.toEnglishDigits
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class HapticType {
    KEYPRESS, SUCCESS, ERROR
}

private fun performLockHaptic(vibrator: Vibrator?, type: HapticType) {
    val vib = vibrator ?: return
    try {
        if (!vib.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (type) {
                HapticType.KEYPRESS -> {
                    vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                }
                HapticType.SUCCESS -> {
                    vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
                HapticType.ERROR -> {
                    vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), intArrayOf(0, 255, 0, 255), -1))
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (type) {
                HapticType.KEYPRESS -> vib.vibrate(VibrationEffect.createOneShot(10, 90))
                HapticType.SUCCESS -> vib.vibrate(VibrationEffect.createOneShot(35, 180))
                HapticType.ERROR -> vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), -1))
            }
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.KEYPRESS -> vib.vibrate(10)
                HapticType.SUCCESS -> vib.vibrate(35)
                HapticType.ERROR -> vib.vibrate(longArrayOf(0, 45, 60, 45), -1)
            }
        }
    } catch (_: Exception) {
        // Fallback safely if device lacks vibration hardware permission
    }
}

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

    val vibrator = remember(context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isBiometricSupported = remember(context) { BiometricAuthHelper.isBiometricAvailable(context) }

    var enteredPasscode by remember { mutableStateOf("") }
    var isCheckingPasscode by remember { mutableStateOf(false) }
    var showRecoveryView by remember { mutableStateOf(false) }
    var recoveryPhraseInput by remember { mutableStateOf("") }
    var showHintText by remember { mutableStateOf(false) }
    val recoveryHint = settings.recoveryHint

    val shakeOffset = remember { Animatable(0f) }

    val currentEnteredPasscode by rememberUpdatedState(enteredPasscode)
    val currentIsCheckingPasscode by rememberUpdatedState(isCheckingPasscode)
    val currentPasscodeHash by rememberUpdatedState(settings.passcodeHash.orEmpty())

    val triggerErrorAnimationAndHaptic = {
        scope.launch {
            performLockHaptic(vibrator, HapticType.ERROR)
            shakeOffset.animateTo(24f, tween(40))
            shakeOffset.animateTo(-24f, tween(40))
            shakeOffset.animateTo(16f, tween(35))
            shakeOffset.animateTo(-16f, tween(35))
            shakeOffset.animateTo(8f, tween(30))
            shakeOffset.animateTo(0f, tween(30))
        }
        Unit
    }

    val triggerBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (activity != null && isBiometricSupported) {
            BiometricAuthHelper.authenticate(
                activity = activity,
                title = context.getString(R.string.lock_ledger_locked),
                subtitle = context.getString(R.string.lock_enter_pin_prompt),
                negativeButtonText = context.getString(R.string.lock_cancel_btn),
                onSuccess = {
                    performLockHaptic(vibrator, HapticType.SUCCESS)
                    onUnlockSuccess()
                },
                onError = { _, _ -> },
                onFailed = {
                    performLockHaptic(vibrator, HapticType.ERROR)
                }
            )
        }
    }

    // Auto-launch Biometric prompt on screen appearance if enabled and supported
    LaunchedEffect(isBiometricSupported, isBiometricEnabled) {
        if (isBiometricSupported && isBiometricEnabled && !showRecoveryView) {
            kotlinx.coroutines.delay(200)
            triggerBiometricPrompt()
        }
    }

    val onKeyPress = remember(vibrator) {
        { key: String ->
            if (!currentIsCheckingPasscode && currentEnteredPasscode.length < 4) {
                performLockHaptic(vibrator, HapticType.KEYPRESS)
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
                                HashUtils.wipeCharArray(passChars)
                            }
                        }
                        if (isMatch) {
                            performLockHaptic(vibrator, HapticType.SUCCESS)
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

    val onDeleteClick = {
        if (!isCheckingPasscode) {
            performLockHaptic(vibrator, HapticType.KEYPRESS)
            if (enteredPasscode.isNotEmpty()) {
                enteredPasscode = enteredPasscode.dropLast(1)
            }
        }
    }

    val onForgotClick = {
        performLockHaptic(vibrator, HapticType.KEYPRESS)
        showRecoveryView = true
    }

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
                performLockHaptic(vibrator, HapticType.SUCCESS)
                keyboardController?.hide()
                focusManager.clearFocus()
                val successMsg = context.getString(R.string.lock_recovery_matched)
                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                onUnlockBypassedAndDisabled()
            } else {
                performLockHaptic(vibrator, HapticType.ERROR)
                val errorMsg = context.getString(R.string.lock_recovery_wrong)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
        Unit
    }

    val onReturnToKeypadClick = {
        performLockHaptic(vibrator, HapticType.KEYPRESS)
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
        color = DarkBackground
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
                    onKeyPress = onKeyPress,
                    onDeleteClick = onDeleteClick,
                    onForgotClick = onForgotClick,
                    onBiometricClick = {
                        performLockHaptic(vibrator, HapticType.KEYPRESS)
                        triggerBiometricPrompt()
                    }
                )
            }
        }
    }
}

@Composable
private fun PasscodeDotIndicators(
    enteredLength: Int,
    shakeOffsetPx: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.offset(x = shakeOffsetPx.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 4) {
            val filled = enteredLength > i
            val dotScale by animateFloatAsState(
                targetValue = if (filled) 1.25f else 1.0f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessHigh,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "dotScale_$i"
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(if (filled) EmeraldPrimary else Color.White.copy(alpha = 0.12f))
                    .border(
                        width = 1.2.dp,
                        color = if (filled) EmeraldPrimary else Color.White.copy(alpha = 0.25f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PasscodeKeypadContent(
    enteredPasscode: String,
    isCheckingPasscode: Boolean,
    shakeOffsetPx: Float,
    isBiometricSupported: Boolean,
    onKeyPress: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onForgotClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val row1 = remember { listOf("1", "2", "3") }
    val row2 = remember { listOf("4", "5", "6") }
    val row3 = remember { listOf("7", "8", "9") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Area with Micro-animation
        val lockHeaderScale by animateFloatAsState(
            targetValue = if (isCheckingPasscode) 1.15f else if (enteredPasscode.isNotEmpty()) 1.05f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "lockHeaderScale"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(lockHeaderScale)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(id = R.string.lock_app_locked_desc),
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.lock_ledger_locked),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.lock_enter_pin_prompt),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.62f)
            )
        }

        // 4 Round Indicators
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PasscodeDotIndicators(
                enteredLength = enteredPasscode.length,
                shakeOffsetPx = shakeOffsetPx
            )
        }

        // Keypad Area
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                KeypadRow(row = row1, onKeyClick = onKeyPress)
                KeypadRow(row = row2, onKeyClick = onKeyPress)
                KeypadRow(row = row3, onKeyClick = onKeyPress)

                // Last row with Biometric Icon / "0" / Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBiometricSupported) {
                        KeypadIconButton(
                            icon = Icons.Default.Fingerprint,
                            contentDescription = stringResource(id = R.string.sec_biometric_title),
                            onClick = onBiometricClick
                        )
                    } else {
                        Box(modifier = Modifier.size(72.dp))
                    }

                    KeypadButton(text = "0", isFunctional = false) {
                        onKeyPress("0")
                    }

                    KeypadButton(text = stringResource(id = R.string.lock_delete_btn), isFunctional = true) {
                        onDeleteClick()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.lock_forgot_pin),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoralAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onForgotClick() }
                        .padding(8.dp)
                        .testTag("lock_forgot_pin_btn"),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecoveryPhraseContent(
    recoveryPhraseInput: String,
    onRecoveryPhraseChange: (String) -> Unit,
    recoveryHint: String?,
    showHintText: Boolean,
    onToggleHint: () -> Unit,
    onVerifyClick: () -> Unit,
    onReturnToKeypadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CoralAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = stringResource(id = R.string.lock_recover_account),
                tint = CoralAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_desc),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = recoveryPhraseInput,
            onValueChange = { onRecoveryPhraseChange(it.toEnglishDigits()) },
            modifier = Modifier.fillMaxWidth().testTag("recovery_phrase_input_lock"),
            label = { Text(stringResource(id = R.string.lock_recovery_phrase_hint), color = Color.White.copy(alpha = 0.6f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralAccent,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedLabelColor = CoralAccent,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (recoveryPhraseInput.isNotBlank()) {
                        onVerifyClick()
                    }
                }
            )
        )

        if (!recoveryHint.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { onToggleHint() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showHintText) stringResource(id = R.string.lock_hide_hint) else stringResource(id = R.string.lock_show_hint),
                    color = WarningAmber,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        AnimatedVisibility(visible = showHintText && !recoveryHint.isNullOrBlank()) {
            Text(
                text = stringResource(id = R.string.lock_hint_prefix, recoveryHint ?: ""),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_recovery_unlock_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
            shape = RoundedCornerShape(16.dp),
            enabled = recoveryPhraseInput.isNotBlank()
        ) {
            Text(
                text = stringResource(id = R.string.lock_verify_and_unlock),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onReturnToKeypadClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(id = R.string.lock_back_to_keypad_desc),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.lock_return_to_keypad),
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun KeypadRow(row: List<String>, onKeyClick: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.forEach { digit ->
            KeypadButton(text = digit, isFunctional = false, onClick = { onKeyClick(digit) })
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    isFunctional: Boolean,
    onClick: () -> Unit
) {
    val bg = remember(isFunctional) { if (isFunctional) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.12f) }
    val textCol = remember(isFunctional) { if (isFunctional) Color.White.copy(alpha = 0.8f) else Color.White }
    val textSize = remember(isFunctional) { if (isFunctional) 13.sp else 24.sp }
    val fontWeight = remember(isFunctional) { if (isFunctional) FontWeight.Medium else FontWeight.ExtraBold }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .testTag("keypad_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textCol,
            fontSize = textSize,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KeypadIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(EmeraldPrimary.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = EmeraldPrimary.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .testTag("keypad_biometric_btn"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = EmeraldPrimary,
            modifier = Modifier.size(30.dp)
        )
    }
}
