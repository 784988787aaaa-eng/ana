/**
 * =====================================================================
 * ملف: مدير تراخيص وتفعيل التطبيق عبر  (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المحرك السحابي للتحقق من تراخيص التطبيق وإدارتها عبر قاعدة
 * بيانات   السحابية. يتيح للمستخدمين تفعيل النسخة المدفوعة/المميزة
 * بواسطة البريد الإلكتروني، ويطبق خوارزمية ذكية لإدارة الأجهزة المتعددة عبر طابور
 * الإخراج التناوبي (: ---)، بالإضافة إلى المراقبة اللحظية
 * للتراخيص لرصد عمليات الإلغاء أو نقل التفعيل لأجهزة أخرى فور حدوثها.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. نمذجة نتائج فحص الترخيص (  ):
 *    - استخدام [] و [] لتصنيف حالات الترخيص بوضوح.
 * 2. التفعيل السحابي المعاملاتي (  ):
 *    - استخدام معاملات  [] لضمان اتساق تسجيل الأجهزة ومنع التضارب.
 * 3. خوارزمية طابور الأجهزة المتعددة (  ):
 *    - السماح بعدد محدد من الأجهزة المصرحة [_]، وطرد الجهاز الأقدم تلقائياً
 *      عند تفعيل جهاز جديد يتجاوز الحد الأقصى.
 * 4. المراقبة اللحظية للتراخيص وإدارة دورة الحياة الآمنة (- ):
 *    - مزامنة كائن [] مع إلغاء آمن لمنع تسريب الذاكرة أو مضاعفة المستمعين.
 * 5. مبدأ سلامة الترخيص دون اتصال (-  ):
 *    - القاعدة الإلزامية: انقطاع الشبكة ليس إلغاءً للترخيص (  !=  ).
 *    - عند انقطاع الإنترنت أو الخطأ المؤقت يتم الاحتفاظ بالكاش المحلي المشفر دون مسحه أو تعطيل التطبيق.
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، وسجلات النظام، وموارد التطبيق، ومكتبات 
// ---------------------------------------------------------------------
import android.content.Context
import com.smartledger.aldaftar.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =========================================================================
// قسم: نمذجة حالات ونتائج التحقق من الترخيص (  )
// =========================================================================

/**
 * [فئة نتائج فحص وتفعيل الترخيص - ]:
 * فئة مغلقة ( ) تمثل كافة الاحتمالات الممكنة لعملية التحقق من الترخيص السحابي.
 */
sealed class LicenseCheckResult {
    /** نجاح التحقق وربط الجهاز بالترخيص السحابي (مع بيان ما إذا تم نقله من جهاز آخر) */
    data class Success(val email: String, val deviceId: String, val isTransferred: Boolean = false) : LicenseCheckResult()
    /** عدم تطابق الجهاز الحالي مع الأجهزة المسجلة في الوثيقة السحابية */
    data class DeviceMismatch(val email: String, val activeDeviceId: String, val currentDeviceId: String) : LicenseCheckResult()
    /** الحساب غير مسجل كمرخص أو تم تعطيله من قبل الإدارة */
    data class NotLicensed(val email: String, val message: String) : LicenseCheckResult()
    /** انقطاع الشبكة المؤقت مع الحفاظ على التفعيل المحلي */
    data class NetworkOutage(val message: String) : LicenseCheckResult()
    /** حدوث خطأ في الشبكة أو في صحة البريد الإلكتروني المدخل */
    data class Error(val message: String) : LicenseCheckResult()
}

// =========================================================================
// قسم: الكائن الأحادي لمدير التراخيص السحابي (  )
// =========================================================================

/**
 * [الكائن الأحادي لإدارة تراخيص  - ]:
 * يحتوي على منطق الاتصال بالسحابة والتحقق اللحظي وإدارة وثائق الترخيص في .
 */
object FirebaseLicenseManager {

    /** وسم السجلات التشخيصية */
    /** اسم مجموعة وثائق التراخيص في  */
    private const val COLLECTION_LICENSES = "licenses"
    
    /** قفل التزامن لإدارة المستمع اللحظي بأمان خيطي كامل */
    private val listenerLock = Any()
    /** كائن مراقبة التغييرات اللحظية في الوثيقة السحابية */
    private var licenseListenerRegistration: ListenerRegistration? = null

    /**
     * [تنظيف وتوحيد البريد الإلكتروني - ]:
     * إزالة الفراغات الزائدة وتحويل الأحرف لصغيرة لتجنب أخطاء المطابقة.
     */
    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    /**
     * [التحقق من الترخيص والتفعيل - ]:
     * الدالة العامة لتفعيل البريد وتفويض منطق الطابور التناوبي ().
     */
    suspend fun verifyAndActivateEmail(context: Context, email: String, currentDeviceId: String): LicenseCheckResult {
        return verifyAndActivateEmailWithFifo(context, email, currentDeviceId)
    }

