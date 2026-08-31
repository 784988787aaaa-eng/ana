/**
 * =====================================================================
 * ملف: حالة استخدام معاملات الحبايب (HabayebTransactionUseCase.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن وسيط الأعمال المركزي (Business Domain Use Case) لإدارة وحفظ
 * معاملات ديون العملاء (قسم الحبايب)، وإجراء عمليات الصرف وإعادة تقييم العملات الأجنبية،
 * ومعالجة الحذف الفردي والجماعي مع الحفظ في سلة المحذوفات والاهتزاز اللمسي.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. حفظ وإنشاء العملاء والمعاملات الافتتاحية (Customer & Opening Transaction Creation):
 *    - ربط العميل بالرصيد الافتتاحي وتصنيفه، والتحقق من صلاحية الفترة التجريبية وتفعيل التطبيق.
 * 2. المعاملات متعددة العملات وأسعار الصرف (Multi-Currency Transactions & Conversions):
 *    - تسجيل حركات بالعملات الأجنبية، وحساب المبالغ المعادلة بدقة، وتحديث سعر الصرف للحركات الفردية.
 * 3. إعادة التقييم الشامل للعملات التاريخية (Historical Transaction Revaluation):
 *    - تعديل أسعار صرف العملات الأجنبية وتحديث المعاملات المرتبطة ضمن معاملة ذرية [withTransaction].
 * 4. الحذف الآمن والحفظ في سلة المحذوفات (Soft Delete & Trash Preservation):
 *    - ترحيل الحسابات والمعاملات المحذوفة إلى سلة المحذوفات كحزم (Bundles) قابلة للاسترجاع.
 */
package com.example.domain.usecase.habayeb

// ---------------------------------------------------------------------
// استيراد حزم أندرويد الأساسية، وكيانات قاعدة البيانات، ومستودع العمليات
// ---------------------------------------------------------------------
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.DatabaseDefaults
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.repository.FinanceRepository
import com.example.ui.helper.VibrationHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.viewmodel.FinanceConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID

/**
 * [فئة حالة استخدام معاملات الحبايب - HabayebTransactionUseCase]:
 * @property application سياق التطبيق العام للاهتزازات وقاعدة البيانات.
 * @property repository مستودع البيانات المالية لتنفيذ الاستعلامات.
 * @property sharedPrefs التفضيلات المشتركة لتخزين روابط التصنيفات.
 */
