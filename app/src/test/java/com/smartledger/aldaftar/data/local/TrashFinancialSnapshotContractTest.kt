package com.smartledger.aldaftar.data.local

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.TrashJsonSerializer
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import java.math.BigDecimal
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class TrashFinancialSnapshotContractTest {
    private fun tx() = HabayebTransaction(
        id="t",customerId="c",type="OWED_BY_THEM",amount=BigDecimal("14000.0000"),
        timestamp=1_800_000_000L,description="fixed",linkedMainTxId="r",
        isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100.0000"),
        exchangeRate=BigDecimal("140.0000"),isRateCalculated=true,
        equivalentAmount=BigDecimal("14000.0000"),baseCurrencyCode="ر.ي"
    )

    @Test fun trashRoundTripPreservesEveryFinancialField() {
        val original=tx()
        val restored=TrashItemParser.parseHabayebTransaction(
            JSONObject(TrashJsonSerializer.serializeHabayebTransaction(original))
        )
        assertEquals(original,restored)
    }

    @Test fun trashSerializationDoesNotContainOnlyDisplayAmount() {
        val json=JSONObject(TrashJsonSerializer.serializeHabayebTransaction(tx()))
        listOf("amount","currency_code","foreign_amount","exchange_rate","is_rate_calculated","equivalent_amount","base_currency_code")
            .forEach { assertEquals(true,json.has(it)) }
    }

    @Test fun restoreIsAStateChangeNotARevaluation() {
        val restored=TrashItemParser.parseHabayebTransaction(
            JSONObject(TrashJsonSerializer.serializeHabayebTransaction(tx()))
        )
        assertEquals(0,BigDecimal("14000").compareTo(restored.equivalentAmount))
        assertEquals(0,BigDecimal("140").compareTo(restored.exchangeRate))
    }
}
