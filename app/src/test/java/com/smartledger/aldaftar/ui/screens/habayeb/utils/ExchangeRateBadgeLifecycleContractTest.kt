package com.smartledger.aldaftar.ui.screens.habayeb.utils

import androidx.compose.ui.graphics.Color
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.screens.habayeb.components.row.CustomerTransactionRowStateCalculator
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * اختبارات دورة حياة ملصق سعر الصرف (النشط / غير النشط) عبر دورة العملات الثلاث الكاملة:
 * YER -> SAR -> USD -> YER
 *
 * القواعد الحاكمة:
 * 1. المعاملة الأجنبية غير المصروفة (currency != base, isRateCalculated=false) => يظهر ملصق "سعر صرف غير نشط"
 * 2. المعاملة الأجنبية المصروفة (currency != base, isRateCalculated=true) => يظهر ملصق "سعر صرف نشط"
 * 3. المعاملة المحلية غير المرتبطة بصرف (currency == base, isRateCalculated=false) => يختفي الملصق تماماً
 * 4. المعاملة التي أصبحت عملتها مطابقة لكنها مرتبطة بصرف نشط (currency == base, isRateCalculated=true) => يبقى "سعر صرف نشط" ظاهراً لتمكين الإلغاء
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExchangeRateBadgeLifecycleContractTest {

    enum class BadgeState {
        NONE,            // مخفي بالكامل
        INACTIVE_RATE,   // سعر صرف غير نشط
        ACTIVE_RATE      // سعر صرف نشط
    }

    private fun evaluateBadgeState(tx: HabayebTransaction, currentBaseCurrency: String): BadgeState {
        val cached = CustomerTransactionRowStateCalculator.calculate(
            tx = tx,
            currencySymbol = currentBaseCurrency,
            initialType = "CREDITOR",
            debtColor = Color.Red,
            creditColor = Color.Green
        )
        val showBadge = cached.isTxForeign || cached.isCalculated
        return when {
            !showBadge -> BadgeState.NONE
            cached.isCalculated -> BadgeState.ACTIVE_RATE
            else -> BadgeState.INACTIVE_RATE
        }
    }

    private fun createTx(
        id: String,
        amount: String,
        currencyCode: String,
        baseCurrencyCode: String,
        isRateCalculated: Boolean,
        exchangeRate: String = "1",
        equivalentAmount: String? = null
    ): HabayebTransaction {
        val amountBD = BigDecimal(amount)
        val rateBD = BigDecimal(exchangeRate)
        val equivBD = equivalentAmount?.let { BigDecimal(it) } ?: amountBD
        return HabayebTransaction(
            id = id,
            customerId = "cust-1",
            amount = amountBD,
            type = TransactionType.OWED_BY_THEM.value,
            description = "Test Tx $id",
            timestamp = System.currentTimeMillis() / 1000L,
            currencyCode = currencyCode,
            baseCurrencyCode = baseCurrencyCode,
            exchangeRate = rateBD,
            isRateCalculated = isRateCalculated,
            equivalentAmount = equivBD,
            foreignAmount = amountBD
        )
    }

    @Test
    fun testCompleteThreeCurrencyCycleWithOldAndNewTransactions() {
        // --- المرحلة 1: العملة الأساسية الحالية = YER (الريال اليمني) ---
        var baseCurrency = "ر.ي"

        // المعاملات الخمس الأساسية
        val tx1_SarUncalculated = createTx("tx1", "100", "ر.س", "ر.ي", isRateCalculated = false)
        val tx2_SarCalculated = createTx("tx2", "200", "ر.س", "ر.ي", isRateCalculated = true, exchangeRate = "140", equivalentAmount = "28000")
        val tx3_UsdUncalculated = createTx("tx3", "100", "$", "ر.ي", isRateCalculated = false)
        val tx4_UsdCalculated = createTx("tx4", "300", "$", "ر.ي", isRateCalculated = true, exchangeRate = "530", equivalentAmount = "159000")
        val tx5_YerLocal = createTx("tx5", "50000", "ر.ي", "ر.ي", isRateCalculated = false)

        // التحقق في المرحلة 1 (YER)
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx1_SarUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx2_SarCalculated, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx3_UsdUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx4_UsdCalculated, baseCurrency))
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx5_YerLocal, baseCurrency))

        // --- المرحلة 2: الانتقال الأول (YER -> SAR) ---
        baseCurrency = "ر.س"

        // المعاملة 1 (100 SAR بدون صرف): أصبحت محلية وغير مرتبطة بصرف => يختفي الملصق
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx1_SarUncalculated, baseCurrency))

        // المعاملة 2 (200 SAR مصروفة سابقاً): أصبحت مطابقة للأساسية ولكنها مرتبطة بصرف سابق => يظل نشطاً!
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx2_SarCalculated, baseCurrency))

        // المعاملة 3 (100 USD بدون صرف): أجنبية => سعر صرف غير نشط
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx3_UsdUncalculated, baseCurrency))

        // المعاملة 4 (300 USD مصروفة): أجنبية => سعر صرف نشط
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx4_UsdCalculated, baseCurrency))

        // المعاملة 5 (50,000 YER): أصبحت أجنبية بالنسبة لـ SAR => سعر صرف غير نشط
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx5_YerLocal, baseCurrency))

        // إدخالات جديدة أثناء كون SAR هي الأساسية
        val tx6_SarNewLocal = createTx("tx6", "500", "ر.س", "ر.س", isRateCalculated = false)
        val tx7_YerNewUncalculated = createTx("tx7", "70000", "ر.ي", "ر.س", isRateCalculated = false)
        val tx8_UsdNewCalculated = createTx("tx8", "50", "$", "ر.س", isRateCalculated = true, exchangeRate = "3.75", equivalentAmount = "187.5")

        assertEquals(BadgeState.NONE, evaluateBadgeState(tx6_SarNewLocal, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx7_YerNewUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx8_UsdNewCalculated, baseCurrency))

        // --- المرحلة 3: الانتقال الثاني (SAR -> USD) ---
        baseCurrency = "$"

        // المعاملات بالدولار
        // tx3 (100 USD غير مصروف): أصبح محلياً بدون صرف => يختفي الملصق
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx3_UsdUncalculated, baseCurrency))
        // tx4 (300 USD مصروف سابقاً): أصبح محلياً لكنه مصروف سابقاً => يظل نشطاً!
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx4_UsdCalculated, baseCurrency))
        // tx8 (50 USD مصروف سابقاً مقابل SAR): يظل نشطاً!
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx8_UsdNewCalculated, baseCurrency))

        // المعاملات بالسعودي
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx1_SarUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx2_SarCalculated, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx6_SarNewLocal, baseCurrency))

        // المعاملات باليمني
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx5_YerLocal, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx7_YerNewUncalculated, baseCurrency))

        // إدخالات جديدة أثناء كون USD هي الأساسية
        val tx9_UsdNewLocal = createTx("tx9", "1000", "$", "$", isRateCalculated = false)
        val tx10_SarNewCalculated = createTx("tx10", "375", "ر.س", "$", isRateCalculated = true, exchangeRate = "0.266667", equivalentAmount = "100")
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx9_UsdNewLocal, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx10_SarNewCalculated, baseCurrency))

        // --- المرحلة 4: الانتقال الثالث والعودة إلى البداية (USD -> YER) ---
        baseCurrency = "ر.ي"

        // المعاملات باليمني
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx5_YerLocal, baseCurrency))
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx7_YerNewUncalculated, baseCurrency))

        // المعاملات بالسعودي
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx1_SarUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx2_SarCalculated, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx6_SarNewLocal, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx10_SarNewCalculated, baseCurrency))

        // المعاملات بالدولار
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx3_UsdUncalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx4_UsdCalculated, baseCurrency))
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx8_UsdNewCalculated, baseCurrency))
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx9_UsdNewLocal, baseCurrency))
    }

    @Test
    fun testExplicitUserDeactivationCancelsRateAndFollowsLocalRule() {
        // سيناريو الإلغاء الصريح:
        // 1. معاملة SAR مصروفة والعملة الأساسية YER => نشط
        var tx = createTx("tx-deact", "200", "ر.س", "ر.ي", isRateCalculated = true, exchangeRate = "140", equivalentAmount = "28000")
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx, "ر.ي"))

        // 2. تحويل الأساسية إلى SAR => يظل نشطاً
        assertEquals(BadgeState.ACTIVE_RATE, evaluateBadgeState(tx, "ر.س"))

        // 3. المستخدم يضغط على الملصق ويلغي الصرف صراحة:
        tx = tx.copy(isRateCalculated = false)

        // 4. بما أن الأساسية SAR والعملة SAR وألغي الصرف => يختفي الملصق فوراً
        assertEquals(BadgeState.NONE, evaluateBadgeState(tx, "ر.س"))

        // 5. إذا تم تحويل الأساسية لاحقاً إلى USD => يظهر كـ "غير نشط"
        assertEquals(BadgeState.INACTIVE_RATE, evaluateBadgeState(tx, "$"))
    }
}
