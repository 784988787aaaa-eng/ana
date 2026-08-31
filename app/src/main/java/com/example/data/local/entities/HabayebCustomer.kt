/**
 * =====================================================================
 * ملف: كيان العميل في دفتر ديون الحبايب (HabayebCustomer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `habayeb_customers` في قاعدة بيانات Room،
 * وهو المسؤول عن حفظ الملف التعريفي للعملاء والمدينين والدائنين في وحدة "ديون الحبايب".
 * 
 * [المسؤوليات المعمارية وقواعد الأداء]:
 * 1. الفهارس المخصصة (Composite & Single Column Indices):
 *    - فهرسة [name] للبحث اللحظي فائق السرعة عن العملاء بالاسم أو الأحرف الأولى.
 *    - فهرسة [phone] للبحث الفوري عن أرقام الهواتف أو مطابقة جهات الاتصال.
 *    - فهرسة [createdAt] و [initialType] لسرعة الفرز والتصفية بحسب الأحدث أو بحسب طبيعة الحساب.
 * 2. ثبات التصنيف المبدئي [initialType]:
 *    - يحفظ نية المستخدم الأصلية عند إنشاء الحساب (هل تم إنشاؤه في تبويب "لنا" أم "علينا").
 *    - لا يتغير هذا الحقل آلياً حتى عند تقلب رصيد الحساب بين الموجب والسالب نتيجة السداد.
 */
package com.example.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room لتحديد الجداول والأعمدة والفهارس ونوع المعاملة
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.TransactionType

/**
 * [فئة بيانات العميل - HabayebCustomer]:
 * تمثل بنية جدول `habayeb_customers` الحاوية للمعلومات الشخصية والاتصالية للعميل.
 *
 * @property id المعرف الفريد الثابت (UUID) لبطاقة العميل.
 * @property name اسم العميل أو الجهة المالية.
 * @property phone رقم الهاتف للتواصل السريع وإرسال مطالبات كشف الحساب.
 * @property notes ملاحظات إضافية حول العميل وطبيعة التعاملات معه.
 * @property createdAt الطابع الزمني بالمللي ثانية لتاريخ إنشاء بطاقة العميل.
 * @property initialType التصنيف المبدئي الثابت للعميل (لنا: `OWED_BY_THEM` / علينا: `OWED_TO_THEM`).
 */
@Entity(
    tableName = "habayeb_customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["createdAt"]),
        Index(value = ["initialType"])
    ]
)
data class HabayebCustomer(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "initialType", defaultValue = "OWED_BY_THEM") val initialType: String = TransactionType.OWED_BY_THEM.value
)

// =====================================================================
// --- ملاحظات وتوصيات المعمارية البرمجية ---
// =====================================================================
// 1) هوية العميل هي نقطة الارتكاز لحركات الحبايب؛ أي تعديل في المفتاح أو علاقات
//    الربط يجب أن يراجع جميع استعلامات HabayebDao والحسابات التابعة.
// 2) يفضّل إبقاء القيم المشتقة خارج Entity ما لم تكن مطلوبة فعلاً للتخزين.
