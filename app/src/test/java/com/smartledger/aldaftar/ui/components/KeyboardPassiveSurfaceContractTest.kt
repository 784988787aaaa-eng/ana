package com.smartledger.aldaftar.ui.components

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

/** A passive accordion must not open the IME merely because it expands. */
class KeyboardPassiveSurfaceContractTest {
    @Test fun businessPhoneExpansionDoesNotRequestAutomaticKeyboard() {
        val candidates = listOf(
            File("src/main/java/com/smartledger/aldaftar/ui/screens/business/BusinessProfilePhonesSection.kt"),
            File("app/src/main/java/com/smartledger/aldaftar/ui/screens/business/BusinessProfilePhonesSection.kt")
        )
        val source = candidates.firstOrNull(File::exists)?.readText() ?: error("source not found")
        assertFalse(source.contains("RequestFocusAndShowKeyboard"))
        assertFalse(source.contains("requestFocusAndShowKeyboard"))
    }
}
