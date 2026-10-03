package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.communication.AutoCommunicationCoordinator
import com.smartledger.aldaftar.domain.communication.CommunicationChannelType
import com.smartledger.aldaftar.domain.communication.CustomerPhoneHelper
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.notifications.BusinessNamePrefixer
import com.smartledger.aldaftar.domain.notifications.TransactionNotificationBuilder
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFilterParameters
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.ui.state.CustomersUiState
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class SearchAndCommunicationAutomationTest {

    private fun createCustomerUi(
        id: String,
        name: String,
        phone: String = "",
        notes: String = "",
        balance: BigDecimal = BigDecimal.ZERO
    ): CustomerUiState {
        val original = HabayebCustomer(
            id = id,
            name = name,
            phone = phone,
            notes = notes,
            createdAt = 1700000000L,
            initialType = if (balance.compareTo(BigDecimal.ZERO) >= 0) "OWED_BY_THEM" else "OWED_TO_THEM"
        )
        return CustomerUiState(
            id = id,
            name = name,
            phone = phone,
            notes = notes,
            displayNetDebt = balance,
            defaultCurrencyTotal = balance,
            defaultCurrencyTotalAbs = balance.abs(),
            originalCustomer = original,
            normalizedName = StringUtils.normalizeArabic(name)
        )
    }

    // =========================================================================
    // SECTION 1: SEARCH AUDIT & ARABIC NORMALIZATION TESTS
    // =========================================================================

    @Test
    fun testSearch_arabicNormalizationMatchesVariations() {
        val customers = listOf(
            createCustomerUi("1", "أحمد محمد", balance = BigDecimal("1000")),
            createCustomerUi("2", "احمد علي", balance = BigDecimal("2000")),
            createCustomerUi("3", "صيدلية الأمل", balance = BigDecimal("500")),
            createCustomerUi("4", "صيدليه السلام", balance = BigDecimal("700")),
            createCustomerUi("5", "عبدالله الحكيمي", balance = BigDecimal("1500")),
            createCustomerUi("6", "عبد الله طاهر", balance = BigDecimal("300"))
        )
        val uiState = CustomersUiState(customers = customers, isInitialized = true)

        // 1. Search with Hamza "أحمد" should match both "أحمد" and "احمد"
        val res1 = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "أحمد"),
            emptyMap()
        )
        assertTrue(res1.filteredCustomers.any { it.id == "1" })
        assertTrue(res1.filteredCustomers.any { it.id == "2" })

        // 2. Search without Hamza "احمد" should match both
        val res2 = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "احمد"),
            emptyMap()
        )
        assertTrue(res2.filteredCustomers.any { it.id == "1" })
        assertTrue(res2.filteredCustomers.any { it.id == "2" })

        // 3. Search Ta'a Marbuta vs Ha'a "صيدلية" vs "صيدليه"
        val res3 = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "صيدلية"),
            emptyMap()
        )
        assertEquals(2, res3.filteredCustomers.size)

        val res4 = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "صيدليه"),
            emptyMap()
        )
        assertEquals(2, res4.filteredCustomers.size)

        // 4. Search with and without space "عبدالله" vs "عبد الله"
        val res5 = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "عبدالله"),
            emptyMap()
        )
        assertTrue(res5.filteredCustomers.any { it.id == "5" })
        assertTrue(res5.filteredCustomers.any { it.id == "6" })
    }

    @Test
    fun testSearch_numericQueryAndPhoneMatching() {
        val customers = listOf(
            createCustomerUi("1", "محمود", phone = "771234567", balance = BigDecimal("10000")),
            createCustomerUi("2", "خالد", phone = "739876543", balance = BigDecimal("500")),
            createCustomerUi("3", "سامي", phone = "711112233", balance = BigDecimal("100"))
        )
        val uiState = CustomersUiState(customers = customers, isInitialized = true)

        // 1. Search by amount "10000"
        val resAmount = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "10000"),
            emptyMap()
        )
        assertEquals(1, resAmount.filteredCustomers.size)
        assertEquals("1", resAmount.filteredCustomers.first().id)

        // 2. Search by phone substring "9876"
        val resPhone = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "9876"),
            emptyMap()
        )
        assertEquals(1, resPhone.filteredCustomers.size)
        assertEquals("2", resPhone.filteredCustomers.first().id)

        // 3. Search with Arabic Eastern Digits "١٠٠"
        val resEastern = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "١٠٠"),
            emptyMap()
        )
        assertTrue(resEastern.filteredCustomers.any { it.id == "3" })
    }

    @Test
    fun testSearch_emptyQueryReturnsAllAccordingToCurrentFilters() {
        val customers = listOf(
            createCustomerUi("1", "عميل 1", balance = BigDecimal("1000")),
            createCustomerUi("2", "عميل 2", balance = BigDecimal("-500")),
            createCustomerUi("3", "عميل مغلق", balance = BigDecimal.ZERO)
        )
        val uiState = CustomersUiState(customers = customers, isInitialized = true)

        // Empty query with Tab 0 (All active)
        val resAll = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = ""),
            emptyMap()
        )
        assertEquals(2, resAll.filteredCustomers.size) // Only active accounts

        // Empty query with Tab 1 (Debtors only)
        val resDebtors = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(query = "", tab = 1),
            emptyMap()
        )
        assertEquals(1, resDebtors.filteredCustomers.size)
        assertEquals("1", resDebtors.filteredCustomers.first().id)
    }

    @Test
    fun testSearch_combinedWithFiltersCategoriesAndSort() {
        val customers = listOf(
            createCustomerUi("1", "محمد أحمد", balance = BigDecimal("1000")),
            createCustomerUi("2", "محمد علي", balance = BigDecimal("5000")),
            createCustomerUi("3", "علي حسن", balance = BigDecimal("3000"))
        )
        val categoryMap = mapOf("1" to "موردين", "2" to "موردين", "3" to "عملاء")
        val uiState = CustomersUiState(customers = customers, isInitialized = true)

        // Search "محمد" + Category "موردين" + Sort Descending (finSort = 1)
        val res = HabayebFinancialCalculator.calculateFilteredResult(
            uiState,
            HabayebFilterParameters(
                query = "محمد",
                selectedCat = "موردين",
                finSort = 1
            ),
            categoryMap
        )

        assertEquals(2, res.filteredCustomers.size)
        assertEquals("2", res.filteredCustomers[0].id) // 5000 first
        assertEquals("1", res.filteredCustomers[1].id) // 1000 second
    }

    // =========================================================================
    // SECTION 2: BUSINESS PROFILE NAME PREFIX IN NOTIFICATIONS TESTS
    // =========================================================================

    @Test
    fun testBusinessNamePrefixer_withValidName_addsPrefix() {
        val originalMessage = "🔴 *دين عليكم*\n💰 10,000 ر.ي\n📝 سلفه\n◀ *الإجمالي عليكم:* 10,000 ر.ي"
        val businessName = "صيدلية الأقصى للأدوية"

        val prefixed = BusinessNamePrefixer.prefix(originalMessage, businessName)

        val expected = "صيدلية الأقصى للأدوية:\n🔴 *دين عليكم*\n💰 10,000 ر.ي\n📝 سلفه\n◀ *الإجمالي عليكم:* 10,000 ر.ي"
        assertEquals(expected, prefixed)
    }

    @Test
    fun testBusinessNamePrefixer_withBlankOrNull_returnsExactOriginalMessage() {
        val originalMessage = "🟢 *سداد لكم*\n💰 5,000 ر.ي"

        assertEquals(originalMessage, BusinessNamePrefixer.prefix(originalMessage, null))
        assertEquals(originalMessage, BusinessNamePrefixer.prefix(originalMessage, ""))
        assertEquals(originalMessage, BusinessNamePrefixer.prefix(originalMessage, "   "))
    }

    @Test
    fun testTransactionNotificationBuilder_whatsappNotification_respectsBusinessName() {
        val tx = HabayebTransaction(
            id = "tx_1",
            customerId = "cust_1",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("10000"),
            timestamp = 1700000000L,
            description = "سلفه"
        )
        val customer = HabayebCustomer(
            id = "cust_1",
            name = "محمد صالح",
            phone = "771234567",
            notes = "",
            createdAt = 1700000000L,
            initialType = "OWED_BY_THEM"
        )

        // Without business name
        val msgWithoutName = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = customer,
            businessName = null
        )
        assertFalse(msgWithoutName.startsWith("صيدلية الأقصى"))
        assertTrue(msgWithoutName.contains("🔴 *دين عليكم*"))

        // With business name
        val msgWithName = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = customer,
            businessName = "صيدلية الأقصى"
        )
        assertTrue(msgWithName.startsWith("صيدلية الأقصى:\n🔴 *دين عليكم*"))
    }

    // =========================================================================
    // SECTION 3: AUTOMATED INSTANT COMMUNICATION & LIFECYCLE TESTS
    // =========================================================================

    @Test
    fun testCustomerPhoneHelper_validationsAndNormalizations() {
        // Validations
        assertTrue(CustomerPhoneHelper.isValidDestination("771234567"))
        assertTrue(CustomerPhoneHelper.isValidDestination("+967771234567"))
        assertTrue(CustomerPhoneHelper.isValidDestination("00967771234567"))
        assertTrue(CustomerPhoneHelper.isValidDestination("0501234567"))
        assertFalse(CustomerPhoneHelper.isValidDestination(""))
        assertFalse(CustomerPhoneHelper.isValidDestination("123"))
        assertFalse(CustomerPhoneHelper.isValidDestination(null))

        // WhatsApp normalization
        assertEquals("967771234567", CustomerPhoneHelper.normalizeForWhatsApp("771234567"))
        assertEquals("967771234567", CustomerPhoneHelper.normalizeForWhatsApp("+967771234567"))
        assertEquals("967771234567", CustomerPhoneHelper.normalizeForWhatsApp("00967 771 234 567"))

        // SMS normalization
        assertEquals("+967771234567", CustomerPhoneHelper.normalizeForSms("771234567"))
        assertEquals("+967771234567", CustomerPhoneHelper.normalizeForSms("+967771234567"))
    }

    @Test
    fun testAutoCommunicationCoordinator_sequentialDispatchAndDeduplication() {
        val coordinator = AutoCommunicationCoordinator()
        coordinator.clearForTests()

        val channels = listOf(
            CommunicationChannelType.WhatsApp("رسالة واتساب"),
            CommunicationChannelType.SMS("رسالة SMS")
        )

        // 1. Enqueue
        coordinator.enqueue(
            transactionId = "tx_100",
            customerId = "cust_1",
            customerPhone = "771234567",
            channels = channels
        )

        val req1 = coordinator.pendingRequest.value
        assertNotNull(req1)
        assertEquals("tx_100", req1?.transactionId)
        assertTrue(req1?.currentChannel is CommunicationChannelType.WhatsApp)
        assertFalse(req1?.isLaunched == true)

        // 2. Mark launched
        coordinator.markCurrentChannelLaunched()
        assertTrue(coordinator.pendingRequest.value?.isLaunched == true)

        // 3. User returns from WhatsApp -> Activity ON_RESUME
        coordinator.onActivityResumed()

        // 4. Now current channel is SMS and isLaunched reset to false
        val req2 = coordinator.pendingRequest.value
        assertNotNull(req2)
        assertTrue(req2?.currentChannel is CommunicationChannelType.SMS)
        assertFalse(req2?.isLaunched == true)

        // 5. Mark launched for SMS
        coordinator.markCurrentChannelLaunched()
        assertTrue(coordinator.pendingRequest.value?.isLaunched == true)

        // 6. User returns from SMS -> Activity ON_RESUME
        coordinator.onActivityResumed()

        // 7. Queue finished, pending request is null
        assertNull(coordinator.pendingRequest.value)

        // 8. Deduplication check: Attempting to enqueue the same tx_100 again does nothing
        coordinator.enqueue(
            transactionId = "tx_100",
            customerId = "cust_1",
            customerPhone = "771234567",
            channels = channels
        )
        assertNull(coordinator.pendingRequest.value)
    }
}
