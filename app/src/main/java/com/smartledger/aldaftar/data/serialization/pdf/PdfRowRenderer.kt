/**
 * =====================================================================
 * ملف: الواجهة الموحدة لرسم صفوف وبطاقات  (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن نمط الواجهة الموحدة (نمط الواجهة الموحدة) الذي يجمع ويوجه كافة
 * عمليات رسم الصفوف، والبطاقات التلخيصية، وشريط الصافي النهائي، وفهرس دفاتر الحسابات.
 * يعمل كوسيط ينوب عن المستدعي ويوجه المهام إلى الرسامين المتخصصين:
 * - []: لصفوف المعاملات الفردية.
 * - []: لصفوف الإجماليات وملخص العملات الأجنبية.
 * - []: لصفوف فهرس العملاء والبطاقة التلخيصية الكبرى.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تطبيق نمط الواجهة الموحدة (النمط البنيوي للواجهة الموحدة):
 *    - عزل تفاصيل التنفيذ الداخلي وتوفير نقطة دخول برمجية نظيفة لمحركات .
 * 2. التوجيه الدقيق للمهام الرسومية المتخصصة:
 *    - تفويض حساب الارتفاعات ورسم الخلايا بدقة نقطية.
 * 3. المحافظة على معايير الأداء وتوحيد توقيعات الدوال.
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات ونماذج النطاق والحسابات
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.state.CustomerUiState
import java.math.BigDecimal

/**
 * [الكائن الأحادي للواجهة الموحدة لرسم الصفوف - ]:
 * يفوض عمليات الرسم للرسامين المتخصصين وفق سياق البيانات.
 */
object PdfRowRenderer {

    // -------------------------------------------------------------------------
    // 1.     & 
    // -------------------------------------------------------------------------

    /**
     * [بناء النص التوضيحي للمعاملة - ]:
     * يفوض التوليد لـ [].
     */
    fun buildTransactionDescriptionText(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): String = PdfTransactionRowRenderer.buildTransactionDescriptionText(context, pt, initialType)

    /**
     * [حساب الارتفاع الرأسي لصف المعاملة - ]:
     * يفوض الحساب لـ [].
     */
    fun calculateTransactionRowHeight(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value,
        availableWidth: Int = 190
    ): Float = PdfTransactionRowRenderer.calculateTransactionRowHeight(context, pt, initialType, availableWidth)

    /**
     * [رسم صف معاملة فردية - ]:
     * يفوض الرسم المباشر لـ [].
     */
    fun drawSingleTransactionRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        pt: ProcessedTransaction,
        currentY: Float,
        rowHeight: Float,
        runningBal: BigDecimal,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ) = PdfTransactionRowRenderer.drawSingleTransactionRow(
        canvas, context, index, pt, currentY, rowHeight, runningBal, initialType
    )

    // -------------------------------------------------------------------------
    // 2.  ,   &  
    // -------------------------------------------------------------------------

    /**
     * [رسم صف إجماليات كشف الحساب - ]:
     * يفوض الرسم لـ [].
     */
    fun drawTotalsRow(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        totalDebts: BigDecimal,
        totalPayments: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float = PdfStatementTotalsRenderer.drawTotalsRow(
        canvas, context, currentY, totalDebts, totalPayments, currencySymbol, initialType
    )

    /**
     * [رسم شريط الصافي النهائي لكشف الحساب - ]:
     * يفوض الرسم لـ [].
     */
    fun drawFinalNetBanner(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        netBalance: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float = PdfStatementTotalsRenderer.drawFinalNetBanner(
        canvas, context, currentY, netBalance, currencySymbol, initialType
    )

    /**
     * [رسم ملخص مديونيات العملات الأجنبية - ]:
     * يفوض الرسم لـ [].
     */
    fun drawForeignCurrenciesSummary(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        uncalculatedForeignSums: Map<String, BigDecimal>,
        currencySymbol: String
    ): Float = PdfStatementTotalsRenderer.drawForeignCurrenciesSummary(
        canvas, context, currentY, uncalculatedForeignSums, currencySymbol
    )

    // -------------------------------------------------------------------------
    // 3.    ( & - )
    // -------------------------------------------------------------------------

    /**
     * [حساب ارتفاع صف ملخص العميل - ]:
     * يفوض الحساب لـ [].
     */
    fun calculateCustomerSummaryRowHeight(
        context: Context,
        c: CustomerUiState,
        nameWidth: Int = 175,
        foreignWidth: Int = 125
    ): Float = PdfCustomerSummaryRenderer.calculateCustomerSummaryRowHeight(context, c, nameWidth, foreignWidth)

    /**
     * [رسم صف ملخص العميل في التقرير الشامل - ]:
     * يفوض الرسم لـ [].
     */
    fun drawCustomerSummaryRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        c: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) = PdfCustomerSummaryRenderer.drawCustomerSummaryRow(
        canvas, context, index, c, currentY, rowHeight, currencySymbol
    )

    // -------------------------------------------------------------------------
    // 4.     & 
    // -------------------------------------------------------------------------

    /**
     * [رسم ترويسة جدول فهرس الدفتر - ]:
     * يفوض الرسم لـ [].
     */
    fun drawBookletIndexHeader(canvas: Canvas, y: Float, context: Context) =
        PdfCustomerSummaryRenderer.drawBookletIndexHeader(canvas, y, context)

    /**
     * [حساب ارتفاع صف فهرس الدفتر - ]:
     * يفوض الحساب لـ [].
     */
    fun calculateBookletIndexRowHeight(customer: CustomerUiState, availableWidth: Int = 225): Float =
        PdfCustomerSummaryRenderer.calculateBookletIndexRowHeight(customer, availableWidth)

    /**
     * [رسم صف فهرس الدفتر - ]:
     * يفوض الرسم لـ [].
     */
    fun drawBookletIndexRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        customer: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) = PdfCustomerSummaryRenderer.drawBookletIndexRow(
        canvas, context, index, customer, currentY, rowHeight, currencySymbol
    )

    // -------------------------------------------------------------------------
    // 5.   
    // -------------------------------------------------------------------------

    /**
     * [رسم البطاقة التلخيصية الشاملة - ]:
     * يفوض الرسم لـ [].
     */
    fun drawComprehensiveSummaryCard(
        canvas: Canvas,
        context: Context,
        primaryColorHex: String,
        summary: ComprehensivePdfSummary,
        totalItems: Int,
        currencySymbol: String,
        startY: Float = 98f
    ) = PdfCustomerSummaryRenderer.drawComprehensiveSummaryCard(
        canvas, context, primaryColorHex, summary, totalItems, currencySymbol, startY
    )
}

