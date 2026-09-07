/**
 * =====================================================================
 * ملف: مدير مصادقة جوجل درايف (GoogleDriveAuthManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف القلب النابض للمصادقة وتفويض بروتوكول OAuth 2.0 المباشر مع خوادم Google.
 * يتولى إدارة جلسات المزامنة السحابية عبر Google Drive من خلال:
 * 1. تخزين واسترجاع الرموز المميزة (Access Token & Refresh Token) في مستودع مشفر آمن
 *    (EncryptedSharedPreferences) لمنع تسريب بيانات الاعتماد.
 * 2. تبادل رمز التفويض (Authorization Code Exchange) للحصول على الرموز المميزة.
 * 3. التجديد التلقائي لرمز الوصول (Token Refresh) عند اقتراب انتهاء صلاحيته عبر [CloudNetworkEngine].
 * 4. إدارة تسجيل الدخول والخروج وإلغاء الصلاحيات (Revoke Access) وحذف الجلسة بأمان.
 * 
 * [قواعد الأمان والخصوصية]:
 * - عدم طباعة رموز الوصول الحساسة (Access Tokens / Authorization Headers) في سجلات التطبيق (Logcat).
 * - استخدام طبقة تشفير أجهزة أندرويد الحديثة (AES256_GCM) مع خطة بديلة للمحافظة على استقرار التطبيق.
 */
package com.smartledger.aldaftar.data

// ---------------------------------------------------------------------
// استيراد حزم التشفير وشبكة OkHttp وخدمات تسجيل الدخول بحسابات Google
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.smartledger.aldaftar.BuildConfig
import com.smartledger.aldaftar.data.cloud.CloudNetworkEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

/**
 * [فئات حالة المصادقة السحابية - GoogleDriveAuthState]:
 * فئة مختومة (Sealed Class) تمثل الحالات الأربع الصريحة لجلسة Google Drive:
 * - Authenticated: تم التحقق والجلسة صالحة وتحمل البريد ورمز الوصول.
 * - Expired: رمز الوصول منتهي الصلاحية ويتطلب تجديداً فورياً.
 * - RefreshFailed: تعذر تجديد الرمز بسبب مشكلة في الشبكة أو إلغاء الصلاحية.
 * - NotSignedIn: لا توجد جلسة نشطة أو تم تسجيل الخروج.
 */
sealed class GoogleDriveAuthState {
    data class Authenticated(val email: String, val accessToken: String) : GoogleDriveAuthState()
    object Expired : GoogleDriveAuthState()
    data class RefreshFailed(val reason: String) : GoogleDriveAuthState()
    object NotSignedIn : GoogleDriveAuthState()
}

/**
 * [فئة مدير المصادقة - GoogleDriveAuthManager]:
 * الفئة المسؤولة عن دورة حياة جلسة OAuth 2.0 والرموز المميزة.
 */
