package com.example.ui.screens.habayeb.utils

/*
 * =====================================================================================
 * إعدادات وإدارة العملات المالية والتحويلات (Currency Configuration & Conversion Utility)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * ملف مركزي شامل لإدارة العملات في نظام "حبايب":
 * 1. تعريف نموذج العملة (Currency Data Class) برموزها وأسمائها وأعلامها.
 * 2. توفير قائمة العملات الافتراضية المدعومة (ريال يمني YER، ريال سعودي SAR، دولار أمريكي USD).
 * 3. تحليل وسوم العملات المضمنة في نصوص الملاحظات واستخراج التفاصيل النظيفة مع التخزين المؤقت (LRU Cache).
 * 4. إجراء عمليات التحويل المالي الدقيقة وحسابات أسعار الصرف باستخدام BigDecimal وتقريب HALF_EVEN.
 * 5. استخراج العملة والمبلغ الفعلي للمعاملة المالية مع دعم المعاملات القديمة والحديثة.
 * =====================================================================================
 */

import android.content.Context
import com.example.R
import com.example.data.local.BigDecimalConverter
import com.example.data.local.entities.HabayebTransaction
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Collections
import java.util.LinkedHashMap
import java.util.Locale

/*
 * =====================================================================================
 * نموذج العملة (Currency Data Class)
 * -------------------------------------------------------------------------------------
 * [الحقول]:
 * - code: الرمز الدولي للعملة (مثل: "YER", "SAR", "USD").
 * - symbol: الرمز المختصر أو الشائع للعملة (مثل: "ر.ي", "ر.س", "$").
 * - arabicName: الاسم الكامل للعملة باللغة العربية (مثل: "ريال يمني").
 * - flagEmoji: رمز تعبيري لعلم الدولة التابعة لها العملة.
 * =====================================================================================
 */
data class Currency(
    val code: String,
    val symbol: String,
    val arabicName: String,
    val flagEmoji: String
)

/*
 * =====================================================================================
 * كائن إدارة العملات (CurrencyConfig Object)
 * -------------------------------------------------------------------------------------
 * يوفر كافة العمليات الحسابية والتحليلية المتعلقة بالعملات وأسعار الصرف.
 * =====================================================================================
 */
object CurrencyConfig {
    
    // محول الأرقام العشرية الدقيقة
    private val converter = BigDecimalConverter()

    /*
     * تحويل النص إلى BigDecimal بأمان
     */
    fun parseBigDecimal(value: String): BigDecimal {
        return converter.fromString(value) ?: BigDecimal.ZERO
    }

    // القائمة الافتراضية الثابتة للعملات الأساسية
    private val DEFAULT_CURRENCY_DEFINITIONS = listOf(
        Currency("YER", "ر.ي", "ريال يمني", "🇾🇪"),
        Currency("SAR", "ر.س", "ريال سعودي", "🇸🇦"),
        Currency("USD", "$", "دولار أمريكي", "🇺🇸")
    )

    // قائمة العملات العامة المتاحة
    val currencies: List<Currency> = DEFAULT_CURRENCY_DEFINITIONS

    /*
     * استرجاع قائمة العملات مع تحديث النصوص من ملفات الموارد (Strings) وفق لغة الجهاز
     */
    fun getCurrencies(context: Context? = null): List<Currency> {
        if (context == null) return currencies
        val yerSym = context.getString(R.string.currency_yer)
        val yerName = context.getString(R.string.currency_name_yer)
        val sarSym = context.getString(R.string.currency_sar)
        val sarName = context.getString(R.string.currency_name_sar)
        val usdSym = context.getString(R.string.currency_usd)
        val usdName = context.getString(R.string.currency_name_usd)

        return DEFAULT_CURRENCY_DEFINITIONS.map { defaultCurr ->
            when (defaultCurr.code) {
                "YER" -> defaultCurr.copy(
                    symbol = yerSym.ifEmpty { defaultCurr.symbol },
                    arabicName = yerName.ifEmpty { defaultCurr.arabicName }
                )
                "SAR" -> defaultCurr.copy(
                    symbol = sarSym.ifEmpty { defaultCurr.symbol },
                    arabicName = sarName.ifEmpty { defaultCurr.arabicName }
                )
                "USD" -> defaultCurr.copy(
                    symbol = usdSym.ifEmpty { defaultCurr.symbol },
                    arabicName = usdName.ifEmpty { defaultCurr.arabicName }
                )
                else -> defaultCurr
            }
        }
    }

    /*
     * البحث عن عملة بواسطة رمزها المختصر أو كودها الدولي
     */
    fun getBySymbol(symbol: String): Currency? =
        currencies.find { it.symbol == symbol || it.code == symbol }

