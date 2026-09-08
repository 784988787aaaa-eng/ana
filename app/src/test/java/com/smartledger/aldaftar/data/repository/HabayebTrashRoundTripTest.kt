package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class HabayebTrashRoundTripTest {
    @Test fun customerRoundTripPreservesRestoreCriticalFields() {
        val original = HabayebCustomer("c1", "عميل", "1", "ملاحظة", 123L, "OWED_TO_THEM", 7)
        val restored = TrashItemParser.parseHabayebCustomer(
            JSONObject(TrashJsonSerializer.serializeHabayebCustomer(original))
        )
        assertEquals(original, restored)
    }

    @Test fun transactionRoundTripPreservesRelationshipAndMoneyFields() {
        val original = HabayebTransaction(
            id = "t1", customerId = "c1", type = "OWED_BY_THEM", amount = BigDecimal("12.50"),
            timestamp = 123L, description = "وصف", linkedMainTxId = "main",
            isForeign = true, currencyCode = "USD", foreignAmount = BigDecimal("10"),
            exchangeRate = BigDecimal("1.25"), isRateCalculated = true,
            equivalentAmount = BigDecimal("12.50"), baseCurrencyCode = "YER"
        )
        val restored = TrashItemParser.parseHabayebTransaction(
            JSONObject(TrashJsonSerializer.serializeHabayebTransaction(original))
        )
        assertEquals(original, restored)
    }

    @Test fun nullLinkedMainTransactionIdSurvivesRoundTrip() {
        val original = HabayebTransaction("t1", "c1", "OWED_BY_THEM", BigDecimal.ONE, 1L, "")
        val restored = TrashItemParser.parseHabayebTransaction(
            JSONObject(TrashJsonSerializer.serializeHabayebTransaction(original))
        )
        assertNull(restored.linkedMainTxId)
    }
}
