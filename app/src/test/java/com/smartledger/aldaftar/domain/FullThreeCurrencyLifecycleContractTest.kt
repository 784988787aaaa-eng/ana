package com.smartledger.aldaftar.domain

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.RecurringRepository
import com.smartledger.aldaftar.data.repository.SettingsRepository
import com.smartledger.aldaftar.data.repository.TrashRepository
import com.smartledger.aldaftar.data.serialization.excel.AllCustomersExcelEngine
import com.smartledger.aldaftar.data.serialization.excel.SingleCustomerExcelEngine
import com.smartledger.aldaftar.data.serialization.pdf.PdfReportCalculator
import com.smartledger.aldaftar.data.serialization.pdf.PdfTransactionRowRenderer
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.notifications.TransactionNotificationBuilder
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebTransactionUseCase
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import com.smartledger.aldaftar.ui.components.CurrencyDialogState
import com.smartledger.aldaftar.ui.components.CurrencySettingsState
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.components.row.CustomerTransactionRowStateCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateBadgeLifecycleContractTest.BadgeState
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashStrings
import java.io.File
import java.math.BigDecimal
import java.util.Calendar
import java.util.zip.ZipFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * عقد الاختبار التراكمي الشامل لدورة العملات الثلاث (YER / SAR / USD):
 * يضمن الدقة الحسابية الكاملة بـ BigDecimal عبر جميع طبقات التطبيق:
 * UI State -> UseCase -> Room DAO -> Calculators -> PDF -> Excel -> Backup -> Trash -> Notifications.
 */
