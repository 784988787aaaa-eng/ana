package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

/** مصادقة Google Drive المباشرة داخل التطبيق دون وسيط */
class GoogleDriveInternalAuth(private val context: Context) {
    companion object {
        val SCOPE_DRIVE_FILE = Scope("https://www.googleapis.com/auth/drive.file")
        val SCOPE_DRIVE_APPDATA = Scope("https://www.googleapis.com/auth/drive.appdata")
    }

    fun client(webClientId: String? = null): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE_DRIVE_FILE, SCOPE_DRIVE_APPDATA)

        if (!webClientId.isNullOrBlank()) {
            builder.requestServerAuthCode(webClientId, true)
        }
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
