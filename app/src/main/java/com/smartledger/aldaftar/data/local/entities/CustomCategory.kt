/**
 * =====================================================================
 * ملف: كيان التصنيف المخصص (CustomCategory.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `custom_categories` في قاعدة بيانات Room،
 * وهو المسؤول عن تمكين المستخدم من إنشاء تصنيفات وتجميعات مرنة للحسابات والمعاملات
 * (مثل: موردين، أصدقاء، عملاء جملة، مصاريف دورية).
 * 
 * [المسؤوليات المعمارية]:
 * 1. دعم التخصيص المرن للواجهة: حفظ الترتيب البصري [displayOrder] والرمز التعبيري [iconEmoji].
 * 2. ربط التصنيف بالتبويب المناسب [tabType]: لتصفية التصنيفات وعزل تصنيفات ديون الحبايب عن تصنيفات اليومية.
 * 3. حماية تصنيفات النظام [isSystemClosed]: قفل التصنيفات الحساسة لمنع المستخدم من حذفها عن طريق الخطأ.
 */
package com.smartledger.aldaftar.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room لتحديد الجداول والأعمدة والمفاتيح
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [فئة بيانات التصنيف المخصص - CustomCategory]:
 * تمثل بنية جدول `custom_categories` لإدارة التصنيفات المعرفة من قبل المستخدم أو النظام.
 *
 * @property id المعرف الأساسي التلقائي للتصنيف.
 * @property name الاسم العربي للتصنيف (مثل: "موردين"، "أقارب"، "عملاء نقليات").
 * @property tabType نوع التبويب أو الوحدة المالية التابع لها التصنيف (مثل: "HABAYEB").
 * @property iconEmoji الرمز التعبيري الأيقوني المميز للتصنيف لسهولة التعرف البصري.
 * @property displayOrder رقم الترتيب التسلسلي لعرض التصنيفات في شريط التصفية الأفقي.
 * @property isSystemClosed مؤشر قفل التصنيف من قبل النظام؛ إذا كانت القيمة `true` يُمنع حذف التصنيف.
 */
@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tabType") val tabType: String,
    @ColumnInfo(name = "iconEmoji") val iconEmoji: String,
    @ColumnInfo(name = "displayOrder") val displayOrder: Int = 0,
    @ColumnInfo(name = "isSystemClosed") val isSystemClosed: Boolean = false
)

