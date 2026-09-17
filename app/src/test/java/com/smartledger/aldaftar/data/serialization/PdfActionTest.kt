package com.smartledger.aldaftar.data.serialization

import com.smartledger.aldaftar.data.serialization.pdf.PdfAction
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfActionTest {
    @Test fun parsesCaseInsensitively() {
        assertEquals(PdfAction.SAVE_LOCAL, PdfAction.from("save_local"))
    }

    @Test fun unknownActionFallsBackToShare() {
        assertEquals(PdfAction.SHARE, PdfAction.from("unsupported"))
    }
}
