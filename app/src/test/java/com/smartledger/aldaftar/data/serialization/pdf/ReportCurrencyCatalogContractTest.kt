package com.smartledger.aldaftar.data.serialization.pdf

import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportCurrencyCatalogContractTest {
    @Test
    fun appReportCurrencyCatalogContainsOnlySupportedCurrencies() {
        assertEquals(listOf("YER", "SAR", "USD"), CurrencyConfig.currencies.map { it.code })
    }
}
