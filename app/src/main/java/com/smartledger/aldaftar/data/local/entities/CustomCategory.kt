package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * يمثل تصنيفاً مخصصاً في قاعدة البيانات.
 * يحفظ الترتيب والرمز ونطاق التبويب دون تغيير بنية الجدول التاريخية.
 */
@Entity(tableName = "custom_categories")
data class CustomCategory(
    /** المعرف التلقائي للتصنيف، وتستخدمه طبقة البيانات للتمييز بين السجلات. */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    /** اسم التصنيف الذي يظهر للمستخدم. */
    @ColumnInfo(name = "name") val name: String,
    /** نوع التبويب الذي ينتمي إليه التصنيف لعزل سياق الاستخدام. */
    @ColumnInfo(name = "tabType") val tabType: String,
    /** الرمز البصري المحفوظ للتصنيف. */
    @ColumnInfo(name = "iconEmoji") val iconEmoji: String,
    /** ترتيب العرض المحفوظ لضمان ثبات ترتيب التصنيفات. */
    @ColumnInfo(name = "displayOrder") val displayOrder: Int = 0,
    /** يحدد ما إذا كان التصنيف محمياً من الحذف بواسطة قواعد النظام. */
    @ColumnInfo(name = "isSystemClosed") val isSystemClosed: Boolean = false
)
