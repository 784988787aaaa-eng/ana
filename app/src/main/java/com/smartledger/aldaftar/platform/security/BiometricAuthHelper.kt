package com.smartledger.aldaftar.platform.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

object BiometricAuthHelper {

    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    private val isSessionActive = AtomicBoolean(false)
    @Volatile
    private var activePromptRef: WeakReference<BiometricPrompt>? = null

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Throwable) {
            false
        }
    }

    fun isAuthenticationInProgress(): Boolean = isSessionActive.get()

    fun cancelActiveAuthentication() {
        try {
            activePromptRef?.get()?.cancelAuthentication()
        } catch (_: Throwable) {
        } finally {
            activePromptRef = null
            isSessionActive.set(false)
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            isSessionActive.set(false)
            return
        }

        if (!isBiometricAvailable(activity)) {
            isSessionActive.set(false)
            onError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "Biometrics unavailable")
            return
        }

        // Prevent overlapping biometric sessions on recomposition or rapid taps
        if (!isSessionActive.compareAndSet(false, true)) {
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    activePromptRef = null
                    isSessionActive.set(false)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    activePromptRef = null
                    isSessionActive.set(false)
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt remains open on single failed biometric attempt until user cancels or succeeds
                    onFailed()
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            activePromptRef = WeakReference(biometricPrompt)
            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            activePromptRef = null
            isSessionActive.set(false)
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, t.message ?: "Authentication error")
        }
    }
}


