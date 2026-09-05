package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * يمثل التزاماً مالياً ثابتاً محفوظاً في قاعدة البيانات.
 * تعتمد الحقول النقدية على القيمة العشرية للحفاظ على دقة المبالغ عند القراءة والحساب.
 * لا يغير الكيان أسماء الأعمدة أو أنواعها لأن ذلك يمس التوافق مع البيانات السابقة.
 */
@Entity(tableName = "fixed_commitments")
data class FixedCommitment(
    /** اسم الالتزام ومفتاحه الأساسي لمنع تكرار البند المالي نفسه. */
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    /** إجمالي المبلغ المستهدف للالتزام بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "targetAmount") val targetAmount: BigDecimal,
    /** المبلغ المسدد حتى اللحظة بقيمة عشرية دقيقة. */
    @ColumnInfo(name = "currentProgress") val currentProgress: BigDecimal,
    /** ترتيب الالتزام في واجهة العرض. */
    @ColumnInfo(name = "orderIndex") val orderIndex: Int = 0
)
