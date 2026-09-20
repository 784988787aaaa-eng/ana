package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

/** مصادقة Google Drive مباشرة من التطبيق، بدون Cloudflare أو خادم وسيط. */
class GoogleDriveInternalAuth(private val context: Context) {
    companion object {
        val SCOPE_DRIVE_FILE = Scope("https://www.googleapis.com/auth/drive.file")
        val SCOPE_DRIVE_APPDATA = Scope("https://www.googleapis.com/auth/drive.appdata")
    }

    fun client(): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE_DRIVE_FILE, SCOPE_DRIVE_APPDATA)
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getLastSignedInAccount() =
        GoogleSignIn.getLastSignedInAccount(context)
}
