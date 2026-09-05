/**
 * =====================================================================
 * ملف: مدير جلسة مصادقة حساب جوجل الموحد (GoogleAuthSessionManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المصدر المركزي الوحيد للحقيقة (Single Source of Truth) لحالة
 * جلسة حساب Google للمستخدم في جميع أنحاء التطبيق.
 * يضمن مزامنة لحظية وفورية لحالة تسجيل الدخول أو الخروج بين مختلف الشاشات
 * (شاشة التفعيل والترخيص، شاشة إعدادات النسخ الاحتياطي السحابي، وشاشة إدارة الحساب)
 * عبر تدفقات الحالة التفاعلية [StateFlow].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. إدارة تدفق حالة البريد الإلكتروني المتفاعل (Reactive Session StateFlow):
 *    - بث البريد الإلكتروني المسجل لحظياً لكافة واجهات المستخدم ومراقبي الحالة.
 * 2. التهيئة التلقائية من مستودع المصادقة (Auth Persistence Sync):
 *    - استرجاع البريد الإلكتروني المحفوظ مسبقاً من [GoogleDriveAuthManager] عند إقلاع التطبيق.
 * 3. التحديث الموحد للجلسة (Unified Session Mutation):
 *    - تنظيف وتوحيد البريد عند تسجيل الدخول، ومسح الجلسة فوراً عند تسجيل الخروج.
 */
package com.smartledger.aldaftar.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.GoogleDriveAuthManager
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [حالات جلسة مصادقة حساب Google الموحدة - GoogleAuthState]
 */
sealed class GoogleAuthState {
    object SignedOut : GoogleAuthState()
    object SigningIn : GoogleAuthState()
    data class Authenticated(val email: String) : GoogleAuthState()
    data class AuthFailed(val errorMessage: String, val statusCode: Int? = null) : GoogleAuthState()
}

/**
 * [نتيجة معالجة تسجيل الدخول الموحدة - GoogleSignInOutcome]
 */
sealed class GoogleSignInOutcome {
    data class Success(
        val email: String,
        val serverAuthCode: String?,
        val isDriveAuthorized: Boolean
    ) : GoogleSignInOutcome()

    object Cancelled : GoogleSignInOutcome()

    data class Failed(
        val message: String,
        val statusCode: Int? = null
    ) : GoogleSignInOutcome()
}

/**
 * [الكائن الأحادي لمدير جلسة حساب جوجل - GoogleAuthSessionManager]:
 * يوفر مصدر حقيقة مركزي وموحد لحالة المصادقة بحساب Google عبر كامل التطبيق.
 */
object GoogleAuthSessionManager {

    /** وسم السجلات التشخيصية لجلسة المصادقة */
    private const val TAG = "GoogleAuthSession"

