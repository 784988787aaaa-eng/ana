package com.example.data.serialization

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.serialization.pdf.PdfReportCalculator
import com.example.domain.model.TransactionType
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.viewmodel.FinanceConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise-grade RFC 4180 compliant CSV report generator for financial statements.
 * Ensures zero data loss, UTF-8 BOM for Microsoft Excel Arabic support, and exact BigDecimal accounting precision.
 */
object CsvReportGenerator {
    private const val TAG = "CsvReportGenerator"
    private const val LOCALE_AR = "ar"
    private const val DEFAULT_EXCHANGE_RATES_JSON = "{}"
    private const val DEFAULT_EXCHANGE_RATE_STR = "1.00"
    private const val FILE_PREFIX = "statement_"
    private const val MIME_TYPE_CSV = "text/comma-separated-values"
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"
    private const val CRLF = "\r\n"

    private val DATE_FORMATTER_SHORT = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_AR)) }
    private val DATE_FORMATTER_LONG = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale(LOCALE_AR)) }

    enum class CsvAction {
        SHARE,
        SAVE_LOCAL,
        WHATSAPP_DIRECT;

        companion object {
            fun from(action: String): CsvAction {
                return values().find { it.name.equals(action, ignoreCase = true) } ?: SHARE
            }
        }
    }

    /**
     * RFC 4180 compliant cell escaping:
     * - Encloses text in double quotes.
     * - Escapes internal quotes by doubling them (" -> "").
     * - Preserves commas (both Arabic and English) and newlines safely within quoted cell boundaries.
     */
    fun String?.escapeCsv(): String {
        if (this == null) return "\"\""
        val escaped = this.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Formats BigDecimal with exact plain decimal notation (no scientific exponents).
     */
    fun BigDecimal?.toCsvDecimal(): String {
        if (this == null) return "0.00"
        return this.toPlainString()
    }

    fun generateAndShareCsvReport(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String = DEFAULT_EXCHANGE_RATES_JSON,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleCsvReportAsync(
            context,
            scope,
            customer,
            transactions,
            currencySymbol,
            exchangeRatesJson,
            CsvAction.SHARE,
            onFinished
        )
    }

    fun generateAndHandleCsvReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String = DEFAULT_EXCHANGE_RATES_JSON,
        action: CsvAction,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = generateCsvFileInternal(context, customer, transactions, currencySymbol, exchangeRatesJson)
                withContext(Dispatchers.Main) {
                    if (file != null) {
                        when (action) {
                            CsvAction.SAVE_LOCAL -> {
                                com.example.ui.helper.LocalFileSaver.saveAndShowToast(
                                    context = context,
                                    cachedFile = file,
                                    mimeType = MIME_TYPE_CSV,
                                    displayName = file.name
                                )
                            }
                            CsvAction.WHATSAPP_DIRECT -> {
                                com.example.ui.screens.habayeb.utils.CustomerShareHelper.triggerWhatsAppDirectFile(
                                    context = context,
                                    customer = customer,
                                    file = file,
                                    mimeType = MIME_TYPE_CSV
                                )
                            }
                            CsvAction.SHARE -> {
                                triggerShareIntent(context, file, customer.name)
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.habayeb_export_csv_failed, context.getString(R.string.csv_error_creating_file)),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating CSV", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    private fun generateCsvFileInternal(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String
    ): File? {
        val sanitizedName = customer.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        val fileName = "${FILE_PREFIX}${sanitizedName}_${System.currentTimeMillis() % 100000}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                // Write standard UTF-8 Byte Order Mark (BOM) so Microsoft Excel opens Arabic natively
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                fos.bufferedWriter(Charsets.UTF_8).use { writer ->
                    // 1. Business Profile Header Section
                    val bizHeader = BusinessProfileLoader.load(context)
                    if (bizHeader.displayedName.isNotBlank()) {
                        writer.write(bizHeader.displayedName.escapeCsv() + CRLF)
                    }
                    if (bizHeader.displayedDesc.isNotBlank()) {
                        writer.write(bizHeader.displayedDesc.escapeCsv() + CRLF)
                    }
                    if (bizHeader.phonesStr.isNotBlank()) {
                        writer.write(bizHeader.phonesStr.escapeCsv() + CRLF)
                    }
                    writer.write(CRLF)

                    // 2. Customer Metadata Section
                    val reportTitle = context.getString(R.string.csv_report_for_account, customer.name)
                    writer.write(reportTitle.escapeCsv() + CRLF)

                    val notRegStr = context.getString(R.string.csv_not_registered)
                    val phoneStr = context.getString(R.string.csv_phone_label, customer.phone.ifEmpty { notRegStr })
                    writer.write(phoneStr.escapeCsv() + CRLF)

                    val dateFormatted = DATE_FORMATTER_SHORT.get().format(Date())
                    val dateStr = context.getString(R.string.csv_report_date, dateFormatted)
                    writer.write(dateStr.escapeCsv() + CRLF)
                    writer.write(CRLF)

                    // 3. Table Column Headers (RFC 4180 standard)
                    val isOwedToThemAccount = customer.initialType == TransactionType.OWED_TO_THEM.value
                    val colHeaders = listOf(
                        context.getString(R.string.pdf_col_m),
                        context.getString(R.string.pdf_col_date),
                        context.getString(R.string.pdf_col_description),
                        context.getString(R.string.habayeb_filter_by_type).trimEnd(':'),
                        context.getString(R.string.habayeb_col_amount),
                        context.getString(R.string.pdf_col_other_currencies),
                        context.getString(R.string.trash_exchange_rate_label).trimEnd(':'),
                        context.getString(R.string.trash_equivalent_info, currencySymbol, "").replace("()", "").trim(),
                        context.getString(R.string.pdf_col_remaining) + " ($currencySymbol)"
                    )
                    writer.write(colHeaders.joinToString(",") { it.escapeCsv() } + CRLF)

                    // 4. Chronological Transaction Processing with Exact BigDecimal Balances
                    val normDefaultSymbol = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol
                    val chronological = transactions.sortedWith(compareBy<HabayebTransaction> { it.timestamp }.thenBy { it.id })
                    var runningBalance = BigDecimal.ZERO

                    chronological.forEachIndexed { index, tx ->
                        val (resolvedCurrency, resolvedAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(
                            tx, currencySymbol, exchangeRatesJson
                        )
                        val isTxForeign = resolvedCurrency != normDefaultSymbol

                        val baseCurrencyAmount = if (isTxForeign) {
                            if (tx.isRateCalculated) resolvedAmount else BigDecimal.ZERO
                        } else {
                            resolvedAmount
                        }

                        val txType = TransactionType.fromValue(tx.type)
                        val isCol4 = if (isOwedToThemAccount) {
                            txType == TransactionType.OWED_TO_THEM || txType == TransactionType.PAYMENT_BY_THEM
                        } else {
                            txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM
                        }

                        if (isCol4) {
                            runningBalance = runningBalance.add(baseCurrencyAmount)
                        } else {
                            runningBalance = runningBalance.subtract(baseCurrencyAmount)
                        }

                        val typeName = when (txType) {
                            TransactionType.OWED_BY_THEM -> context.getString(R.string.pdf_tx_type_owed_by_them)
                            TransactionType.PAYMENT_BY_THEM -> if (isOwedToThemAccount) context.getString(R.string.pdf_tx_type_payment_to_them) else context.getString(R.string.pdf_tx_type_payment_by_them)
                            TransactionType.OWED_TO_THEM -> context.getString(R.string.pdf_tx_type_owed_to_them)
                            TransactionType.PAYMENT_TO_THEM -> context.getString(R.string.pdf_tx_type_payment_to_them)
                            else -> context.getString(R.string.pdf_tx_type_new)
                        }

                        val txDateStr = DATE_FORMATTER_LONG.get().format(Date(if (tx.timestamp > 1000000000000L) tx.timestamp else tx.timestamp * 1000))
                        val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)

                        val txCurrencyStr = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) tx.currencyCode else currencySymbol
                        val displayAmount = tx.foreignAmount.toCsvDecimal()
                        val exchangeRateStr = if (isTxForeign && tx.isRateCalculated) tx.exchangeRate.toCsvDecimal() else DEFAULT_EXCHANGE_RATE_STR
                        val equivAmountStr = if (isTxForeign && tx.isRateCalculated) tx.equivalentAmount.toCsvDecimal() else tx.foreignAmount.toCsvDecimal()

                        val rowCells = listOf(
                            (index + 1).toString().escapeCsv(),
                            txDateStr.escapeCsv(),
                            cleanDetails.escapeCsv(),
                            typeName.escapeCsv(),
                            displayAmount.escapeCsv(),
                            txCurrencyStr.escapeCsv(),
                            exchangeRateStr.escapeCsv(),
                            equivAmountStr.escapeCsv(),
                            runningBalance.toCsvDecimal().escapeCsv()
                        )
                        writer.write(rowCells.joinToString(",") + CRLF)
                    }

                    // 5. Final Financial Summary Block (Totals & Multicurrency Integrity)
                    val summary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                    writer.write(CRLF)
                    writer.write(listOf(context.getString(R.string.pdf_summary_independent_totals).escapeCsv()).joinToString(",") + CRLF)

                    val debitLabel = if (isOwedToThemAccount) context.getString(R.string.pdf_card_owed_by_them_to_them_dynamic) else context.getString(R.string.pdf_card_owed_by_them)
                    writer.write(listOf(debitLabel.escapeCsv(), summary.totalDebts.toCsvDecimal().escapeCsv(), currencySymbol.escapeCsv()).joinToString(",") + CRLF)

                    val creditLabel = if (isOwedToThemAccount) context.getString(R.string.pdf_card_owed_to_them_to_them_dynamic) else context.getString(R.string.pdf_card_owed_to_them)
                    writer.write(listOf(creditLabel.escapeCsv(), summary.totalPayments.toCsvDecimal().escapeCsv(), currencySymbol.escapeCsv()).joinToString(",") + CRLF)

                    val netBalanceLabel = context.getString(R.string.pdf_card_net_remaining)
                    writer.write(listOf(netBalanceLabel.escapeCsv(), summary.calculatedNetDebt.toCsvDecimal().escapeCsv(), currencySymbol.escapeCsv()).joinToString(",") + CRLF)

                    val statusStr = when {
                        summary.calculatedNetDebt > BigDecimal.ZERO -> if (isOwedToThemAccount) context.getString(R.string.pdf_status_to_him_word) else context.getString(R.string.pdf_status_owed_word)
                        summary.calculatedNetDebt < BigDecimal.ZERO -> if (isOwedToThemAccount) context.getString(R.string.pdf_status_owed_word) else context.getString(R.string.pdf_status_to_him_word)
                        else -> context.getString(R.string.pdf_status_balanced_word)
                    }
                    writer.write(listOf(context.getString(R.string.pdf_col_status).escapeCsv(), statusStr.escapeCsv()).joinToString(",") + CRLF)

                    // 6. Foreign Currencies Breakdown (if any uncalculated currency amounts exist)
                    if (summary.uncalculatedForeignSums.isNotEmpty()) {
                        writer.write(CRLF)
                        writer.write(listOf(context.getString(R.string.pdf_independent_totals_uncalculated).escapeCsv()).joinToString(",") + CRLF)
                        for ((curr, bal) in summary.uncalculatedForeignSums) {
                            val currStatus = when {
                                bal > BigDecimal.ZERO -> if (isOwedToThemAccount) context.getString(R.string.pdf_status_to_him_word) else context.getString(R.string.pdf_status_owed_word)
                                bal < BigDecimal.ZERO -> if (isOwedToThemAccount) context.getString(R.string.pdf_status_owed_word) else context.getString(R.string.pdf_status_to_him_word)
                                else -> context.getString(R.string.pdf_status_balanced_word)
                            }
                            writer.write(listOf(curr.escapeCsv(), bal.toCsvDecimal().escapeCsv(), currStatus.escapeCsv()).joinToString(",") + CRLF)
                        }
                    }

                    writer.flush()
                }
            }
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing CSV file", e)
            return null
        }
    }

    private fun triggerShareIntent(context: Context, file: File, customerName: String) {
        try {
            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_CSV
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.csv_share_subject, customerName))
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.csv_share_text, customerName))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.csv_share_chooser_title)))
            Toast.makeText(context, context.getString(R.string.habayeb_export_csv_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share CSV", e)
            Toast.makeText(context, context.getString(R.string.habayeb_export_csv_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }
}
