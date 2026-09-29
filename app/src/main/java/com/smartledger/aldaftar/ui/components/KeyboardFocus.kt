package com.smartledger.aldaftar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.window.DialogWindowProvider
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.android.awaitFrame

/**
 * Opens the IME only for a field that explicitly owns focus.
 *
 * The caller must place this composable in the same Compose window as the
 * target input. This is especially important for Dialogs, because a Compose
 * Dialog owns a separate Android Window from the Activity.
 *
 * The first attempt happens on the first attached frame. Two additional
 * frame-bounded attempts cover Android/IME connection timing without sleeps,
 * unbounded retries, or global ALWAYS_VISIBLE window flags.
 */
suspend fun requestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    imeTargetView: View? = null,
    attempts: Int = 3
) {
    val boundedAttempts = attempts.coerceIn(1, 3)

    repeat(boundedAttempts) { attempt ->
        awaitFrame()

        // Compose versions used by this project expose requestFocus() with a
        // return type that may be Unit rather than Boolean. Keep the helper
        // source-compatible with that API while still honoring a Boolean
        // result when the runtime provides one. A successful Unit-returning
        // request is treated as a successful focus request; an exception is
        // still a failed attempt and is retried on the next frame.
        val focusResult = runCatching { focusRequester.requestFocus() }
        val focused = focusResult.isSuccess &&
            ((focusResult.getOrNull() as? Boolean) ?: true)

        if (focused) {
            runCatching { keyboardController?.show() }

            val view = imeTargetView
            if (view != null && view.isAttachedToWindow && view.isShown) {
                runCatching {
                    ViewCompat.getWindowInsetsController(view)
                        ?.show(WindowInsetsCompat.Type.ime())
                }
            }
        }

        if (focused) return

        // The next frame is the retry boundary. This keeps the fallback
        // deterministic and gives Compose/Android one more frame to attach
        // the input connection.
        if (attempt == boundedAttempts - 1) return
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
 * Configures the Android window that hosts a Compose Dialog for IME resize.
 * The window is not told to show the IME; focus ownership remains explicit.
 */
@Composable
fun ConfigureDialogImeWindow() {
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
    DisposableEffect(dialogWindow) {
        dialogWindow?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
        )
        onDispose { }
    }
}

/**
 * Automatic IME opening is opt-in.
 *
 * IMPORTANT: call this next to the target input, inside the Dialog/BottomSheet
 * that owns that input. Calling it from the Activity-level parent of a Dialog
 * creates a race between two Android Windows and is not a reliable IME contract.
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
    val hostView = LocalView.current

    LaunchedEffect(enabled, key, autoShow) {
        if (!enabled || !autoShow) {
            if (!enabled) {
                hideKeyboardAndClearFocus(focusManager, keyboardController)
            }
            return@LaunchedEffect
        }

        requestFocusAndShowKeyboard(
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            imeTargetView = hostView
        )
    }

    DisposableEffect(enabled, key, autoShow) {
        onDispose {
            hideKeyboardAndClearFocus(focusManager, keyboardController)
        }
    }
}
