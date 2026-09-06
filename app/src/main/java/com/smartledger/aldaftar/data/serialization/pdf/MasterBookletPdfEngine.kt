/**
 * =====================================================================
 * ملف: محرك دفاتر وكشوفات الأستاذ العامة المجمعة (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المحرك المتقدم المسؤول عن تجميع وتوليد كتيب الحسابات الماستر
 * (   ) بصيغة  لعدة عملاء أو لجميع الحسابات
 * دفعة واحدة، مع تدفق سلس ومستمر للصفحات، وإدارة ذكية للذاكرة، وحساب دقيق لإجمالي
 * الصفحات عبر جولتين: تجريبية لحساب المقاسات وفعلية للرسم.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. المعالجة المجمعة المتدفقة (  & ):
 *    - تقسيم معالجة العملاء إلى دفعات (  50) مع إمكانية إلغاء الكوروتين [].
 * 2. التخزين المؤقت المحلي للعمليات ( - ):
 *    - استخدام [] لتفادي الاستعلام المتكرر من قاعدة البيانات بين الجولة التجريبية والفعلية.
 * 3. إدارة لوحات الرسم والصفحات وسياق الدفتر []:
 *    - إنشاء صفحات مقاس 4 ورسم الترويسة والفاصل السفلي لكل صفحة تلقائياً.
 * 4. إدارة الموارد وتدوير البيتماب (  ):
 *    - ضمان تدوير وتحرير صور الشعارات النقطية لمنع نفاد الذاكرة () في التقارير الضخمة.
 */
package com.smartledger.aldaftar.data.serialization.pdf

import android.os.Build
// ---------------------------------------------------------------------
// استيراد حزم أندرويد والرسومات وتوليد  وقواعد البيانات والكوروتين
// ---------------------------------------------------------------------
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.data.serialization.BusinessProfileLoader
import com.smartledger.aldaftar.ui.state.CustomerUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlin.coroutines.coroutineContext

/**
 * [وعاء بيانات هوية المنشأة للكتيب - ]:
 * يضم معلومات المنشأة وشعارها وأبعادها.
 */
data class BusinessProfileData(
    val name: String,
    val desc: String,
    val phonesStr: String,
    val scaledLogo: Bitmap?,
    val hasLogo: Boolean,
    val logoW: Float,
    val logoH: Float
)

/**
 * [البيانات الوصفية للتقرير المالي - ]:
 * يجمع التواريخ المنسقة والعملة ولون السمة الأساسي.
 */
data class PdfReportMetaData(
    val docDateText: String,
    val docTimeText: String,
    val currencySymbol: String,
    val primaryColorHex: String
)

/**
 * [الكائن الأحادي لمحرك دفتر الحسابات الماستر - ]:
 * يبني كتيبات وكشوفات الحسابات المجمعة لكافة العملاء أو المحدد منهم.
 */
object MasterBookletPdfEngine {

    private const val PREFS_BUSINESS_PROFILE = "business_profile"
    private const val PREF_BIZ_NAME = "biz_name"
    private const val PREF_BIZ_DESC = "biz_desc"
    private const val PREF_BIZ_LOGO_PATH = "biz_logo_path"
    private const val PREF_BIZ_PHONES = "biz_phones"
    private const val DEFAULT_PHONES_JSON = "[]"
    private const val PHONE_DELIMITER = " - "

    /**
     * [توليد كتيب الحسابات الماستر بصيغة  لاتزامياً - ]:
     * ينفذ الجولة التجريبية ثم الجولة الحقيقية لرسم كشوفات الحسابات المتتابعة وتحديث شريط التقدم.
     *
     */
    suspend fun generateBookletPdfAsync(
        context: Context,
        allCustomers: List<CustomerUiState>,
        selectedIds: List<String>,
        onlySelected: Boolean,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
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

            //      
            val header = BusinessProfileLoader.load(context)
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

            //   &    
            val database = AppDatabase.getDatabase(context)
            val repository = FinanceRepository(database, context.applicationContext as Application)

            //              
            val txCacheMap = mutableMapOf<String, List<com.smartledger.aldaftar.data.local.entities.HabayebTransaction>>()

            // '    
            val summary = PdfReportCalculator.calculateComprehensiveReport(targetCustomers)

            //  :       
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

                    //            1
                    dryCtx.currentY = 78f

                    //        
                    val customerChunks = targetCustomers.chunked(50)
                    var processedCount = 0
                    for (chunk in customerChunks) {
                        coroutineContext.ensureActive()
                        for (customer in chunk) {
                            coroutineContext.ensureActive()
                            val transactions = txCacheMap.getOrPut(customer.id) {
                                repository.getTransactionsForCustomerDirect(customer.id)
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

            //  :  
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

                //      1
                val canvas = realCtx.currentPageCanvas
                if (canvas != null) {
                    PdfPageRenderer.drawBusinessHeader(
                        canvas, realCtx.displayedName, realCtx.displayedDesc, realCtx.phonesStr,
                        realCtx.hasLogo, realCtx.scaledLogo, realCtx.logoW, realCtx.logoH, realCtx.docDateText, realCtx.docTimeText
                    )
                }
                realCtx.currentY = 78f

                // 2.      
                val customerChunks = targetCustomers.chunked(50)
                var processedCount = 0
                for (chunk in customerChunks) {
                    coroutineContext.ensureActive()
                    for (customer in chunk) {
                        coroutineContext.ensureActive()
                        val transactions = txCacheMap[customer.id]
                            ?: repository.getTransactionsForCustomerDirect(customer.id)
                        val singleSummary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                        drawCustomerLedgerSheet(realCtx, customer, singleSummary)
                        processedCount++
                        withContext(Dispatchers.Main) {
                            onProgress(processedCount, totalCustomers)
                        }
                    }
                }

                realCtx.finishLastPage()

                //     
                val outputDir = File(context.cacheDir, "pdf_reports")
                if (!outputDir.exists()) outputDir.mkdirs()
                val file = File(outputDir, "MasterBookletReport_${System.currentTimeMillis()}.pdf")
                val tempFile = File.createTempFile(file.nameWithoutExtension, ".tmp", outputDir)
                try {
                    FileOutputStream(tempFile).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                        outputStream.flush()
                        outputStream.fd.sync()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            java.nio.file.Files.move(tempFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                        } catch (_: Exception) {
                            java.nio.file.Files.move(tempFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                        }
                    } else {
                        if (file.exists() && !file.delete()) throw IllegalStateException("تعذر استبدال ملف التقرير")
                        if (!tempFile.renameTo(file)) throw IllegalStateException("تعذر تثبيت ملف التقرير")
                    }
                    file
                } finally {
                    if (tempFile.exists()) tempFile.delete()
                }
            } finally {
                realCtx?.closeSafely() ?: run {
                    try { pdfDocument.close() } catch (t: Throwable) {}
                }
            }

            withContext(Dispatchers.Main) {
                onFinished(outputFile)
            }

        } catch (e: CancellationException) {
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
            withContext(Dispatchers.Main) {
                onCancelled()
            }
        } catch (e: Exception) {
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
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
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
            }
        }
    }

