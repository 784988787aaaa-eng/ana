package com.smartledger.aldaftar.data.account

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.data.cloud.CloudConnectionStore
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import com.smartledger.aldaftar.domain.license.LicenseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * المستودع المركزي الموحد لجلسة الحساب (Single Source of Truth)
 * يوحد حالة الحساب المتصل بين نافذة الترخيص ونافذة النسخ الاحتياطي السحابي.
 */
class UnifiedAccountSessionRepository(
    private val context: Context,
    private val licenseRepository: LicenseRepository,
    private val cloudArchiveStore: CloudArchiveStore
) {
    private val appContext = context.applicationContext
    private val cloudConnectionStore = CloudConnectionStore(appContext)
    private val googleAuth = GoogleDriveInternalAuth(appContext)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _session = MutableStateFlow(buildInitialSession())
    val session: StateFlow<UnifiedAccountSession> = _session.asStateFlow()

    init {
        repositoryScope.launch {
            refreshSession()
        }
    }

    private fun buildInitialSession(): UnifiedAccountSession {
        val lastGoogleAccount = googleAuth.getLastSignedInAccount()
        val storedEmail = cloudConnectionStore.email() ?: lastGoogleAccount?.email
        val isSignedIn = !storedEmail.isNullOrBlank() || lastGoogleAccount != null
        val licenseSnap = licenseRepository.snapshot()

        return UnifiedAccountSession(
            isSignedIn = isSignedIn,
            email = storedEmail ?: lastGoogleAccount?.email,
            displayName = lastGoogleAccount?.displayName,
            photoUrl = lastGoogleAccount?.photoUrl?.toString(),
            provider = if (isSignedIn) AccountProvider.GOOGLE else AccountProvider.NONE,
            accountCode = licenseSnap.accountCode,
            isCloudConnected = isSignedIn,
            licenseSnapshot = licenseSnap
        )
    }

    suspend fun refreshSession(): UnifiedAccountSession = withContext(Dispatchers.IO) {
        val lastGoogleAccount = googleAuth.getLastSignedInAccount()
        val storedEmail = cloudConnectionStore.email() ?: lastGoogleAccount?.email
        val isSignedIn = !storedEmail.isNullOrBlank() || lastGoogleAccount != null
        var licenseSnap = licenseRepository.snapshot()

        // إذا كان المستخدم مسجلاً بحساب Google والترخيص غير مفعل بعد، نحاول التفعيل التلقائي إن كان الحساب مرخصاً في السحابة
        if (isSignedIn && !storedEmail.isNullOrBlank() && !licenseSnap.isPaid) {
            val autoActivated = runCatching {
                licenseRepository.checkAndAutoActivateCloudAccount(storedEmail)
            }.getOrNull()
            if (autoActivated != null) {
                licenseSnap = autoActivated
            }
        }

        // إذا كان المستخدم مسجلاً بحساب Google ومعه ترخيص حساب، نحاول تحديث التحقق
        if (isSignedIn && licenseSnap.type == LicenseType.ACCOUNT && !licenseSnap.accountCode.isNullOrBlank()) {
            runCatching {
                licenseSnap = licenseRepository.verifyAccountOnline()
            }
        }

        val updatedSnap = licenseRepository.snapshot()
        val updated = UnifiedAccountSession(
            isSignedIn = isSignedIn,
            email = storedEmail ?: lastGoogleAccount?.email,
            displayName = lastGoogleAccount?.displayName,
            photoUrl = lastGoogleAccount?.photoUrl?.toString(),
            provider = if (isSignedIn) AccountProvider.GOOGLE else AccountProvider.NONE,
            accountCode = updatedSnap.accountCode,
            isCloudConnected = isSignedIn,
            licenseSnapshot = updatedSnap
        )
        _session.value = updated
        updated
    }

    suspend fun signInWithGoogle(
        account: GoogleSignInAccount,
        serverAuthCode: String? = null
    ): UnifiedAccountSession = withContext(Dispatchers.IO) {
        val email = account.email?.trim()?.lowercase()
        if (!email.isNullOrBlank()) {
            cloudConnectionStore.saveEmail(email)
            cloudArchiveStore.saveEmail(email)
        }

        if (!serverAuthCode.isNullOrBlank()) {
            runCatching {
                cloudArchiveStore.connectWithServerAuthCode(serverAuthCode)
            }
        }

        // 1. التحقق التلقائي والتفعيل السحابي الفوري بمجرد تسجيل الدخول بنفس الحساب المرخص
        var finalSnap = licenseRepository.snapshot()
        if (!email.isNullOrBlank()) {
            val autoActivatedSnap = runCatching {
                licenseRepository.checkAndAutoActivateCloudAccount(email)
            }.getOrNull()
            if (autoActivatedSnap != null) {
                finalSnap = autoActivatedSnap
            }
        }

        // 2. التحقق من وجود ترخيص حساب مسبق مرتبط إن وُجد
        if (!finalSnap.isPaid && finalSnap.type == LicenseType.ACCOUNT && !finalSnap.accountCode.isNullOrBlank()) {
            runCatching {
                finalSnap = licenseRepository.verifyAccountOnline()
            }
        }

        val updatedSnap = licenseRepository.snapshot()
        val newSession = UnifiedAccountSession(
            isSignedIn = true,
            email = email,
            displayName = account.displayName,
            photoUrl = account.photoUrl?.toString(),
            provider = AccountProvider.GOOGLE,
            accountCode = updatedSnap.accountCode,
            isCloudConnected = true,
            licenseSnapshot = updatedSnap
        )
        _session.value = newSession
        newSession
    }

    suspend fun signOutUnified() = withContext(Dispatchers.IO) {
        // 1. تسجيل الخروج من عميل Google
        runCatching {
            googleAuth.client().signOut()
        }

        // 2. مسح بيانات التخزين السحابي
        cloudArchiveStore.disconnect()
        cloudConnectionStore.clear()

        // 3. مسح جلسة ترخيص الحساب إن كانت من نوع ACCOUNT (مع الحفاظ الكامل على الترخيص المحلي LOCAL)
        licenseRepository.signOutAccount()

        // 4. تحديث حالة الجلسة المركزية فوراً
        val currentSnap = licenseRepository.snapshot()
        val signedOutSession = UnifiedAccountSession(
            isSignedIn = false,
            email = null,
            displayName = null,
            photoUrl = null,
            provider = AccountProvider.NONE,
            accountCode = null,
            isCloudConnected = false,
            licenseSnapshot = currentSnap
        )
        _session.value = signedOutSession
    }

    fun updateLicenseSnapshot(snapshot: LicenseSnapshot) {
        val current = _session.value
        _session.value = current.copy(
            licenseSnapshot = snapshot,
            accountCode = snapshot.accountCode ?: current.accountCode
        )
    }
}
