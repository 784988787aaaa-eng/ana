package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * يمثل عنصراً محفوظاً مؤقتاً في سلة المحذوفات.
 * يحتفظ بالبيانات المتسلسلة اللازمة للاستعادة دون تغيير جداول السلة مع كل تحديث للكيانات.
 * يبقى هذا الكيان مستقلاً عن الجداول الأصلية لتسهيل الاستعادة الآمنة والحذف النهائي المنضبط.
 */
@Entity(tableName = "deleted_items")
data class DeletedItemEntity(
    /** المعرف الفريد لسجل المحذوف في السلة. */
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    /** النظام الفرعي الذي أنشأ العنصر قبل حذفه. */
    @ColumnInfo(name = "sourceSystem") val sourceSystem: String,
    /** اسم الجدول الأصلي الذي يجب أن تعاد إليه البيانات. */
    @ColumnInfo(name = "originalTableName") val originalTableName: String,
    /** البيانات المتسلسلة اللازمة لإعادة بناء العنصر الأصلي. */
    @ColumnInfo(name = "jsonData") val jsonData: String,
    /** وقت الحذف المستخدم في الفرز وسياسات الاحتفاظ والتنظيف. */
    @ColumnInfo(name = "deletedAt") val deletedAt: Long = System.currentTimeMillis()
)
