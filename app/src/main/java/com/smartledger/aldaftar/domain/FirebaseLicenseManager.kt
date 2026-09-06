/**
 * =====================================================================
 * ملف: مدير تراخيص وتفعيل التطبيق عبر Firebase (FirebaseLicenseManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المحرك السحابي للتحقق من تراخيص التطبيق وإدارتها عبر قاعدة
 * بيانات Google Firestore السحابية. يتيح للمستخدمين تفعيل النسخة المدفوعة/المميزة
 * بواسطة البريد الإلكتروني، ويطبق خوارزمية ذكية لإدارة الأجهزة المتعددة عبر طابور
 * الإخراج التناوبي (FIFO: First-In-First-Out)، بالإضافة إلى المراقبة اللحظية
 * للتراخيص لرصد عمليات الإلغاء أو نقل التفعيل لأجهزة أخرى فور حدوثها.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. نمذجة نتائج فحص الترخيص (License Result Modeling):
 *    - استخدام [LicenseState] و [LicenseCheckResult] لتصنيف حالات الترخيص بوضوح.
 * 2. التفعيل السحابي المعاملاتي (Transactional Cloud Activation):
 *    - استخدام معاملات Firestore [runTransaction] لضمان اتساق تسجيل الأجهزة ومنع التضارب.
 * 3. خوارزمية طابور الأجهزة المتعددة (FIFO Device Management):
 *    - السماح بعدد محدد من الأجهزة المصرحة [devices_max]، وطرد الجهاز الأقدم تلقائياً
 *      عند تفعيل جهاز جديد يتجاوز الحد الأقصى.
 * 4. المراقبة اللحظية للتراخيص وإدارة دورة الحياة الآمنة (Lifecycle-Safe Listener):
 *    - مزامنة كائن [ListenerRegistration] مع إلغاء آمن لمنع تسريب الذاكرة أو مضاعفة المستمعين.
 * 5. مبدأ سلامة الترخيص دون اتصال (Offline-Safe License Resilience):
 *    - القاعدة الإلزامية: انقطاع الشبكة ليس إلغاءً للترخيص (NETWORK OUTAGE != LICENSE REVOCATION).
 *    - عند انقطاع الإنترنت أو الخطأ المؤقت يتم الاحتفاظ بالكاش المحلي المشفر دون مسحه أو تعطيل التطبيق.
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، وسجلات النظام، وموارد التطبيق، ومكتبات Firebase
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =========================================================================
// قسم: نمذجة حالات ونتائج التحقق من الترخيص (SEALED RESULT CLASS)
// =========================================================================

/**
 * [فئة نتائج فحص وتفعيل الترخيص - LicenseCheckResult]:
 * فئة مغلقة (Sealed Class) تمثل كافة الاحتمالات الممكنة لعملية التحقق من الترخيص السحابي.
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
// قسم: الكائن الأحادي لمدير التراخيص السحابي (FIREBASE LICENSE MANAGER)
// =========================================================================

/**
 * [الكائن الأحادي لإدارة تراخيص Firebase - FirebaseLicenseManager]:
 * يحتوي على منطق الاتصال بالسحابة والتحقق اللحظي وإدارة وثائق الترخيص في Firestore.
 */
object FirebaseLicenseManager {

    /** وسم السجلات التشخيصية */
    private const val TAG = "FirebaseLicenseManager"
    /** اسم مجموعة وثائق التراخيص في Firestore */
    private const val COLLECTION_LICENSES = "licenses"
    
    /** قفل التزامن لإدارة المستمع اللحظي بأمان خيطي كامل */
    private val listenerLock = Any()
    /** كائن مراقبة التغييرات اللحظية في الوثيقة السحابية */
    private var licenseListenerRegistration: ListenerRegistration? = null

