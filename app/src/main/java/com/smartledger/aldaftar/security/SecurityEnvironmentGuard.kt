package com.smartledger.aldaftar.security

import android.content.Context
import android.os.Debug
import android.os.Build
import com.smartledger.aldaftar.BuildConfig
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest

/**
 * فحص دفاعي خفيف لبيئة التشغيل.
 * الهدف ليس الادعاء بإمكانية منع Frida/Xposed 100%، بل رفع كلفة العبث وكشف الحالات
 * الشائعة قبل قبول عمليات الترخيص الحساسة. لا يعتمد الفحص على دالة Boolean واحدة مكشوفة.
 */
object SecurityEnvironmentGuard {
    data class Assessment(
        val debuggerAttached: Boolean,
        val rootIndicators: Int,
        val hookingIndicators: Int,
        val signatureValid: Boolean,
        val suspiciousInstaller: Boolean
    ) {
        val compromised: Boolean
            get() = debuggerAttached || rootIndicators > 0 || hookingIndicators > 0 || !signatureValid
    }

    fun assess(context: Context): Assessment {
        val app = context.applicationContext
        val debug = !BuildConfig.DEBUG && Debug.isDebuggerConnected()
        val root = rootIndicators()
        val hooks = hookingIndicators()
        val signature = signatureValid(app)
        val installer = suspiciousInstaller(app)
        return Assessment(debug, root, hooks, signature, installer)
    }

    private fun rootIndicators(): Int {
        var count = 0
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su",
            "/data/adb/magisk", "/data/adb/ksu", "/data/adb/modules", "/system/app/Superuser.apk"
        )
        count += paths.count { File(it).exists() }
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "command -v su"))
            if (process.inputStream.bufferedReader().use { it.readLine() }?.isNotBlank() == true) count++
            process.destroy()
        } catch (_: Throwable) { }
        return count
    }

    private fun hookingIndicators(): Int {
        var count = 0
        val suspiciousClasses = listOf(
            "de.robv.android.xposed.XposedBridge",
            "com.saurik.substrate.MS",
            "re.frida.server.Frida"
        )
        count += suspiciousClasses.count { classExists(it) }
        if (portOpen(127, 0, 0, 1, 27042)) count++
        if (portOpen(127, 0, 0, 1, 27043)) count++
        return count
    }

    private fun classExists(name: String): Boolean = try {
        Class.forName(name, false, SecurityEnvironmentGuard::class.java.classLoader)
        true
    } catch (_: Throwable) { false }

    private fun portOpen(a: Int, b: Int, c: Int, d: Int, port: Int): Boolean = try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress("$a.$b.$c.$d", port), 80)
            true
        }
    } catch (_: Throwable) { false }

    private fun signatureValid(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true
        val expected = BuildConfig.EXPECTED_RELEASE_CERT_SHA256.trim().lowercase()
        if (expected.isBlank()) return true
        return try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES)
            }
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.signingInfo?.apkContentsSigners else {
                @Suppress("DEPRECATION") info.signatures
            }
            signatures?.any { sig ->
                val digest = MessageDigest.getInstance("SHA-256").digest(sig.toByteArray())
                    .joinToString("") { "%02x".format(it.toInt() and 0xff) }
                MessageDigest.isEqual(digest.toByteArray(), expected.toByteArray())
            } == true
        } catch (_: Throwable) { false }
    }

    private fun suspiciousInstaller(context: Context): Boolean {
        if (BuildConfig.DEBUG || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val installer = context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            installer != null && installer != "com.android.vending" && installer != "com.google.android.packageinstaller"
        } catch (_: Throwable) { false }
    }
}
