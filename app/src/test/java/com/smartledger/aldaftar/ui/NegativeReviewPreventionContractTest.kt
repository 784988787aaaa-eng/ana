package com.smartledger.aldaftar.ui

import com.smartledger.aldaftar.data.local.BigDecimalConverter
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFilterParameters
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class NegativeReviewPreventionContractTest {

    @Test
    fun arabicAndPersianDigits_convertSeamlesslyInInputFields() {
        val mixedInput = "١٢٣.٤٥"
        val normalized = StringUtils.normalizeDigits(mixedInput)
        assertEquals("123.45", normalized)

        val parsed = CurrencyConfig.parseBigDecimalOrNull(mixedInput)
        assertNotNull(parsed)
        assertEquals(BigDecimal("123.45"), parsed)
    }

    @Test
    fun arabicCommaAsDecimalSeparator_parsedAccuratelyWithoutError() {
        val arabicCommaInput = "٥٠٠٫٧٥"
        val parsed = CurrencyConfig.parseBigDecimalOrNull(arabicCommaInput)
        assertNotNull(parsed)
        assertEquals(BigDecimal("500.75"), parsed)
    }

    @Test
    fun unexchangedForeignBalances_keepCustomerVisibleInLists_preventingFalseZeroBalanceImpression() {
        val customer = HabayebCustomer(
            id = "cust-foreign",
            name = "Foreign Customer",
            phone = "123",
            notes = "",
            createdAt = 1000L
        )

        // Local YER balance is 0, but Foreign SAR balance is 500
        val balanceYER = com.smartledger.aldaftar.data.local.CustomerCurrencyBalance(
            customerId = customer.id,
            currencyCode = "ر.ي",
            netAmount = BigDecimal.ZERO,
            netEquivalentAmount = BigDecimal.ZERO,
            lastTimestamp = 1000L,
            txCount = 1
        )
        val balanceSAR = com.smartledger.aldaftar.data.local.CustomerCurrencyBalance(
            customerId = customer.id,
            currencyCode = "ر.س",
            netAmount = BigDecimal("500.0000"),
            netEquivalentAmount = BigDecimal.ZERO,
            lastTimestamp = 1000L,
            txCount = 1
        )

        val uiState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer),
            listOf(balanceYER, balanceSAR),
            AppSettings(currencySymbol = "ر.ي")
        )

        val customerState = uiState.customers.single()

        // Local total is 0, but the customer MUST NOT be classified as closed or hidden!
        assertFalse("Customer with foreign balance must remain active and visible", customerState.isClosed)
        assertEquals("ر.س", customerState.displayCurrencySymbol)
        assertEquals(BigDecimal("500.0000"), customerState.displayNetDebt)
    }

    @Test
    fun zeroExchangeRateInput_rejectedGracefullyWithClearErrorMessage() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            FinancialPolicy.convertDirectedAmount(
                amount = BigDecimal("100"),
                sourceCurrency = "ر.س",
                targetCurrency = "ر.ي",
                rate = BigDecimal.ZERO,
                rateSourceCurrency = "ر.س",
                rateTargetCurrency = "ر.ي"
            )
        }
        assertTrue("Error message must explain the invalid exchange rate in clear Arabic",
            exception.message?.contains("غير صالح") == true || exception.message?.contains("غير موجود") == true)
    }

    @Test
    fun invalidMathExpression_returnsNull_preventingAppCrashes() {
        val invalidExpr = "12.5.0 + abc"
        val evaluated = com.smartledger.aldaftar.domain.evaluateSimpleExpression(invalidExpr)
        assertNull("Invalid math expressions must return null safely without throwing exceptions", evaluated)
    }

    @Test
    fun validMathExpression_evaluatesWithPrecision() {
        val validExpr = "١٠٠ + ٥٠ × ٢"
        val evaluated = com.smartledger.aldaftar.domain.evaluateSimpleExpression(validExpr)
        assertNotNull(evaluated)
        assertEquals(BigDecimal("200"), evaluated)
    }

    @Test
    fun smartFormatter_stripsZerosCleanlyForUserReadability() {
        assertEquals("0", HabayebMathHelper.formatSmart(BigDecimal.ZERO))
        assertEquals("100", HabayebMathHelper.formatSmart(BigDecimal("100.0000")))
        assertEquals("100.5", HabayebMathHelper.formatSmart(BigDecimal("100.5000")))
        assertEquals("100.25", HabayebMathHelper.formatSmart(BigDecimal("100.2500")))
    }

    @Test
    fun rateBadge_compactDisplay_preventsDialogMultiLineWrapping() {
        val rateUSD = BigDecimal("550.0000")
        val badge = ExchangeRateHelper.formatCompactRateBadge("{}", "$", "ر.ي", rateUSD)
        assertEquals("1 $ = 550 ر.ي", badge)
    }

    @Test
    fun searchFilter_withNoMatchingCustomers_returnsEmptyFilteredResultWithoutCrashing() {
        val customer = HabayebCustomer(
            id = "c1", name = "أحمد علي", phone = "777111222", notes = "", createdAt = 1L
        )
        val balance = com.smartledger.aldaftar.data.local.CustomerCurrencyBalance(
            customerId = customer.id,
            currencyCode = "ر.ي",
            netAmount = BigDecimal("1000.0000"),
            netEquivalentAmount = BigDecimal("1000.0000"),
            lastTimestamp = 1000L,
            txCount = 1
        )
        val uiState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), listOf(balance), AppSettings(currencySymbol = "ر.ي")
        )

        val filterParams = HabayebFilterParameters(
            query = "محمود", // Query that won't match "أحمد علي"
            tab = 0,
            finSort = 0,
            histSort = 0,
            hiddenIds = emptySet(),
            selectedCat = null,
            pinnedIds = emptySet()
        )

        val filtered = HabayebFinancialCalculator.calculateFilteredResult(uiState, filterParams, emptyMap())
        assertTrue("Filtered list must be empty when search term has no match", filtered.filteredCustomers.isEmpty())
        assertEquals("Active customer count remains tracked", 1, filtered.activeCustomersCount)
    }
}
