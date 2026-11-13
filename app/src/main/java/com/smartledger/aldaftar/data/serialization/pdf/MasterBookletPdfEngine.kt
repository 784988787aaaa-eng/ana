package com.smartledger.aldaftar.data.serialization.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.util.Log
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.state.CustomerUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlin.coroutines.coroutineContext

data class BusinessProfileData(
    val name: String,
    val desc: String,
    val phonesStr: String,
    val scaledLogo: Bitmap?,
    val hasLogo: Boolean,
    val logoW: Float,
    val logoH: Float
)

data class PdfReportMetaData(
    val docDateText: String,
    val docTimeText: String,
    val currencySymbol: String,
    val primaryColorHex: String
)

private const val TAG = "MasterBookletPdfEngine"

object MasterBookletPdfEngine {


    suspend fun generateBookletPdfAsync(
        context: Context,
        allCustomers: List<CustomerUiState>,
        selectedIds: List<String>,
        onlySelected: Boolean,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        businessProfile: com.smartledger.aldaftar.data.local.entities.BusinessProfile,
        transactionsByCustomer: Map<String, List<com.smartledger.aldaftar.data.local.entities.HabayebTransaction>>,
        onProgress: (processed: Int, total: Int) -> Unit,
        onFinished: (File?) -> Unit,
        onCancelled: () -> Unit = {}
    ) = withContext(Dispatchers.IO) {

        var scaledLogoToRecycle: android.graphics.Bitmap? = null
        var rawBitmapToRecycle: android.graphics.Bitmap? = null
        try {
            val targetCustomers = if (onlySelected && selectedIds.isNotEmpty()) {
                allCustomers.filter { selectedIds.contains(it.id) }
            } else {
                allCustomers
            }

            val totalCustomers = targetCustomers.size
            if (totalCustomers == 0) {
                withContext(Dispatchers.Main) {
                    onFinished(null)
                }
                return@withContext
            }

            val header = BusinessProfileLoader.load(context, businessProfile)
            scaledLogoToRecycle = header.scaledLogo
            rawBitmapToRecycle = header.rawBitmap

            val now = Date()
            val docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now))
            val docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))

            val businessProfile = BusinessProfileData(
                name = header.displayedName,
                desc = header.displayedDesc,
                phonesStr = header.phonesStr,
                scaledLogo = header.scaledLogo,
                hasLogo = header.hasLogo,
                logoW = header.logoW,
                logoH = header.logoH
            )

            val reportMetaData = PdfReportMetaData(
                docDateText = docDateText,
                docTimeText = docTimeText,
                currencySymbol = currencySymbol,
                primaryColorHex = primaryColorHex
            )

            val txCacheMap = mutableMapOf<String, List<com.smartledger.aldaftar.data.local.entities.HabayebTransaction>>()

            var totalPagesInDryRun = 1
            run {
                val dryDoc = PdfDocument()
                var dryCtx: BookletDrawingContext? = null
                try {
                    dryCtx = BookletDrawingContext(
                        context = context,
                        pdfDocument = dryDoc,
                        isDryRun = true,
                        totalPagesInDryRun = 1,
                        businessProfile = businessProfile,
                        reportMetaData = reportMetaData
                    )

                    val headerBottomY = PdfPageRenderer.calculateHeaderBottomY(
                        dryCtx.displayedName, dryCtx.displayedDesc, dryCtx.phonesStr, dryCtx.hasLogo, dryCtx.logoH
                    )
                    dryCtx.currentY = headerBottomY

                    val customerChunks = targetCustomers.chunked(50)
                    var processedCount = 0
                    for (chunk in customerChunks) {
                        coroutineContext.ensureActive()
                        for (customer in chunk) {
                            coroutineContext.ensureActive()
                            val transactions = txCacheMap.getOrPut(customer.id) {
                                transactionsByCustomer[customer.id].orEmpty()
                            }
                            val singleSummary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                            drawCustomerLedgerSheet(dryCtx, customer, singleSummary)
                            processedCount++
                        }
                    }
                    dryCtx.finishLastPage()
                    totalPagesInDryRun = dryCtx.currentPageNumber - 1
                } finally {
                    dryCtx?.closeSafely() ?: run {
                        try { dryDoc.close() } catch (t: Throwable) {}
                    }
                }
            }

            coroutineContext.ensureActive()

            val pdfDocument = PdfDocument()
            var realCtx: BookletDrawingContext? = null
            val outputFile = try {
                realCtx = BookletDrawingContext(
                    context = context,
                    pdfDocument = pdfDocument,
                    isDryRun = false,
                    totalPagesInDryRun = totalPagesInDryRun,
                    businessProfile = businessProfile,
                    reportMetaData = reportMetaData
                )

                val canvas = realCtx.currentPageCanvas
                if (canvas != null) {
                    val headerBottomY = PdfPageRenderer.drawBusinessHeader(
                        canvas, realCtx.displayedName, realCtx.displayedDesc, realCtx.phonesStr,
                        realCtx.hasLogo, realCtx.scaledLogo, realCtx.logoW, realCtx.logoH, realCtx.docDateText, realCtx.docTimeText
                    )
                    realCtx.currentY = headerBottomY
                }

                val customerChunks = targetCustomers.chunked(50)
                var processedCount = 0
                for (chunk in customerChunks) {
                    coroutineContext.ensureActive()
                    for (customer in chunk) {
                        coroutineContext.ensureActive()
                        val transactions = txCacheMap[customer.id]
                            ?: transactionsByCustomer[customer.id].orEmpty()
                        val singleSummary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                        drawCustomerLedgerSheet(realCtx, customer, singleSummary)
                        processedCount++
                        withContext(Dispatchers.Main) {
                            onProgress(processedCount, totalCustomers)
                        }
                    }
                }

                realCtx.finishLastPage()

                val outputDir = File(context.cacheDir, "pdf_reports")
                if (!outputDir.exists()) outputDir.mkdirs()
                val file = File(outputDir, "MasterBookletReport_${System.currentTimeMillis()}.pdf")
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    outputStream.flush()
                }
                file
            } finally {
                realCtx?.closeSafely() ?: run {
                    try { pdfDocument.close() } catch (t: Throwable) {}
                }
            }

            withContext(Dispatchers.Main) {
                onFinished(outputFile)
            }

        } catch (e: CancellationException) {
            Log.i(TAG, "Master booklet PDF generation cancelled by user")
            withContext(NonCancellable + Dispatchers.Main.immediate) {
                onCancelled()
            }
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating booklet PDF", e)
            withContext(Dispatchers.Main) {
                onFinished(null)
            }
        } finally {
            try {
                if (rawBitmapToRecycle != null && !rawBitmapToRecycle.isRecycled) {
                    if (scaledLogoToRecycle != null && scaledLogoToRecycle != rawBitmapToRecycle && !scaledLogoToRecycle.isRecycled) {
                        scaledLogoToRecycle.recycle()
                    }
                    rawBitmapToRecycle.recycle()
                } else if (scaledLogoToRecycle != null && !scaledLogoToRecycle.isRecycled) {
                    scaledLogoToRecycle.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recycling bitmaps", e)
            }
        }
    }

    private fun drawCustomerLedgerSheet(
        ctx: BookletDrawingContext,
        customer: CustomerUiState,
        summary: com.smartledger.aldaftar.data.serialization.pdf.SingleCustomerPdfSummary
    ) {
        if (ctx.currentY > 150f) {
            if (ctx.currentY + 200f > 780f) {
                ctx.startNewPage()
                ctx.currentY = 42f
            } else {
                ctx.currentY += 16f
            }
        }
        ctx.currentY = PdfPageRenderer.drawCustomerStatementSheet(
            canvas = ctx.currentPageCanvas,
            context = ctx.context,
            customer = customer,
            summary = summary,
            startY = ctx.currentY,
            primaryColorHex = ctx.primaryColorHex,
            currencySymbol = ctx.currencySymbol,
            isDryRun = ctx.isDryRun,
            includeCustomerHeaderBanner = true,
            onPageBreakNeeded = { newHeader ->
                ctx.startNewPage()
                if (newHeader && !ctx.isDryRun) {
                    ctx.currentPageCanvas?.let { currentCanvas ->
                        PdfPageRenderer.drawSubsequentPageHeader(currentCanvas, customer.name, ctx.primaryColorHex, ctx.context)
                        PdfPageRenderer.drawTableHeader(currentCanvas, 45f, ctx.context, customer.originalCustomer.initialType)
                    }
                }
                ctx.currentPageCanvas
            }
        )
    }
}

