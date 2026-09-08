package com.smartledger.aldaftar.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HashUtilsTest {
    @Test fun `رمز PIN الصحيح يمر والتحريف يفشل`() {
        val encoded = HashUtils.createPinHash("1234")
        assertTrue(HashUtils.verifyPin("1234", encoded))
        assertFalse(HashUtils.verifyPin("1235", encoded))
    }
}
