package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

/** مصادقة Google داخل التطبيق؛ مسار المتصفح محصور بالربط اليدوي. */
class GoogleDriveInternalAuth(private val context: Context) {
    fun client(webClientId: String) = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .requestServerAuthCode(webClientId, true)
            .build()
    )
}
