package com.smartledger.aldaftar.domain.communication

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * محرك تشغيل الـ Android Intents لقنوات التواصل المباشر.
 * يضمن:
 * 1. عدم فرض إرسال صامت (تجهيز الرقم والنص في شاشة المحادثة فقط، والمستخدم يضغط إرسال بنفسه).
 * 2. التحقق الآمن والـ fallback إلى Chooser عند عدم وجود تطبيق مخصص.
 * 3. حماية التطبيق من الانهيار عند حدوث ActivityNotFoundException.
 */
object CommunicationIntentLauncher {

    fun openWhatsApp(context: Context, phone: String, message: String): Boolean {
        return try {
            val waPhone = CustomerPhoneHelper.normalizeForWhatsApp(phone)
            val uri = Uri.parse("https://wa.me/$waPhone?text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun openSms(context: Context, phone: String, message: String): Boolean {
        return try {
            val smsPhone = CustomerPhoneHelper.normalizeForSms(phone)
            val uri = Uri.parse("smsto:$smsPhone")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
