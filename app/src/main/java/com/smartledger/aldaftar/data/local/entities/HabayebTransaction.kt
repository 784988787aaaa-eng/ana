package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import java.math.BigDecimal

@Entity(
    tableName = "habayeb_transactions",
    foreignKeys = [
        ForeignKey(
            entity = HabayebCustomer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["timestamp"]),
        Index(value = ["type"]),
        Index(value = ["currency_code"]),
        Index(value = ["customerId", "timestamp"]),
        Index(value = ["customerId", "type"]),
        Index(value = ["linkedMainTxId"])
    ]
)
data class HabayebTransaction(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "customerId") val customerId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "amount") val amount: BigDecimal,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "linkedMainTxId") val linkedMainTxId: String? = null,
    @ColumnInfo(name = "is_foreign") val isForeign: Boolean = false,
    @ColumnInfo(name = "currency_code") val currencyCode: String = FinancialPolicy.DEFAULT_CURRENCY_CODE,
    @ColumnInfo(name = "foreign_amount") val foreignAmount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "exchange_rate") val exchangeRate: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "is_rate_calculated") val isRateCalculated: Boolean = false,
    @ColumnInfo(name = "equivalent_amount") val equivalentAmount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "base_currency_code") val baseCurrencyCode: String = FinancialPolicy.DEFAULT_CURRENCY_CODE,
    @ColumnInfo(name = "snapshot_version") val snapshotVersion: Int = 1,
    @ColumnInfo(name = "rate_context") val rateContext: String = "HISTORICAL_SNAPSHOT"
) {

    val originalAmount: BigDecimal
        get() = if (foreignAmount.compareTo(BigDecimal.ZERO) > 0) foreignAmount else amount

    val originalCurrency: String
        get() {
            val code = currencyCode.trim()
            if (code.isNotBlank() && code != FinancialPolicy.DEFAULT_CURRENCY_CODE) {
                return FinancialPolicy.canonicalCurrencySymbol(code)
            }
            val base = baseCurrencyCode.trim()
            if (base.isNotBlank() && base != FinancialPolicy.DEFAULT_CURRENCY_CODE) {
                return FinancialPolicy.canonicalCurrencySymbol(base)
            }
            return FinancialPolicy.FALLBACK_CURRENCY_SYMBOL
        }

    fun isForeignUnder(defaultCurrency: String): Boolean {
        val canonicalDefault = FinancialPolicy.canonicalCurrencySymbol(defaultCurrency)
        return originalCurrency != canonicalDefault
    }

    val currencySymbol: String get() = originalCurrency

    val isExchanged: Boolean get() = isRateCalculated

    val targetCurrencySymbol: String
        get() {
            val base = baseCurrencyCode.trim()
            if (base.isNotBlank() && base != FinancialPolicy.DEFAULT_CURRENCY_CODE) {
                return FinancialPolicy.canonicalCurrencySymbol(base)
            }
            return originalCurrency
        }

    val exchangedAmount: BigDecimal get() = equivalentAmount
}

