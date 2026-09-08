package com.smartledger.aldaftar.domain

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Dedicated client for Support ID only.
 * It is deliberately independent from the removed licensing subsystem.
 */
class SupportIdentityClient(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetch(): Result<String> {
        val user = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(IllegalStateException("Authentication is required."))

        val idToken = user.getIdToken(false).await()?.token
            ?: return Result.failure(IllegalStateException("Authentication token is unavailable."))

        val appCheckToken = FirebaseAppCheck.getInstance()
            .getAppCheckToken(false)
            .await()
            .token

        val baseUrl = runCatching { com.smartledger.aldaftar.BuildConfig.SUPPORT_ID_BACKEND_URL }
            .getOrDefault("")
            .trim()
            .trimEnd('/')

        if (!baseUrl.startsWith("https://")) {
            return Result.failure(IllegalStateException("Support service is not configured."))
        }

        val request = Request.Builder()
            .url("$baseUrl/v1/support/identity")
            .header("Authorization", "Bearer $idToken")
            .header("X-Firebase-AppCheck", appCheckToken)
            .header("Accept", "application/json")
            .get()
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Support service unavailable (${response.code}).")
                }
                val id = org.json.JSONObject(body).optString("supportId").trim()
                if (id.isBlank()) throw IllegalStateException("Empty support identity.")
                id
            }
        }
    }
}
