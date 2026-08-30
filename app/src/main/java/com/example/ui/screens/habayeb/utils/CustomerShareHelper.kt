package com.example.ui.screens.habayeb.utils

/*
 * =====================================================================================
 * مُساعد مشاركة كشوفات وإشعارات العملاء (Customer Statement & Transaction Share Helper)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * ملف مخصص لإدارة وصياغة ومشاركة كافة الرسائل النصية وكشوف الحسابات وإشعارات المعاملات:
 * 1. صياغة وتنسيق رسائل المطالبات وكشوف الحسابات بطريقة مهنية واضحة مع إبراز العملات وأسعار الصرف.
 * 2. دعم التوافقية العالية لمشاركة الرسائل عبر مختلف أجهزة ومصنعي Android (SMS Intents & System Choosers).
 * 3. دعم الإرسال المباشر عبر واتساب العادي وواتساب للأعمال (WhatsApp Business) سواء للنصوص أو ملفات PDF عبر FileProvider.
 * 4. عزل منطق المشاركة النصية خارج مكونات واجهة المستخدم (Jetpack Compose) لتحقيق فصل تام للمسؤوليات.
 * =====================================================================================
 */

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.ui.helper.formatCurrency

/*
 * =====================================================================================
 * كائن مساعد المشاركة (CustomerShareHelper Object)
 * -------------------------------------------------------------------------------------
 * يوفر دوال بناء النصوص وإطلاق نوايا المشاركة (Intents).
 * =====================================================================================
 */
object CustomerShareHelper {

