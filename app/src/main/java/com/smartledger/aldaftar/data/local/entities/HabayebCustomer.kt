package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartledger.aldaftar.domain.model.TransactionType

/**
 * يمثل بطاقة العميل في دفتر ديون الحبايب.
 * يحافظ على الفهارس الحالية لتسريع البحث والفرز دون تعديل مخطط الجدول.
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
    /** المعرف الفريد للعميل والمستخدم كمرجع في حركاته. */
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    /** اسم العميل أو الجهة المالية المرتبطة بالحساب. */
    @ColumnInfo(name = "name") val name: String,
    /** رقم الهاتف المحفوظ للتواصل مع العميل. */
    @ColumnInfo(name = "phone") val phone: String,
    /** ملاحظات وصفية لا تدخل في الحساب المالي. */
    @ColumnInfo(name = "notes") val notes: String,
    /** وقت إنشاء بطاقة العميل بوحدة الزمن المعتمدة في التطبيق. */
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    /** نوع الرصيد الابتدائي وفق القيم المعتمدة في نموذج المعاملات. */
    @ColumnInfo(name = "initialType", defaultValue = "OWED_BY_THEM") val initialType: String = TransactionType.OWED_BY_THEM.value
)
