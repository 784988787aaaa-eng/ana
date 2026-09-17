package com.smartledger.aldaftar.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HashUtilsTest {
    @Test fun `رمز PIN الصحيح يمر والتحريف يفشل`() {
        val encoded = HashUtils.createPinHash("1234")
        assertTrue(HashUtils.verifyPin("1234", encoded))
        assertFalse(HashUtils.verifyPin("1235", encoded))
    }
}
