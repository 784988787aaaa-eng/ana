/*
 * ================================================================
 * التوثيق الهندسي العربي الفائق — SignatureFingerprintCalculator.kt
 * ================================================================
 * المسؤولية المعمارية:
 * مكوّن يحسب بصمة التوقيع اعتماداً على البيانات الداخلة ويعزل الحساب عن طبقة العرض.
 *
 * المشهد التعليمي والبصري:
 * تخيل شاشة «الإعدادات» على الهاتف: كل بطاقة هنا تمثل منطقة قرار واضحة؛ يقرأ المستخدم
 * الحالة أولاً، ثم يختار الإجراء، ثم يظهر الحوار المناسب عند الحاجة. هذا الملف يشرح
 * كيف تتحول حالة النظام إلى عناصر مرئية دون نقل مسؤوليات التخزين أو الأمن إلى Compose.
 *
 * فهرس العناصر التنفيذية المكتشفة:
 * - `object SignatureFingerprintCalculator {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `fun getSha1Fingerprint(context: Context): String {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `fun getSha256Fingerprint(context: Context): String {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `private fun getFingerprint(context: Context, algorithm: String): String {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val signatures = @Suppress("DEPRECATION") packageInfo.signatures`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val md = java.security.MessageDigest.getInstance(algorithm)`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `val publicKey = md.digest(signatures[0].toByteArray())`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 * - `private fun formatBytesToFingerprint(bytes: ByteArray): String {`: عنصر تنفيذي داخل الملف؛ يوضح العقدة/السلوك الذي يقدمه هذا التصريح، وتبقى تفاصيل التنفيذ الأصلية أدناه دون تعديل.
 *
 * قاعدة الثبات المطلقة:
 * النص التنفيذي الأصلي محفوظ ككتلة متصلة أدناه دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.settings.components

import android.content.Context
import com.example.R

// تم فصل الحسابات التقنية عن واجهة العرض للحفاظ على مسؤولية كل طبقة.
object SignatureFingerprintCalculator {

    fun getSha1Fingerprint(context: Context): String {
        return getFingerprint(context, "SHA-1")
    }

    fun getSha256Fingerprint(context: Context): String {
        return getFingerprint(context, "SHA-256")
    }

    private fun getFingerprint(context: Context, algorithm: String): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(android.content.pm.PackageManager.GET_SIGNATURES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }
            val signatures = @Suppress("DEPRECATION") packageInfo.signatures
            if (signatures != null && signatures.isNotEmpty()) {
                val md = java.security.MessageDigest.getInstance(algorithm)
                val publicKey = md.digest(signatures[0].toByteArray())
                formatBytesToFingerprint(publicKey)
            } else {
                context.getString(R.string.settings_signature_unavailable)
            }
        } catch (e: Exception) {
            context.getString(R.string.settings_signature_unavailable)
        }
    }

    private fun formatBytesToFingerprint(bytes: ByteArray): String {
        return bytes.joinToString(":") { String.format("%02X", it) }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * 1. الحفاظ على هذا المكوّن في طبقة العرض وعدم نقل قواعد العمل الحساسة إليه؛ القرار النهائي يجب أن يبقى في ViewModel/Domain.
 * 2. إضافة اختبارات UI للحالات الأساسية وحالات الخطأ والحدود دون تغيير السلوك الحالي.
 * 3. مراجعة الوصولية واتساق Material 3 عند اختلاف أحجام الشاشات والوضعين الفاتح والداكن.
 * 4. يفضل لاحقاً توفير اختبارات ثابتة لمدخلات معروفة ومخرجات متوقعة لضمان عدم تغير الخوارزمية.
 */
