package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.TransactionRepository
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.domain.model.CurrencyPair
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import java.math.BigDecimal
import java.util.UUID

/** قواعد معاملات الحبايب دون تأثيرات واجهة. */
class HabayebTransactionUseCase(
    private val habayeb: HabayebRepository,
    private val transactions: TransactionRepository,
    private val mutations: HabayebMutationRepository
) {
    private fun id() = "dtx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
    private fun convert(amount: BigDecimal, pair: CurrencyPair): BigDecimal =
        if (pair.isSelfPair) amount else FinancialPolicy.normalize(amount.multiply(pair.safeRate))

    suspend fun saveHabayebCustomer(customer:HabayebCustomer, initialAmount:BigDecimal, initialType:String, customTimestamp:Long=System.currentTimeMillis()/1000, initialDetails:String="", isForeign:Boolean=false, currencyCode:String="DEFAULT", foreignAmount:BigDecimal=BigDecimal.ZERO, exchangeRate:BigDecimal=BigDecimal.ONE, isRateCalculated:Boolean=false, equivalentAmount:BigDecimal=BigDecimal.ZERO, selectedCategoryFilter:String?, settings:AppSettings) {
        val opening = initialAmount.compareTo(BigDecimal.ZERO).takeIf { it > 0 }?.let {
            HabayebTransaction(id(),customer.id,initialType,initialAmount,customTimestamp,initialDetails.ifEmpty{customer.notes},isForeign=isForeign,currencyCode=currencyCode,foreignAmount=foreignAmount,exchangeRate=exchangeRate,isRateCalculated=isRateCalculated,equivalentAmount=equivalentAmount,baseCurrencyCode=settings.currencySymbol)
        }
        habayeb.insertCustomerWithOpeningTransaction(customer,opening)
    }
    suspend fun addHabayebTransaction(customerId:String,type:String,amount:BigDecimal,desc:String,timestamp:Long=System.currentTimeMillis()/1000,editingTxId:String?=null,linkedMainTxId:String?=null,isForeign:Boolean=false,currencyCode:String="DEFAULT",foreignAmount:BigDecimal=BigDecimal.ZERO,exchangeRate:BigDecimal=BigDecimal.ONE,isRateCalculated:Boolean=false,equivalentAmount:BigDecimal=BigDecimal.ZERO,baseCurrencySymbol:String) {
        val txId=editingTxId?:id(); val link=(linkedMainTxId ?: editingTxId?.let{habayeb.getHabayebTransactionById(it)?.linkedMainTxId})?.trim()?.takeIf{it.isNotEmpty()&&it!="0"&&it!="null"&&it!=txId}
        habayeb.insertHabayebTransaction(HabayebTransaction(txId,customerId,type,amount,timestamp,desc,link,isForeign,currencyCode,foreignAmount,exchangeRate,isRateCalculated,equivalentAmount,baseCurrencySymbol))
    }
    suspend fun updateTransactionExchangeRate(txId:String,newRate:BigDecimal,calculateRate:Boolean,defaultCurrency:String) {
        val tx=habayeb.getHabayebTransactionById(txId)?:return
        val target=tx.currencyCode.takeIf{it.isNotBlank()&&it!="DEFAULT"} ?: tx.baseCurrencyCode.ifBlank{defaultCurrency}
        val pair=CurrencyPair(defaultCurrency,target,if(newRate.compareTo(BigDecimal.ZERO)<=0) BigDecimal.ONE else newRate)
        val source=if(tx.foreignAmount.compareTo(BigDecimal.ZERO)>0) tx.foreignAmount else tx.amount
        val enabled=calculateRate&&!pair.isSelfPair; val equivalent=convert(source,pair)
        if(tx.linkedMainTxId!=null) transactions.getTransactionById(tx.linkedMainTxId)?.let { transactions.saveTransaction(it.copy(amount=if(enabled) equivalent else BigDecimal.ZERO)) }
        habayeb.insertHabayebTransaction(tx.copy(currencyCode=target,baseCurrencyCode=defaultCurrency,isForeign=!pair.isSelfPair,exchangeRate=if(pair.isSelfPair) BigDecimal.ONE else pair.safeRate,isRateCalculated=enabled,equivalentAmount=if(enabled) equivalent else BigDecimal.ZERO,amount=if(enabled) equivalent else source,foreignAmount=source))
    }
    suspend fun revalueHistoricalTransactions(baseCurrencyCode:String,targetCurrencyCode:String,newRate:BigDecimal) {
        val rate=if(newRate.compareTo(BigDecimal.ZERO)<=0) BigDecimal.ONE else newRate
        habayeb.getAllTransactionsDirect().filter{it.currencyCode==targetCurrencyCode&&it.baseCurrencyCode==baseCurrencyCode&&it.isRateCalculated}.forEach { tx ->
            val source=if(tx.foreignAmount.compareTo(BigDecimal.ZERO)>0) tx.foreignAmount else tx.amount; val equivalent=FinancialPolicy.normalize(source.multiply(rate))
            tx.linkedMainTxId?.let{transactions.getTransactionById(it)?.let{m->transactions.saveTransaction(m.copy(amount=equivalent))}}
            habayeb.insertHabayebTransaction(tx.copy(foreignAmount=source,exchangeRate=rate,equivalentAmount=equivalent,amount=equivalent))
        }
    }
    suspend fun updateCustomerName(id:String,name:String)=habayeb.updateCustomerName(id,name)
    suspend fun updateCustomer(customer:HabayebCustomer)=habayeb.updateCustomer(customer)
    suspend fun deleteCustomer(id:String) = mutations.deleteCustomerToTrash(id)
    suspend fun deleteMultipleCustomers(ids:List<String>) { ids.forEach { deleteCustomer(it) } }
    suspend fun deleteTransaction(id:String,isEdit:Boolean=false) = mutations.deleteTransactionToTrash(id, !isEdit)
    suspend fun deleteMultipleTransactions(ids:List<String>) { ids.forEach{deleteTransaction(it)} }
}
