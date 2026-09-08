package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HabayebTrashSerializationTest {
    @Test fun bundleKeepsStableCategoryAndPinScopes() {
        val customer = HabayebCustomer("c1", "عميل", "", "", 1L, categoryId = 7)
        val json = JSONObject(TrashJsonSerializer.serializeHabayebBundle(customer, emptyList(), "اسم", setOf(0, 7)))
        val data = json.getJSONObject("customer")
        assertEquals(7, data.getInt("categoryId"))
        assertEquals(2, data.getJSONArray("pinnedScopeCategoryIds").length())
    }

    @Test fun bundleKeepsLinkedTransactionId() {
        val customer = HabayebCustomer("c1", "عميل", "", "", 1L)
        val tx = HabayebTransaction("t1", "c1", "OWED_BY_THEM", BigDecimal.ONE, 1L, "", "main")
        val json = JSONObject(TrashJsonSerializer.serializeHabayebBundle(customer, listOf(tx), null, emptySet()))
        assertEquals("main", json.getJSONArray("transactions").getJSONObject(0).getString("linkedMainTxId"))
    }

    @Test fun globalScopeIsExplicitAndStable() {
        assertEquals(0, HabayebCategoryDataRepository.scopeCategoryId(null))
        assertEquals(9, HabayebCategoryDataRepository.scopeCategoryId(9))
        assertTrue(HabayebCategoryDataRepository.MAX_PINNED_COUNT > 0)
    }
    @Test fun customerSerializationPreservesRestoreCriticalFields() {
        val customer = HabayebCustomer(
            "c1", "عميل", "", "", 1L, initialType = "OWED_TO_THEM", categoryId = 7
        )
        val json = JSONObject(TrashJsonSerializer.serializeHabayebCustomer(customer))
        assertEquals("OWED_TO_THEM", json.getString("initialType"))
        assertEquals(7, json.getInt("categoryId"))
    }

    @Test fun bundlePreservesCustomerInitialTypeForRestore() {
        val customer = HabayebCustomer("c1", "عميل", "", "", 1L, initialType = "OWED_TO_THEM")
        val json = JSONObject(TrashJsonSerializer.serializeHabayebBundle(customer, emptyList(), null, emptySet()))
        assertEquals("OWED_TO_THEM", json.getJSONObject("customer").getString("initialType"))
    }

}
