package com.smartledger.aldaftar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

/**
 * Reliable first-field focus + IME opening for Compose input surfaces.
 *
 * Dialog windows can exist for a few frames before the IME is willing to
 * honour a show() request. We therefore make the window IME-visible and
 * retry focus/show for a short, bounded period. This is deliberately local
 * to input surfaces so normal screens never pop the keyboard unexpectedly.
 */
suspend fun requestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    attempts: Int = 8,
    delayMs: Long = 35L,
    postToView: (() -> Unit)? = null
) {
    awaitFrame()
    awaitFrame()

    repeat(attempts) { attempt ->
        runCatching { focusRequester.requestFocus() }
        runCatching { keyboardController?.show() }
        postToView?.invoke()
        if (attempt == 0 || attempt == attempts - 1) {
            // Compose's controller can race the dialog window during its first frame.
            // Ask the platform IME controller as a second, idempotent path.
            // The caller supplies postToView so this stays scoped to the focused field.
        }
        if (attempt < attempts - 1) delay(delayMs)
    }
}

/**
 * Use on a dialog/bottom-sheet that has an obvious first editable field.
 * It does not run on ordinary screens unless the caller places it there.
 */
@Composable
fun RequestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    enabled: Boolean = true,
    key: Any? = Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    LaunchedEffect(enabled, key) {
        if (!enabled) return@LaunchedEffect

        val dialogWindow = (view.parent as? DialogWindowProvider)?.window
        val activityWindow = view.context.findActivity()?.window
        val window = dialogWindow ?: activityWindow
        window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        requestFocusAndShowKeyboard(
            focusRequester = focusRequester,
            keyboardController = keyboardController,
            attempts = 12,
            delayMs = 40L,
            postToView = {
                view.post {
                    runCatching { keyboardController?.show() }
                    runCatching { ViewCompat.getWindowInsetsController(view)?.show(WindowInsetsCompat.Type.ime()) }
                }
            }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

