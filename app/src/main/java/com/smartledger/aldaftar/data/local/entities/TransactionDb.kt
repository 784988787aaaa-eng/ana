package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * يمثل قيداً مالياً في دفتر اليومية العام.
 * يحافظ على المبلغ كقيمة عشرية ويثبت الفهارس الحالية لتقارير الدخل والمصروف.
 * عدم تغيير الأعمدة أو الفهارس يحمي البيانات التاريخية من اختلاف المخطط.
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
    /** المعرف الفريد للقيد المالي. */
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    /** وقت تسجيل القيد وفق وحدة الزمن المعتمدة في التطبيق. */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    /** نوع القيد الذي يحدد أثره في تقارير السيولة. */
    @ColumnInfo(name = "type") val type: String,
    /** التصنيف المالي المستخدم في الفرز والتقارير. */
    @ColumnInfo(name = "category") val category: String,
    /** قيمة القيد المالية المحفوظة بدقة عشرية. */
    @ColumnInfo(name = "amount") val amount: BigDecimal,
    /** البيان النصي المصاحب للقيد. */
    @ColumnInfo(name = "description") val description: String
)
