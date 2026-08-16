package com.example.data.serialization

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.serialization.pdf.PdfColors
import com.example.data.serialization.pdf.PdfDrawingUtils
import com.example.data.serialization.pdf.PdfPageRenderer
import com.example.data.serialization.pdf.PdfReportCalculator
import com.example.data.serialization.pdf.PdfRowRenderer
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.state.CustomerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.util.Date

enum class PdfAction {
    SHARE,
    VIEW,
    SAVE_LOCAL,
    WHATSAPP_DIRECT;

    companion object {
        fun from(action: String): PdfAction {
            return values().find { it.name.equals(action, ignoreCase = true) } ?: SHARE
        }
    }
}

data class BusinessHeaderData(
    val displayedName: String,
    val displayedDesc: String,
    val phonesStr: String,
    val hasLogo: Boolean,
    val logoW: Float,
    val logoH: Float,
    val scaledLogo: Bitmap?,
    val rawBitmap: Bitmap?
)

object BusinessProfileLoader {
    private const val TAG = "BusinessProfileLoader"
    private const val PREF_BUSINESS_PROFILE = "business_profile"
    private const val PREF_BUSINESS_PROFILE_ALT = "business_profile_prefs"
    private const val KEY_BIZ_NAME = "biz_name"
    private const val KEY_BIZ_DESC = "biz_desc"
    private const val KEY_BIZ_LOGO_PATH = "biz_logo_path"
    private const val KEY_BIZ_PHONES = "biz_phones"
    private const val KEY_ALT_NAME = "business_name"
    private const val KEY_ALT_SLOGAN = "business_slogan"
    private const val KEY_ALT_LOGO_PATH = "logo_path"
    private const val KEY_ALT_PHONE = "business_phone"

