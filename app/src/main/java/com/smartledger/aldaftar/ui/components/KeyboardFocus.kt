package com.smartledger.aldaftar.ui.components

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay

/**
 * Reliable first-field focus for Compose dialogs.
 *
 * Some OEM keyboards ignore the first show() call while a Dialog is still
 * attaching its window. We intentionally retry for a short, bounded period.
 * This keeps the UX fast without introducing a permanent keyboard loop.
 */
suspend fun requestFocusAndShowKeyboard(
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    attempts: Int = 5,
    delayMs: Long = 45L
) {
    awaitFrame()
    repeat(attempts) { attempt ->
        try {
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {
            // The dialog may still be attaching; the next bounded attempt handles it.
        }
        if (attempt < attempts - 1) delay(delayMs)
    }
}
