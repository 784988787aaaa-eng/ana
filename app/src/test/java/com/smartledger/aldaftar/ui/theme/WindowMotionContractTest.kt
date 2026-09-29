package com.smartledger.aldaftar.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test

class WindowMotionContractTest {
    @Test fun dialogMotionIsShortAndDeterministic() {
        assertTrue(MizanAnimationTokens.DURATION_DIALOG_ENTER in 120..200)
        assertTrue(MizanAnimationTokens.DURATION_DIALOG_EXIT in 90..160)
        assertTrue(MizanAnimationTokens.DURATION_DIALOG_EXIT < MizanAnimationTokens.DURATION_DIALOG_ENTER)
    }
    @Test fun standardMotionDoesNotUseLongBlockingAnimations() {
        assertTrue(MizanAnimationTokens.DURATION_STANDARD <= 300)
        assertTrue(MizanAnimationTokens.DURATION_CROSSFADE <= 200)
    }
}