class GoogleDriveAuthManager(
    private val context: Context,
    private val updateState: ((CloudSyncState) -> Unit)? = null
) {
    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على روابط نقاط نهاية Google OAuth 2.0، ونطاقات الصلاحيات (Scopes)، وثوابت المفاتيح.
     */
    companion object {
        private const val TAG = "GoogleDriveAuthManager"

        // مفاتيح التخزين الثابتة الإلزامية لحماية جلسات المستخدمين القائمة
        private const val PREFS_NAME = "secure_google_drive_sync_prefs"
        private const val FALLBACK_PREFS_NAME = "google_drive_sync_prefs"

        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_EMAIL = "email"

        // نطاقات الصلاحيات المطلوبة (مجلد التطبيق المعزول وملفات التطبيق والبريد)
        const val SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
        const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"
        const val SCOPE_USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email"
        const val FULL_OAUTH_SCOPES = "$SCOPE_DRIVE_APPDATA $SCOPE_DRIVE_FILE $SCOPE_USERINFO_EMAIL"

        // روابط نقاط نهاية خوادم Google OAuth 2.0
        const val AUTH_ENDPOINT_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val USERINFO_ENDPOINT_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
        const val REDIRECT_URI_LOCAL = "http://localhost/oauth2callback"

        private const val PARAM_REFRESH_TOKEN = "refresh_token"
        private const val PARAM_CODE = "code"
        private const val PARAM_REDIRECT_URI = "redirect_uri"

        private const val RESPONSE_ACCESS_TOKEN = "access_token"
        private const val RESPONSE_REFRESH_TOKEN = "refresh_token"
        private const val RESPONSE_EXPIRES_IN = "expires_in"
        private const val TOKEN_EXPIRY_BUFFER_MS = 300_000L // 5 دقائق كمهلة أمان استباقية قبل الانتهاء الفعلي
        private const val DEFAULT_EXPIRES_IN_SEC = 3600L
        private const val GOOGLE_OAUTH_EXCHANGE_PATH = "/v1/google/oauth/exchange"
        private const val GOOGLE_OAUTH_REFRESH_PATH = "/v1/google/oauth/refresh"
    }

    // محرك الشبكة السحابي الموحد لإعادة المحاولة عند التقلبات
    private val cloudEngine = CloudNetworkEngine.getInstance(context)
    private val refreshMutex = Mutex()

    /**
     * [خاصية التخزين المشفر - sharedPrefs]:
     * تستخدم MasterKey بنظام تشفير AES256 لحفظ الرموز، مع التبديل الآمن في حال عدم توفر التشفير العتادي.
     */
    val sharedPrefs: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.w(TAG, "تعذر استخدام التشفير للتفضيلات، جاري الرجوع للملف الاحتياطي: ${e.javaClass.simpleName}")
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // معرف عميل OAuth عام مطلوب لبدء Google Sign-In. لا يُعامل كسِر.
    val clientId: String
        get() = BuildConfig.GOOGLE_CLIENT_ID.trim()

    val scope: String
        get() = FULL_OAUTH_SCOPES

    /**
     * [دالة التحقق من تسجيل الدخول الحقيقي]:
     * تفحص حساب Google المسجل ومطابقة الصلاحيات الممنوحة مع نطاق Google Drive.
     */
    fun isUserTrulySignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val requiredScope = Scope(SCOPE_DRIVE_FILE)
        return account != null && GoogleSignIn.hasPermissions(account, requiredScope)
    }

    /**
     * [دالة بناء عميل تسجيل الدخول - GoogleSignInClient]:
     * تجهز إعدادات GoogleSignInOptions مع طلب الصلاحيات ورمز التفويض من الخادم.
     */
    fun getGoogleSignInClient(): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .apply {
                if (clientId.isNotBlank()) {
                    requestIdToken(clientId)
                }
            }
            .requestScopes(
                Scope(SCOPE_DRIVE_APPDATA),
                Scope(SCOPE_DRIVE_FILE)
            )
        if (clientId.isNotBlank()) {
            try {
                // يطلب رمز خادم مرة واحدة بصورة طبيعية؛ Google يمنح refresh token عند أول تبادل ناجح.
                builder.requestServerAuthCode(clientId, false)
            } catch (e: Exception) {
                Log.e(TAG, "تعذر تعيين serverAuthCode في GoogleSignInOptions", e)
            }
        }
        return GoogleSignIn.getClient(context, builder.build())
    }

    /**
     * [دالة توليد رابط التفويض المباشر]:
     * تبني رابط تفويض OAuth 2.0 للمتصفح للوصول غير المتصل (offline access).
     */
    fun getAuthUrl(): String {
        return "$AUTH_ENDPOINT_URL" +
                "?client_id=${URLEncoder.encode(clientId, "UTF-8")}" +
                "&redirect_uri=${URLEncoder.encode(REDIRECT_URI_LOCAL, "UTF-8")}" +
                "&response_type=code" +
                "&scope=${URLEncoder.encode(scope, "UTF-8")}" +
                "&prompt=consent" +
                "&access_type=offline"
    }

    /**
     * [دالة حفظ الرموز المميزة - storeTokens]:
     * تحفظ رمز الوصول ورمز التجديد ووقت الانتهاء في التفضيلات المشفرة بأمان.
     */
    fun storeTokens(accessToken: String, refreshToken: String?, expiresInSec: Long) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        if (!refreshToken.isNullOrEmpty()) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresInSec * 1000))
        editor.apply()
    }

    /**
     * [دالة تخزين البريد الإلكتروني]:
     * تحفظ بريد الحساب وتحدث الجلسة المركزية في [GoogleAuthSessionManager].
     */
    fun storeEmail(email: String) {
        sharedPrefs.edit().putString(KEY_EMAIL, email).apply()
        com.smartledger.aldaftar.domain.GoogleAuthSessionManager.updateEmail(email)
    }

    fun getStoredAccessToken(): String? = sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    fun getStoredRefreshToken(): String? = sharedPrefs.getString(KEY_REFRESH_TOKEN, null)
    fun getStoredEmail(): String? = sharedPrefs.getString(KEY_EMAIL, null)

    /**
     * [دالة فحص انتهاء صلاحية الرمز]:
     * تتحقق مما إذا كان رمز الوصول قريباً من الانتهاء ضمن مهلة الأمان (5 دقائق).
     */
    fun isTokenExpired(): Boolean {
        val expiry = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() >= (expiry - TOKEN_EXPIRY_BUFFER_MS)
    }

    /**
     * [دالة مسح بيانات المصادقة]:
     * تحذف الرموز والبريد من التخزين المشفر وتصفر جلسة التطبيق العامة.
     */
    fun clearAuthData() {
        sharedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_EMAIL)
            .apply()
        com.smartledger.aldaftar.domain.GoogleAuthSessionManager.clearSession()
    }

    fun logout() {
        logoutAsync(onComplete = null)
    }

    /**
     * [دالة تسجيل الخروج غير المتزامنة - logoutAsync]:
     * تلغي الأذونات من Google وتوقع الخروج من حساب Google ثم تنظف البيانات المحلية.
     */
    fun logoutAsync(onComplete: (() -> Unit)? = null) {
        try {
            val signInClient = getGoogleSignInClient()
            signInClient.revokeAccess().addOnCompleteListener {
                signInClient.signOut().addOnCompleteListener {
                    clearAuthData()
                    updateState?.invoke(CloudSyncState.Idle)
                    onComplete?.invoke()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تسجيل الخروج وإلغاء الصلاحيات: ${e.javaClass.simpleName}")
            clearAuthData()
            updateState?.invoke(CloudSyncState.Idle)
            onComplete?.invoke()
        }
    }

    /**
     * [دالة إيقاف المزامنة السحابية في الإعدادات]:
     * تعطل خيار المزامنة السحابية في جدول إعدادات قاعدة البيانات المحلية عند تسجيل الخروج.
     */
    suspend fun disableCloudSyncInSettings() = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val settings = db.settingsDao().getSettingsDirect()
            if (settings != null && settings.isCloudSyncEnabled) {
                db.settingsDao().insertOrUpdateSettings(settings.copy(isCloudSyncEnabled = false))
                Log.d(TAG, "تم تعطيل المزامنة السحابية في الإعدادات")
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل تعطيل المزامنة السحابية في الإعدادات: ${e.javaClass.simpleName}")
        }
    }

    /**
     * [دالة التحقق من حالة المصادقة المباشرة - checkAuthState]:
     * تفحص حالة تسجيل الدخول وصلاحية الرموز وتنفذ التجديد تلقائياً إذا لزم الأمر.
     */
    suspend fun checkAuthState(): GoogleDriveAuthState = withContext(Dispatchers.IO) {
        val email = getStoredEmail() ?: ""
        val currentToken = getStoredAccessToken()

        // 1. إذا كان رمز الوصول الحالي صالحاً وغير منتهي الصلاحية، الجلسة مصادقة فوراً دون الحاجة لطلب شبكي
        if (!currentToken.isNullOrEmpty() && !isTokenExpired()) {
            return@withContext GoogleDriveAuthState.Authenticated(email, currentToken)
        }

        // 2. إذا كان الرمز منتهياً أو غير موجود، نحاول التجديد عبر Refresh Token
        val refreshToken = getStoredRefreshToken()
        if (!refreshToken.isNullOrEmpty()) {
            val refreshed = refreshAccessTokenIfNeeded()
            if (refreshed != null) {
                return@withContext GoogleDriveAuthState.Authenticated(email, refreshed)
            } else {
                return@withContext GoogleDriveAuthState.RefreshFailed("فشل تجديد رمز الوصول")
            }
        }

        // 3. لا يوجد رمز وصول صالح ولا رمز تجديد
        return@withContext GoogleDriveAuthState.NotSignedIn
    }

    /**
     * [دالة تجديد رمز الوصول - refreshAccessTokenIfNeeded]:
     * ترسل طلب POST مشفراً إلى نقطة نهاية Google Token لتجديد الرمز باستخدام Refresh Token.
     */
    suspend fun refreshAccessTokenIfNeeded(): String? = refreshMutex.withLock {
        withContext(Dispatchers.IO) {
            if (!isTokenExpired()) {
                getStoredAccessToken()?.takeIf { it.isNotEmpty() }?.let { return@withContext it }
            }

            val refreshToken = getStoredRefreshToken()?.takeIf { it.isNotEmpty() }
                ?: return@withContext null

            try {
                val request = Request.Builder()
                    .url(BuildConfig.LICENSE_BACKEND_URL.trim().trimEnd('/') + GOOGLE_OAUTH_REFRESH_PATH)
                    .header("Accept", "application/json")
                    .post(FormBody.Builder().add(PARAM_REFRESH_TOKEN, refreshToken).build())
                    .build()

                cloudEngine.client.newCall(request).execute().use { response ->
                    val rawBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        if (response.code == 400 || response.code == 401) {
                            clearAuthData()
                        }
                        return@withContext null
                    }
                    val json = JSONObject(rawBody)
                    val accessToken = json.optString(RESPONSE_ACCESS_TOKEN).takeIf { it.isNotBlank() }
                        ?: return@withContext null
                    val expiresIn = json.optLong(RESPONSE_EXPIRES_IN, DEFAULT_EXPIRES_IN_SEC)
                    storeTokens(accessToken, refreshToken, expiresIn)
                    accessToken
                }
            } catch (e: Exception) {
                Log.e(TAG, "تعذر تجديد رمز الوصول: ${e.javaClass.simpleName}")
                null
            }
        }
    }

    /**
     * [دالة معالجة رمز التفويض - handleAuthorizationCode]:
     * تستقبل رمز التفويض (Auth Code) وتتبادله مع خوادم Google للحصول على Access Token و Refresh Token.
     */
    suspend fun handleAuthorizationCode(
        code: String,
        inputEmail: String? = null,
        redirectUri: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        updateState?.invoke(CloudSyncState.Authenticating)
        val sanitizedCode = code.trim().replace("\\s".toRegex(), "")
        if (sanitizedCode.isBlank()) {
            updateState?.invoke(CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_link_failed)))
            return@withContext false
        }

        try {
            val form = FormBody.Builder()
                .add(PARAM_CODE, sanitizedCode)
                .apply { if (redirectUri.isNotBlank()) add(PARAM_REDIRECT_URI, redirectUri.trim()) }
                .build()
            val request = Request.Builder()
                .url(BuildConfig.LICENSE_BACKEND_URL.trim().trimEnd('/') + GOOGLE_OAUTH_EXCHANGE_PATH)
                .header("Accept", "application/json")
                .post(form)
                .build()

            cloudEngine.client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.w(TAG, "فشل تبادل رمز Google OAuth (رمز الحالة: ${response.code})")
                    updateState?.invoke(
                        CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_link_failed))
                    )
                    return@withContext false
                }
                val json = JSONObject(rawBody)
                val accessToken = json.optString(RESPONSE_ACCESS_TOKEN).takeIf { it.isNotBlank() }
                    ?: return@withContext false
                val refreshToken = json.optString(RESPONSE_REFRESH_TOKEN).takeIf { it.isNotBlank() }
                    ?: getStoredRefreshToken()
                val expiresIn = json.optLong(RESPONSE_EXPIRES_IN, DEFAULT_EXPIRES_IN_SEC)
                val email = json.optString(KEY_EMAIL).trim().lowercase().takeIf { it.isNotBlank() }
                    ?: inputEmail?.trim()?.lowercase()
                    ?: fetchUserEmail(accessToken)
                    ?: return@withContext false

                storeTokens(accessToken, refreshToken, expiresIn)
                storeEmail(email)
                updateState?.invoke(CloudSyncState.Authenticated(email))
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل الاتصال أثناء تبادل رمز Google OAuth: ${e.javaClass.simpleName}")
            updateState?.invoke(
                CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_network_unstable))
            )
            false
        }
    }

    /**
     * [دالة جلب البريد الإلكتروني للمستخدم - fetchUserEmail]:
     * تستعلم من نقطة نهاية Google Userinfo لاستخراج البريد الإلكتروني المرتبط بالرمز المميز.
     */
    private suspend fun fetchUserEmail(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(USERINFO_ENDPOINT_URL)
                .header("Authorization", "Bearer $accessToken")
                .get()
                .build()

            cloudEngine.client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val rawBody = response.body?.string() ?: ""
                    val json = JSONObject(rawBody)
                    if (json.has("email")) json.getString("email") else null
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "تعذر استرجاع البريد الإلكتروني للمستخدم: ${e.javaClass.simpleName}")
            null
        }
    }
}
