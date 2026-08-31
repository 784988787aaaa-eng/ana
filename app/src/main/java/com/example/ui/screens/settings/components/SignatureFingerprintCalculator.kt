package com.example.ui.screens.settings.components

import android.content.Context
import com.example.R

// تم فصل الحسابات التقنية عن واجهة العرض للحفاظ على مسؤولية كل طبقة.
object SignatureFingerprintCalculator {

    fun getSha1Fingerprint(context: Context): String {
        return getFingerprint(context, "SHA-1")
    }

    fun getSha256Fingerprint(context: Context): String {
        return getFingerprint(context, "SHA-256")
    }

    private fun getFingerprint(context: Context, algorithm: String): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(android.content.pm.PackageManager.GET_SIGNATURES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.GET_SIGNATURES
                )
            }
            val signatures = @Suppress("DEPRECATION") packageInfo.signatures
            if (signatures != null && signatures.isNotEmpty()) {
                val md = java.security.MessageDigest.getInstance(algorithm)
                val publicKey = md.digest(signatures[0].toByteArray())
                formatBytesToFingerprint(publicKey)
            } else {
                context.getString(R.string.settings_signature_unavailable)
            }
        } catch (e: Exception) {
            context.getString(R.string.settings_signature_unavailable)
        }
    }

    private fun formatBytesToFingerprint(bytes: ByteArray): String {
        return bytes.joinToString(":") { String.format("%02X", it) }
    }
}
