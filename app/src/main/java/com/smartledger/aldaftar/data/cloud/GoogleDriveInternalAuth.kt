package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

/** Google OAuth 2.0 for direct Google Drive access from the Android app. */
class GoogleDriveInternalAuth(private val context: Context) {
    companion object {
        val SCOPE_DRIVE_FILE = Scope("https://www.googleapis.com/auth/drive.file")
    }

    fun client(): GoogleSignInClient {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(SCOPE_DRIVE_FILE)
        val webClientId = com.smartledger.aldaftar.BuildConfig.GOOGLE_CLIENT_ID.trim()
        if (webClientId.isNotBlank()) {
            builder.requestIdToken(webClientId)
        }
        return GoogleSignIn.getClient(context, builder.build())
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)
}
