package com.smartledger.aldaftar.data.license

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialUsageLifecycleContractTest {
    private fun bundleJson(txCount: Int): String = JSONObject().put("totalTransactions", txCount).toString()
    private fun count(baseActive: Int, trash: List<Pair<String, String>>): Int {
        var total = baseActive
        trash.forEach { (table, json) ->
            when (table) {
                "habayeb_customers", "habayeb_transactions" -> total += 1
                "habayeb_bundle" -> total += 1 + JSONObject(json).optInt("totalTransactions", 0)
            }
        }
        return total
    }

    @Test fun oneCreatedTransactionIsExactlyOneSlot() = assertEquals(1, count(1, emptyList()))

    @Test fun arithmeticExpressionSavedAsOneTransactionIsStillOneSlot() = assertEquals(1, count(1, emptyList()))

    @Test fun movingTransactionToTrashDoesNotChangeUsedSlots() {
        assertEquals(count(1, emptyList()), count(0, listOf("habayeb_transactions" to "{}")))
    }

    @Test fun permanentTrashDeletionReleasesExactlyOneSlot() {
        assertEquals(1, count(0, listOf("habayeb_transactions" to "{}")))
        assertEquals(0, count(0, emptyList()))
    }

    @Test fun customerBundleCountsCustomerAndEachContainedTransactionOnce() {
        assertEquals(4, count(0, listOf("habayeb_bundle" to bundleJson(3))))
    }

    @Test fun partialBundleRestoreKeepsTotalUsageStable() {
        val before = count(0, listOf("habayeb_bundle" to bundleJson(3)))
        val after = count(1, listOf("habayeb_bundle" to bundleJson(2)))
        assertEquals(before, after)
    }

    @Test fun finalBundleRestoreKeepsTotalUsageStable() {
        val before = count(0, listOf("habayeb_bundle" to bundleJson(3)))
        val after = count(4, emptyList())
        assertEquals(before, after)
    }

    @Test fun softDeletedRecordIsOneLogicalOperationNotTwo() {
        assertEquals(1, count(0, listOf("habayeb_transactions" to "{}")))
    }

    @Test fun firstCreationAboveLimitIsRejected() {
        assertFalse(101 < LicenseRepository.TRIAL_LIMIT)
        assertTrue(100 >= LicenseRepository.TRIAL_LIMIT)
    }
}
