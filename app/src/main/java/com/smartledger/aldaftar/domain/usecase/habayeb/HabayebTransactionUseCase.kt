package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.domain.model.CurrencyPair
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import java.math.BigDecimal
import java.util.UUID

/** قواعد معاملات الحبايب دون تأثيرات واجهة. */
class HabayebTransactionUseCase(
    private val habayeb: HabayebRepository,
    private val mutations: HabayebMutationRepository
) {
    private fun id() = "dtx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"

    suspend fun saveHabayebCustomer(
        customer: HabayebCustomer,
        initialAmount: BigDecimal,
        initialType: String,
        customTimestamp: Long = System.currentTimeMillis() / 1000,
        initialDetails: String = "",
        isForeign: Boolean = false,
        currencyCode: String = "DEFAULT",
        foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ZERO,
        isRateCalculated: Boolean = false,
        equivalentAmount: BigDecimal = BigDecimal.ZERO,
        selectedCategoryFilter: String?,
        settings: AppSettings
    ): Boolean {
        val canonicalTxCurrency = if (currencyCode.isNotBlank() && currencyCode != "DEFAULT") {
            FinancialPolicy.canonicalCurrencySymbol(currencyCode)
        } else {
            FinancialPolicy.canonicalCurrencySymbol(settings.currencySymbol)
        }
        val canonicalBaseCurrency = FinancialPolicy.canonicalCurrencySymbol(settings.currencySymbol)
        if (isRateCalculated) {
            FinancialPolicy.validateTransactionSnapshot(
                canonicalTxCurrency,
                canonicalBaseCurrency,
                isRateCalculated,
                exchangeRate,
                initialAmount,
                foreignAmount,
                equivalentAmount
            )
        }
        val opening = initialAmount.compareTo(BigDecimal.ZERO).takeIf { it > 0 }?.let {
            HabayebTransaction(
                id = id(),
                customerId = customer.id,
                type = initialType,
                amount = initialAmount,
                timestamp = customTimestamp,
                description = initialDetails.ifEmpty { customer.notes },
                isForeign = isForeign,
                currencyCode = canonicalTxCurrency,
                foreignAmount = foreignAmount,
                exchangeRate = exchangeRate,
                isRateCalculated = isRateCalculated,
                equivalentAmount = equivalentAmount,
                baseCurrencyCode = canonicalBaseCurrency
            )
        }
        return habayeb.insertCustomerWithOpeningTransaction(customer, opening)
    }

    suspend fun addHabayebTransaction(
        customerId: String,
        type: String,
        amount: BigDecimal,
        desc: String,
        timestamp: Long = System.currentTimeMillis() / 1000,
        editingTxId: String? = null,
        linkedMainTxId: String? = null,
        isForeign: Boolean = false,
        currencyCode: String = "DEFAULT",
        foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ZERO,
        isRateCalculated: Boolean = false,
        equivalentAmount: BigDecimal = BigDecimal.ZERO,
        baseCurrencySymbol: String
    ): Boolean {
        val canonicalTxCurrency = if (currencyCode.isNotBlank() && currencyCode != "DEFAULT") {
            FinancialPolicy.canonicalCurrencySymbol(currencyCode)
        } else {
            FinancialPolicy.canonicalCurrencySymbol(baseCurrencySymbol)
        }
        val canonicalBaseCurrency = FinancialPolicy.canonicalCurrencySymbol(baseCurrencySymbol)
        FinancialPolicy.validateTransactionSnapshot(
            canonicalTxCurrency,
            canonicalBaseCurrency,
            isRateCalculated,
            exchangeRate,
            amount,
            foreignAmount,
            equivalentAmount
        )
        val txId = editingTxId ?: id()
        val link = (linkedMainTxId ?: editingTxId?.let { habayeb.getHabayebTransactionById(it)?.linkedMainTxId })
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != "0" && it != "null" && it != txId }
        val tx = HabayebTransaction(
            id = txId,
            customerId = customerId,
            type = type,
            amount = amount,
            timestamp = timestamp,
            description = desc,
            linkedMainTxId = link,
            isForeign = isForeign,
            currencyCode = canonicalTxCurrency,
            foreignAmount = foreignAmount,
            exchangeRate = exchangeRate,
            isRateCalculated = isRateCalculated,
            equivalentAmount = equivalentAmount,
            baseCurrencyCode = canonicalBaseCurrency
        )
        return if (editingTxId != null) {
            habayeb.updateHabayebTransaction(tx)
            true
        } else {
            habayeb.insertHabayebTransaction(tx)
        }
    }

    suspend fun updateTransactionExchangeRate(
        txId: String,
        newRate: BigDecimal,
        calculateRate: Boolean,
        defaultCurrency: String
    ) {
        val tx = habayeb.getHabayebTransactionById(txId) ?: return
        val canonicalDefault = FinancialPolicy.canonicalCurrencySymbol(defaultCurrency)
        val rawTarget = tx.currencyCode.takeIf { it.isNotBlank() && it != FinancialPolicy.DEFAULT_CURRENCY_CODE }
            ?: tx.baseCurrencyCode.takeIf { it.isNotBlank() && it != FinancialPolicy.DEFAULT_CURRENCY_CODE }
            ?: canonicalDefault
        val target = FinancialPolicy.canonicalCurrencySymbol(rawTarget)
        val base = if (calculateRate && !tx.isRateCalculated) {
            canonicalDefault
        } else {
            val rawBase = tx.baseCurrencyCode.takeIf { it.isNotBlank() && it != FinancialPolicy.DEFAULT_CURRENCY_CODE }
                ?: canonicalDefault
            FinancialPolicy.canonicalCurrencySymbol(rawBase)
        }
        val effectiveBase = if (calculateRate) base else canonicalDefault
        val pair = CurrencyPair(target, effectiveBase, newRate)
        val source = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
        val enabled = calculateRate && !pair.isSelfPair
        val equivalent = if (enabled) {
            FinancialPolicy.convertDirectedAmount(
                amount = source,
                sourceCurrency = target,
                targetCurrency = effectiveBase,
                rate = pair.safeRate,
                rateSourceCurrency = target,
                rateTargetCurrency = effectiveBase
            )
        } else {
            BigDecimal.ZERO
        }
        val effectiveIsForeign = target != canonicalDefault || enabled
        habayeb.updateHabayebTransaction(
            tx.copy(
                currencyCode = target,
                baseCurrencyCode = effectiveBase,
                isForeign = effectiveIsForeign,
                exchangeRate = if (enabled) pair.safeRate else BigDecimal.ZERO,
                isRateCalculated = enabled,
                equivalentAmount = equivalent,
                amount = if (enabled) equivalent else source,
                foreignAmount = source
            )
        )
    }

    suspend fun revalueHistoricalTransactions(
        baseCurrencyCode: String,
        targetCurrencyCode: String,
        newRate: BigDecimal
    ) {
        require(newRate > BigDecimal.ZERO) { "سعر الصرف غير موجود أو غير صالح" }
        habayeb.revalueHistoricalTransactions(baseCurrencyCode, targetCurrencyCode, newRate)
    }

    suspend fun revalueHistoricalTransactionsBatch(
        baseCurrencyCode: String,
        pairs: List<Pair<String, BigDecimal>>
    ) {
        val validPairs = pairs.filter { it.second > BigDecimal.ZERO }
        require(validPairs.isNotEmpty()) { "سعر الصرف غير موجود أو غير صالح" }
        habayeb.revalueHistoricalTransactionsBatch(baseCurrencyCode, validPairs)
    }

    suspend fun updateCustomerName(id: String, name: String) = habayeb.updateCustomerName(id, name)

    suspend fun updateCustomer(customer: HabayebCustomer) = habayeb.updateCustomer(customer)

    suspend fun deleteCustomer(id: String) = mutations.deleteCustomerToTrash(id)

    suspend fun deleteMultipleCustomers(ids: List<String>) {
        ids.forEach { deleteCustomer(it) }
    }

    suspend fun deleteTransaction(id: String, isEdit: Boolean = false) =
        mutations.deleteTransactionToTrash(id, !isEdit)

    suspend fun deleteMultipleTransactions(ids: List<String>) {
        ids.forEach { deleteTransaction(it) }
    }
}
