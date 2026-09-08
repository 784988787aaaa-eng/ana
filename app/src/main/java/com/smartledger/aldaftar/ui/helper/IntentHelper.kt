package com.smartledger.aldaftar.ui.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

// Presentation helper for sharing an exported file using FileProvider
fun shareExportedFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(com.smartledger.aldaftar.R.string.intent_share_backup_title)))
    } catch (e: Exception) {
        // حماية تجربة المستخدم من الرسائل التقنية غير المفهومة مع تسجيل الخطأ في السجلات
        android.util.Log.e("IntentHelper", "Failed to share exported file", e)
        Toast.makeText(context, context.getString(com.smartledger.aldaftar.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}


// Helper to dial a phone number
fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("IntentHelper", "Failed to launch dialer", e)
        Toast.makeText(context, context.getString(com.smartledger.aldaftar.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}

// Helper to open WhatsApp chat with a message
fun openWhatsAppChat(context: Context, phoneNumber: String, message: String) {
    try {
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "").trim()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("IntentHelper", "Failed to launch WhatsApp", e)
        Toast.makeText(context, context.getString(com.smartledger.aldaftar.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}

