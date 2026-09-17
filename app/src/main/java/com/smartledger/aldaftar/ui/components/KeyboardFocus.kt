package com.smartledger.aldaftar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

/**
 * Opens the IME only for a field that explicitly owns focus.
 *
 * Important lifecycle rule: requesting the keyboard is paired with an explicit
 * cleanup when the owner leaves composition or becomes disabled. No window is
 * forced into ALWAYS_VISIBLE mode; that global window flag is a common cause
 * of the keyboard surviving dialog dismissal or reappearing on the next frame.
 */
suspend fun requestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    attempts: Int = 4,
    delayMs: Long = 35L
) {
    awaitFrame()
    repeat(attempts) { attempt ->
        runCatching { focusRequester.requestFocus() }
        runCatching { keyboardController?.show() }
        if (attempt < attempts - 1) delay(delayMs)
    }
}

/** Hides the IME and removes Compose focus from the current input owner. */
fun hideKeyboardAndClearFocus(
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) {
    runCatching { focusManager.clearFocus(force = true) }
    runCatching { keyboardController?.hide() }
}

/**
 * Automatic IME opening is opt-in. Callers must set autoShow=true only when
 * opening the surface should intentionally focus a specific field. Cleanup is
 * tied to the composable lifecycle so a dismissed surface cannot leave a
 * focused text field or IME behind.
 */
@Composable
fun RequestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    enabled: Boolean = true,
    key: Any? = Unit,
    autoShow: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(enabled, key, autoShow) {
        if (!enabled || !autoShow) {
            if (!enabled) hideKeyboardAndClearFocus(focusManager, keyboardController)
            return@LaunchedEffect
        }
        requestFocusAndShowKeyboard(
            focusRequester = focusRequester,
            keyboardController = keyboardController
        )
    }

    DisposableEffect(enabled, key, autoShow) {
        onDispose {
            hideKeyboardAndClearFocus(focusManager, keyboardController)
        }
    }
}
