package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * كيان الإعدادات العامة للتطبيق.
 * يحافظ على بنية جدول الإعدادات الحالية لضمان التوافق مع قواعد البيانات السابقة.
 * لا يغير هذا الملف أي عمود تاريخي أو نوع مخزن، لأن تغيير المخطط قد يمنع ترقية بيانات المستخدم.
 */
object DatabaseDefaults {
    /** يحدد رمز العملة الافتراضي عند إنشاء إعدادات التطبيق لأول مرة. */
    const val DEFAULT_CURRENCY_SYMBOL = "ر.ي"
}

/**
 * يمثل الصف الوحيد لإعدادات التطبيق.
 * القيم المالية القديمة محفوظة كما هي، بينما تُستخدم القيم العشرية المساعدة للقراءة الدقيقة.
 * الحقول الحساسة لا تُفسر هنا ولا تُفكك، بل تُمرر إلى طبقات الحماية المخصصة لها.
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    /** المعرف الثابت للصف الوحيد في جدول الإعدادات. */
    @PrimaryKey @ColumnInfo(name = "id") val id: Int = 1,
    /** رمز العملة الأساسية المستخدمة في العرض والحسابات المرتبطة بالإعدادات. */
    @ColumnInfo(name = "currencySymbol") val currencySymbol: String = DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL,
    /** يحدد إتاحة وحدة مصروفات المدارس والتعليم. */
    @ColumnInfo(name = "schoolExpensesEnabled") val schoolExpensesEnabled: Boolean = true,
    /** يحدد وضع المظهر: تلقائي أو فاتح أو داكن وفق القيم المعتمدة في التطبيق. */
    @ColumnInfo(name = "themeMode") val themeMode: Int = 0,
    /** يحدد طلب تأكيد الخروج لمنع الإغلاق العرضي. */
    @ColumnInfo(name = "doubleCheckExit") val doubleCheckExit: Boolean = true,
    /** يحدد تفعيل قفل التطبيق برمز الحماية. */
    @ColumnInfo(name = "isPasscodeEnabled") val isPasscodeEnabled: Boolean = false,
    /** تجزئة رمز الحماية المخزنة وفق آلية الحماية المخصصة للتطبيق. */
    @ColumnInfo(name = "passcodeHash") val passcodeHash: String? = null,
    /** تجزئة عبارة الاسترداد المستخدمة للتحقق من الاسترداد. */
    @ColumnInfo(name = "recoveryPhraseHash") val recoveryPhraseHash: String? = null,
    /** تلميح استرداد اختياري لا يمثل السر نفسه. */
    @ColumnInfo(name = "recoveryHint") val recoveryHint: String? = null,
    /** الجزء المؤقت من حالة الترخيص المحفوظة بالتوافق مع النظام القائم. */
    @ColumnInfo(name = "tempPart") val tempPart: String = "",
    /** الجزء الدائم من حالة الترخيص المحفوظة بالتوافق مع النظام القائم. */
    @ColumnInfo(name = "permPart") val permPart: String = "",
    /** المعرف الموحد المرتبط ببصمة الجهاز ضمن منظومة الترخيص. */
    @ColumnInfo(name = "unifiedDeviceId") val unifiedDeviceId: String = "",
    /** يحدد ما إذا كان التطبيق في حالة التشغيل الأول. */
    @ColumnInfo(name = "isFirstLaunch") val isFirstLaunch: Boolean = true,
    /** يحدد تفعيل النسخ الاحتياطي التلقائي. */
    @ColumnInfo(name = "isAutoBackupEnabled") val isAutoBackupEnabled: Boolean = true,
    /** يحدد تفعيل المزامنة السحابية التلقائية. */
    @ColumnInfo(name = "isCloudSyncEnabled") val isCloudSyncEnabled: Boolean = false,
    /** سعر الصرف التاريخي الأول المحفوظ كما هو لضمان توافق الإصدارات السابقة. */
    @ColumnInfo(name = "exchangeRateSar") val exchangeRateSar: Double = 1.0,
    /** سعر الصرف التاريخي الثاني المحفوظ كما هو لضمان توافق الإصدارات السابقة. */
    @ColumnInfo(name = "exchangeRateUsd") val exchangeRateUsd: Double = 1.0,
    /** سعر الصرف التاريخي الثالث المحفوظ كما هو لضمان توافق الإصدارات السابقة. */
    @ColumnInfo(name = "exchangeRateYer") val exchangeRateYer: Double = 1.0,
    /** تمثيل أسعار الصرف الحديثة النصية دون فرض تغيير على الأعمدة التاريخية. */
    @ColumnInfo(name = "exchangeRatesJson") val exchangeRatesJson: String = "{}"
) {
    /** يحول سعر الصرف التاريخي إلى قيمة عشرية دون استخدام تحويل نصي تقريبي. */
    val exchangeRateSarBigDecimal: BigDecimal get() = BigDecimal.valueOf(exchangeRateSar)
    /** يحول سعر الصرف التاريخي إلى قيمة عشرية دون استخدام تحويل نصي تقريبي. */
    val exchangeRateUsdBigDecimal: BigDecimal get() = BigDecimal.valueOf(exchangeRateUsd)
    /** يحول سعر الصرف التاريخي إلى قيمة عشرية دون استخدام تحويل نصي تقريبي. */
    val exchangeRateYerBigDecimal: BigDecimal get() = BigDecimal.valueOf(exchangeRateYer)
}