class HabayebTransactionUseCase(
    private val application: Application,
    private val repository: FinanceRepository,
    private val sharedPrefs: SharedPreferences
) {
    /**
     * الثوابت والمعرفات المساعدة لتوليد المعرفات الفريدة.
     */
    companion object {
        private const val TAG = "HabayebTxUseCase"
        private const val FALLBACK_NONE = "NONE"

        /** توليد معرف فريد للمعاملة يبدأ بـ dtx_ */
        private fun generateTxId(): String = "dtx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
    }

    /**
     * [حفظ عميل جديد مع رصيد افتتاحي - saveHabayebCustomer]:
     * يتحقق من الترخيص، ويحفظ العميل والمعاملة في قاعدة البيانات، ويربط التصنيف المختار.
     *
     * @param customer بيانات العميل الجديد.
     * @param transaction المعاملة الافتتاحية إن وجدت.
     * @param selectedCategoryFilter التصنيف المختار للعميل.
     * @param onActivationRequired رد نداء عند انتهاء النسخة التجريبية والحاجة للتفعيل.
     * @param onCategoryUpdated رد نداء عند تحديث التصنيف.
     */
    suspend fun saveHabayebCustomer(
        customer: HabayebCustomer,
        transaction: HabayebTransaction?,
        selectedCategoryFilter: String?,
        onActivationRequired: () -> Unit,
        onCategoryUpdated: () -> Unit
    ) = withContext(Dispatchers.IO) {
        if (transaction != null && transaction.amount > BigDecimal.ZERO && repository.isTrialExpiredDirect()) {
            onActivationRequired()
            return@withContext
        }
        try {
            repository.insertCustomerWithOpeningTransaction(customer, transaction)

            if (selectedCategoryFilter != null && selectedCategoryFilter != FinanceConstants.CATEGORY_CLOSED) {
                sharedPrefs.edit().putString("${HabayebCategoryManager.PREFIX_CAT_LINK}${customer.id}", selectedCategoryFilter).apply()
                onCategoryUpdated()
            }

            VibrationHelper.triggerSuccessVibration(application)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error saving customer", e)
        }
    }

    /**
     * [واجهة موسعة لحفظ العميل مع المعاملة متعددة العملات]:
     */
    suspend fun saveHabayebCustomer(
        customer: HabayebCustomer,
        initialAmount: BigDecimal,
        initialType: String,
        customTimestamp: Long = System.currentTimeMillis() / 1000,
        initialDetails: String = "",
        isForeign: Boolean = false,
        currencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE,
        foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ONE,
        isRateCalculated: Boolean = false,
        equivalentAmount: BigDecimal = BigDecimal.ZERO,
        selectedCategoryFilter: String?,
        settings: AppSettings,
        onActivationRequired: () -> Unit,
        onCategoryUpdated: () -> Unit
    ) {
        val transaction = if (initialAmount > BigDecimal.ZERO) {
            HabayebTransaction(
                id = generateTxId(),
                customerId = customer.id,
                type = initialType,
                amount = initialAmount,
                timestamp = customTimestamp,
                description = initialDetails.ifEmpty { customer.notes },
                isForeign = isForeign,
                currencyCode = currencyCode,
                foreignAmount = foreignAmount,
                exchangeRate = exchangeRate,
                isRateCalculated = isRateCalculated,
                equivalentAmount = equivalentAmount,
                baseCurrencyCode = settings.currencySymbol
            )
        } else null

        saveHabayebCustomer(
            customer = customer,
            transaction = transaction,
            selectedCategoryFilter = selectedCategoryFilter,
            onActivationRequired = onActivationRequired,
            onCategoryUpdated = onCategoryUpdated
        )
    }

    /**
     * [إضافة معاملة جديدة للعميل - addHabayebTransaction]:
     * يفحص الترخيص ويسجل المعاملة ويفعل الاهتزاز اللمسي للنجاح.
     */
    suspend fun addHabayebTransaction(
        transaction: HabayebTransaction,
        onActivationRequired: () -> Unit
    ) = withContext(Dispatchers.IO) {
        if (repository.isTrialExpiredDirect()) {
            onActivationRequired()
            return@withContext
        }
        try {
            repository.insertHabayebTransaction(transaction)
            VibrationHelper.triggerSuccessVibration(application)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error adding transaction", e)
        }
    }

    /**
     * [إضافة أو تعديل معاملة بتفاصيل دقيقة ومتعددة العملات]:
     */
    suspend fun addHabayebTransaction(
        customerId: String,
        type: String,
        amount: BigDecimal,
        desc: String,
        timestamp: Long = System.currentTimeMillis() / 1000,
        editingTxId: String? = null,
        linkedMainTxId: String? = null,
        isForeign: Boolean = false,
        currencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE,
        foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ONE,
        isRateCalculated: Boolean = false,
        equivalentAmount: BigDecimal = BigDecimal.ZERO,
        baseCurrencySymbol: String,
        onActivationRequired: () -> Unit
    ) {
        val txId = editingTxId ?: generateTxId()
        val candidateLinkedId = if (linkedMainTxId != null) {
            linkedMainTxId
        } else if (editingTxId != null) {
            repository.getHabayebTransactionById(editingTxId)?.linkedMainTxId
        } else {
            null
        }
        val cleanLinkedMainTxId = candidateLinkedId?.trim()?.takeIf { 
            it.isNotBlank() && !it.equals("null", ignoreCase = true) && it != "0" && it != txId 
        }

        val transaction = HabayebTransaction(
            id = txId,
            customerId = customerId,
            type = type,
            amount = amount,
            timestamp = timestamp,
            description = desc,
            linkedMainTxId = cleanLinkedMainTxId,
            isForeign = isForeign,
            currencyCode = currencyCode,
            foreignAmount = foreignAmount,
            exchangeRate = exchangeRate,
            isRateCalculated = isRateCalculated,
            equivalentAmount = equivalentAmount,
            baseCurrencyCode = baseCurrencySymbol
        )
        addHabayebTransaction(transaction, onActivationRequired)
    }

    /**
     * [تحديث سعر الصرف لمعاملة أجنبية محددة - updateTransactionExchangeRate]:
     * يعيد احتساب القيمة المعادلة ويحدث المعاملة الرئيسية المرتبطة داخل عملية ذرية.
     */
    suspend fun updateTransactionExchangeRate(
        txId: String,
        newRate: BigDecimal,
        calculateRate: Boolean,
        defaultCurrency: String
    ) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(application)
            db.withTransaction {
                val tx = repository.getHabayebTransactionById(txId)
                if (tx != null) {
                    val parsed = CurrencyConfig.parseTransactionCurrency(tx.description, FALLBACK_NONE)
                    val txCurrency = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) {
                        tx.currencyCode
                    } else if (parsed.first != FALLBACK_NONE) {
                        parsed.first
                    } else if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
                        tx.baseCurrencyCode
                    } else defaultCurrency

                    val pair = com.example.domain.model.CurrencyPair(
                        baseCurrency = defaultCurrency,
                        targetCurrency = txCurrency,
                        rate = if (newRate <= BigDecimal.ZERO) BigDecimal.ONE else newRate
                    )

                    val isSelfConversion = pair.isSelfPair
                    val finalRate = if (isSelfConversion) BigDecimal.ONE else pair.safeRate
                    val finalCalculateRate = if (isSelfConversion) false else calculateRate

                    val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                    val finalEquivalent = CurrencyConfig.convertAmountBigDecimal(
                        sourceAmount,
                        pair.baseCurrency,
                        pair.targetCurrency,
                        finalRate
                    )

                    if (tx.linkedMainTxId != null) {
                        val mainTx = repository.getTransactionById(tx.linkedMainTxId)
                        if (mainTx != null) {
                            val updatedMainTx = mainTx.copy(amount = if (finalCalculateRate) finalEquivalent else BigDecimal.ZERO)
                            repository.saveTransaction(updatedMainTx)
                        }
                    }

                    val updatedTx = tx.copy(
                        currencyCode = txCurrency,
                        baseCurrencyCode = defaultCurrency,
                        isForeign = !pair.isSelfPair,
                        exchangeRate = finalRate,
                        isRateCalculated = finalCalculateRate,
                        equivalentAmount = if (finalCalculateRate) finalEquivalent else BigDecimal.ZERO,
                        amount = if (finalCalculateRate) finalEquivalent else sourceAmount,
                        foreignAmount = sourceAmount
                    )
                    repository.insertHabayebTransaction(updatedTx)
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error updating transaction exchange rate", e)
        }
    }

    /**
     * [إعادة التقييم الشامل للمعاملات التاريخية لعملة معينة - revalueHistoricalTransactions]:
     * يمر على كافة المعاملات السابقة لعملة محددة ويعيد احتساب قيمتها وفق سعر الصرف الجديد.
     */
    suspend fun revalueHistoricalTransactions(
        baseCurrencyCode: String,
        targetCurrencyCode: String,
        newRate: BigDecimal
    ) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(application)
            db.withTransaction {
                val transactions = repository.getAllTransactionsDirect()
                val finalRate = if (newRate <= BigDecimal.ZERO) BigDecimal.ONE else newRate

                val normTargetCurrency = CurrencyConfig.getBySymbol(targetCurrencyCode)?.symbol ?: targetCurrencyCode
                val normBaseCurrency = CurrencyConfig.getBySymbol(baseCurrencyCode)?.symbol ?: baseCurrencyCode

                transactions.forEach { tx ->
                    val txCurrencyRaw = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) tx.currencyCode else baseCurrencyCode
                    val txBaseCurrencyRaw = if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) tx.baseCurrencyCode else DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL

                    val normTxCurrency = CurrencyConfig.getBySymbol(txCurrencyRaw)?.symbol ?: txCurrencyRaw
                    val normTxBaseCurrency = CurrencyConfig.getBySymbol(txBaseCurrencyRaw)?.symbol ?: txBaseCurrencyRaw

                    if (normTxCurrency == normTargetCurrency && normTxBaseCurrency == normBaseCurrency && tx.isRateCalculated) {
                        val sourceForeign = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                        val finalEquivalent = CurrencyConfig.convertAmountBigDecimal(
                            sourceForeign,
                            baseCurrencyCode,
                            normTxCurrency,
                            finalRate
                        )

                        if (tx.linkedMainTxId != null) {
                            val mainTx = repository.getTransactionById(tx.linkedMainTxId)
                            if (mainTx != null) {
                                val updatedMainTx = mainTx.copy(amount = finalEquivalent)
                                repository.saveTransaction(updatedMainTx)
                            }
                        }

                        val updatedTx = tx.copy(
                            foreignAmount = sourceForeign,
                            exchangeRate = finalRate,
                            equivalentAmount = finalEquivalent,
                            amount = finalEquivalent,
                            baseCurrencyCode = baseCurrencyCode
                        )
                        repository.insertHabayebTransaction(updatedTx)
                    }
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error revaluing historical transactions", e)
        }
    }

    /**
     * [تحديث اسم العميل - updateCustomerName]:
     */
    suspend fun updateCustomerName(customerId: String, newName: String) = withContext(Dispatchers.IO) {
        try {
            repository.updateCustomerName(customerId, newName)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error updating customer name", e)
        }
    }

    /**
     * [تحديث بيانات العميل بالكامل - updateCustomer]:
     */
    suspend fun updateCustomer(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        try {
            repository.updateCustomer(customer)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error updating customer", e)
        }
    }

    /**
     * [حذف عميل فردي مع ترحيل بياناته لسلة المحذوفات - deleteCustomer]:
     */
    suspend fun deleteCustomer(customerId: String) = withContext(Dispatchers.IO) {
        try {
            val customer = repository.getCustomerByIdDirect(customerId)
            val customerTxs = repository.getTransactionsForCustomerDirect(customerId)

            if (customer != null) {
                repository.softDeleteHabayebBundleToTrash(customer, customerTxs)
            }
            repository.deleteCustomerAndTransactions(customerId)
            VibrationHelper.triggerDeleteVibration(application)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error deleting customer", e)
        }
    }

    /**
     * [حذف مجموعة عملاء دفعة واحدة مع سلة المحذوفات - deleteMultipleCustomers]:
     */
    suspend fun deleteMultipleCustomers(customerIds: List<String>) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(application)
            db.withTransaction {
                val allCustomers = repository.getAllCustomersDirect()
                val customerMap = allCustomers.associateBy { it.id }
                for (id in customerIds) {
                    val customer = customerMap[id]
                    val customerTxs = repository.getTransactionsForCustomerDirect(id)
                    if (customer != null) {
                        repository.softDeleteHabayebBundleToTrash(customer, customerTxs)
                    }
                    repository.deleteCustomerAndTransactions(id)
                }
            }
            VibrationHelper.triggerDeleteVibration(application)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error deleting multiple customers", e)
        }
    }

    /**
     * [حذف معاملة فردية - deleteTransaction]:
     * ينقل المعاملة لسلة المحذوفات ويحذف الحركة المرتبطة بها في المعاملات الرئيسية.
     */
    suspend fun deleteTransaction(txId: String, isEdit: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val tx = repository.getHabayebTransactionById(txId)
            if (tx != null && !isEdit) {
                repository.softDeleteHabayebTransactionToTrash(tx)
            }
            if (tx?.linkedMainTxId != null) {
                repository.deleteTransactionById(tx.linkedMainTxId)
            }
            repository.deleteHabayebTransactionById(txId)
            if (!isEdit) {
                VibrationHelper.triggerDeleteVibration(application)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error deleting transaction", e)
        }
    }

    /**
     * [حذف مجموعة معاملات دفعة واحدة - deleteMultipleTransactions]:
     */
    suspend fun deleteMultipleTransactions(txIds: List<String>) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(application)
            db.withTransaction {
                txIds.forEach { txId ->
                    val tx = repository.getHabayebTransactionById(txId)
                    if (tx != null) {
                        repository.softDeleteHabayebTransactionToTrash(tx)
                        if (tx.linkedMainTxId != null) {
                            repository.deleteTransactionById(tx.linkedMainTxId)
                        }
                    }
                    repository.deleteHabayebTransactionById(txId)
                }
            }
            VibrationHelper.triggerDeleteVibration(application)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error deleting multiple transactions", e)
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// هذا القسم توثيقي فقط؛ لا يغيّر أي تعليمة تنفيذية في الملف الأصلي.
// - الحفاظ على ذرية العمليات متعددة الجداول باستخدام Room transaction حيث يلزم.
// - فصل سياسات التحقق والترخيص عن عمليات الحفظ تدريجياً فقط في إعادة تصميم مستقبلية.
// - اختبار الحذف والاسترجاع وإعادة التقييم للعملات كسيناريوهات تكامل كاملة.
// - عدم تغيير ترتيب تحديثات البيانات دون تحليل آثار الاتساق وسلة المحذوفات.
// - أي تنفيذ فعلي لهذه التوصيات يُرحّل إلى مهمة هندسية مستقلة ولا يُجرى داخل هذا الملف أثناء التوثيق.
