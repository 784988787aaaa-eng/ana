/**
 * =====================================================================
 * ملف: كيان قيود دفتر اليومية العام (TransactionDb.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `transactions` في قاعدة بيانات Room،
 * وهو المسؤول عن تسجيل قيود الدخل والمصروف في دفتر اليومية المالي العام للتطبيق.
 * 
 * [المسؤوليات المعمارية وقواعد الأداء]:
 * 1. الدقة المالية التامة: استخدام كائن [BigDecimal] لتخزين المبلغ المالي [amount] لمنع تراكم أخطاء الفاصلة العائمة.
 * 2. الفهارس المركبة (Composite Indices):
 *    - فهرس `(type, timestamp)` لتسريع حساب إجمالي المصاريف أو الإيرادات خلال فترة محددة.
 *    - فهرس `(category, timestamp)` لتسريع استخراج تقارير التصنيفات وتوزيع المصروفات في الرسوم البيانية.
 * 3. الفهارس المفردة: على `timestamp` للترتيب الزمني و `category` للتصفية و `type` لحساب السيولة النقدية.
 */
package com.example.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room لتحديد الجداول والأعمدة والفهارس ودقة الأرقام
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * [فئة بيانات قيد اليومية - TransactionDb]:
 * تمثل بنية الجدول الأساسي لقيود حركة الصندوق (دخل ومصروف).
 *
 * @property id المعرف الفريد الثابت (UUID) للقيد المالي.
 * @property timestamp الطابع الزمني بالمللي ثانية لتاريخ وتوقيت تسجيل الحركة.
 * @property type نوع الحركة المالية (دخل: `INCOME` / مصروف: `EXPENSE`).
 * @property category التصنيف المالي للحركة (مثل: "مشتريات"، "رواتب"، "إيجار").
 * @property amount المبلغ المالي المسجل بدقة [BigDecimal].
 * @property description البيان التوضيحي أو تفاصيل المعاملة.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["type"]),
        Index(value = ["type", "timestamp"]),
        Index(value = ["category", "timestamp"])
    ]
)
data class TransactionDb(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "amount") val amount: BigDecimal,
    @ColumnInfo(name = "description") val description: String
)

