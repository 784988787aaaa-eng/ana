package com.smartledger.aldaftar.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveLayoutTest {
    @Test fun compactBoundaryIsStable() {
        assertEquals(MizanWindowSizeClass.Compact, mizanWindowSizeClassForWidth(320))
        assertEquals(MizanWindowSizeClass.Compact, mizanWindowSizeClassForWidth(599))
    }

    @Test fun mediumBoundaryIsStable() {
        assertEquals(MizanWindowSizeClass.Medium, mizanWindowSizeClassForWidth(600))
        assertEquals(MizanWindowSizeClass.Medium, mizanWindowSizeClassForWidth(839))
    }

    @Test fun expandedBoundaryIsStable() {
        assertEquals(MizanWindowSizeClass.Expanded, mizanWindowSizeClassForWidth(840))
        assertEquals(MizanWindowSizeClass.Expanded, mizanWindowSizeClassForWidth(1280))
    }

}
