/**
 * =====================================================================
 * ملف: كيان عناصر سلة المهملات والمحذوفات (TrashItemEntity.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `deleted_items` في قاعدة بيانات Room،
 * وهو الحاوية الرئيسية لنظام سلة المهملات واسترداد البيانات المحذوفة (Soft Delete Container).
 * 
 * [المسؤوليات المعمارية ونمط الحفظ المرن]:
 * 1. نمط التغليف التسلسلي (JSON Envelope Pattern):
 *    - تخزين الكائنات المحذوفة ككتل بيانات نصية بتنسيق JSON المعياري في حقل [jsonData].
 *    - الحفاظ الكامل على كافة حقول الكيان وتفاصيله المرجعية وعلاقاته الشجرية دون الحاجة لتغيير جداول السلة مع كل تحديث لقاعدة البيانات.
 * 2. التوجيه الدقيق للاستعادة:
 *    - استخدام [sourceSystem] و [originalTableName] لتحديد مسار ومحلل الاستعادة (Restoration Parser) المناسب لإعادة الكيان إلى جدوله الأصلي بدقة متناهية.
 * 3. إدارة دورة حياة المحذوفات:
 *    - استخدام [deletedAt] لترتيب العناصر حسب وقت الحذف وتطبيق سياسات التنظيف والاحتفاظ المؤقت.
 */
package com.example.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room لتحديد الجداول والأعمدة والمفاتيح
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [فئة بيانات العنصر المحذوف - DeletedItemEntity]:
 * تمثل بنية الجدول المخصص للاحتفاظ بالمعاملات والحسابات المحذوفة مؤقتاً في سلة المهملات.
 *
 * @property id المعرف الفريد الثابت (UUID) لسجل الحذف في سلة المهملات.
 * @property sourceSystem اسم النظام الفرعي المالي الذي نشأ منه العنصر (مثل: "HABAYEB" أو "LEDGER").
 * @property originalTableName اسم الجدول الأصلي أو نوع الحزمة المحذوفة (مثل: `habayeb_bundle` أو `transactions`).
 * @property jsonData النص المتسلسل بصيغة JSON الحاوي لكافة بيانات الكائن أو الحزمة المحذوفة.
 * @property deletedAt الطابع الزمني بالمللي ثانية لتاريخ وتوقيت الحذف؛ الافتراضي هو التوقيت الحالي للنظام.
 */
@Entity(tableName = "deleted_items")
data class DeletedItemEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "sourceSystem") val sourceSystem: String,
    @ColumnInfo(name = "originalTableName") val originalTableName: String,
    @ColumnInfo(name = "jsonData") val jsonData: String,
    @ColumnInfo(name = "deletedAt") val deletedAt: Long = System.currentTimeMillis()
)

// =====================================================================
// --- ملاحظات وتوصيات المعمارية البرمجية ---
// =====================================================================
// 1) يمثل هذا الكيان السجل المحذوف القابل للاسترجاع؛ يجب الحفاظ على بياناته
//    الأصلية بما يكفي لإعادة البناء دون خسارة دلالية.
// 2) أي تغيير في serialization أو retention يجب أن يراجع مع TrashDao وTrashCleanupWorker.