    private val _sessionState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.SignedOut)
    val sessionState: StateFlow<GoogleAuthState> = _sessionState.asStateFlow()

    /** تدفق الحالة الداخلي القابل للتعديل للبريد الإلكتروني الحالي */
    private val _currentEmail = MutableStateFlow<String?>(null)

    /**
     * تدفق الحالة العام المتاح للقراءة فقط (Read-Only StateFlow):
     * تراقبه واجهات Jetpack Compose لإعادة رسم المكونات فور تغير حساب المستخدم.
     */
    val currentEmail: StateFlow<String?> = _currentEmail.asStateFlow()

    /**
     * [تهيئة جلسة المصادقة عند بدء التطبيق - initialize]:
     * يسترجع البريد المحفوظ في تفضيلات أمان جوجل درايف ويحدث الحالة الموحدة.
     *
     * @param context سياق التطبيق للوصول للتخزين المحلي.
     */
    fun initialize(context: Context) {
        try {
            val authManager = GoogleDriveAuthManager(context)
            val email = authManager.getStoredEmail()
            val cleanEmail = email?.trim()?.lowercase()
            if (!cleanEmail.isNullOrBlank()) {
                _currentEmail.value = cleanEmail
                _sessionState.value = GoogleAuthState.Authenticated(cleanEmail)
            } else {
                _currentEmail.value = null
                _sessionState.value = GoogleAuthState.SignedOut
            }
            Log.d(TAG, "Initialized unified Google Auth Session with email: ${_currentEmail.value != null}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing GoogleAuthSessionManager", t)
        }
    }

    /**
     * [تعيين حالة بدء تسجيل الدخول - setSigningIn]
     */
    fun setSigningIn() {
        _sessionState.value = GoogleAuthState.SigningIn
        Log.d(TAG, "Google auth session state: SigningIn")
    }

    /**
     * [تحديث البريد الإلكتروني للجلسة - updateEmail]:
     * يقوم بتنظيف وتوحيد حالة الأحرف للبريد وتحديث التدفق التفاعلي.
     *
     * @param email البريد الإلكتروني الجديد لحساب Google أو null.
     */
    fun updateEmail(email: String?) {
        val cleanEmail = email?.trim()?.lowercase()
        if (!cleanEmail.isNullOrBlank()) {
            _currentEmail.value = cleanEmail
            _sessionState.value = GoogleAuthState.Authenticated(cleanEmail)
        } else {
            _currentEmail.value = null
            _sessionState.value = GoogleAuthState.SignedOut
        }
        Log.d(TAG, "Unified Google Auth Session email updated: ${_currentEmail.value != null}")
    }

    /**
     * [تعيين حالة فشل المصادقة - setAuthFailed]
     */
    fun setAuthFailed(message: String, statusCode: Int? = null) {
        _sessionState.value = GoogleAuthState.AuthFailed(message, statusCode)
        Log.w(TAG, "Google auth session failed: statusCode=$statusCode, message=$message")
    }

    /**
     * [مسح جلسة المصادقة بالكامل - clearSession]:
     * يعيد البريد الإلكتروني إلى null عند تسجيل الخروج لإشعار كافة شاشات التطبيق فوراً.
     */
    fun clearSession() {
        _currentEmail.value = null
        _sessionState.value = GoogleAuthState.SignedOut
        Log.d(TAG, "Unified Google Auth Session cleared.")
    }

    /**
     * [التحقق من صحة الجلسة الحالية - isSessionValid]
     */
    fun isSessionValid(): Boolean {
        return !_currentEmail.value.isNullOrBlank() && _sessionState.value is GoogleAuthState.Authenticated
    }

    /**
     * [المعالج المركزي لنتيجة تسجيل الدخول - handleSignInActivityResult]:
     * Facade موحد يعالج نتيجة ActivityResult لـ Google Sign-In:
     * 1. استخراج GoogleSignInAccount بأمان وتدقيق وجود الحساب والبريد ورمز التفويض.
     * 2. تشخيص آمن يمنع تسريب أي رموز حساسة أو توكنات.
     * 3. التمييز الدقيق بين الإلغاء الفعلي وأخطاء التهيئة والشبكة.
     * 4. تمرير authCode إلى المسار المركزي لتبادل التوكنات لـ Google Drive عند توفره.
     * 5. تحديث الجلسة الموحدة لإشعار كافة واجهات التطبيق في آن واحد.
     */
    fun handleSignInActivityResult(
        resultCode: Int,
        data: Intent?,
        context: Context,
        backupSyncViewModel: BackupSyncViewModel?,
        onOutcome: (GoogleSignInOutcome) -> Unit
    ) {
        Log.d(TAG, "handleSignInActivityResult: resultCode=$resultCode, hasIntentData=${data != null}")

        if (resultCode == Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email?.trim()?.lowercase() ?: ""
                val authCode = account?.serverAuthCode

                // تسجيل تشخيصي آمن دون طباعة أي توكن أو كود
                Log.i(
                    TAG,
                    "GoogleSignIn resultCode=RESULT_OK, account=${account != null}, email=${email.isNotEmpty()}, serverAuthCode=${!authCode.isNullOrEmpty()}"
                )

                if (email.isNotEmpty()) {
                    updateEmail(email)
                }

                if (!authCode.isNullOrEmpty() && backupSyncViewModel != null) {
                    backupSyncViewModel.handleGoogleOAuthCode(authCode, email) { driveSuccess ->
                        Log.i(TAG, "Google OAuth server code exchange result: driveSuccess=$driveSuccess")
                        onOutcome(
                            GoogleSignInOutcome.Success(
                                email = email,
                                serverAuthCode = authCode,
                                isDriveAuthorized = driveSuccess
                            )
                        )
                    }
                } else if (email.isNotEmpty()) {
                    backupSyncViewModel?.googleDriveSyncHelper?.storeEmail(email)
                    Log.w(TAG, "GoogleSignIn account obtained, but serverAuthCode is null")
                    onOutcome(
                        GoogleSignInOutcome.Success(
                            email = email,
                            serverAuthCode = null,
                            isDriveAuthorized = false
                        )
                    )
                } else {
                    val msg = context.getString(R.string.backup_toast_connect_failed)
                    setAuthFailed(msg)
                    onOutcome(GoogleSignInOutcome.Failed(msg))
                }
            } catch (e: Exception) {
                processSignInException(e, resultCode, data, context, onOutcome)
            }
        } else {
            if (data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val email = account?.email?.trim()?.lowercase() ?: ""
                    val authCode = account?.serverAuthCode
                    if (email.isNotEmpty()) {
                        updateEmail(email)
                        onOutcome(
                            GoogleSignInOutcome.Success(
                                email = email,
                                serverAuthCode = authCode,
                                isDriveAuthorized = false
                            )
                        )
                    } else {
                        val msg = context.getString(R.string.backup_toast_connect_failed)
                        setAuthFailed(msg)
                        onOutcome(GoogleSignInOutcome.Failed(msg))
                    }
                } catch (e: Exception) {
                    processSignInException(e, resultCode, data, context, onOutcome)
                }
            } else {
                Log.i(TAG, "GoogleSignIn cancelled: resultCode=$resultCode, data=null")
                restoreSessionOrSignedOut()
                onOutcome(GoogleSignInOutcome.Cancelled)
            }
        }
    }

    private fun restoreSessionOrSignedOut() {
        val current = _currentEmail.value
        if (!current.isNullOrBlank()) {
            _sessionState.value = GoogleAuthState.Authenticated(current)
        } else {
            _sessionState.value = GoogleAuthState.SignedOut
        }
    }

    private fun processSignInException(
        e: Exception,
        resultCode: Int,
        data: Intent?,
        context: Context,
        onOutcome: (GoogleSignInOutcome) -> Unit
    ) {
        if (e is ApiException) {
            val sc = e.statusCode
            Log.w(TAG, "GoogleSignIn failed: statusCode=$sc, resultCode=$resultCode")

            val isExplicitCancel = (sc == GoogleSignInStatusCodes.SIGN_IN_CANCELLED || sc == 16) && resultCode == Activity.RESULT_CANCELED

            if (isExplicitCancel) {
                Log.i(TAG, "GoogleSignIn confirmed explicit cancellation by user (statusCode=$sc)")
                restoreSessionOrSignedOut()
                onOutcome(GoogleSignInOutcome.Cancelled)
            } else {
                val userErrorMessage = when (sc) {
                    CommonStatusCodes.NETWORK_ERROR -> context.getString(R.string.settings_gdrive_link_failed_network)
                    CommonStatusCodes.DEVELOPER_ERROR -> context.getString(R.string.settings_gdrive_link_failed_api_code_pattern, sc)
                    else -> context.getString(R.string.settings_gdrive_link_failed_api_code_pattern, sc)
                }
                setAuthFailed(userErrorMessage, sc)
                onOutcome(GoogleSignInOutcome.Failed(userErrorMessage, sc))
            }
        } else {
            Log.e(TAG, "GoogleSignIn unexpected error: ${e.javaClass.simpleName}")
            val msg = e.localizedMessage ?: "Google Sign-In error"
            setAuthFailed(msg)
            onOutcome(GoogleSignInOutcome.Failed(msg))
        }
    }
}