    fun load(context: Context): BusinessHeaderData {
        val prefs = context.getSharedPreferences(PREF_BUSINESS_PROFILE, Context.MODE_PRIVATE)
        val altPrefs = context.getSharedPreferences(PREF_BUSINESS_PROFILE_ALT, Context.MODE_PRIVATE)

        var bizName = prefs.getString(KEY_BIZ_NAME, "")?.trim().orEmpty()
        if (bizName.isBlank()) {
            bizName = altPrefs.getString(KEY_ALT_NAME, "")?.trim().orEmpty()
        }

        var bizDesc = prefs.getString(KEY_BIZ_DESC, "")?.trim().orEmpty()
        if (bizDesc.isBlank()) {
            bizDesc = altPrefs.getString(KEY_ALT_SLOGAN, "")?.trim().orEmpty()
        }

        var bizLogoPath = prefs.getString(KEY_BIZ_LOGO_PATH, "")?.trim().orEmpty()
        if (bizLogoPath.isBlank()) {
            bizLogoPath = altPrefs.getString(KEY_ALT_LOGO_PATH, "")?.trim().orEmpty()
        }

        val bizPhones = mutableListOf<String>()
        try {
            val phonesJson = prefs.getString(KEY_BIZ_PHONES, "[]") ?: "[]"
            val jsonArray = JSONArray(phonesJson)
            for (i in 0 until jsonArray.length()) {
                val p = jsonArray.getString(i).trim()
                if (p.isNotBlank()) bizPhones.add(p)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading business phones", e)
        }

        if (bizPhones.isEmpty()) {
            val altPhone = altPrefs.getString(KEY_ALT_PHONE, "")?.trim().orEmpty()
            if (altPhone.isNotBlank()) {
                bizPhones.add(altPhone)
            }
        }

        val displayedName = if (bizName.isNotBlank()) bizName else context.getString(R.string.app_name)
        val displayedDesc = if (bizDesc.isNotBlank()) bizDesc else context.getString(R.string.pdf_default_desc)

        val logoResult = PdfDrawingUtils.loadAndScaleLogo(context, bizLogoPath)
        val phonesToDraw = if (bizPhones.isNotEmpty()) bizPhones else listOf(context.getString(R.string.pdf_certified_identity))
        val phonesStr = if (bizPhones.isNotEmpty()) context.getString(R.string.pdf_phone_prefix) + " " + phonesToDraw.joinToString(" - ") else phonesToDraw.joinToString(" - ")

        return BusinessHeaderData(
            displayedName = displayedName,
            displayedDesc = displayedDesc,
            phonesStr = phonesStr,
            hasLogo = logoResult.hasLogo,
            logoW = logoResult.width,
            logoH = logoResult.height,
            scaledLogo = logoResult.bitmap,
            rawBitmap = logoResult.rawBitmapToRecycle
        )
    }
}

object PdfReportGenerator {
    private const val TAG = "PdfReportGenerator"
    private const val MIME_TYPE_PDF = "application/pdf"
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    private fun generatePdfFileInternal(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ): File? {
        val summary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
        val customerUiState = CustomerUiState(
            id = customer.id.toString(),
            name = customer.name,
            phone = customer.phone,
            originalCustomer = customer
        )

        // 1. Dry Run to calculate EXACT total pages based on dynamic StaticLayout heights
        var totalPages = 1
        var dryPageCount = 1
        PdfPageRenderer.drawCustomerStatementSheet(
            canvas = null,
            context = context,
            customer = customerUiState,
            summary = summary,
            startY = 98f,
            primaryColorHex = primaryColorHex,
            currencySymbol = currencySymbol,
            isDryRun = true,
            includeCustomerHeaderBanner = false,
            onPageBreakNeeded = { _ ->
                dryPageCount++
                null
            }
        )
        totalPages = dryPageCount

        val header = BusinessProfileLoader.load(context)
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        return try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val now = Date()
            PdfPageRenderer.drawBusinessHeader(
                canvas = canvas,
                displayedName = header.displayedName,
                displayedDesc = header.displayedDesc,
                phonesStr = header.phonesStr,
                hasLogo = header.hasLogo,
                scaledLogo = header.scaledLogo,
                logoW = header.logoW,
                logoH = header.logoH,
                docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now)),
                docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))
            )

            val paintTitle = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_DARK)
                textSize = 14.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_statement_title, customer.name), 25f, 74f, 545, paintTitle, Layout.Alignment.ALIGN_CENTER)

            // Table Header starting at Y = 98f
            PdfPageRenderer.drawCustomerStatementSheet(
                canvas = canvas,
                context = context,
                customer = customerUiState,
                summary = summary,
                startY = 98f,
                primaryColorHex = primaryColorHex,
                currencySymbol = currencySymbol,
                isDryRun = false,
                includeCustomerHeaderBanner = false,
                onPageBreakNeeded = { newHeader ->
                    PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    if (newHeader) {
                        PdfPageRenderer.drawSubsequentPageHeader(canvas, customer.name, primaryColorHex, context)
                        PdfPageRenderer.drawTableHeader(canvas, 32f, context, customer.initialType)
                    }
                    canvas
                }
            )

            PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
            pdfDocument.finishPage(page)

            val sanitizedName = customer.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val fileName = "habayeb_${sanitizedName}_${System.currentTimeMillis() % 100000}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating customer PDF", e)
            null
        } finally {
            pdfDocument.close()
            recycleBitmapsSafely(header.rawBitmap, header.scaledLogo)
        }
    }

    private fun generateAllCustomersPdfFileInternal(
        context: Context,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ): File? {
        val summary = PdfReportCalculator.calculateComprehensiveReport(customers)
        val totalItems = customers.size

        // 1. Dry Run to calculate EXACT total pages based on dynamic customer summary row heights
        var totalPages = 1
        run {
            var dryY = 276f
            var dryPages = 1
            for (c in customers) {
                val rowHeight = PdfRowRenderer.calculateCustomerSummaryRowHeight(context, c)
                if (dryY + rowHeight > 760f) {
                    dryPages++
                    dryY = 76f
                }
                dryY += rowHeight
            }
            totalPages = dryPages
        }

        val header = BusinessProfileLoader.load(context)
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        return try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val now = Date()
            val docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now))
            val docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))

            PdfPageRenderer.drawBusinessHeader(
                canvas = canvas,
                displayedName = header.displayedName,
                displayedDesc = header.displayedDesc,
                phonesStr = header.phonesStr,
                hasLogo = header.hasLogo,
                scaledLogo = header.scaledLogo,
                logoW = header.logoW,
                logoH = header.logoH,
                docDateText = docDateText,
                docTimeText = docTimeText
            )

            val paintTitle = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_DARK)
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_comprehensive_report_title), 25f, 126f, 545, paintTitle, Layout.Alignment.ALIGN_CENTER)

            PdfRowRenderer.drawComprehensiveSummaryCard(
                canvas = canvas,
                context = context,
                primaryColorHex = primaryColorHex,
                summary = summary,
                totalItems = totalItems,
                currencySymbol = currencySymbol
            )

            PdfPageRenderer.drawAllCustomersTableHeader(canvas, 246f, context)

            var currentY = 276f

            for ((index, c) in customers.withIndex()) {
                val rowHeight = PdfRowRenderer.calculateCustomerSummaryRowHeight(context, c)
                if (currentY + rowHeight > 760f) {
                    PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    val paintMiniHeader = Paint().apply {
                        color = Color.parseColor(primaryColorHex)
                        textSize = 9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_comprehensive_report_subpage, currentPageNumber), 25f, 22f, 545, paintMiniHeader, Layout.Alignment.ALIGN_NORMAL)

                    val paintMiniLine = Paint().apply {
                        color = Color.parseColor(PdfColors.HEADER_BORDER)
                        strokeWidth = 0.5f
                        style = Paint.Style.STROKE
                    }
                    canvas.drawLine(25f, 34f, 570f, 34f, paintMiniLine)

                    currentY = 46f
                    PdfPageRenderer.drawAllCustomersTableHeader(canvas, currentY, context)
                    currentY += 30f
                }

                PdfRowRenderer.drawCustomerSummaryRow(canvas, context, index, c, currentY, rowHeight, currencySymbol)
                currentY += rowHeight
            }

            PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
            pdfDocument.finishPage(page)

            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "Statement_Comprehensive_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating all customers PDF", e)
            null
        } finally {
            pdfDocument.close()
            recycleBitmapsSafely(header.rawBitmap, header.scaledLogo)
        }
    }

    private fun recycleBitmapsSafely(rawBitmap: Bitmap?, scaledLogo: Bitmap?) {
        try {
            if (rawBitmap != null && !rawBitmap.isRecycled) {
                if (scaledLogo != null && scaledLogo != rawBitmap && !scaledLogo.isRecycled) {
                    scaledLogo.recycle()
                }
                rawBitmap.recycle()
            } else if (scaledLogo != null && !scaledLogo.isRecycled) {
                scaledLogo.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recycling bitmaps", e)
        }
    }

    fun triggerShareOrViewIntent(context: Context, file: File?, action: PdfAction) {
        if (file == null) {
            Toast.makeText(context, context.getString(R.string.habayeb_toast_pdf_export_failed, context.getString(R.string.csv_error_creating_file)), Toast.LENGTH_LONG).show()
            return
        }
        try {
            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            val uri = FileProvider.getUriForFile(context, authority, file)

            when (action) {
                PdfAction.SHARE -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = MIME_TYPE_PDF
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.pdf_chooser_title)))
                }
                PdfAction.VIEW -> {
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, MIME_TYPE_PDF)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(viewIntent)
                }
                else -> {}
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.habayeb_toast_pdf_export_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    fun triggerShareOrViewIntent(context: Context, file: File?, action: String) {
        triggerShareOrViewIntent(context, file, PdfAction.from(action))
    }

    fun generateAndHandleCustomerPdfReport(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ) {
        generateAndHandleCustomerPdfReportAsync(context, scope, customer, transactions, currencySymbol, PdfAction.from(action), primaryColorHex)
    }

    fun generateAndHandleCustomerPdfReport(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ) {
        generateAndHandleCustomerPdfReportAsync(
            context,
            CoroutineScope(Dispatchers.Main),
            customer,
            transactions,
            currencySymbol,
            PdfAction.from(action),
            primaryColorHex
        )
    }

    fun generateAndHandleCustomerPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: PdfAction,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = generatePdfFileInternal(context, customer, transactions, currencySymbol, primaryColorHex)
                withContext(Dispatchers.Main) {
                    if (action == PdfAction.SAVE_LOCAL) {
                        if (file != null) {
                            com.example.ui.helper.LocalFileSaver.saveAndShowToast(
                                context = context,
                                cachedFile = file,
                                mimeType = MIME_TYPE_PDF,
                                displayName = file.name
                            )
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else if (action == PdfAction.WHATSAPP_DIRECT) {
                        if (file != null) {
                            com.example.ui.screens.habayeb.utils.CustomerShareHelper.triggerWhatsAppDirectFile(
                                context = context,
                                customer = customer,
                                file = file,
                                mimeType = MIME_TYPE_PDF
                            )
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        triggerShareOrViewIntent(context, file, action)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating customer PDF async", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    fun generateAndHandleCustomerPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleCustomerPdfReportAsync(
            context, scope, customer, transactions, currencySymbol, PdfAction.from(action), primaryColorHex, onFinished
        )
    }

    fun generateAndHandleAllCustomersPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        action: PdfAction,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = generateAllCustomersPdfFileInternal(context, customers, currencySymbol, primaryColorHex)
                withContext(Dispatchers.Main) {
                    triggerShareOrViewIntent(context, file, action)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating all customers PDF async", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    fun generateAndHandleAllCustomersPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleAllCustomersPdfReportAsync(
            context, scope, customers, currencySymbol, PdfAction.from(action), primaryColorHex, onFinished
        )
    }
}

