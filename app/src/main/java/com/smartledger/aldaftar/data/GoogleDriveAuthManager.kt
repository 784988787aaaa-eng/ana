package com.smartledger.aldaftar.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.smartledger.aldaftar.security.SecurePreferenceStore
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

sealed class GoogleDriveAuthState {
    data class Authenticated(val email: String, val accessToken: String) : GoogleDriveAuthState()
    object Expired : GoogleDriveAuthState()
    data class RefreshFailed(val reason: String) : GoogleDriveAuthState()
    object NotSignedIn : GoogleDriveAuthState()
}

class GoogleDriveAuthManager(
    private val context: Context,
    private val updateState: ((CloudSyncState) -> Unit)? = null
) {

    companion object {
        private const val TAG = "GoogleDriveAuthManager"

        private const val PREFS_NAME = "secure_google_drive_sync_prefs"
        private const val FALLBACK_PREFS_NAME = "google_drive_sync_prefs"
        private const val ENCRYPTED_FALLBACK_PREFS_NAME = "google_drive_secure_fallback"
        private const val ENCRYPTED_FALLBACK_KEY_ALIAS = "google_drive_secure_fallback_key"

        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRY = "token_expiry"
        const val KEY_EMAIL = "email"
        const val KEY_CLIENT_ID_OVERRIDE = "client_id_override"
        const val KEY_CLIENT_SECRET_OVERRIDE = "client_secret_override"

        const val SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
        const val SCOPE_DRIVE_FILE = "https://www.googleapis.com/auth/drive.file"
        const val SCOPE_USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email"
        const val FULL_OAUTH_SCOPES = "$SCOPE_DRIVE_APPDATA $SCOPE_DRIVE_FILE $SCOPE_USERINFO_EMAIL"

        const val AUTH_ENDPOINT_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        const val TOKEN_ENDPOINT_URL = "https://oauth2.googleapis.com/token"
        const val USERINFO_ENDPOINT_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
        const val REDIRECT_URI_LOCAL = "http://localhost/oauth2callback"

        private const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        private const val GRANT_TYPE_AUTH_CODE = "authorization_code"
        private const val PARAM_CLIENT_ID = "client_id"
        private const val PARAM_CLIENT_SECRET = "client_secret"
        private const val PARAM_REFRESH_TOKEN = "refresh_token"
        private const val PARAM_GRANT_TYPE = "grant_type"
        private const val PARAM_CODE = "code"
        private const val PARAM_REDIRECT_URI = "redirect_uri"

        private const val RESPONSE_ACCESS_TOKEN = "access_token"
        private const val RESPONSE_REFRESH_TOKEN = "refresh_token"
        private const val RESPONSE_EXPIRES_IN = "expires_in"
        private const val TOKEN_EXPIRY_BUFFER_MS = 300_000L
        private const val DEFAULT_EXPIRES_IN_SEC = 3600L
    }

    private val cloudEngine = CloudNetworkEngine.getInstance(context)
    private val refreshMutex = Mutex()

    val sharedPrefs: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context.applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Throwable) {

            Log.w(TAG, "Keystore unavailable; using encrypted fallback store")
            SecurePreferenceStore(context, ENCRYPTED_FALLBACK_PREFS_NAME, ENCRYPTED_FALLBACK_KEY_ALIAS)
        }.also { secureStore ->

            migrateLegacyPlaintextPreferences(secureStore)
        }
    }

    private fun migrateLegacyPlaintextPreferences(target: SharedPreferences) {
        try {
            val legacy = context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
            val entries = legacy.all.toMap()
            if (entries.isEmpty()) return
            val editor = target.edit()
            entries.forEach { (key, value) ->
                if (!target.contains(key)) {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toMutableSet())
                    }
                }
            }
            check(editor.commit()) { "Legacy Google auth migration failed" }
            legacy.edit().clear().commit()
        } catch (_: Throwable) {

            Log.w(TAG, "Legacy Google auth migration failed safely")
        }
    }

    val clientId: String
        get() = sharedPrefs.getString(KEY_CLIENT_ID_OVERRIDE, null)?.takeIf { it.isNotEmpty() } ?: BuildConfig.GOOGLE_CLIENT_ID

    val clientSecret: String
        get() = sharedPrefs.getString(KEY_CLIENT_SECRET_OVERRIDE, null)?.takeIf { it.isNotEmpty() } ?: BuildConfig.GOOGLE_CLIENT_SECRET

    val scope: String
        get() = FULL_OAUTH_SCOPES

    fun getClientIdOverride(): String = sharedPrefs.getString(KEY_CLIENT_ID_OVERRIDE, "") ?: ""
    fun getClientSecretOverride(): String = sharedPrefs.getString(KEY_CLIENT_SECRET_OVERRIDE, "") ?: ""

    fun saveClientCredentialsOverride(clientIdStr: String?, clientSecretStr: String?) {
        val editor = sharedPrefs.edit()
        if (clientIdStr.isNullOrEmpty()) {
            editor.remove(KEY_CLIENT_ID_OVERRIDE)
        } else {
            editor.putString(KEY_CLIENT_ID_OVERRIDE, clientIdStr.trim())
        }
        if (clientSecretStr.isNullOrEmpty()) {
            editor.remove(KEY_CLIENT_SECRET_OVERRIDE)
        } else {
            editor.putString(KEY_CLIENT_SECRET_OVERRIDE, clientSecretStr.trim())
        }
        editor.apply()
    }

    fun getAppSignatureSHA1(): String {
        try {
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }

            if (signatures != null && signatures.isNotEmpty()) {
                val sig = signatures[0]
                val md = java.security.MessageDigest.getInstance("SHA-1")
                val publicKey = md.digest(sig.toByteArray())
                return publicKey.joinToString(":") { String.format("%02X", it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل استخراج بصمة التطبيق SHA-1", e)
        }
        return context.getString(com.smartledger.aldaftar.R.string.gdrive_sha1_fetch_failed)
    }

    fun isUserTrulySignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val requiredScope = Scope(SCOPE_DRIVE_FILE)
        return account != null && GoogleSignIn.hasPermissions(account, requiredScope)
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope(SCOPE_DRIVE_APPDATA),
                Scope(SCOPE_DRIVE_FILE)
            )
        if (clientId.isNotBlank()) {
            try {

                builder.requestServerAuthCode(clientId, true)
            } catch (e: Exception) {
                Log.e(TAG, "تعذر تعيين serverAuthCode في GoogleSignInOptions", e)
            }
        }
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getAuthUrl(): String {
        return "$AUTH_ENDPOINT_URL" +
                "?client_id=${URLEncoder.encode(clientId, "UTF-8")}" +
                "&redirect_uri=${URLEncoder.encode(REDIRECT_URI_LOCAL, "UTF-8")}" +
                "&response_type=code" +
                "&scope=${URLEncoder.encode(scope, "UTF-8")}" +
                "&prompt=consent" +
                "&access_type=offline"
    }

    fun storeTokens(accessToken: String, refreshToken: String?, expiresInSec: Long) {
        val editor = sharedPrefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        if (!refreshToken.isNullOrEmpty()) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        }
        editor.putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (expiresInSec * 1000))
        editor.apply()
    }

    fun storeEmail(email: String) {
        sharedPrefs.edit().putString(KEY_EMAIL, email).apply()
        com.smartledger.aldaftar.domain.GoogleAuthSessionManager.updateEmail(email)
    }

    fun getStoredAccessToken(): String? = sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    fun getStoredRefreshToken(): String? = sharedPrefs.getString(KEY_REFRESH_TOKEN, null)
    fun getStoredEmail(): String? = sharedPrefs.getString(KEY_EMAIL, null)

    fun isTokenExpired(): Boolean {
        val expiry = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() >= (expiry - TOKEN_EXPIRY_BUFFER_MS)
    }

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

    suspend fun checkAuthState(): GoogleDriveAuthState = withContext(Dispatchers.IO) {
        val email = getStoredEmail() ?: ""
        val currentToken = getStoredAccessToken()

        if (!currentToken.isNullOrEmpty() && !isTokenExpired()) {
            return@withContext GoogleDriveAuthState.Authenticated(email, currentToken)
        }

        val refreshToken = getStoredRefreshToken()
        if (!refreshToken.isNullOrEmpty()) {
            val refreshed = refreshAccessTokenIfNeeded()
            if (refreshed != null) {
                return@withContext GoogleDriveAuthState.Authenticated(email, refreshed)
            } else {
                return@withContext GoogleDriveAuthState.RefreshFailed("فشل تجديد رمز الوصول")
            }
        }

        return@withContext GoogleDriveAuthState.NotSignedIn
    }

    suspend fun refreshAccessTokenIfNeeded(): String? = refreshMutex.withLock {
        withContext(Dispatchers.IO) {

            if (!isTokenExpired()) {
                val currentToken = getStoredAccessToken()
                if (!currentToken.isNullOrEmpty()) {
                    return@withContext currentToken
                }
            }

            val refreshToken = getStoredRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                Log.w(TAG, "لا يوجد رمز تجديد محفوظ لتجديد الجلسة")
                return@withContext null
            }

            try {
                cloudEngine.executeWithRetry(
                    operationName = "RefreshAccessToken",
                    maxRetries = 2,
                    initialDelayMs = 250L
                ) {
                    val formBuilder = FormBody.Builder()
                        .add(PARAM_CLIENT_ID, clientId)
                        .add(PARAM_REFRESH_TOKEN, refreshToken)
                        .add(PARAM_GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN)
                    if (clientSecret.isNotEmpty()) {
                        formBuilder.add(PARAM_CLIENT_SECRET, clientSecret)
                    }

                    val request = Request.Builder()
                        .url(TOKEN_ENDPOINT_URL)
                        .post(formBuilder.build())
                        .build()

                    cloudEngine.client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val rawBody = response.body?.string() ?: ""
                            val json = JSONObject(rawBody)
                            val accessToken = json.getString(RESPONSE_ACCESS_TOKEN)
                            val expiresIn = json.optLong(RESPONSE_EXPIRES_IN, DEFAULT_EXPIRES_IN_SEC)

                            storeTokens(accessToken, refreshToken, expiresIn)
                            Log.d(TAG, "تم تجديد رمز الوصول بنجاح.")
                            accessToken
                        } else {
                            val rawBody = response.body?.string() ?: ""
                            Log.w(TAG, "استجابة غير ناجحة عند تجديد رمز الوصول (رمز الحالة: ${response.code})")
                            if (response.code == 400 || response.code == 401) {
                                if (rawBody.contains("invalid_grant") || rawBody.contains("unauthorized_client")) {
                                    clearAuthData()
                                }
                            }
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "تعذر تجديد رمز الوصول: ${e.javaClass.simpleName}")
                null
            }
        }
    }

    suspend fun handleAuthorizationCode(
        code: String,
        inputEmail: String? = null,
        redirectUri: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        updateState?.invoke(CloudSyncState.Authenticating)
        val sanitizedCode = code.trim()
            .replace("\\s".toRegex(), "")
            .filter { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it in listOf('-', '_', '/', '.') }

        try {
            cloudEngine.executeWithRetry(
                operationName = "ExchangeAuthCode",
                maxRetries = 2,
                initialDelayMs = 500L
            ) {
                val builder = FormBody.Builder()
                    .add(PARAM_CODE, sanitizedCode)
                    .add(PARAM_CLIENT_ID, clientId.trim())
                    .add(PARAM_GRANT_TYPE, GRANT_TYPE_AUTH_CODE)

                val trimmedSecret = clientSecret.trim()
                if (trimmedSecret.isNotEmpty() && trimmedSecret != "YOUR_GOOGLE_CLIENT_SECRET") {
                    builder.add(PARAM_CLIENT_SECRET, trimmedSecret)
                }
                if (redirectUri.isNotEmpty()) {
                    builder.add(PARAM_REDIRECT_URI, redirectUri.trim())
                }

                val request = Request.Builder()
                    .url(TOKEN_ENDPOINT_URL)
                    .post(builder.build())
                    .build()

                cloudEngine.client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val rawBody = response.body?.string() ?: ""
                        val json = JSONObject(rawBody)
                        val accessToken = json.getString(RESPONSE_ACCESS_TOKEN)
                        val refreshToken = json.optString(RESPONSE_REFRESH_TOKEN, "").takeIf { it.isNotEmpty() } ?: getStoredRefreshToken()
                        val expiresIn = json.optLong(RESPONSE_EXPIRES_IN, DEFAULT_EXPIRES_IN_SEC)

                        storeTokens(accessToken, refreshToken, expiresIn)

                        val email = inputEmail ?: fetchUserEmail(accessToken) ?: "account@google.com"
                        storeEmail(email)

                        updateState?.invoke(CloudSyncState.Authenticated(email))
                        true
                    } else {
                        val errorMsg = response.body?.string() ?: "Authorization code exchange error"
                        Log.e(TAG, "فشل تبادل رمز التفويض (رمز الحالة: ${response.code})")

                        var detailedError = ""
                        try {
                            val json = JSONObject(errorMsg)
                            val err = json.optString("error")
                            val desc = json.optString("error_description")
                            detailedError = if (err.isNotEmpty() && desc.isNotEmpty()) {
                                "$err: $desc"
                            } else if (err.isNotEmpty()) {
                                err
                            } else {
                                errorMsg
                            }
                        } catch (_: Exception) {
                            detailedError = errorMsg
                        }

                        updateState?.invoke(
                            CloudSyncState.Error(
                                context.getString(com.smartledger.aldaftar.R.string.gdrive_error_link_failed) + "\n($detailedError)"
                            )
                        )
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل الاتصال أثناء تبادل رمز التفويض: ${e.javaClass.simpleName}")
            updateState?.invoke(
                CloudSyncState.Error(
                    context.getString(com.smartledger.aldaftar.R.string.gdrive_error_network_unstable) + "\n(${e.localizedMessage ?: ""})"
                )
            )
            false
        }
    }

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
