package com.smartledger.aldaftar.data.license

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialGateSourceContractTest {
    private fun source(path: String): String = File("src/main/java/$path").readText()

    @Test fun transactionCreationMustUseTheAtomicLicenseGate() {
        val src = source("com/smartledger/aldaftar/data/repository/DomainRepositories.kt")
        assertTrue(src.contains("licenseRepository.runAuthorizedCreation(::getHabayebTransactionsCountDirect, 1)"))
    }

    @Test fun recurringCreationMustUseTheSameGate() {
        val src = source("com/smartledger/aldaftar/data/repository/RecurringRepository.kt")
        assertTrue(src.contains("runAuthorizedCreation"))
        assertTrue(src.contains("currentOperationsCount"))
    }

    @Test fun gateMustCheckLiveCountBeforeMutationAndEmitLicenseEvent() {
        val src = source("com/smartledger/aldaftar/data/license/LicenseRepository.kt")
        assertTrue(src.contains("val used = currentUsed()"))
        assertTrue(src.contains("if (used + slots > TRIAL_LIMIT)"))
        assertTrue(src.contains("_onLicenseRequired.tryEmit(Unit)"))
    }
}
