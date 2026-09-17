package com.smartledger.aldaftar.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * UI contract: the IME must never be made globally persistent by a dialog window.
 * The helper must also own lifecycle cleanup so a dismissed surface cannot keep
 * a focused field alive.
 */
class KeyboardLifecycleContractTest {
    private fun source(name: String): String {
        val candidates = listOf(
            File("src/main/java/com/smartledger/aldaftar/ui/components/$name"),
            File("app/src/main/java/com/smartledger/aldaftar/ui/components/$name")
        )
        return candidates.firstOrNull(File::exists)?.readText()
            ?: error("Source file not found: $name")
    }

    @Test fun keyboardHelperMustNotForceAlwaysVisibleWindowState() {
        val helper = source("KeyboardFocus.kt")
        assertFalse(helper.contains("SOFT_INPUT_STATE_ALWAYS_" + "VISIBLE"))
        assertTrue(helper.contains("clearFocus(force = true)"))
        assertTrue(helper.contains("keyboardController?.hide()"))
    }

    @Test fun animatedDialogMustCleanImeBeforeAndAfterDismissal() {
        val dialog = source("MizanAnimatedDialog.kt")
        assertTrue(dialog.contains("hideKeyboardAndClearFocus(focusManager, keyboardController)"))
        assertTrue(dialog.contains("DisposableEffect(Unit)"))
    }
}
