package com.smartledger.aldaftar.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FloatingUiStateNormalizerTest {
    @Test fun searchClampsSizeAndRestoresInvalidRatiosToDefaults() {
        val state = FloatingUiStateNormalizer.search(FloatingSearchState(99, Float.NaN, -2f))
        assertEquals(FloatingUiStateNormalizer.MAX_SIZE_LEVEL, state.sizeLevel)
        assertEquals(.80f, state.ratioX)
        assertEquals(.70f, state.ratioY)
    }

    @Test fun addWithoutPositionDoesNotPersistStaleCoordinates() {
        val state = FloatingUiStateNormalizer.add(FloatingAddState(-3, .2f, .3f, false))
        assertEquals(FloatingUiStateNormalizer.MIN_SIZE_LEVEL, state.sizeLevel)
        assertEquals(-1f, state.ratioX)
        assertEquals(-1f, state.ratioY)
        assertFalse(state.hasPosition)
    }

    @Test fun addWithPositionClampsOnlyInvalidCoordinates() {
        val state = FloatingUiStateNormalizer.add(FloatingAddState(1, .25f, 2f, true))
        assertEquals(.25f, state.ratioX)
        assertEquals(-1f, state.ratioY)
    }
}
