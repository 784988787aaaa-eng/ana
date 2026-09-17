package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.domain.model.CurrencyPair
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.math.BigDecimal
import java.util.UUID

/** قواعد معاملات الحبايب دون تأثيرات واجهة. */
class HabayebTransactionUseCase(
    private val habayeb: HabayebRepository,
    private val mutations: HabayebMutationRepository
) {
    private fun id() = "dtx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
    suspend fun saveHabayebCustomer(customer:HabayebCustomer, initialAmount:BigDecimal, initialType:String, customTimestamp:Long=System.currentTimeMillis()/1000, initialDetails:String="", isForeign:Boolean=false, currencyCode:String="DEFAULT", foreignAmount:BigDecimal=BigDecimal.ZERO, exchangeRate:BigDecimal=BigDecimal.ZERO, isRateCalculated:Boolean=false, equivalentAmount:BigDecimal=BigDecimal.ZERO, selectedCategoryFilter:String?, settings:AppSettings) {
        val opening = initialAmount.compareTo(BigDecimal.ZERO).takeIf { it > 0 }?.let {
            HabayebTransaction(id(),customer.id,initialType,initialAmount,customTimestamp,initialDetails.ifEmpty{customer.notes},isForeign=isForeign,currencyCode=currencyCode,foreignAmount=foreignAmount,exchangeRate=exchangeRate,isRateCalculated=isRateCalculated,equivalentAmount=equivalentAmount,baseCurrencyCode=settings.currencySymbol)
        }
        habayeb.insertCustomerWithOpeningTransaction(customer,opening)
    }
    suspend fun addHabayebTransaction(customerId:String,type:String,amount:BigDecimal,desc:String,timestamp:Long=System.currentTimeMillis()/1000,editingTxId:String?=null,linkedMainTxId:String?=null,isForeign:Boolean=false,currencyCode:String="DEFAULT",foreignAmount:BigDecimal=BigDecimal.ZERO,exchangeRate:BigDecimal=BigDecimal.ZERO,isRateCalculated:Boolean=false,equivalentAmount:BigDecimal=BigDecimal.ZERO,baseCurrencySymbol:String) {
        if (isForeign && isRateCalculated) {
            require(exchangeRate > BigDecimal.ZERO) { "المعاملة المصروفة تحتاج سعر صرف صالح" }
            require(equivalentAmount >= BigDecimal.ZERO) { "المكافئ المالي غير صالح" }
            require(currencyCode.isNotBlank() && currencyCode != "DEFAULT") { "عملة المعاملة الأجنبية مطلوبة" }
            require(baseCurrencySymbol.isNotBlank() && baseCurrencySymbol != "DEFAULT") { "عملة الأساس التاريخية مطلوبة" }
        }
        val txId=editingTxId?:id(); val link=(linkedMainTxId ?: editingTxId?.let{habayeb.getHabayebTransactionById(it)?.linkedMainTxId})?.trim()?.takeIf{it.isNotEmpty()&&it!="0"&&it!="null"&&it!=txId}
        habayeb.insertHabayebTransaction(HabayebTransaction(txId,customerId,type,amount,timestamp,desc,link,isForeign,currencyCode,foreignAmount,exchangeRate,isRateCalculated,equivalentAmount,baseCurrencySymbol))
    }
    suspend fun updateTransactionExchangeRate(txId:String,newRate:BigDecimal,calculateRate:Boolean,defaultCurrency:String) {
        val tx=habayeb.getHabayebTransactionById(txId)?:return
        val target=tx.currencyCode.takeIf{it.isNotBlank()&&it!="DEFAULT"} ?: tx.baseCurrencyCode.ifBlank{defaultCurrency}
        val base=tx.baseCurrencyCode.takeIf{it.isNotBlank()&&it!="DEFAULT"} ?: defaultCurrency
        val pair=CurrencyPair(base,target,newRate)
        val source=if(tx.foreignAmount.compareTo(BigDecimal.ZERO)>0) tx.foreignAmount else tx.amount
        val enabled=calculateRate&&!pair.isSelfPair
        val equivalent=if (enabled) CurrencyConfig.convertDirectedAmount(
            amount=source, sourceCurrency=target, targetCurrency=base,
            rate=pair.safeRate, rateSourceCurrency=base, rateTargetCurrency=target
        ) else BigDecimal.ZERO
        habayeb.insertHabayebTransaction(tx.copy(currencyCode=target,baseCurrencyCode=base,isForeign=!pair.isSelfPair,exchangeRate=if(enabled) pair.safeRate else BigDecimal.ZERO,isRateCalculated=enabled,equivalentAmount=equivalent,amount=if(enabled) equivalent else source,foreignAmount=source))
    }
    suspend fun revalueHistoricalTransactions(baseCurrencyCode:String,targetCurrencyCode:String,newRate:BigDecimal) {
        require(newRate > BigDecimal.ZERO) { "سعر الصرف غير موجود أو غير صالح" }
        habayeb.revalueHistoricalTransactions(baseCurrencyCode, targetCurrencyCode, newRate)
    }
    suspend fun updateCustomerName(id:String,name:String)=habayeb.updateCustomerName(id,name)
    suspend fun updateCustomer(customer:HabayebCustomer)=habayeb.updateCustomer(customer)
    suspend fun deleteCustomer(id:String) = mutations.deleteCustomerToTrash(id)
    suspend fun deleteMultipleCustomers(ids:List<String>) { ids.forEach { deleteCustomer(it) } }
    suspend fun deleteTransaction(id:String,isEdit:Boolean=false) = mutations.deleteTransactionToTrash(id, !isEdit)
    suspend fun deleteMultipleTransactions(ids:List<String>) { ids.forEach{deleteTransaction(it)} }
}