    /*
     * إرسال رسالة SMS بموثوقية تامة عبر تجربة 3 طرق متتالية لضمان التوافق مع كافة مصنعي أجهزة أندرويد
     * Method 1: ACTION_SENDTO مع smsto:
     * Method 2: ACTION_VIEW مع sms:
     * Method 3: موجه المشاركة العام للنظام (System Intent Chooser)
     */
    private fun sendSmsReliably(context: Context, rawPhone: String, body: String, fallbackChooserTitleId: Int) {
        val cleanPhone = rawPhone.replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace("[", "")
            .replace("]", "")
        
        try {
            // الطريقة الأولى: ACTION_SENDTO مع smsto:
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(if (cleanPhone.isBlank()) "smsto:" else "smsto:$cleanPhone")
                putExtra("sms_body", body)
                putExtra("body", body)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e1: Exception) {
            try {
                // الطريقة الثانية: ACTION_VIEW مع sms: كبديل لبعض الأجهزة
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (cleanPhone.isBlank()) "sms:" else "sms:$cleanPhone")
                    putExtra("sms_body", body)
                    putExtra("body", body)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // الطريقة الثالثة: فتح موجه النظام العام للاختيار
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(fallbackChooserTitleId)))
            }
        }
    }

    /**
     * تحديد المسمى النصي الدقيق لنوع المعاملة بناءً على اتجاه الحساب.
     */
    fun resolveTxTypeTitle(context: Context, txType: String, isAccountOwedToThem: Boolean): String {
        return when (txType) {
            "OWED_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_by)
            "PAYMENT_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_by)
            "OWED_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_to)
            "PAYMENT_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_to)
            else -> context.getString(R.string.pdf_tx_type_new)
        }
    }

    /*
     * بناء نص إشعار معاملة مفردة للمشاركة عبر SMS أو WhatsApp
     */
    fun buildSingleTxShareBody(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String {
        // 1. سطر عنوان المعاملة التوضيحي
        val header = when (tx.type) {
            "OWED_BY_THEM" -> context.getString(R.string.msg_header_debt_against)
            "PAYMENT_BY_THEM" -> context.getString(R.string.msg_header_payment_against)
            "OWED_TO_THEM" -> context.getString(R.string.msg_header_debt_for)
            "PAYMENT_TO_THEM" -> context.getString(R.string.msg_header_payment_for)
            else -> context.getString(R.string.msg_header_debt_against)
        }

        val bullet = context.getString(R.string.msg_bullet)

        // 2. سطر مبلغ المعاملة وسعر الصرف إن وجد
        val isExchangeTx = tx.isForeign && tx.isRateCalculated
        val amountLine = if (isExchangeTx) {
            val foreignSymbol = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) tx.currencyCode else ""
            val foreignAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.foreignAmount)
            val arrow = context.getString(R.string.msg_exchange_arrow)
            val equivAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.equivalentAmount)
            val ratePrefix = context.getString(R.string.msg_rate_prefix)
            val rateFormatted = com.example.ui.helper.HabayebMathHelper.formatRate(tx.exchangeRate)
            "$bullet $foreignAmtFormatted $foreignSymbol $arrow $equivAmtFormatted $currencySymbol $ratePrefix $rateFormatted"
        } else if (tx.isForeign) {
            val foreignSymbol = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) tx.currencyCode else ""
            val foreignAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(
                if (tx.foreignAmount.compareTo(java.math.BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
            )
            "$bullet $foreignAmtFormatted $foreignSymbol"
        } else {
            val amtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.amount)
            "$bullet $amtFormatted $currencySymbol"
        }

        val lines = mutableListOf<String>()
        lines.add(header)
        lines.add(amountLine)

        // 3. سطر الملاحظات والبيان إن وجد
        val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)
        if (cleanDetails.isNotBlank()) {
            val statementPrefix = context.getString(R.string.msg_statement_prefix)
            lines.add("$statementPrefix $cleanDetails")
        }

        // 4. أرصدة العملات الأجنبية غير المحولة إن وجدت
        if (allCustomerTxs.isNotEmpty()) {
            val foreignMap = mutableMapOf<String, java.math.BigDecimal>()
            for (t in allCustomerTxs) {
                val (tCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(t, currencySymbol)
                val normCurrency = CurrencyConfig.getBySymbol(tCurrency)?.symbol ?: tCurrency
                val normDefault = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol

                if (normCurrency != normDefault) {
                    val safeBd = bdAmount.setScale(4, java.math.RoundingMode.HALF_EVEN)
                    val currVal = foreignMap[normCurrency] ?: java.math.BigDecimal.ZERO
                    when (t.type) {
                        "OWED_BY_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                        "PAYMENT_BY_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "OWED_TO_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "PAYMENT_TO_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                    }
                }
            }

            for ((fSymbol, fNetBd) in foreignMap) {
                if (fNetBd.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    val foreignTotalPrefix = if (fNetBd.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        context.getString(R.string.msg_foreign_total_against)
                    } else {
                        context.getString(R.string.msg_foreign_total_for)
                    }
                    val formattedForeignNet = com.example.ui.helper.HabayebMathHelper.formatSmart(fNetBd.abs())
                    lines.add("$foreignTotalPrefix $formattedForeignNet $fSymbol")
                }
            }
        }

        // 5. سطر إجمالي الرصيد بالعملة المحلية
        val totalPrefix = if (netDebt.compareTo(java.math.BigDecimal.ZERO) > 0) {
            context.getString(R.string.msg_total_against)
        } else if (netDebt.compareTo(java.math.BigDecimal.ZERO) < 0) {
            context.getString(R.string.msg_total_for)
        } else {
            context.getString(R.string.msg_total_against)
        }
        val formattedNetDebt = com.example.ui.helper.HabayebMathHelper.formatSmart(netDebt.abs())
        lines.add("$totalPrefix $formattedNetDebt $currencySymbol")

        return lines.joinToString("\n")
    }

    /*
     * بناء نص كشف حساب شامل للعميل
     */
    fun buildStatementShareBody(
        context: Context,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String {
        val debtStatus = when {
            netDebt.compareTo(java.math.BigDecimal.ZERO) > 0 -> context.getString(R.string.habayeb_statement_status_owed_by_them, formatCurrency(netDebt.abs(), currencySymbol))
            netDebt.compareTo(java.math.BigDecimal.ZERO) < 0 -> context.getString(R.string.habayeb_statement_status_owed_to_them, formatCurrency(netDebt.abs(), currencySymbol))
            else -> context.getString(R.string.habayeb_statement_status_balanced_new, formatCurrency(java.math.BigDecimal.ZERO, currencySymbol))
        }
        val title = context.getString(R.string.habayeb_statement_header, customer.name)
        val footer = context.getString(R.string.habayeb_statement_footer)

        val foreignLines = mutableListOf<String>()
        if (allCustomerTxs.isNotEmpty()) {
            val isAccountOwedToThem = customer.initialType == "OWED_TO_THEM"
            val foreignMap = mutableMapOf<String, java.math.BigDecimal>()
            for (t in allCustomerTxs) {
                val (tCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(t, currencySymbol)
                val normCurrency = CurrencyConfig.getBySymbol(tCurrency)?.symbol ?: tCurrency
                val normDefault = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol

                if (normCurrency != normDefault) {
                    val safeBd = bdAmount.setScale(4, java.math.RoundingMode.HALF_EVEN)
                    val currVal = foreignMap[normCurrency] ?: java.math.BigDecimal.ZERO
                    when (t.type) {
                        "OWED_BY_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                        "PAYMENT_BY_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "OWED_TO_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "PAYMENT_TO_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                    }
                }
            }

            for ((fSymbol, fNetBd) in foreignMap) {
                if (fNetBd.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    val foreignTotalPrefix = if (fNetBd.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        context.getString(R.string.msg_foreign_total_against)
                    } else {
                        context.getString(R.string.msg_foreign_total_for)
                    }
                    val formattedForeignNet = com.example.ui.helper.HabayebMathHelper.formatSmart(fNetBd.abs())
                    foreignLines.add("\n$foreignTotalPrefix $formattedForeignNet $fSymbol")
                }
            }
        }
        val foreignText = if (foreignLines.isNotEmpty()) foreignLines.joinToString("") + "\n" else ""

        return "$title• $debtStatus\n$foreignText$footer"
    }

    /*
     * إرسال كشف الحساب عبر الرسائل القصيرة SMS
     */
    fun triggerSmsStatement(
        context: Context,
        customer: HabayebCustomer,
        debt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildStatementShareBody(context, customer, debt, currencySymbol, allCustomerTxs)
        sendSmsReliably(context, customer.phone, body, R.string.habayeb_statement_send)
    }

    /*
     * إرسال كشف الحساب عبر واتساب
     */
    fun triggerWhatsAppStatement(
        context: Context,
        customer: HabayebCustomer,
        debt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildStatementShareBody(context, customer, debt, currencySymbol, allCustomerTxs)
        try {
            val waUrl = "https://wa.me/${customer.phone.replace("+", "").replace(" ", "")}?text=${Uri.encode(body)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.habayeb_statement_send_whatsapp)))
        }
    }

    /*
     * إرسال إشعار معاملة مفردة عبر الرسائل القصيرة SMS
     */
    fun triggerSingleTxSms(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildSingleTxShareBody(context, tx, customer, netDebt, currencySymbol, allCustomerTxs)
        sendSmsReliably(context, customer.phone, body, R.string.habayeb_tx_send_notice)
    }

    /*
     * إرسال إشعار معاملة مفردة عبر واتساب
     */
    fun triggerSingleTxWhatsApp(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildSingleTxShareBody(context, tx, customer, netDebt, currencySymbol, allCustomerTxs)
        try {
            val waUrl = "https://wa.me/${customer.phone.replace("+", "").replace(" ", "")}?text=${Uri.encode(body)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.habayeb_tx_whatsapp_choose)))
        }
    }

    /*
     * مشاركة ملف مباشر (مثل PDF كشف الحساب) عبر واتساب لجهة الاتصال المحددة
     */
    fun triggerWhatsAppDirectFile(
        context: Context,
        customer: HabayebCustomer,
        file: java.io.File,
        mimeType: String
    ) {
        if (customer.phone.isBlank()) return
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            val cleanPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
            val jid = "$cleanPhone@s.whatsapp.net"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("jid", jid)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            try {
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    intent.setPackage("com.whatsapp.w4b")
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    intent.setPackage(null)
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.pdf_chooser_title)))
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, context.getString(R.string.toast_operation_failed), android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