    private fun drawCoverAndIndexDryRun(ctx: BookletDrawingContext, customers: List<CustomerUiState>) {
        //   
        ctx.currentY = 78f
        //  
        ctx.currentY += 22f
        //  
        ctx.currentY += 18f

        //  
        ctx.currentY += 18f
        //  
        ctx.currentY += 24f

        //  
        customers.forEachIndexed { _, customer ->
            val rowHeight = PdfRowRenderer.calculateBookletIndexRowHeight(customer)
            if (ctx.currentY + rowHeight > 780f) {
                ctx.startNewPage()
                //    +      
                ctx.currentY = 69f
            }
            ctx.currentY += rowHeight
        }
    }

    private fun drawCoverAndIndexReal(
        ctx: BookletDrawingContext,
        customers: List<CustomerUiState>,
        summary: com.smartledger.aldaftar.data.serialization.pdf.ComprehensivePdfSummary
    ) {
        val context = ctx.context
        val canvas = ctx.currentPageCanvas ?: return

        // 1.   
        PdfPageRenderer.drawBusinessHeader(
            canvas, ctx.displayedName, ctx.displayedDesc, ctx.phonesStr,
            ctx.hasLogo, ctx.scaledLogo, ctx.logoW, ctx.logoH, ctx.docDateText, ctx.docTimeText
        )
        ctx.currentY = 78f

        // 2.  
        val paintTitle = Paint().apply {
            color = Color.parseColor(ctx.primaryColorHex)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_booklet_title),
            25f, ctx.currentY, 545, paintTitle, Layout.Alignment.ALIGN_CENTER
        )
        ctx.currentY += 22f

        // 3.  
        val paintSub = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_MEDIUM)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_booklet_subtitle),
            25f, ctx.currentY, 545, paintSub, Layout.Alignment.ALIGN_CENTER
        )
        ctx.currentY += 18f

        // 4.   
        val paintIndexTitle = Paint().apply {
            color = Color.parseColor(ctx.primaryColorHex)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_index_title),
            25f, ctx.currentY, 545, paintIndexTitle, Layout.Alignment.ALIGN_NORMAL
        )
        ctx.currentY += 18f

        // 5.    
        PdfRowRenderer.drawBookletIndexHeader(canvas, ctx.currentY, context)
        ctx.currentY += 24f

        // 6.   
        customers.forEachIndexed { index, customer ->
            val rowHeight = PdfRowRenderer.calculateBookletIndexRowHeight(customer)
            if (ctx.currentY + rowHeight > 780f) {
                ctx.startNewPage()
                val nextCanvas = ctx.currentPageCanvas ?: return@forEachIndexed
                //      
                PdfRowRenderer.drawBookletIndexHeader(nextCanvas, 45f, context)
                ctx.currentY = 69f
            }

            val currentCanvas = ctx.currentPageCanvas ?: return@forEachIndexed
            PdfRowRenderer.drawBookletIndexRow(
                canvas = currentCanvas,
                context = context,
                index = index,
                customer = customer,
                currentY = ctx.currentY,
                rowHeight = rowHeight,
                currencySymbol = ctx.currencySymbol
            )

            ctx.currentY += rowHeight
        }
    }

    private fun drawCustomerLedgerSheet(
        ctx: BookletDrawingContext,
        customer: CustomerUiState,
        summary: com.smartledger.aldaftar.data.serialization.pdf.SingleCustomerPdfSummary
    ) {
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

/**
 * [سياق وحالة رسم كتيب الحسابات - ]:
 * يدير حالة الصفحات الحالية ومؤشر الإحداثي الرأسي ، وأرقام الصفحات، ورسم التذييلات تلقائياً.
 *
 */
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
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
            }
            currentPageObject = null
            currentPageCanvas = null
        }
        try {
            pdfDocument.close()
        } catch (t: Throwable) {
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
        }
    }
}