@RunWith(RobolectricTestRunner::class)
class FullThreeCurrencyLifecycleContractTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var habayebRepo: HabayebRepository
    private lateinit var mutationRepo: HabayebMutationRepository
    private lateinit var trashRepo: TrashRepository
    private lateinit var recurringRepo: RecurringRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var txUseCase: HabayebTransactionUseCase
    private lateinit var backupEngine: BackupEngine

    private val trashStrings = TrashStrings(
        systemHabayeb = "الحبايب",
        unknownText = "غير معروف",
        noPhoneText = "لا يوجد هاتف",
        noNotesText = "بدون ملاحظات",
        debtTxText = "معاملة",
        owedByThemText = "عليه",
        paymentByThemText = "سدد",
        owedToThemText = "له",
        paymentToThemText = "تسديد له",
        categoryLabelText = "الفئة: %s",
        customerLabelText = "تابع لحساب: %s",
        equivalentInfoTemplate = "ما يعادل %s (بسعر %s)",
        progressTextTemplate = "%s",
        customerBundleDescTemplate = "عدد المعاملات: %d",
        ledgerBundleDescTemplate = "عدد السجلات: %d",
        exchangeRateLabelText = "سعر الصرف"
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habayebRepo = HabayebRepository(db, db.habayebDao())
        mutationRepo = HabayebMutationRepository(db)
        trashRepo = TrashRepository(db.trashDao())
        recurringRepo = RecurringRepository(db, db.recurringConfigDao())
        settingsRepo = SettingsRepository(db.settingsDao())
        txUseCase = HabayebTransactionUseCase(habayebRepo, mutationRepo)
        backupEngine = BackupEngine(context, db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun evaluateBadge(tx: HabayebTransaction, currentDefault: String): BadgeState {
        val row = CustomerTransactionRowStateCalculator.calculate(
            tx = tx,
            currencySymbol = currentDefault,
            initialType = TransactionType.OWED_BY_THEM.value,
            debtColor = Color.Red,
            creditColor = Color.Green
        )
        return when {
            !row.isTxForeign && !row.isCalculated -> BadgeState.NONE
            row.isCalculated -> BadgeState.ACTIVE_RATE
            else -> BadgeState.INACTIVE_RATE
        }
    }

    @Test
    fun scenario1_largeVolumeBigDecimalPrecisionZeroCrossingsAndSymmetricFlip() = runBlocking {
        val customer = HabayebCustomer(
            id = "cust-precision",
            name = "عميل الدقة القصوى",
            phone = "777000111",
            notes = "",
            createdAt = 1_000L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        habayebRepo.insertCustomer(customer)

        // أرقام عالية مع هللات دقيقة لاختبار انعدام أخطاء الفاصلة العائمة
        habayebRepo.insertHabayebTransaction(
            HabayebTransaction(
                id = "big-1",
                customerId = customer.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("10000000.3400"),
                timestamp = 1_001L,
                description = "مبلغ ضخم بهللات",
                isForeign = false,
                currencyCode = "ر.ي",
                baseCurrencyCode = "ر.ي"
            )
        )
        habayebRepo.insertHabayebTransaction(
            HabayebTransaction(
                id = "big-2",
                customerId = customer.id,
                type = TransactionType.PAYMENT_BY_THEM.value,
                amount = BigDecimal("10000000.0000"),
                timestamp = 1_002L,
                description = "سداد المبلغ الصحيح",
                isForeign = false,
                currencyCode = "ر.ي",
                baseCurrencyCode = "ر.ي"
            )
        )
        // بعد المعاملتين الباقي = 0.3400 بالضبط
        // إضافة 300 معاملة تعبر الصفر ذهاباً وإياباً بهللات (0.01 و 0.0025)
        for (i in 1..300) {
            val type = if (i % 2 == 1) TransactionType.OWED_BY_THEM.value else TransactionType.PAYMENT_BY_THEM.value
            val amt = if (i <= 200) BigDecimal("0.0100") else BigDecimal("0.0025")
            habayebRepo.insertHabayebTransaction(
                HabayebTransaction(
                    id = "loop-$i",
                    customerId = customer.id,
                    type = type,
                    amount = amt,
                    timestamp = 1_003L + i,
                    description = "قيد $i",
                    isForeign = false,
                    currencyCode = "ر.ي",
                    baseCurrencyCode = "ر.ي"
                )
            )
        }
        // الأثر التراكمي للحلقة: 100 قيد فردي + 100 قيد زوجي بـ 0.0100 (= 0)، و50 قيد فردي + 50 قيد زوجي بـ 0.0025 (= 0).
        // معاملة إضافية بقيمة 0.0025 تُسدد بعد 4 معاملات في العملات الأجنبية
        habayebRepo.insertHabayebTransaction(
            HabayebTransaction(
                id = "halala-yer",
                customerId = customer.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("0.0025"),
                timestamp = 2_000L,
                description = "كسر عشري دقيق",
                isForeign = false,
                currencyCode = "ر.ي",
                baseCurrencyCode = "ر.ي"
            )
        )
        habayebRepo.insertHabayebTransaction(
            HabayebTransaction(
                id = "sar-small",
                customerId = customer.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("12.3450"),
                timestamp = 2_001L,
                description = "سعودي غير مصروف",
                isForeign = true,
                currencyCode = "ر.س",
                foreignAmount = BigDecimal("12.3450"),
                baseCurrencyCode = "ر.ي"
            )
        )
        habayebRepo.insertHabayebTransaction(
            HabayebTransaction(
                id = "sar-cross-pay",
                customerId = customer.id,
                type = TransactionType.PAYMENT_BY_THEM.value,
                amount = BigDecimal("20.0000"),
                timestamp = 2_002L,
                description = "سداد يعبر الصفر للسعودي",
                isForeign = true,
                currencyCode = "ر.س",
                foreignAmount = BigDecimal("20.0000"),
                baseCurrencyCode = "ر.ي"
            )
        )

        val expectedYer = BigDecimal("0.3425")
        val expectedSar = BigDecimal("-7.6550")

        val daoBalances = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        val mainUiState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), daoBalances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()
        val txsBeforeFlip = habayebRepo.getTransactionsForCustomerDirect(customer.id)
        val detailResult = CustomerHistoryCalculator.calculate(txsBeforeFlip, "ر.ي", "{}")
        val pdfSummary = PdfReportCalculator.calculateSingleCustomerReport(txsBeforeFlip, "ر.ي", "{}")

        MoneyAssertions.exact("0.3425", mainUiState.defaultCurrencyTotal)
        MoneyAssertions.exact("0.3425", detailResult.netDebtMap.getValue("ر.ي"))
        MoneyAssertions.exact("0.3425", pdfSummary.calculatedNetDebt)
        MoneyAssertions.exact("-7.6550", mainUiState.foreignDebts.getValue("ر.س"))
        MoneyAssertions.exact("-7.6550", detailResult.netDebtMap.getValue("ر.س"))
        MoneyAssertions.exact("-7.6550", pdfSummary.uncalculatedForeignSums.getValue("ر.س"))

        // عكس نوع العميل (من له/عليه إلى العكس) ثم إرجاعه
        habayebRepo.updateCustomer(customer.copy(initialType = TransactionType.OWED_TO_THEM.value))
        val balancesFlipped = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        val flippedUi = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer.copy(initialType = TransactionType.OWED_TO_THEM.value)),
            balancesFlipped,
            AppSettings(currencySymbol = "ر.ي")
        ).customers.single()
        MoneyAssertions.exact(expectedYer.negate().setScale(4).toPlainString(), flippedUi.defaultCurrencyTotal)
        MoneyAssertions.exact(expectedSar.negate().setScale(4).toPlainString(), flippedUi.foreignDebts.getValue("ر.س"))

        // إعادة النوع الأصلي يجب أن تعيد الرصيد كما كان حرفياً دون أي تشوه
        habayebRepo.updateCustomer(customer)
        val balancesRestored = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        val restoredUi = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balancesRestored, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()
        val restoredDetail = CustomerHistoryCalculator.calculate(
            habayebRepo.getTransactionsForCustomerDirect(customer.id), "ر.ي", "{}"
        )
        MoneyAssertions.exact("0.3425", restoredUi.defaultCurrencyTotal)
        MoneyAssertions.exact("0.3425", restoredDetail.netDebtMap.getValue("ر.ي"))
        MoneyAssertions.exact("-7.6550", restoredUi.foreignDebts.getValue("ر.س"))
    }

    @Test
    fun scenario2_fullThreeCurrencyCycle_yerToSarToUsdToYer_keepsHistoricalTruthIntact() = runBlocking {
        val customer = HabayebCustomer("c-cycle", "دورة العملات", "", "", 1L, TransactionType.OWED_BY_THEM.value)
        habayebRepo.insertCustomer(customer)

        // المرحلة 1: الافتراضية = YER
        val yerSettings = AppSettings(currencySymbol = "ر.ي", exchangeRatesJson = "{}")
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("13950"), desc = "old sar->yer", timestamp = 10L,
            editingTxId = "tx-old-exchanged", isForeign = true, currencyCode = "ر.س",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("139.5"),
            isRateCalculated = true, equivalentAmount = BigDecimal("13950"),
            baseCurrencySymbol = "ر.ي"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("50"), desc = "old usd unexchanged", timestamp = 11L,
            editingTxId = "tx-old-unexchanged", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("50"), exchangeRate = BigDecimal.ZERO,
            isRateCalculated = false, equivalentAmount = BigDecimal.ZERO,
            baseCurrencySymbol = "ر.ي"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("25000"), desc = "old yer local", timestamp = 12L,
            editingTxId = "tx-old-local", isForeign = false, currencyCode = "ر.ي",
            baseCurrencySymbol = "ر.ي"
        )

        var ui = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first(), yerSettings
        ).customers.single()
        MoneyAssertions.exact("38950.0000", ui.defaultCurrencyTotal)
        MoneyAssertions.exact("50.0000", ui.foreignDebts.getValue("$"))

        // المرحلة 2: التبديل إلى SAR دون إعادة تقييم التاريخ
        val sarSettings = AppSettings(currencySymbol = "ر.س")
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("375"), desc = "new usd->sar", timestamp = 20L,
            editingTxId = "tx-sar-new", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("3.75"),
            isRateCalculated = true, equivalentAmount = BigDecimal("375"),
            baseCurrencySymbol = "ر.س"
        )

        val oldExAfterSar = habayebRepo.getHabayebTransactionById("tx-old-exchanged")!!
        assertEquals("ر.ي", oldExAfterSar.baseCurrencyCode)
        MoneyAssertions.exact("13950.0000", oldExAfterSar.equivalentAmount)

        ui = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), db.habayebDao().getAllCustomerBalancesFlow("ر.س").first(), sarSettings
        ).customers.single()
        MoneyAssertions.exact("375.0000", ui.defaultCurrencyTotal)
        MoneyAssertions.exact("38950.0000", ui.foreignDebts.getValue("ر.ي"))
        MoneyAssertions.exact("50.0000", ui.foreignDebts.getValue("$"))

        // المرحلة 3: التبديل إلى USD دون إعادة تقييم التاريخ
        val usdSettings = AppSettings(currencySymbol = "$")
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("26.6667"), desc = "new sar->usd", timestamp = 30L,
            editingTxId = "tx-usd-new", isForeign = true, currencyCode = "ر.س",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("0.266666666667"),
            isRateCalculated = true, equivalentAmount = BigDecimal("26.6667"),
            baseCurrencySymbol = "$"
        )

        ui = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), db.habayebDao().getAllCustomerBalancesFlow("$").first(), usdSettings
        ).customers.single()
        // 50 USD (أصبح غير مصروف بعملة الافتراضي) + 26.6667 USD (مصروف إلى USD) = 76.6667 USD
        MoneyAssertions.exact("76.6667", ui.defaultCurrencyTotal)
        MoneyAssertions.exact("375.0000", ui.foreignDebts.getValue("ر.س"))
        MoneyAssertions.exact("38950.0000", ui.foreignDebts.getValue("ر.ي"))

        // المرحلة 4: العودة إلى YER — نفس التجميع المستقل لكل عملة أساس تاريخية
        ui = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first(), yerSettings
        ).customers.single()
        MoneyAssertions.exact("38950.0000", ui.defaultCurrencyTotal)
        MoneyAssertions.exact("375.0000", ui.foreignDebts.getValue("ر.س"))
        MoneyAssertions.exact("76.6667", ui.foreignDebts.getValue("$"))
    }

    @Test
    fun scenario3_activateDeactivateAfterDefaultSwitch_invertedRatesAndMultiTabSave() = runBlocking {
        val customer = HabayebCustomer("c-toggle", "تفعيل وتعطيل", "", "", 1L, TransactionType.OWED_BY_THEM.value)
        habayebRepo.insertCustomer(customer)

        // أُنشئت في YER: واحدة غير مصروفة بالريال السعودي، والأخرى محلية باليمني
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100"), desc = "unexchanged sar", timestamp = 10L,
            editingTxId = "tx-sar-unex", isForeign = true, currencyCode = "ر.س",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal.ZERO,
            isRateCalculated = false, equivalentAmount = BigDecimal.ZERO,
            baseCurrencySymbol = "ر.ي"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("14000"), desc = "local yer", timestamp = 11L,
            editingTxId = "tx-yer-local", isForeign = false, currencyCode = "ر.ي",
            baseCurrencySymbol = "ر.ي"
        )

        // التبديل إلى USD ثم تفعيل الصرف على كلا المعاملتين القديمتين
        txUseCase.updateTransactionExchangeRate("tx-sar-unex", BigDecimal("0.266666666667"), true, "$")
        txUseCase.updateTransactionExchangeRate("tx-yer-local", BigDecimal("0.001818181818"), true, "$")

        val sarActivated = habayebRepo.getHabayebTransactionById("tx-sar-unex")!!
        val yerActivated = habayebRepo.getHabayebTransactionById("tx-yer-local")!!

        assertEquals("$", sarActivated.baseCurrencyCode)
        assertEquals("ر.س", sarActivated.currencyCode)
        assertTrue(sarActivated.isRateCalculated)
        MoneyAssertions.exact("26.6667", sarActivated.equivalentAmount)
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadge(sarActivated, "$"))

        assertEquals("$", yerActivated.baseCurrencyCode)
        assertEquals("ر.ي", yerActivated.currencyCode)
        assertTrue(yerActivated.isRateCalculated)
        assertTrue(yerActivated.isForeign)
        MoneyAssertions.exact("25.4545", yerActivated.equivalentAmount)
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadge(yerActivated, "$"))

        // تعطيل الصرف على المعاملة السعودية عند كون الافتراضية هي SAR => يصبح NONE وتختفي الشارة
        txUseCase.updateTransactionExchangeRate("tx-sar-unex", BigDecimal.ZERO, false, "ر.س")
        val sarDeactivated = habayebRepo.getHabayebTransactionById("tx-sar-unex")!!
        assertFalse(sarDeactivated.isRateCalculated)
        assertFalse(sarDeactivated.isForeign)
        assertEquals("ر.س", sarDeactivated.baseCurrencyCode)
        MoneyAssertions.exact("100.0000", sarDeactivated.amount)
        assertEquals(BadgeState.NONE, evaluateBadge(sarDeactivated, "ر.س"))

        // تعطيل الصرف على المعاملة اليمنية عند كون الافتراضية USD => تعود غير نشطة INACTIVE_RATE
        txUseCase.updateTransactionExchangeRate("tx-yer-local", BigDecimal.ZERO, false, "$")
        val yerDeactivated = habayebRepo.getHabayebTransactionById("tx-yer-local")!!
        assertFalse(yerDeactivated.isRateCalculated)
        assertTrue(yerDeactivated.isForeign)
        assertEquals("$", yerDeactivated.baseCurrencyCode)
        MoneyAssertions.exact("14000.0000", yerDeactivated.amount)
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadge(yerDeactivated, "$"))

        // اختبار تقليب اتجاه المعادلة دون قلب الرقم العشري < 1 بواسطة formatActiveRateBadge + حفظ عدة تبويبات معاً
        var initialJson = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140"))
        initialJson = ExchangeRateHelper.setRate(initialJson, "$", "ر.ي", BigDecimal("550"))

        val settingsState = CurrencySettingsState(
            initialSettings = AppSettings(currencySymbol = "ر.ي", exchangeRatesJson = initialJson),
            currencyYer = "ر.ي",
            currencySar = "ر.س",
            currencyUsd = "$"
        )

        settingsState.toggleEquationDirection()
        assertEquals("ر.ي" to "ر.س", settingsState.displayPair)
        assertEquals("0.007142857143", settingsState.rateInputStr)
        // التحقق من أن الحقل لا يقلب الرقم < 1 إلى 140
        assertFalse(settingsState.rateInputStr == "140")

        // العودة للاتجاه القياسي وتعديل SAR و USD في نفس الجلسة قبل الحفظ
        settingsState.toggleEquationDirection()
        settingsState.onRateInputChange("١٤٥")
        settingsState.onTargetCurrencyChange("$")
        settingsState.onRateInputChange("560")

        var savedFlag = false
        settingsState.handleSave(
            settings = AppSettings(currencySymbol = "ر.ي", exchangeRatesJson = initialJson),
            onSaveSettings = { _, _, _, _ -> savedFlag = true },
            onDismiss = {}
        )

        assertFalse(savedFlag)
        val revalueState = settingsState.activeDialogState as? CurrencyDialogState.RevalueConfirm
        assertNotNull(revalueState)
        val pairsMap = revalueState!!.modifiedPairs.toMap()
        MoneyAssertions.numeric("145", pairsMap.getValue("ر.س"))
        MoneyAssertions.numeric("560", pairsMap.getValue("$"))

        var persistedSettings: AppSettings? = null
        var batchPairsReceived: List<Pair<String, BigDecimal>> = emptyList()
        settingsState.handleConfirmHistoricalAndFuture(
            settings = AppSettings(currencySymbol = "ر.ي", exchangeRatesJson = initialJson),
            targetCurrency = revalueState.targetCurrency,
            newRate = revalueState.newRate,
            onSaveSettings = { updated, _, _, _ -> persistedSettings = updated },
            onDismiss = {},
            modifiedPairs = revalueState.modifiedPairs,
            onRevalueAllPairs = { batchPairsReceived = it }
        )

        assertNotNull(persistedSettings)
        assertEquals(2, batchPairsReceived.size)
        MoneyAssertions.numeric("145", ExchangeRateHelper.getRate(persistedSettings!!.exchangeRatesJson, "ر.س", "ر.ي"))
        MoneyAssertions.numeric("560", ExchangeRateHelper.getRate(persistedSettings!!.exchangeRatesJson, "$", "ر.ي"))
    }

    @Test
    fun scenario4_modifyUsdToSarRate_futureOnlyVsHistoricalRevaluationAndRecurringFreeze() = runBlocking {
        val customer = HabayebCustomer("c-reval", "إعادة التقييم", "", "", 1L, TransactionType.OWED_BY_THEM.value)
        habayebRepo.insertCustomer(customer)

        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("375"), desc = "usd to sar", timestamp = 100L,
            editingTxId = "tx-usd-sar", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("3.75"),
            isRateCalculated = true, equivalentAmount = BigDecimal("375"),
            baseCurrencySymbol = "ر.س"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("55000"), desc = "usd to yer", timestamp = 101L,
            editingTxId = "tx-usd-yer", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("550"),
            isRateCalculated = true, equivalentAmount = BigDecimal("55000"),
            baseCurrencySymbol = "ر.ي"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100"), desc = "usd unexchanged", timestamp = 102L,
            editingTxId = "tx-usd-unex", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal.ZERO,
            isRateCalculated = false, equivalentAmount = BigDecimal.ZERO,
            baseCurrencySymbol = "ر.س"
        )

        val dayStart = Calendar.getInstance().apply {
            clear()
            set(2026, 2, 1, 9, 0, 0)
        }.timeInMillis
        val recurring = RecurringConfig(
            id = "rec-1",
            originalTxId = "tx-usd-sar",
            customerId = customer.id,
            customerName = customer.name,
            amount = BigDecimal("375.0000"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "قسط شهري مثبت",
            frequency = "DAILY",
            daysOfWeek = emptyList(),
            daysOfMonth = emptyList(),
            timeHour = 9,
            timeMinute = 0,
            startDateMillis = dayStart,
            endDateMillis = dayStart + 86_400_000L,
            lastExecutedTimestamp = 0L,
            isActive = true,
            isForeign = true,
            currencyCode = "$",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("3.75"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("375.0000"),
            baseCurrencyCode = "ر.س"
        )
        recurringRepo.save(recurring)

        // أولاً: "للمعاملات القادمة فقط" — يحفظ السعر الجديد دون المساس بالتاريخ
        val oldSarJson = ExchangeRateHelper.setRate("{}", "$", "ر.س", BigDecimal("3.75"))
        val state = CurrencySettingsState(
            initialSettings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = oldSarJson),
            currencyYer = "ر.ي",
            currencySar = "ر.س",
            currencyUsd = "$"
        )
        state.onTargetCurrencyChange("$")
        state.onRateInputChange("3.80")
        var futureSavedRate = BigDecimal.ZERO
        var revalueTriggered = false
        state.handleConfirmFutureOnly(
            settings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = oldSarJson),
            targetCurrency = "$",
            newRate = BigDecimal("3.80"),
            onSaveSettings = { updated, _, _, applyHistorical ->
                futureSavedRate = ExchangeRateHelper.getRate(updated.exchangeRatesJson, "$", "ر.س")
                revalueTriggered = applyHistorical
            },
            onDismiss = {}
        )
        assertFalse(revalueTriggered)
        MoneyAssertions.numeric("3.80", futureSavedRate)
        MoneyAssertions.exact("375.0000", habayebRepo.getHabayebTransactionById("tx-usd-sar")!!.equivalentAmount)

        // ثانياً: "للسابقة والقادمة" — يعيد تقييم USD->SAR فقط ولا يمس USD->YER أو غير المصروف أو قالب التكرار
        txUseCase.revalueHistoricalTransactionsBatch("ر.س", listOf("$" to BigDecimal("3.80")))

        val revaluedSar = habayebRepo.getHabayebTransactionById("tx-usd-sar")!!
        val untouchedYer = habayebRepo.getHabayebTransactionById("tx-usd-yer")!!
        val untouchedUnex = habayebRepo.getHabayebTransactionById("tx-usd-unex")!!

        MoneyAssertions.numeric("3.80", revaluedSar.exchangeRate)
        MoneyAssertions.exact("380.0000", revaluedSar.equivalentAmount)
        MoneyAssertions.exact("380.0000", revaluedSar.amount)
        assertEquals("ر.س", revaluedSar.baseCurrencyCode)

        MoneyAssertions.numeric("550", untouchedYer.exchangeRate)
        MoneyAssertions.exact("55000.0000", untouchedYer.equivalentAmount)
        assertEquals("ر.ي", untouchedYer.baseCurrencyCode)

        assertFalse(untouchedUnex.isRateCalculated)
        MoneyAssertions.exact("100.0000", untouchedUnex.amount)

        val storedRecurring = recurringRepo.byOriginalTransaction("tx-usd-sar")!!
        MoneyAssertions.numeric("3.75", storedRecurring.exchangeRate)
        MoneyAssertions.exact("375.0000", storedRecurring.equivalentAmount)

        val executedCount = recurringRepo.executeDue(dayStart + 1_000L)
        assertEquals(1, executedCount)
        val generatedTx = habayebRepo.getTransactionsForCustomerDirect(customer.id)
            .first { it.linkedMainTxId == "tx-usd-sar" }
        MoneyAssertions.numeric("3.75", generatedTx.exchangeRate)
        MoneyAssertions.exact("375.0000", generatedTx.equivalentAmount)
        assertEquals("ر.س", generatedTx.baseCurrencyCode)
    }

    @Test
    fun scenario5_crossLayerParity_roomUiPdfExcelBackupTrashAndNotifications() = runBlocking {
        val customer = HabayebCustomer(
            id = "c-parity",
            name = "عميل التطابق الكامل",
            phone = "771122334",
            notes = "تجربة متعددة الطبقات",
            createdAt = 5_000L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        habayebRepo.insertCustomer(customer)

        val ratesJson = ExchangeRateHelper.setRate(
            ExchangeRateHelper.setRate("{}", "$", "ر.س", BigDecimal("3.75")),
            "$", "ر.ي", BigDecimal("550")
        )
        val activeSettings = AppSettings(
            currencySymbol = "ر.س",
            exchangeRatesJson = ratesJson,
            isFirstLaunch = false,
            onboardingShown = true
        )
        settingsRepo.saveSettings(activeSettings)

        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("200.50"), desc = "محلي سعودي", timestamp = 5_001L,
            editingTxId = "p-local-sar", isForeign = false, currencyCode = "SAR",
            baseCurrencySymbol = "SAR"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("375.00"), desc = "دولار مصروف للسعودي", timestamp = 5_002L,
            editingTxId = "p-ex-sar", isForeign = true, currencyCode = "USD",
            foreignAmount = BigDecimal("100"), exchangeRate = BigDecimal("3.75"),
            isRateCalculated = true, equivalentAmount = BigDecimal("375.00"),
            baseCurrencySymbol = "SAR"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("27500.00"), desc = "دولار مصروف تاريخياً لليمني", timestamp = 5_003L,
            editingTxId = "p-hist-yer", isForeign = true, currencyCode = "$",
            foreignAmount = BigDecimal("50"), exchangeRate = BigDecimal("550"),
            isRateCalculated = true, equivalentAmount = BigDecimal("27500.00"),
            baseCurrencySymbol = "ر.ي"
        )
        txUseCase.addHabayebTransaction(
            customerId = customer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("10000"), desc = "يمني غير مصروف", timestamp = 5_004L,
            editingTxId = "p-unex-yer", isForeign = true, currencyCode = "YER",
            foreignAmount = BigDecimal("10000"), exchangeRate = BigDecimal.ZERO,
            isRateCalculated = false, equivalentAmount = BigDecimal.ZERO,
            baseCurrencySymbol = "ر.س"
        )

        // 1) القائمة الرئيسية (Room DAO + HabayebFinancialCalculator)
        val allTxs = habayebRepo.getTransactionsForCustomerDirect(customer.id)
        val daoBalances = db.habayebDao().getAllCustomerBalancesFlow("ر.س").first()
        val customersUiState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), daoBalances, activeSettings
        )
        val customerUi = customersUiState.customers.single()
        MoneyAssertions.exact("575.5000", customerUi.defaultCurrencyTotal)
        MoneyAssertions.exact("37500.0000", customerUi.foreignDebts.getValue("ر.ي"))
        assertFalse(customerUi.foreignDebts.containsKey("$"))

        // 2) شاشة التفاصيل (CustomerHistoryCalculator + CustomerTransactionRowStateCalculator)
        val details = CustomerHistoryCalculator.calculate(allTxs, "ر.س", ratesJson)
        MoneyAssertions.exact("575.5000", details.netDebtMap.getValue("ر.س"))
        MoneyAssertions.exact("37500.0000", details.netDebtMap.getValue("ر.ي"))
        val histYerTx = allTxs.first { it.id == "p-hist-yer" }
        val histYerRow = CustomerTransactionRowStateCalculator.calculate(
            tx = histYerTx,
            currencySymbol = "ر.س",
            initialType = customer.initialType,
            debtColor = Color.Red,
            creditColor = Color.Green,
            runningBalance = details.runningBalances[histYerTx.id]
        )
        assertEquals("(27,500 ر.ي)", histYerRow.equivalentAmountText)

        // 3) PDF (PdfReportCalculator + PdfTransactionRowRenderer)
        val pdfSummary = PdfReportCalculator.calculateSingleCustomerReport(allTxs, "ر.س", ratesJson)
        MoneyAssertions.exact("575.5000", pdfSummary.calculatedNetDebt)
        MoneyAssertions.exact("37500.0000", pdfSummary.uncalculatedForeignSums.getValue("ر.ي"))
        val histProcessed = pdfSummary.sortedProcessedTxs.first { it.tx.id == "p-hist-yer" }
        val pdfDesc = PdfTransactionRowRenderer.buildTransactionDescriptionText(context, histProcessed, customer.initialType)
        assertTrue("PDF description must specify target base currency: $pdfDesc", pdfDesc.contains("= 27,500 ر.ي"))

        // 4) Excel (SingleCustomerExcelEngine & AllCustomersExcelEngine)
        val singleXlsx = SingleCustomerExcelEngine.generate(
            context, customer, allTxs, BusinessProfile(name = "متجر الأمل"), "ر.س", ratesJson
        )!!
        val allXlsx = AllCustomersExcelEngine.generate(
            context, customersUiState.customers, BusinessProfile(name = "متجر الأمل"), "ر.س"
        )!!
        try {
            ZipFile(singleXlsx).use { zip ->
                val sheetXml = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
                // التحويل التاريخي إلى YER يظهر بالوصف مع عملته الأساسية YER ولا يملأ عمود المعادل الخاص بـ SAR
                assertTrue(sheetXml.contains("50 USD × 550 = 27,500 YER"))
                // الصف رقم 8 (p-local-sar)، الصف 9 (p-ex-sar -> يمتلك صيغة G9)، الصف 10 (p-hist-yer -> G10 فارغ بدون <f>)
                assertTrue(sheetXml.contains("<c r=\"G9\""))
                assertFalse(sheetXml.contains("<c r=\"G10\" s=\"4\"><f>"))
            }
            ZipFile(allXlsx).use { zip ->
                val foreignSheetXml = zip.getInputStream(zip.getEntry("xl/worksheets/sheet3.xml")).bufferedReader().readText()
                // عمود العملة B يستخدم رمز ISO المطابق لمعادلة SUMIFS(..., "YER")
                assertTrue(foreignSheetXml.contains("<c r=\"B5\" s=\"6\" t=\"inlineStr\"><is><t>YER</t></is></c>"))
                assertTrue(foreignSheetXml.contains("SUMIFS(C5:C5,B5:B5,&quot;YER&quot;)"))
            }
        } finally {
            singleXlsx.delete()
            allXlsx.delete()
        }

        // 5) Notifications — الكسر العشري < 1 لا ينقلب إلى 140
        val smallRateTx = HabayebTransaction(
            id = "notif-frac",
            customerId = customer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("7.1429"),
            timestamp = 6_000L,
            description = "تحويل يمني إلى سعودي",
            isForeign = true,
            currencyCode = "ر.ي",
            foreignAmount = BigDecimal("1000"),
            exchangeRate = BigDecimal("0.007142857143"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("7.1429"),
            baseCurrencyCode = "ر.س"
        )
        val whatsAppMsg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = smallRateTx,
            customer = customer,
            currencySymbol = "ر.س"
        )
        assertTrue("Expected fractional rate 0.007142857143 in:\n$whatsAppMsg", whatsAppMsg.contains("0.007142857143"))
        assertFalse("Must not invert fractional rate to 140:\n$whatsAppMsg", whatsAppMsg.contains("= 140 "))

        // 6) BackupEngine — نسخ احتياطي + حذف شامل + استعادة تطابق الأرقام 100%
        val backupFile = File(context.cacheDir, "contract_backup_${System.currentTimeMillis()}.slb")
        try {
            backupEngine.create(backupFile)
            habayebRepo.clearAllCustomers()
            assertTrue(habayebRepo.getAllCustomersDirect().isEmpty())

            val restoredSettings = backupEngine.restore(backupFile)
            assertEquals("ر.س", restoredSettings.currencySymbol)
            val restoredTxs = habayebRepo.getTransactionsForCustomerDirect(customer.id)
            assertEquals(4, restoredTxs.size)
            val restoredBalances = db.habayebDao().getAllCustomerBalancesFlow("ر.س").first()
            val restoredUi = HabayebFinancialCalculator.calculateCustomersUiState(
                habayebRepo.getAllCustomersDirect(), restoredBalances, restoredSettings
            ).customers.single()
            MoneyAssertions.exact("575.5000", restoredUi.defaultCurrencyTotal)
            MoneyAssertions.exact("37500.0000", restoredUi.foreignDebts.getValue("ر.ي"))
        } finally {
            backupFile.delete()
        }

        // 7) Trash — حذف معاملة + فك تحليلها في سلة المهملات + استعادتها + حذف حزمة العميل واستعادتها
        mutationRepo.deleteTransactionToTrash("p-ex-sar", saveToTrash = true)
        val deletedTxItem = trashRepo.getAllDeletedItemsDirect().single()
        val parsedDeletedTx = TrashItemParser.parse(
            item = deletedTxItem,
            customersList = habayebRepo.getAllCustomersDirect(),
            currencySymbol = "ر.س",
            strings = trashStrings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Green,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )
        MoneyAssertions.exact("375.0000", parsedDeletedTx.currencyBreakdown.getValue("ر.س").abs())
        trashRepo.restoreDeletedItem(deletedTxItem)
        assertEquals(4, habayebRepo.getTransactionsForCustomerDirect(customer.id).size)

        mutationRepo.deleteCustomerToTrash(customer.id)
        val bundleItem = trashRepo.getAllDeletedItemsDirect().single()
        val parsedBundle = TrashItemParser.parse(
            item = bundleItem,
            customersList = emptyList(),
            currencySymbol = "ر.س",
            strings = trashStrings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Green,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )
        MoneyAssertions.exact("-575.5000", parsedBundle.currencyBreakdown.getValue("ر.س"))
        MoneyAssertions.exact("-37500.0000", parsedBundle.currencyBreakdown.getValue("ر.ي"))
        trashRepo.restoreDeletedItem(bundleItem)

        val finalBalances = db.habayebDao().getAllCustomerBalancesFlow("ر.س").first()
        val finalUi = HabayebFinancialCalculator.calculateCustomersUiState(
            habayebRepo.getAllCustomersDirect(), finalBalances, activeSettings
        ).customers.single()
        MoneyAssertions.exact("575.5000", finalUi.defaultCurrencyTotal)
        MoneyAssertions.exact("37500.0000", finalUi.foreignDebts.getValue("ر.ي"))
    }
}
