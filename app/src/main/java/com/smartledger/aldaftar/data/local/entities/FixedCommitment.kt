/**
 * =====================================================================
 * ملف: كيان الالتزامات المالية الثابتة (FixedCommitment.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `fixed_commitments` في قاعدة بيانات Room،
 * وهو المسؤول عن تتبع المستحقات والالتزامات المالية الدورية (مثل: إيجار المحل،
 * أقساط السيارات، الفواتير الشهرية، والجمعيات العائلية).
 * 
 * [المسؤوليات المعمارية والمالية]:
 * 1. الدقة الحسابية العالية: استخدام كائنات [BigDecimal] للمبلغ المستهدف [targetAmount]
 *    والمبلغ المدفوع الحالي [currentProgress] لمنع أي فقدان للكسور عند حساب نسب الإنجاز.
 * 2. المفتاح الأساسي المعنوي: استخدام اسم الالتزام [name] كمفتاح أساسي لمنع تكرار نفس البند المالي.
 * 3. الترتيب التفاعلي [orderIndex]: إتاحة إعادة ترتيب بطاقات الالتزامات بالسحب والإفلات في الواجهة.
 */
package com.smartledger.aldaftar.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room ودقة العمليات الحسابية BigDecimal
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * [فئة بيانات الالتزام الثابت - FixedCommitment]:
 * تمثل بنية الجدول المخصص لمتابعة سداد الأقساط والالتزامات المجدولة.
 *
 * @property name اسم الالتزام المالي ومفتاحه الأساسي (مثل: "إيجار المحل"، "فاتورة الكهرباء").
 * @property targetAmount إجمالي المبلغ المطلوب سداده بدقة [BigDecimal].
 * @property currentProgress المبلغ المسدد بالفعل حتى اللحظة بدقة [BigDecimal].
 * @property orderIndex رقم الترتيب التسلسلي لعرض الالتزامات في الشاشة.
 */
@Entity(tableName = "fixed_commitments")
data class FixedCommitment(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "targetAmount") val targetAmount: BigDecimal,
    @ColumnInfo(name = "currentProgress") val currentProgress: BigDecimal,
    @ColumnInfo(name = "orderIndex") val orderIndex: Int = 0
)

