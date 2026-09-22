package com.smartledger.aldaftar.domain.admin

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * يمثل ترخيصاً صادراً وموثقاً من قبل الإدارة.
 */
@Entity(tableName = "admin_issued_licenses")
data class AdminIssuedLicense(
    @PrimaryKey
    val licenseId: String,          // معرف الترخيص الفريد مثل SL-A0B69950
    val accountCode: String,        // كود الحساب أو معرف الجهاز (SLD-... أو SL-...)
    val customerEmail: String,      // البريد الإلكتروني للعميل
    val customerPhone: String = "", // رقم هاتف العميل (لإرسال كود التفعيل عبر واتساب مباشرة)
    val customerName: String,       // اسم العميل أو المحل التجاري
    val licenseType: String,        // "LOCAL" (محلي) أو "ACCOUNT" (سحابي) أو "FULL" (شامل)
    val plan: String,               // "LIFETIME" (دائم) أو "TRIAL" (تجريبي)
    val maxDevices: Int = 1,        // أقصى عدد للأجهزة
    val trialDays: Int = 0,         // أيام التجربة (في حال كان تجريبياً)
    val activationToken: String,    // الرمز المشفر الموقع رقمياً كاملاً
    val shortActivationCode: String,// كود التفعيل السحابي (مثل MSMU92MJWW45427UTZJYUQUR)
    val notes: String = "",         // ملاحظات الإدارة
    val issuedAt: Long = System.currentTimeMillis(), // تاريخ الإصدار
    val isActive: Boolean = true    // حالة الترخيص (نشط / ملغى)
)
