package com.smartledger.aldaftar.data.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/** Google authentication for Firebase identity only. It requests no Drive scopes. */
class FirebaseGoogleAuth(private val context: Context) {
    fun client(): GoogleSignInClient =
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(com.smartledger.aldaftar.R.string.firebase_google_web_client_id))
                .requestEmail()
                .build()
        )
}
