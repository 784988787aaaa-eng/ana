package com.smartledger.aldaftar.ui.habayeb

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.CustomerCurrencyBalance
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.repository.CategoryRepository
import com.smartledger.aldaftar.data.repository.HabayebCategoryDataRepository
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebCategoryUseCase
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFilterParameters
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebTransactionUseCase
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Comprehensive verification suite for:
 * 1. Pinned Accounts lifecycle, cross-category persistence, settled-balance visibility, and deduplication.
 * 2. Smart Automatic Category Inheritance when creating a new account from inside a category,
 *    manual override, manual clearing, and general ("الكل") creation.
 */
@RunWith(RobolectricTestRunner::class)
class PinnedAccountsAndSmartCategoryCreationTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryDataRepository: HabayebCategoryDataRepository
    private lateinit var categoryUseCase: HabayebCategoryUseCase
    private lateinit var habayebRepository: HabayebRepository
    private lateinit var mutationRepository: HabayebMutationRepository
    private lateinit var transactionUseCase: HabayebTransactionUseCase

    private val defaultSettings = AppSettings(currencySymbol = "ر.ي")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryRepository = CategoryRepository(db.customCategoryDao())
        categoryDataRepository = HabayebCategoryDataRepository(db, categoryRepository)
        categoryUseCase = HabayebCategoryUseCase(categoryRepository, categoryDataRepository)
        habayebRepository = HabayebRepository(db, db.habayebDao())
        mutationRepository = HabayebMutationRepository(db)
        transactionUseCase = HabayebTransactionUseCase(habayebRepository, mutationRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun pinnedAccount_persistsAcrossAllAndCategoryViews_andUnpinsCleanlyFromAnyView() = runBlocking {
        categoryUseCase.saveCustomCategory("السرايا")
        val sarayaCategory = categoryRepository.getAllCustomCategoriesDirect().first { it.name == "السرايا" }

        val customer = HabayebCustomer(
            id = "cust_saraya_1",
            name = "عميل السرايا الأول",
            phone = "777111222",
            notes = "",
            createdAt = 100L,
            categoryId = sarayaCategory.id
        )
        db.habayebDao().insertCustomer(customer)

        // Pin while viewing "السرايا"
        val pinnedResult = categoryUseCase.togglePinCustomer(customer.id, sarayaCategory.id)
        assertTrue("Pinning customer inside category must succeed", pinnedResult)

        // Must be visible as pinned in both "السرايا" and "الكل" (null)
        val pinnedInCategory = categoryUseCase.pinnedCustomerIdsFlow(sarayaCategory.id).first()
        val pinnedInAll = categoryUseCase.pinnedCustomerIdsFlow(null).first()
        assertTrue("Customer must be pinned inside its category", customer.id in pinnedInCategory)
        assertTrue("Customer must remain pinned when switching to 'الكل'", customer.id in pinnedInAll)

        // Unpin while viewing "الكل" (null)
        val unpinnedResult = categoryUseCase.togglePinCustomer(customer.id, null)
        assertTrue("Unpinning customer from 'الكل' must succeed", unpinnedResult)

        assertFalse(
            "Customer must be unpinned in 'الكل'",
            customer.id in categoryUseCase.pinnedCustomerIdsFlow(null).first()
        )
        assertFalse(
            "Customer must be unpinned in category view",
            customer.id in categoryUseCase.pinnedCustomerIdsFlow(sarayaCategory.id).first()
        )
    }

    @Test
    fun pinnedAccount_survivesCategoryChange_andCategoryDeletionWithoutDeletingAccounts() = runBlocking {
        categoryUseCase.saveCustomCategory("السرايا")
        categoryUseCase.saveCustomCategory("التجار")
        val cats = categoryRepository.getAllCustomCategoriesDirect()
        val saraya = cats.first { it.name == "السرايا" }
        val merchants = cats.first { it.name == "التجار" }

        val customer = HabayebCustomer(
            id = "cust_1",
            name = "مؤسسة الأفق",
            phone = "770000111",
            notes = "",
            createdAt = 200L,
            categoryId = saraya.id
        )
        db.habayebDao().insertCustomer(customer)
        categoryUseCase.togglePinCustomer(customer.id, saraya.id)

        // Move pinned account to "التجار"
        categoryUseCase.assignCategoryToCustomers(listOf(customer.id), merchants.id)
        assertEquals("التجار", categoryDataRepository.customerCategoryName(customer.id))
        assertTrue(
            "Pinned status must survive moving to another category",
            customer.id in categoryUseCase.pinnedCustomerIdsFlow(merchants.id).first()
        )
        assertTrue(
            "Pinned status must remain in global 'الكل' view",
            customer.id in categoryUseCase.pinnedCustomerIdsFlow(null).first()
        )

        // Delete "التجار" category without deleting linked accounts
        categoryUseCase.deleteCustomCategoryWithChoice(merchants, deleteLinkedAccounts = false)
        assertNull(categoryDataRepository.customerCategoryName(customer.id))
        assertTrue(
            "Pinned account must remain pinned in 'الكل' even after its category is deleted",
            customer.id in categoryUseCase.pinnedCustomerIdsFlow(null).first()
        )
    }

    @Test
    fun pinnedAccount_staysVisibleAtTop_evenWhenSettledOrZeroBalance_withoutDuplicates() {
        val pinnedZeroBalance = HabayebCustomer(
            id = "c_pinned_zero",
            name = "عميل مثبت مصفى",
            phone = "771",
            notes = "",
            createdAt = 10L
        )
        val unpinnedActive1 = HabayebCustomer(
            id = "c_active_1",
            name = "عميل نشط 1",
            phone = "772",
            notes = "",
            createdAt = 50L
        )
        val unpinnedActive2 = HabayebCustomer(
            id = "c_active_2",
            name = "عميل نشط 2",
            phone = "773",
            notes = "",
            createdAt = 90L
        )

        val balances = listOf(
            CustomerCurrencyBalance("c_active_1", "ر.ي", BigDecimal("500"), BigDecimal.ZERO, 50L, 1),
            CustomerCurrencyBalance("c_active_2", "ر.ي", BigDecimal("1200"), BigDecimal.ZERO, 90L, 1)
        )

        val uiState = HabayebFinancialCalculator.calculateCustomersUiState(
            customers = listOf(pinnedZeroBalance, unpinnedActive1, unpinnedActive2),
            customerBalances = balances,
            settings = defaultSettings
        )

        val categoryMap = mapOf(
            "c_pinned_zero" to "السرايا",
            "c_active_1" to "السرايا",
            "c_active_2" to "السرايا"
        )

        // 1. In "الكل" (selectedCat = null): pinned zero-balance account must stay visible at index 0
        val allResult = HabayebFinancialCalculator.calculateFilteredResult(
            uiState = uiState,
            params = HabayebFilterParameters(
                query = "",
                tab = 0,
                finSort = 1,
                histSort = 1,
                hiddenIds = emptySet(),
                selectedCat = null,
                pinnedIds = setOf("c_pinned_zero")
            ),
            categoryMap = categoryMap
        )
        assertEquals(
            listOf("c_pinned_zero", "c_active_2", "c_active_1"),
            allResult.filteredCustomers.map { it.id }
        )
        assertEquals(
            "Merged list must never contain duplicate customer IDs",
            allResult.filteredCustomers.size,
            allResult.filteredCustomers.map { it.id }.distinct().size
        )

        // 2. Inside "السرايا" (selectedCat = "السرايا"): pinned zero-balance account must also stay at index 0
        val sarayaResult = HabayebFinancialCalculator.calculateFilteredResult(
            uiState = uiState,
            params = HabayebFilterParameters(
                query = "",
                tab = 0,
                finSort = 1,
                histSort = 1,
                hiddenIds = emptySet(),
                selectedCat = "السرايا",
                pinnedIds = setOf("c_pinned_zero")
            ),
            categoryMap = categoryMap
        )
        assertEquals(
            listOf("c_pinned_zero", "c_active_2", "c_active_1"),
            sarayaResult.filteredCustomers.map { it.id }
        )
    }

    @Test
    fun smartCategoryCreation_inheritsActiveCategory_supportsOverrideAndManualClear() = runBlocking {
        categoryUseCase.saveCustomCategory("السرايا")
        categoryUseCase.saveCustomCategory("الموردين")
        val categories = categoryRepository.getAllCustomCategoriesDirect()
        val saraya = categories.first { it.name == "السرايا" }
        val suppliers = categories.first { it.name == "الموردين" }

        fun resolveCategory(selectedCategory: String?): String? =
            selectedCategory?.trim()?.takeIf { it.isNotEmpty() && it != FinanceConstants.CATEGORY_CLOSED }

        // Case 1: User is inside "السرايا" -> default category is "السرايا"
        val inheritedFromSaraya = resolveCategory("السرايا")
        assertEquals("السرايا", inheritedFromSaraya)

        val resolvedSarayaId = categories.firstOrNull { !it.isSystemClosed && it.name == inheritedFromSaraya }?.id
        val customerInSaraya = HabayebCustomer(
            id = "cust_new_saraya",
            name = "أحمد السرايا",
            phone = "777000111",
            notes = "",
            createdAt = 1000L,
            initialType = TransactionType.OWED_BY_THEM.value,
            categoryId = resolvedSarayaId
        )
        val saved1 = transactionUseCase.saveHabayebCustomer(
            customer = customerInSaraya,
            initialAmount = BigDecimal("250"),
            initialType = TransactionType.OWED_BY_THEM.value,
            selectedCategoryFilter = inheritedFromSaraya,
            settings = defaultSettings
        )
        assertTrue(saved1)
        assertEquals(saraya.id, db.habayebDao().getCustomerByIdDirect("cust_new_saraya")?.categoryId)
        assertEquals("السرايا", categoryDataRepository.customerCategoryName("cust_new_saraya"))

        // Case 2: User is inside "السرايا", changes category in dialog to "الموردين"
        val overriddenCategory = resolveCategory("الموردين")
        val resolvedSuppliersId = categories.firstOrNull { !it.isSystemClosed && it.name == overriddenCategory }?.id
        val customerInSuppliers = HabayebCustomer(
            id = "cust_new_suppliers",
            name = "مورد جديد",
            phone = "777000222",
            notes = "",
            createdAt = 1001L,
            initialType = TransactionType.OWED_TO_THEM.value,
            categoryId = resolvedSuppliersId
        )
        transactionUseCase.saveHabayebCustomer(
            customer = customerInSuppliers,
            initialAmount = BigDecimal("400"),
            initialType = TransactionType.OWED_TO_THEM.value,
            selectedCategoryFilter = overriddenCategory,
            settings = defaultSettings
        )
        assertEquals(suppliers.id, db.habayebDao().getCustomerByIdDirect("cust_new_suppliers")?.categoryId)

        // Case 3: User is inside "السرايا", manually clears category (selectedCategory = null) -> saved with null
        val clearedCategory = resolveCategory(null)
        assertNull("Cleared category must remain null and not re-impose active filter", clearedCategory)
        val customerUncategorized = HabayebCustomer(
            id = "cust_new_uncat",
            name = "عميل عام بدون تصنيف",
            phone = "777000333",
            notes = "",
            createdAt = 1002L,
            initialType = TransactionType.OWED_BY_THEM.value,
            categoryId = null
        )
        transactionUseCase.saveHabayebCustomer(
            customer = customerUncategorized,
            initialAmount = BigDecimal("100"),
            initialType = TransactionType.OWED_BY_THEM.value,
            selectedCategoryFilter = clearedCategory,
            settings = defaultSettings
        )
        assertNull(db.habayebDao().getCustomerByIdDirect("cust_new_uncat")?.categoryId)

        // Case 4: User is inside "الكل" (null) or "المغلقة" (CATEGORY_CLOSED) -> no category forced
        assertNull(resolveCategory(null))
        assertNull(resolveCategory(FinanceConstants.CATEGORY_CLOSED))

        // Verify immediate visibility in "السرايا" filter via HabayebFinancialCalculator
        val allCustomers = db.habayebDao().getAllCustomersDirect()
        val allBalances = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        val catMap = categoryDataRepository.categoryMapFlow.first()
        val uiState = HabayebFinancialCalculator.calculateCustomersUiState(
            customers = allCustomers,
            customerBalances = allBalances,
            settings = defaultSettings
        )
        val sarayaFiltered = HabayebFinancialCalculator.calculateFilteredResult(
            uiState = uiState,
            params = HabayebFilterParameters("", 0, 0, 1, emptySet(), "السرايا", emptySet()),
            categoryMap = catMap
        )
        assertEquals(listOf("cust_new_saraya"), sarayaFiltered.filteredCustomers.map { it.id })
    }

    @Test
    fun sourceContract_addCustomerPopupAndScreenImplementCategoryInheritanceAndPinPersistence() {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        val root = if (File(cwd, "UI_REFACTOR_MASTER_PLAN.md").exists()) cwd else (cwd.parentFile ?: cwd)

        val popupSource = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/AddCustomerPopup.kt"
        ).readText()
        val screenSource = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/HabayebScreen.kt"
        ).readText()
        val vmSource = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/viewmodel/HabayebFinanceViewModel.kt"
        ).readText()

        assertTrue(
            "AddCustomerPopup must include AddCustomerCategorySelectorBar and clear button testTag",
            popupSource.contains("AddCustomerCategorySelectorBar") &&
                popupSource.contains("add_customer_clear_category_button")
        )
        assertTrue(
            "HabayebScreen must share openAddCustomerForCurrentCategory between FAB and list empty state",
            screenSource.contains("openAddCustomerForCurrentCategory") &&
                screenSource.contains("resolveInitialCategoryForNewAccount")
        )
        assertTrue(
            "HabayebFinanceViewModel must resolve and persist categoryId in saveHabayebCustomer",
            vmSource.contains("resolveInitialCategoryForNewAccount") &&
                vmSource.contains("customer.copy(categoryId = resolvedCategoryId)")
        )
    }
}