    /**
     * [تنظيف وتوحيد البريد الإلكتروني - normalizeEmail]:
     * إزالة الفراغات الزائدة وتحويل الأحرف لصغيرة لتجنب أخطاء المطابقة.
     */
    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    /**
     * [التحقق من حالة المصادقة - ensureAuthenticated]:
     * فحص أولي للمستخدم المسجل في Firebase لضمان توفر الصلاحيات الأمنية.
     */
    private fun ensureAuthenticated() {
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                Log.d(TAG, "Current Firebase user is unauthenticated; requests will proceed with default credentials")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firebase auth state check notice: ${t.message}")
        }
    }

    /**
     * [التحقق من الترخيص والتفعيل - verifyAndActivateEmail]:
     * الدالة العامة لتفعيل البريد وتفويض منطق الطابور التناوبي (FIFO).
     */
    suspend fun verifyAndActivateEmail(context: Context, email: String, currentDeviceId: String): LicenseCheckResult {
        return verifyAndActivateEmailWithFifo(context, email, currentDeviceId)
    }

    /**
     * [التحقق والتفعيل بنظام طابور الأجهزة التناوبي - verifyAndActivateEmailWithFifo]:
     * ينفذ معاملة ذرية (Atomic Transaction) على Firestore للتحقق من صلاحية البريد،
     * وإضافة الجهاز الحالي لقائمة الأجهزة النشطة مع طرد الجهاز الأقدم إن تم بلوغ الحد الأقصى.
     *
     * @param context سياق التطبيق للوصول لنصوص الخطأ المترجمة.
     * @param email البريد الإلكتروني للمستخدم.
     * @param currentDeviceId البصمة الموحدة للجهاز الحالي.
     * @return نتيجة العملية من نوع [LicenseCheckResult].
     */
    suspend fun verifyAndActivateEmailWithFifo(context: Context, email: String, currentDeviceId: String): LicenseCheckResult {
        val cleanEmail = normalizeEmail(email)
        // التحقق من صحة صياغة البريد الإلكتروني
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return LicenseCheckResult.Error(context.getString(R.string.licensing_error_invalid_email))
        }

        return try {
            ensureAuthenticated()
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

                    // تطبيق خوارزمية FIFO: إزالة أقدم جهاز مسجل عند امتلاء السعة
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
            Log.w(TAG, "Network outage during email license verification: ${e.message}")
            LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore exception (${e.code}): ${e.message}")
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
            } else {
                LicenseCheckResult.Error(e.localizedMessage ?: "Firestore error")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error verifying email license safely: ${t.javaClass.simpleName}", t)
            LicenseCheckResult.Error(context.getString(R.string.licensing_error_not_registered))
        }
    }

    // =========================================================================
    // قسم: المراقبة اللحظية للترخيص (REALTIME MONITORING)
    // =========================================================================

    /**
     * [بدء المراقبة اللحظية لصلاحية الترخيص - startRealtimeLicenseMonitoring]:
     * يربط مستمع لحظي مع وثيقة المستخدم في Firestore للتنبه فور قيام المشرف بتعطيل الحساب
     * أو في حال تم طرد الجهاز الحالي بواسطة جهاز آخر جديد (FIFO Ejection).
     *
     * @param context سياق التطبيق للوصول للنصوص.
     * @param email بريد المستخدم المرخص.
     * @param currentDeviceId بصمة الجهاز الحالي.
     * @param onKickedOrDisabled دالة رد النداء عند إلغاء الصلاحية أو طرد الجهاز.
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
                    Log.w(TAG, "Realtime listener error notice (${error.code}): ${error.message}")
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
                        Log.w(TAG, "Account disabled remotely by Admin.")
                        onKickedOrDisabled(context.getString(R.string.licensing_error_account_disabled))
                    } else if (!isDeviceAuthorized && (activeDevices.isNotEmpty() || legacyActiveDevice.isNotEmpty())) {
                        Log.w(TAG, "Device kicked out due to multi-device FIFO limit or unlinking.")
                        onKickedOrDisabled(context.getString(R.string.licensing_device_kicked))
                    }
                } else if (snapshot != null && !snapshot.exists()) {
                    Log.w(TAG, "License document deleted.")
                    onKickedOrDisabled(context.getString(R.string.licensing_license_deleted))
                }
            }
        }
    }

    /**
     * [إيقاف المراقبة اللحظية للترخيص - stopRealtimeLicenseMonitoring]:
     * إلغاء تسجيل المستمع اللحظي لتحرير الموارد بأمان خيطي عند إغلاق الشاشة أو تسجيل الخروج.
     */
    fun stopRealtimeLicenseMonitoring() {
        synchronized(listenerLock) {
            licenseListenerRegistration?.remove()
            licenseListenerRegistration = null
        }
    }

    // =========================================================================
    // قسم: إلغاء الربط ومزامنة التراخيص (UNLINKING & SYNC)
    // =========================================================================

    /**
     * [إلغاء ربط الجهاز بالترخيص - unlinkDevice]:
     * يمسح معرفات الأجهزة النشطة من الوثيقة السحابية لإتاحة تفعيل أجهزة جديدة.
     *
     * @param email بريد الحساب المرخص.
     * @return true إذا تم إلغاء الربط بنجاح، وإلا false.
     */
    suspend fun unlinkDevice(email: String): Boolean {
        val cleanEmail = normalizeEmail(email)
        if (cleanEmail.isEmpty()) return false
        return try {
            ensureAuthenticated()
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
            Log.e(TAG, "Error unlinking device safely: ${t.javaClass.simpleName}")
            false
        }
    }

    /**
     * [مزامنة والتحقق من الترخيص المحلي - syncAndVerifyLocalEmailLicense]:
     * يفحص مطابقة الترخيص السحابي مع الذاكرة المشفرة على الجهاز الحالي،
     * مع توفير آلية التراجع الآمن للعمل دون اتصال بالاعتماد على الكاش المحلي.
     *
     * @param context سياق التطبيق للوصول لمدير الأمان وبصمة الجهاز.
     * @return true إذا كان التطبيق مرخصاً ومصرحاً له بالعمل، وإلا false.
     */
    suspend fun syncAndVerifyLocalEmailLicense(context: Context): Boolean {
        val securityManager = AppSecurityManager.getInstance(context)
        val email = securityManager.getActivatedEmail()
        if (email.isBlank()) return false

        val currentDeviceId = LicenseManager.getOrGenerateUnifiedDeviceId(context)

        return try {
            ensureAuthenticated()
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
                    // الترخيص صالح على Firebase لهذا الجهاز: مزامنة وحفظ التفعيل المحلي المشفر
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
            Log.w(TAG, "Sync email license check failed safely (offline or transient exception): ${t.message}")
            // التراجع الآمن عند انقطاع الإنترنت: الوثوق بالكاش المشفر المحلي إن تطابقت بصمة الجهاز
            // NETWORK OUTAGE != LICENSE REVOCATION
            val cachedIsActivated = securityManager.isActivatedCached()
            val cachedForDevice = securityManager.getCachedDeviceId()
            cachedIsActivated && (cachedForDevice == currentDeviceId || cachedForDevice.isBlank())
        }
    }
}
