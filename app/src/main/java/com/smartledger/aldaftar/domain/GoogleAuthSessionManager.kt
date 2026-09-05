/**
 * =====================================================================
 * ملف: مدير جلسة مصادقة حساب جوجل الموحد (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المصدر المركزي الوحيد للحقيقة (   ) لحالة
 * جلسة حساب  للمستخدم في جميع أنحاء التطبيق.
 * يضمن مزامنة لحظية وفورية لحالة تسجيل الدخول أو الخروج بين مختلف الشاشات
 * (شاشة التفعيل والترخيص، شاشة إعدادات النسخ الاحتياطي السحابي، وشاشة إدارة الحساب)
 * عبر تدفقات الحالة التفاعلية [].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. إدارة تدفق حالة البريد الإلكتروني المتفاعل (  ):
 *    - بث البريد الإلكتروني المسجل لحظياً لكافة واجهات المستخدم ومراقبي الحالة.
 * 2. التهيئة التلقائية من مستودع المصادقة (  ):
 *    - استرجاع البريد الإلكتروني المحفوظ مسبقاً من [] عند إقلاع التطبيق.
 * 3. التحديث الموحد للجلسة (  ):
 *    - تنظيف وتوحيد البريد عند تسجيل الدخول، ومسح الجلسة فوراً عند تسجيل الخروج.
 */
package com.smartledger.aldaftar.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
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
 * [حالات جلسة مصادقة حساب  الموحدة - ]
 */
sealed class GoogleAuthState {
    object SignedOut : GoogleAuthState()
    object SigningIn : GoogleAuthState()
    data class Authenticated(val email: String) : GoogleAuthState()
    data class AuthFailed(val errorMessage: String, val statusCode: Int? = null) : GoogleAuthState()
}

/**
 * [نتيجة معالجة تسجيل الدخول الموحدة - ]
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
 * [الكائن الأحادي لمدير جلسة حساب جوجل - ]:
 * يوفر مصدر حقيقة مركزي وموحد لحالة المصادقة بحساب  عبر كامل التطبيق.
 */
object GoogleAuthSessionManager {

    /** وسم السجلات التشخيصية لجلسة المصادقة */

    private val _sessionState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.SignedOut)
    val sessionState: StateFlow<GoogleAuthState> = _sessionState.asStateFlow()

    /** تدفق الحالة الداخلي القابل للتعديل للبريد الإلكتروني الحالي */
    private val _currentEmail = MutableStateFlow<String?>(null)

    /**
     * تدفق الحالة العام المتاح للقراءة فقط (- ):
     * تراقبه واجهات   لإعادة رسم المكونات فور تغير حساب المستخدم.
     */
    val currentEmail: StateFlow<String?> = _currentEmail.asStateFlow()

    /**
     * [تهيئة جلسة المصادقة عند بدء التطبيق - ]:
     * يسترجع البريد المحفوظ في تفضيلات أمان جوجل درايف ويحدث الحالة الموحدة.
     *
     * @  سياق التطبيق للوصول للتخزين المحلي.
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

        } catch (t: Throwable) {

        }
    }

    /**
     * [تعيين حالة بدء تسجيل الدخول - ]
     */
    fun setSigningIn() {
        _sessionState.value = GoogleAuthState.SigningIn

    }

    /**
     * [تحديث البريد الإلكتروني للجلسة - ]:
     * يقوم بتنظيف وتوحيد حالة الأحرف للبريد وتحديث التدفق التفاعلي.
     *
     * @  البريد الإلكتروني الجديد لحساب  أو .
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

    }

    /**
     * [تعيين حالة فشل المصادقة - ]
     */
    fun setAuthFailed(message: String, statusCode: Int? = null) {
        _sessionState.value = GoogleAuthState.AuthFailed(message, statusCode)

    }

    /**
     * [مسح جلسة المصادقة بالكامل - ]:
     * يعيد البريد الإلكتروني إلى  عند تسجيل الخروج لإشعار كافة شاشات التطبيق فوراً.
     */
    fun clearSession() {
        _currentEmail.value = null
        _sessionState.value = GoogleAuthState.SignedOut

    }

    /**
     * [التحقق من صحة الجلسة الحالية - ]
     */
    fun isSessionValid(): Boolean {
        return !_currentEmail.value.isNullOrBlank() && _sessionState.value is GoogleAuthState.Authenticated
    }

    /**
     * [المعالج المركزي لنتيجة تسجيل الدخول - ]:
     *  موحد يعالج نتيجة  لـ  -:
     * 1. استخراج  بأمان وتدقيق وجود الحساب والبريد ورمز التفويض.
     * 2. تشخيص آمن يمنع تسريب أي رموز حساسة أو توكنات.
     * 3. التمييز الدقيق بين الإلغاء الفعلي وأخطاء التهيئة والشبكة.
     * 4. تمرير  إلى المسار المركزي لتبادل التوكنات لـ   عند توفره.
     * 5. تحديث الجلسة الموحدة لإشعار كافة واجهات التطبيق في آن واحد.
     */
    fun handleSignInActivityResult(
        resultCode: Int,
        data: Intent?,
        context: Context,
        backupSyncViewModel: BackupSyncViewModel?,
        onOutcome: (GoogleSignInOutcome) -> Unit
    ) {

        if (resultCode == Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email?.trim()?.lowercase() ?: ""
                val authCode = account?.serverAuthCode

                if (email.isNotEmpty()) {
                    updateEmail(email)
                }

                if (!authCode.isNullOrEmpty() && backupSyncViewModel != null) {
                    backupSyncViewModel.handleGoogleOAuthCode(authCode, email) { driveSuccess ->

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

            val isExplicitCancel = (sc == GoogleSignInStatusCodes.SIGN_IN_CANCELLED || sc == 16) && resultCode == Activity.RESULT_CANCELED

            if (isExplicitCancel) {

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

            val msg = context.getString(R.string.backup_toast_connect_failed)
            setAuthFailed(msg)
            onOutcome(GoogleSignInOutcome.Failed(msg))
        }
    }
}