class BookletDrawingContext(
    val context: Context,
    val pdfDocument: PdfDocument,
    val isDryRun: Boolean,
    val totalPagesInDryRun: Int,
    val businessProfile: BusinessProfileData,
    val reportMetaData: PdfReportMetaData
) {
    val displayedName: String get() = businessProfile.name
    val displayedDesc: String get() = businessProfile.desc
    val phonesStr: String get() = businessProfile.phonesStr
    val scaledLogo: Bitmap? get() = businessProfile.scaledLogo
    val hasLogo: Boolean get() = businessProfile.hasLogo
    val logoW: Float get() = businessProfile.logoW
    val logoH: Float get() = businessProfile.logoH

    val docDateText: String get() = reportMetaData.docDateText
    val docTimeText: String get() = reportMetaData.docTimeText
    val currencySymbol: String get() = reportMetaData.currencySymbol
    val primaryColorHex: String get() = reportMetaData.primaryColorHex
    var currentPageNumber = 1
    var currentY = 42f
    var currentPageCanvas: Canvas? = null
    var currentPageObject: PdfDocument.Page? = null

    init {
        startNewPage()
    }

    fun startNewPage() {
        if (!isDryRun) {
            drawFooterOnCurrentPage()
            currentPageObject?.let { pdfDocument.finishPage(it) }

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            currentPageCanvas = page.canvas
            currentPageObject = page
        }
        currentPageNumber++
        currentY = 42f
    }

    private fun drawFooterOnCurrentPage() {
        val canvas = currentPageCanvas ?: return
        PdfPageRenderer.drawFooter(
            canvas,
            currentPageNumber - 1,
            totalPagesInDryRun,
            primaryColorHex,
            context
        )
    }

    fun finishLastPage() {
        if (!isDryRun) {
            drawFooterOnCurrentPage()
            currentPageObject?.let {
                pdfDocument.finishPage(it)
                currentPageObject = null
                currentPageCanvas = null
            }
        }
    }

    fun closeSafely() {
        if (!isDryRun) {
            try {
                currentPageObject?.let {
                    pdfDocument.finishPage(it)
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Safe close page finish warning: ${t.message}")
            }
            currentPageObject = null
            currentPageCanvas = null
        }
        try {
            pdfDocument.close()
        } catch (t: Throwable) {
            Log.w(TAG, "Safe close pdf document warning: ${t.message}")
        }
    }
}
