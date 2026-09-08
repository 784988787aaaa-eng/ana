/**
 * =====================================================================
 * ملف: كيان إعدادات وتكوينات التطبيق (AppSettingsEntity.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكيان نموذج جدول `app_settings` في قاعدة بيانات Room، وهو المسؤول عن
 * حفظ التفضيلات العامة والإعدادات الأمنية والتكوينات المالية.
 * 
 * [الأنماط المعمارية والتوافقية]:
 * 1. نمط السجل الفردي (Singleton Row Pattern): يحتوي الجدول على صف واحد فقط ذي المعرف الثابت (`id = 1`).
 * 2. التوافق العكسي لقواعد البيانات (Schema Backward Compatibility):
 *    - تم الحفاظ على أعمدة أسعار الصرف التاريخية (`exchangeRateSar` و `exchangeRateUsd` و `exchangeRateYer`)
 *      لضمان عدم انهيار ترقية قواعد البيانات للمستخدمين القدامى.
 *    - يعتمد النظام الحديث على حقل `exchangeRatesJson` لإتاحة مصفوفة ديناميكية غير محدودة من العملات وأسعار التحويل.
 * 3. الأمان والتشفير (Security Subsystem):
 *    - يحتوي على بصمات التجزئة المشفرة للرمز السري وعبارة الاسترداد.
 */
package com.smartledger.aldaftar.data.local.entities

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room ودقة الحسابات العشرية BigDecimal
// ---------------------------------------------------------------------
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * [كائن الثوابت الافتراضية لقاعدة البيانات - DatabaseDefaults]:
 * يحدد القيم الأولية للنظام عند التشغيل لأول مرة.
 */
object DatabaseDefaults {
    /** رمز العملة الافتراضي (الريال اليمني) */
    const val DEFAULT_CURRENCY_SYMBOL = "ر.ي"
}

/**
 * [فئة بيانات إعدادات التطبيق - AppSettings]:
 * تمثل بنية الجدول الشامل لتفضيلات المستخدم والنظام.
 *
 * @property id المعرف الأساسي الثابت، قيمته دائماً 1 لضمان وجود صف واحد فقط.
 * @property currencySymbol رمز العملة الرئيسية المعروضة في الشاشات والتقارير.
 * @property schoolExpensesEnabled مفتاح تفعيل/تعطيل تبويب ومصاريف المدارس والتعليم.
 * @property themeMode وضع المظهر: 0 = تلقائي (النظام)، 1 = فاتح، 2 = داكن.
 * @property doubleCheckExit طلب تأكيد الخروج عند الضغط على زر الرجوع لتفادي الإغلاق العرضي.
 * @property isPasscodeEnabled حالة تفعيل القفل برمز الحماية والتطبيق.
 * @property passcodeHash التجزئة المشفرة (SHA-256) للرمز السري.
 * @property recoveryPhraseHash التجزئة المشفرة لعبارة استرداد الحساب.
 * @property recoveryHint تلميح تذكيري لكلمة المرور لمساعدة المستخدم عند نسيانها.
 * @property isFirstLaunch مؤشر التشغيل لأول مرة لعرض شاشات التهيئة والترحيب.
 * @property exchangeRateSar سعر صرف الريال السعودي (عمود تاريخي للتوافقية).
 * @property exchangeRateUsd سعر صرف الدولار الأمريكي (عمود تاريخي للتوافقية).
 * @property exchangeRateYer سعر صرف الريال اليمني (عمود تاريخي للتوافقية).
 * @property exchangeRatesJson قاموس أسعار الصرف المتعددة الحديث بصيغة نص JSON ديناميكي.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
    @ColumnInfo(name = "currencySymbol") val currencySymbol: String = DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL,
    @ColumnInfo(name = "schoolExpensesEnabled") val schoolExpensesEnabled: Boolean = true,
    @ColumnInfo(name = "themeMode") val themeMode: Int = 0, // 0 = Auto, 1 = Light, 2 = Dark
    @ColumnInfo(name = "doubleCheckExit") val doubleCheckExit: Boolean = true,
    @ColumnInfo(name = "isPasscodeEnabled") val isPasscodeEnabled: Boolean = false,
    @ColumnInfo(name = "passcodeHash") val passcodeHash: String? = null,
    @ColumnInfo(name = "recoveryPhraseHash") val recoveryPhraseHash: String? = null,
    @ColumnInfo(name = "recoveryHint") val recoveryHint: String? = null,
    @ColumnInfo(name = "isFirstLaunch") val isFirstLaunch: Boolean = true,
    @ColumnInfo(name = "exchangeRatesJson") val exchangeRatesJson: String = "{}"
) {
}

