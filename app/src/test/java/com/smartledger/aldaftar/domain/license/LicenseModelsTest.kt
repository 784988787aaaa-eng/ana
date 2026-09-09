package com.smartledger.aldaftar.domain.license

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseModelsTest {
    @Test fun trialAllowsCreationBeforeLimit() {
        assertTrue(LicenseSnapshot(status = LicenseStatus.TRIAL, trialUsed = 99).canCreate)
        assertFalse(LicenseSnapshot(status = LicenseStatus.TRIAL, trialUsed = 100).canCreate)
    }

    @Test fun paidLicenseAllowsCreation() {
        assertTrue(LicenseSnapshot(type = LicenseType.LOCAL, status = LicenseStatus.ACTIVE, trialUsed = 100).canCreate)
    }
}
