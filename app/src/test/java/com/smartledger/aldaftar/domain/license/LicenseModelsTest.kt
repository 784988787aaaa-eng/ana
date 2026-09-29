package com.smartledger.aldaftar.domain.license

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LicenseModelsTest {
    @Test
    fun trialAllowsCreationBeforeLimit() {
        assertTrue(LicenseSnapshot(status = LicenseStatus.TRIAL, trialUsed = 99).canCreate)
        assertFalse(LicenseSnapshot(status = LicenseStatus.TRIAL, trialUsed = 100).canCreate)
    }

    @Test
    fun paidLifetimeLicenseAllowsCreation() {
        val lifetime = LicenseSnapshot(
            type = LicenseType.ACCOUNT,
            plan = LicensePlan.LIFETIME,
            status = LicenseStatus.ACTIVE,
            trialUsed = 100
        )
        assertTrue(lifetime.isPaid)
        assertTrue(lifetime.isLifetime)
        assertFalse(lifetime.isTrialPlan)
        assertTrue(lifetime.canCreate)
        assertNull(lifetime.calculateRemainingDays())
    }

    @Test
    fun activeTrialLicenseCalculatesRemainingDays() {
        val future = System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000
        val trial = LicenseSnapshot(
            type = LicenseType.ACCOUNT,
            plan = LicensePlan.TRIAL,
            status = LicenseStatus.ACTIVE,
            trialEndsAt = future
        )
        assertTrue(trial.isPaid)
        assertTrue(trial.isTrialActive)
        assertEquals(5, trial.calculateRemainingDays())
    }

    @Test
    fun expiredTrialLicenseBlocksCreation() {
        val past = System.currentTimeMillis() - 1000L
        val trialExpired = LicenseSnapshot(
            type = LicenseType.ACCOUNT,
            plan = LicensePlan.TRIAL,
            status = LicenseStatus.TRIAL_EXPIRED,
            trialEndsAt = past
        )
        assertFalse(trialExpired.isPaid)
        assertTrue(trialExpired.isTrialExpired)
        assertFalse(trialExpired.canCreate)
    }

    @Test
    fun deviceReplacedRevocation() {
        val revoked = LicenseSnapshot(
            type = LicenseType.ACCOUNT,
            status = LicenseStatus.REVOKED,
            revocationReason = RevocationReason.DEVICE_REPLACED,
            revocationMessage = "تم تفعيل حساب SmartLedger على جهاز آخر، وتم إلغاء تفعيل هذا الجهاز."
        )
        assertFalse(revoked.isPaid)
        assertTrue(revoked.isDeviceReplaced)
        assertFalse(revoked.canCreate)
    }
}
