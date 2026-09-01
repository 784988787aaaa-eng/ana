/**
 * =====================================================================
 * ملف: كيان قيود معاملات عملاء الحبايب (HabayebTransaction.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `habayeb_transactions` في قاعدة بيانات Room،
 * وهو المسؤول عن تسجيل كافة الحركات المالية التفصيلية (دين، سداد، دفعة، صرف عملات)
 * الخاصة بحسابات العملاء في وحدة "ديون الحبايب".
 * 
 * [المسؤوليات المعمارية وقواعد التصميم المالي]:
 * 1. الدقة المالية الإلزامية الحتمية:
 *    - استخدام كائنات [BigDecimal] لجميع الحقول النقدية وأسعار الصرف لمنع أخطاء التقريب.
 * 2. التكامل المرجعي وحماية البيانات (Referential Integrity):
 *    - ربط المفتاح الأجنبي [customerId] بجدول `habayeb_customers` مع خاصية `CASCADE` للحذف والتحديث التلقائي.
 * 3. منظومة الصرف والعملات المتعددة (Multi-Currency Subsystem):
 *    - دعم تسجيل العملات الأجنبية [foreignAmount] وسعر الصرف [exchangeRate] وحساب المبلغ المكافئ [equivalentAmount].
 * 4. الربط المزدوج مع دفتر اليومية [linkedMainTxId]:
 *    - ربط المعاملة بالقيد المقابل لها في جدول دفتر اليومية المالي العام لضمان تزامن حركة الصندوق والسيولة.
 * 5. الفهارس المركبة لتسريع كشوفات الحساب:
 *    - فهارس ثنائية `(customerId, timestamp)` و `(customerId, type)` لعرض كشف الحساب والفلترة بأعلى كفاءة.
 */
package com.example.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room والعلاقات والفهارس وثوابت المالية
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ui.viewmodel.FinanceConstants
import java.math.BigDecimal

/**
 * [فئة بيانات قيد معاملة العميل - HabayebTransaction]:
 * تمثل بنية السجل المالي الفردي في كشف حساب العميل.
 *
 * @property id المعرف الفريد الثابت (UUID) للمعاملة.
 * @property customerId المعرف الأجنبي للعميل المرتبطة به المعاملة.
 * @property type نوع المعاملة (لنا: `OWED_BY_THEM` / علينا: `OWED_TO_THEM`).
 * @property amount المبلغ المالي بالعملة الأساسية بدقة [BigDecimal].
 * @property timestamp الطابع الزمني بالمللي ثانية لتاريخ وتوقيت تسجيل المعاملة.
 * @property description البيان أو تفاصيل الفاتورة/المعاملة.
 * @property linkedMainTxId معرف الحركة المقابلة في دفتر اليومية العام إن وجدت للتزامن المالي.
 * @property isForeign مؤشر ما إذا كانت المعاملة مسجلة بعملة أجنبية مختلفة عن العملة الأساسية.
 * @property currencyCode رمز عملة المعاملة (مثل: "SAR"، "USD"، "YER").
 * @property foreignAmount المبلغ الأصلي بالعملة الأجنبية قبل الصرف بدقة [BigDecimal].
 * @property exchangeRate سعر الصرف المعتمد في العملية بدقة [BigDecimal].
 * @property isRateCalculated مؤشر يوضح ما إذا تم تطبيق معادلة الصرف التحويلي.
 * @property equivalentAmount المبلغ المكافئ المحسوب بالعملة الأساسية بدقة [BigDecimal].
 * @property baseCurrencyCode رمز العملة الأساسية للتطبيق وقت تنفيذ المعاملة.
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
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "customerId") val customerId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "amount") val amount: BigDecimal,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "linkedMainTxId") val linkedMainTxId: String? = null,
    @ColumnInfo(name = "is_foreign") val isForeign: Boolean = false,
    @ColumnInfo(name = "currency_code") val currencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE,
    @ColumnInfo(name = "foreign_amount") val foreignAmount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "exchange_rate") val exchangeRate: BigDecimal = BigDecimal.ONE,
    @ColumnInfo(name = "is_rate_calculated") val isRateCalculated: Boolean = false,
    @ColumnInfo(name = "equivalent_amount") val equivalentAmount: BigDecimal = BigDecimal.ZERO,
    @ColumnInfo(name = "base_currency_code") val baseCurrencyCode: String = FinanceConstants.DEFAULT_CURRENCY_CODE
) {
    // -----------------------------------------------------------------
    // دوال مساعدة معمارية لتسهيل الربط مع طبقات العرض والواجهة
    // -----------------------------------------------------------------

    /** المبلغ الأصلي بالعملة الأجنبية */
    val originalAmount: BigDecimal get() = foreignAmount

    /** رمز العملة الفعلي المستخدم في المعاملة */
    val currencySymbol: String get() = currencyCode

    /** هل خضعت المعاملة لعملية تحويل وصرف عملات */
    val isExchanged: Boolean get() = isRateCalculated

    /** رمز العملة المستهدفة بعد المصارفة */
    val targetCurrencySymbol: String get() = baseCurrencyCode

    /** القيمة المالية الناتجة بعد المصارفة */
    val exchangedAmount: BigDecimal get() = equivalentAmount
}

