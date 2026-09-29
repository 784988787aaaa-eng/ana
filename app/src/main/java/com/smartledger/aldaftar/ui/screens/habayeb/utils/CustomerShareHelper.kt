package com.smartledger.aldaftar.ui.screens.habayeb.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.notifications.TransactionNotificationBuilder
import com.smartledger.aldaftar.ui.helper.formatCurrency

object CustomerShareHelper {

    private fun sendSmsReliably(context: Context, rawPhone: String, body: String, fallbackChooserTitleId: Int) {
        val cleanPhone = rawPhone.replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace("[", "")
            .replace("]", "")
        
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(if (cleanPhone.isBlank()) "smsto:" else "smsto:$cleanPhone")
                putExtra("sms_body", body)
                putExtra("body", body)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e1: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (cleanPhone.isBlank()) "sms:" else "sms:$cleanPhone")
                    putExtra("sms_body", body)
                    putExtra("body", body)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(fallbackChooserTitleId)))
            }
        }
    }

    fun resolveTxTypeTitle(context: Context, txType: String, isAccountOwedToThem: Boolean): String {
        return when (txType) {
            "OWED_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_by)
            "PAYMENT_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_by)
            "OWED_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_to)
            "PAYMENT_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_to)
            else -> context.getString(R.string.pdf_tx_type_new)
        }
    }

    fun buildSingleTxShareBody(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String {
        return TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = customer,
            netDebt = netDebt,
            currencySymbol = currencySymbol,
            allCustomerTxs = allCustomerTxs
        )
    }

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
                    val formattedForeignNet = com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatSmart(fNetBd.abs())
                    foreignLines.add("\n$foreignTotalPrefix $formattedForeignNet $fSymbol")
                }
            }
        }
        val foreignText = if (foreignLines.isNotEmpty()) foreignLines.joinToString("") + "\n" else ""

        return "$title• $debtStatus\n$foreignText$footer"
    }

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

    fun triggerSingleTxSms(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = TransactionNotificationBuilder.buildSmsNotification(
            tx = tx,
            customer = customer,
            netDebt = netDebt,
            currencySymbol = currencySymbol,
            allCustomerTxs = allCustomerTxs
        )
        sendSmsReliably(context, customer.phone, body, R.string.habayeb_tx_send_notice)
    }

    fun triggerSingleTxWhatsApp(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = customer,
            netDebt = netDebt,
            currencySymbol = currencySymbol,
            allCustomerTxs = allCustomerTxs
        )
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
