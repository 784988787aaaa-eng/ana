package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import java.math.BigDecimal

/**
 * يمثل حركة مالية مرتبطة بعميل في دفتر ديون الحبايب.
 * تستخدم القيم النقدية العشرية لمنع أخطاء الفاصلة العائمة أثناء الحساب والعرض.
 * يحافظ المفتاح الأجنبي على ارتباط الحركة بالعميل، وتضمن الفهارس سرعة كشف الحساب.
 */
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
    /** المعرف الفريد للحركة. */
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    /** معرف العميل المالك للحركة، وتفرض العلاقة المرجعية صحته. */
    @ColumnInfo(name = "customerId") val customerId: String,
    /** نوع الحركة الذي يحدد اتجاه الأثر على رصيد العميل. */
    @ColumnInfo(name = "type") val type: String,
    /** المبلغ الأساسي للحركة بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "amount") val amount: BigDecimal,
    /** وقت تنفيذ الحركة وفق وحدة الزمن المعتمدة في التطبيق. */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    /** وصف الحركة والبيان المرتبط بها. */
    @ColumnInfo(name = "description") val description: String,
    /** معرف الحركة المقابلة في دفتر اليومية عند وجود ربط مالي. */
    @ColumnInfo(name = "linkedMainTxId") val linkedMainTxId: String? = null,
    /** يحدد تسجيل الحركة بعملة تختلف عن العملة الأساسية. */
    @ColumnInfo(name = "is_foreign") val isForeign: Boolean = false,
    /** رمز عملة الحركة كما حفظ وقت تنفيذها. */
    @ColumnInfo(name = "currency_code") val currencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE,
    /** المبلغ الأصلي قبل التحويل بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "foreign_amount") val foreignAmount: BigDecimal = BigDecimal.ZERO,
    /** سعر الصرف المستخدم للحركة بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "exchange_rate") val exchangeRate: BigDecimal = BigDecimal.ONE,
    /** يحدد ما إذا تم اعتماد سعر صرف في حساب الحركة. */
    @ColumnInfo(name = "is_rate_calculated") val isRateCalculated: Boolean = false,
    /** المبلغ المكافئ بالعملة الأساسية بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "equivalent_amount") val equivalentAmount: BigDecimal = BigDecimal.ZERO,
    /** رمز العملة الأساسية وقت تسجيل الحركة لضمان ثبات المعنى التاريخي. */
    @ColumnInfo(name = "base_currency_code") val baseCurrencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE
) {
    /** يعيد المبلغ الأصلي للحركة لاستخدام طبقات العرض دون تكرار الحقل. */
    val originalAmount: BigDecimal get() = foreignAmount
    /** يعيد رمز العملة المحفوظ للحركة. */
    val currencySymbol: String get() = currencyCode
    /** يبين اعتماد الحركة على تحويل صرف. */
    val isExchanged: Boolean get() = isRateCalculated
    /** يعيد رمز العملة الأساسية المرتبطة بالحركة. */
    val targetCurrencySymbol: String get() = baseCurrencyCode
    /** يعيد المبلغ الناتج بعد التحويل. */
    val exchangedAmount: BigDecimal get() = equivalentAmount
}