    /*
     * البحث عن عملة بواسطة الكود الدولي (ISO Code)
     */
    fun getByCode(code: String): Currency? =
        currencies.find { it.code == code }

    // الحد الأقصى لحجم ذاكرة التخزين المؤقت لتحليل وسوم العملات
    private const val MAX_CACHE_SIZE = 500

    // خريطة تخزين مؤقت متزامنة لتفادي تكرار تحليل النصوص المتطابقة
    private val parseCache: MutableMap<String, Pair<String, String>> = Collections.synchronizedMap(
        object : LinkedHashMap<String, Pair<String, String>>(MAX_CACHE_SIZE, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String, String>>?): Boolean {
                return size > MAX_CACHE_SIZE
            }
        }
    )

    /**
     * تنظيف تفاصيل ووصف المعاملة بإزالة وسوم العملات المخفية مثل [ر.س] أو [SAR]
     * وإرجاع النص الأصلي الصافي بدون المسافات الزائدة.
     */
    fun getCleanDetails(description: String): String {
        if (description.isBlank()) return ""
        var clean = description.trim()
        for (currency in currencies) {
            val tagSym = "[${currency.symbol}]"
            val tagCode = "[${currency.code}]"
            if (clean.startsWith(tagSym)) {
                clean = clean.substring(tagSym.length).trim()
            }
            if (clean.startsWith(tagCode)) {
                clean = clean.substring(tagCode.length).trim()
            }
        }
        if (clean.startsWith("[") && clean.contains("]")) {
            val closingIdx = clean.indexOf("]")
            if (closingIdx in 1..10) {
                clean = clean.substring(closingIdx + 1).trim()
            }
        }
        return clean
    }

    /**
     * استخراج رمز العملة والوصف النظيف من حقل وصف المعاملة.
     * في حال عدم وجود وسم عملة، يتم إرجاع رمز العملة الافتراضية الممرر.
     */
    fun parseTransactionCurrency(description: String, defaultCurrencySymbol: String): Pair<String, String> {
        val cacheKey = "$description::$defaultCurrencySymbol"
        val cached = parseCache[cacheKey]
        if (cached != null) return cached

        // البحث عن نمط [الرمز] في بداية الوصف
        for (currency in currencies) {
            val tag = "[${currency.symbol}]"
            if (description.startsWith(tag)) {
                val cleanDesc = description.substring(tag.length).trim()
                val res = Pair(currency.symbol, cleanDesc)
                parseCache[cacheKey] = res
                return res
            }
        }
        val res = Pair(defaultCurrencySymbol, description)
        parseCache[cacheKey] = res
        return res
    }

    /**
     * تحديد رتبة وقوة العملة مقارنة بالعملات الأخرى لتحديد اتجاه الضرب أو القسمة في الصرف.
     */
    fun getCurrencyRank(symbol: String): Int {
        val sym = symbol.uppercase(Locale.ENGLISH).trim()
        return when {
            sym == "ر.ي" || sym == "YER" || sym.contains("يمن") -> 1
            sym == "ر.س" || sym == "SAR" || sym.contains("سعود") -> 2
            sym == "$" || sym == "USD" || sym.contains("دولار") -> 3
            else -> 2 // القيمة الافتراضية لمتوسط القوة
        }
    }

    // دالة لاستخراج المبلغ الأصلي الفعلي للمعاملة بالعملة التي سجلت بها
    fun getOriginalAmount(tx: HabayebTransaction): BigDecimal {
        return tx.foreignAmount
    }
    
    // دالة التحويل الآمنة بين العملات بناءً على أسعار الصرف الحالية
    fun convert(amount: BigDecimal, rate: BigDecimal, toWeaker: Boolean): BigDecimal {
        if (rate <= BigDecimal.ZERO) return amount.setScale(4, RoundingMode.HALF_EVEN)
        return if (toWeaker) {
            amount.multiply(rate, MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_EVEN)
        } else {
            amount.divide(rate, 4, RoundingMode.HALF_EVEN)
        }
    }

    /*
     * تحويل المبلغ باستخدام كائن زوج العملات (CurrencyPair)
     */
    fun convertWithCurrencyPair(
        amount: BigDecimal,
        currencyPair: com.example.domain.model.CurrencyPair
    ): BigDecimal {
        return convertAmountBigDecimal(
            amount = amount,
            baseCurrencySymbol = currencyPair.baseCurrency,
            foreignCurrencySymbol = currencyPair.targetCurrency,
            rate = currencyPair.safeRate
        )
    }

    /*
     * تحويل المبالغ المالية الدقيقة باستخدام BigDecimal مع التحقق من رتب العملات
     */
    fun convertAmountBigDecimal(
        amount: BigDecimal,
        baseCurrencySymbol: String,
        foreignCurrencySymbol: String,
        rate: BigDecimal
    ): BigDecimal {
        val baseNorm = getBySymbol(baseCurrencySymbol)?.symbol ?: baseCurrencySymbol
        val foreignNorm = getBySymbol(foreignCurrencySymbol)?.symbol ?: foreignCurrencySymbol
        if (baseNorm == foreignNorm) {
            return amount.setScale(4, RoundingMode.HALF_EVEN)
        }
        val finalRate = if (rate <= BigDecimal.ZERO) BigDecimal.ONE else rate.setScale(4, RoundingMode.HALF_EVEN)
        val baseRank = getCurrencyRank(baseNorm)
        val foreignRank = getCurrencyRank(foreignNorm)

        return if (baseRank < foreignRank) {
            amount.multiply(finalRate, MathContext.DECIMAL128).setScale(4, RoundingMode.HALF_EVEN)
        } else {
            if (finalRate.compareTo(BigDecimal.ZERO) == 0) {
                amount.setScale(4, RoundingMode.HALF_EVEN)
            } else {
                amount.divide(finalRate, 4, RoundingMode.HALF_EVEN)
            }
        }
    }

    /*
     * تحويل المبالغ المالية بنوع Double (للتوافق العام)
     */
    fun convertAmount(
        amount: Double,
        baseCurrencySymbol: String,
        foreignCurrencySymbol: String,
        rate: Double
    ): Double {
        val amountBD = BigDecimal.valueOf(amount)
        val rateBD = BigDecimal.valueOf(rate)
        return convertAmountBigDecimal(amountBD, baseCurrencySymbol, foreignCurrencySymbol, rateBD).toDouble()
    }

    /**
     * استخراج العملة الحقيقية والمبلغ الفعلي للمعاملة بصيغة BigDecimal بدقة 4 خانات عشرية،
     * مع معالجة حقول قاعدة البيانات الحديثة والوسوم القديمة وحسابات الصرف.
     */
    fun getTransactionCurrencyAndAmountBigDecimal(
        tx: HabayebTransaction,
        defaultCurrencySymbol: String,
        exchangeRatesJson: String = "{}"
    ): Pair<String, BigDecimal> {
        val parsedCurrencyInfo = parseTransactionCurrency(tx.description, defaultCurrencySymbol)
        val rawTxCurrency = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) {
            tx.currencyCode
        } else if (parsedCurrencyInfo.first != defaultCurrencySymbol) {
            parsedCurrencyInfo.first
        } else if (tx.baseCurrencyCode != "DEFAULT" && tx.baseCurrencyCode.isNotBlank()) {
            tx.baseCurrencyCode
        } else {
            defaultCurrencySymbol
        }
        val txCurrency = getBySymbol(rawTxCurrency)?.symbol ?: rawTxCurrency

        if (tx.isRateCalculated) {
            val baseCurrencyRaw = if (tx.baseCurrencyCode != "DEFAULT" && tx.baseCurrencyCode.isNotBlank()) {
                tx.baseCurrencyCode
            } else {
                defaultCurrencySymbol
            }
            val baseCurrency = getBySymbol(baseCurrencyRaw)?.symbol ?: baseCurrencyRaw
            return Pair(baseCurrency, tx.equivalentAmount.setScale(4, RoundingMode.HALF_EVEN))
        }

        val normDefaultSymbol = getBySymbol(defaultCurrencySymbol)?.symbol ?: defaultCurrencySymbol
        val actualAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount

        return if (txCurrency != normDefaultSymbol) {
            Pair(txCurrency, actualAmount.setScale(4, RoundingMode.HALF_EVEN))
        } else {
            Pair(normDefaultSymbol, tx.amount.setScale(4, RoundingMode.HALF_EVEN))
        }
    }

    /**
     * استخراج العملة والمبلغ بصيغة Double للتوافق مع شاشات العرض القديمة.
     */
    fun getTransactionCurrencyAndAmount(
        tx: HabayebTransaction,
        defaultCurrencySymbol: String,
        exchangeRatesJson: String = "{}"
    ): Pair<String, Double> {
        val (curr, bd) = getTransactionCurrencyAndAmountBigDecimal(tx, defaultCurrencySymbol, exchangeRatesJson)
        return Pair(curr, bd.toDouble())
    }

    /**
     * تغليف وصف المعاملة بوسم العملة لتخزينها في حال تطلب الأمر.
     */
    fun formatDescriptionWithCurrency(description: String, symbol: String): String {
        return "[$symbol] $description"
    }

    /**
     * توحيد وتطبيع الأرقام المكتوبة بالأرقام العربية أو الفارسية إلى الأرقام الإنجليزية القياسية.
     */
    fun normalizeDigits(input: String): String = com.example.domain.StringUtils.normalizeDigits(input)
}