    /**
     * [التحقق والتفعيل بنظام طابور الأجهزة التناوبي - ]:
     * ينفذ معاملة ذرية ( ) على  للتحقق من صلاحية البريد،
     * وإضافة الجهاز الحالي لقائمة الأجهزة النشطة مع طرد الجهاز الأقدم إن تم بلوغ الحد الأقصى.
     *
     * @  سياق التطبيق للوصول لنصوص الخطأ المترجمة.
     * @  البريد الإلكتروني للمستخدم.
     * @  البصمة الموحدة للجهاز الحالي.
     * @ نتيجة العملية من نوع [].
     */
    suspend fun verifyAndActivateEmailWithFifo(context: Context, email: String, currentDeviceId: String): LicenseCheckResult {
        val cleanEmail = normalizeEmail(email)
        // التحقق من صحة صياغة البريد الإلكتروني
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return LicenseCheckResult.Error(context.getString(R.string.licensing_error_invalid_email))
        }

        return try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(COLLECTION_LICENSES).document(cleanEmail)

            var isTransferred = false

            // تنفيذ العملية داخل معاملة لضمان التزامن الذري
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)

                // إذا لم تكن وثيقة الترخيص موجودة
                if (!snapshot.exists()) {
                    throw IllegalStateException("NOT_REGISTERED")
                }

                // التحقق من أن الحساب نشط وغير معطل من قبل المشرف
                val isActivated = snapshot.getBoolean("is_activated") ?: false
                if (!isActivated) {
                    throw IllegalStateException("ACCOUNT_DISABLED")
                }

                // قراءة الحد الأقصى للأجهزة المسموح بها (افتراضياً جهاز واحد)
                val devicesMax = (snapshot.getLong("devices_max") ?: 1L).toInt().coerceAtLeast(1)
                @Suppress("UNCHECKED_CAST")
                val activeDevices = (snapshot.get("active_devices") as? List<String>)?.toMutableList()
                    ?: mutableListOf()

                val legacyActiveDevice = snapshot.getString("active_device_id") ?: ""
                if (activeDevices.isEmpty() && legacyActiveDevice.isNotEmpty()) {
                    activeDevices.add(legacyActiveDevice)
                }

                // إذا لم يكن الجهاز الحالي مسجلاً بالفعل في القائمة
                if (!activeDevices.contains(currentDeviceId)) {
                    isTransferred = activeDevices.isNotEmpty()

                    // تطبيق خوارزمية : إزالة أقدم جهاز مسجل عند امتلاء السعة
                    while (activeDevices.size >= devicesMax && activeDevices.isNotEmpty()) {
                        activeDevices.removeAt(0)
                    }
                    activeDevices.add(currentDeviceId)
                }

                // تحديث بيانات الوثيقة السحابية
                val updates = mapOf(
                    "email" to cleanEmail,
                    "is_activated" to true,
                    "devices_max" to devicesMax,
                    "active_devices" to activeDevices,
                    "active_device_id" to currentDeviceId, // للتوافقية مع الإصدارات السابقة
                    "last_updated" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                )

                transaction.set(docRef, updates, SetOptions.merge())
            }.await()

            LicenseCheckResult.Success(email = cleanEmail, deviceId = currentDeviceId, isTransferred = isTransferred)
        } catch (e: IllegalStateException) {
            when (e.message) {
                "ACCOUNT_DISABLED" -> LicenseCheckResult.NotLicensed(cleanEmail, context.getString(R.string.licensing_error_account_disabled))
                else -> LicenseCheckResult.NotLicensed(cleanEmail, context.getString(R.string.licensing_error_not_registered))
            }
        } catch (e: FirebaseNetworkException) {

            LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
        } catch (e: FirebaseFirestoreException) {

            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
            } else {
                LicenseCheckResult.Error(context.getString(R.string.licensing_error_not_registered))
            }
        } catch (t: Throwable) {

            LicenseCheckResult.Error(context.getString(R.string.licensing_error_not_registered))
        }
    }

    // =========================================================================
    // قسم: المراقبة اللحظية للترخيص ( )
    // =========================================================================

    /**
     * [بدء المراقبة اللحظية لصلاحية الترخيص - ]:
     * يربط مستمع لحظي مع وثيقة المستخدم في  للتنبه فور قيام المشرف بتعطيل الحساب
     * أو في حال تم طرد الجهاز الحالي بواسطة جهاز آخر جديد ( ).
     *
     * @  سياق التطبيق للوصول للنصوص.
     * @  بريد المستخدم المرخص.
     * @  بصمة الجهاز الحالي.
     * @  دالة رد النداء عند إلغاء الصلاحية أو طرد الجهاز.
     */
    fun startRealtimeLicenseMonitoring(
        context: Context,
        email: String,
        currentDeviceId: String,
        onKickedOrDisabled: (reason: String) -> Unit
    ) {
        val cleanEmail = normalizeEmail(email)
        if (cleanEmail.isEmpty()) return

        synchronized(listenerLock) {
            // إيقاف أي مستمع نشط سابقاً لمنع تكرار المستمعين
            stopRealtimeLicenseMonitoring()

            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(COLLECTION_LICENSES).document(cleanEmail)

            // تسجيل مستمع اللقطات اللحظية
            licenseListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // أخطاء الشبكة المؤقتة في المستمع لا تؤدي لطرد المستخدم

                    return@addSnapshotListener
                }

                val securityManager = AppSecurityManager.getInstance(context.applicationContext)
                if (!securityManager.isActivatedCached()) {
                    // لا يتم طرد أي جهاز إلا إذا كان مفعلاً ومسجلاً رسمياً بالترخيص
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val isActivated = snapshot.getBoolean("is_activated") ?: false
                    @Suppress("UNCHECKED_CAST")
                    val activeDevices = snapshot.get("active_devices") as? List<String> ?: emptyList()
                    val legacyActiveDevice = snapshot.getString("active_device_id") ?: ""

                    val isDeviceAuthorized = activeDevices.contains(currentDeviceId) || legacyActiveDevice == currentDeviceId

                    if (!isActivated) {

                        onKickedOrDisabled(context.getString(R.string.licensing_error_account_disabled))
                    } else if (!isDeviceAuthorized && (activeDevices.isNotEmpty() || legacyActiveDevice.isNotEmpty())) {

                        onKickedOrDisabled(context.getString(R.string.licensing_device_kicked))
                    }
                } else if (snapshot != null && !snapshot.exists()) {

                    onKickedOrDisabled(context.getString(R.string.licensing_license_deleted))
                }
            }
        }
    }

    /**
     * [إيقاف المراقبة اللحظية للترخيص - ]:
     * إلغاء تسجيل المستمع اللحظي لتحرير الموارد بأمان خيطي عند إغلاق الشاشة أو تسجيل الخروج.
     */
    fun stopRealtimeLicenseMonitoring() {
        synchronized(listenerLock) {
            licenseListenerRegistration?.remove()
            licenseListenerRegistration = null
        }
    }

    // =========================================================================
    // قسم: إلغاء الربط ومزامنة التراخيص ( & )
    // =========================================================================

    /**
     * [إلغاء ربط الجهاز بالترخيص - ]:
     * يمسح معرفات الأجهزة النشطة من الوثيقة السحابية لإتاحة تفعيل أجهزة جديدة.
     *
     * @  بريد الحساب المرخص.
     * @  إذا تم إلغاء الربط بنجاح، وإلا .
     */
    suspend fun unlinkDevice(email: String): Boolean {
        val cleanEmail = normalizeEmail(email)
        if (cleanEmail.isEmpty()) return false
        return try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(COLLECTION_LICENSES).document(cleanEmail)

            val updates = mapOf<String, Any>(
                "active_device_id" to "",
                "active_devices" to emptyList<String>(),
                "last_updated" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            docRef.set(updates, SetOptions.merge()).await()
            true
        } catch (t: Throwable) {

            false
        }
    }

    /**
     * [مزامنة والتحقق من الترخيص المحلي - ]:
     * يفحص مطابقة الترخيص السحابي مع الذاكرة المشفرة على الجهاز الحالي،
     * مع توفير آلية التراجع الآمن للعمل دون اتصال بالاعتماد على الكاش المحلي.
     *
     * @  سياق التطبيق للوصول لمدير الأمان وبصمة الجهاز.
     * @  إذا كان التطبيق مرخصاً ومصرحاً له بالعمل، وإلا .
     */
    suspend fun syncAndVerifyLocalEmailLicense(context: Context): Boolean {
        val securityManager = AppSecurityManager.getInstance(context)
        val email = securityManager.getActivatedEmail()
        if (email.isBlank()) return false

        val currentDeviceId = LicenseManager.getOrGenerateUnifiedDeviceId(context)

        return try {
            val cleanEmail = normalizeEmail(email)
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection(COLLECTION_LICENSES).document(cleanEmail).get().await()

            if (snapshot != null && snapshot.exists()) {
                val isActivated = snapshot.getBoolean("is_activated") ?: false
                val activeDeviceId = snapshot.getString("active_device_id") ?: ""
                @Suppress("UNCHECKED_CAST")
                val activeDevices = snapshot.get("active_devices") as? List<String> ?: emptyList()

                val isAuthorized = activeDevices.contains(currentDeviceId) || activeDeviceId == currentDeviceId

                if (isActivated && isAuthorized) {
                    // الترخيص صالح على  لهذا الجهاز: مزامنة وحفظ التفعيل المحلي المشفر
                    securityManager.setCachedActivation(true, currentDeviceId)
                    true
                } else {
                    // الترخيص ملغى أو نُقل لجهاز آخر: مسح بيانات التفعيل المحلية
                    securityManager.clearActivationData()
                    false
                }
            } else {
                // الوثيقة غير موجودة أو حذفت
                securityManager.clearActivationData()
                false
            }
        } catch (t: Throwable) {

            // التراجع الآمن عند انقطاع الإنترنت: الوثوق بالكاش المشفر المحلي إن تطابقت بصمة الجهاز
            //   !=  
            val cachedIsActivated = securityManager.isActivatedCached()
            val cachedForDevice = securityManager.getCachedDeviceId()
            cachedIsActivated && (cachedForDevice == currentDeviceId || cachedForDevice.isBlank())
        }
    }
}
