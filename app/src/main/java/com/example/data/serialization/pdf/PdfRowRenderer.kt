/**
 * =====================================================================
 * ملف: الواجهة الموحدة لرسم صفوف وبطاقات PDF (PdfRowRenderer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن نمط الواجهة الموحدة (Facade Pattern) الذي يجمع ويوجه كافة
 * عمليات رسم الصفوف، والبطاقات التلخيصية، وشريط الصافي النهائي، وفهرس دفاتر الحسابات.
 * يعمل كوسيط ينوب عن المستدعي ويوجه المهام إلى الرسامين المتخصصين:
 * - [PdfTransactionRowRenderer]: لصفوف المعاملات الفردية.
 * - [PdfStatementTotalsRenderer]: لصفوف الإجماليات وملخص العملات الأجنبية.
 * - [PdfCustomerSummaryRenderer]: لصفوف فهرس العملاء والبطاقة التلخيصية الكبرى.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تطبيق نمط الواجهة الموحدة (Facade Structural Pattern):
 *    - عزل تفاصيل التنفيذ الداخلي وتوفير نقطة دخول برمجية نظيفة لمحركات PDF.
 * 2. التوجيه الدقيق للمهام الرسومية المتخصصة:
 *    - تفويض حساب الارتفاعات ورسم الخلايا بدقة نقطية.
 * 3. المحافظة على معايير الأداء وتوحيد توقيعات الدوال.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات ونماذج النطاق والحسابات
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import com.example.domain.model.TransactionType
import com.example.ui.state.CustomerUiState
import java.math.BigDecimal

/**
 * [الكائن الأحادي للواجهة الموحدة لرسم الصفوف - PdfRowRenderer]:
 * يفوض عمليات الرسم للرسامين المتخصصين وفق سياق البيانات.
 */
object PdfRowRenderer {

    // -------------------------------------------------------------------------
    // 1. Single Customer Transaction Rows & Details
    // -------------------------------------------------------------------------

    /**
     * [بناء النص التوضيحي للمعاملة - buildTransactionDescriptionText]:
     * يفوض التوليد لـ [PdfTransactionRowRenderer].
     */
    fun buildTransactionDescriptionText(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): String = PdfTransactionRowRenderer.buildTransactionDescriptionText(context, pt, initialType)

    /**
     * [حساب الارتفاع الرأسي لصف المعاملة - calculateTransactionRowHeight]:
     * يفوض الحساب لـ [PdfTransactionRowRenderer].
     */
    fun calculateTransactionRowHeight(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value,
        availableWidth: Int = 190
    ): Float = PdfTransactionRowRenderer.calculateTransactionRowHeight(context, pt, initialType, availableWidth)

    /**
     * [رسم صف معاملة فردية - drawSingleTransactionRow]:
     * يفوض الرسم المباشر لـ [PdfTransactionRowRenderer].
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
    // 2. Statement Totals, Net Banner & Foreign Currencies
    // -------------------------------------------------------------------------

    /**
     * [رسم صف إجماليات كشف الحساب - drawTotalsRow]:
     * يفوض الرسم لـ [PdfStatementTotalsRenderer].
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
     * [رسم شريط الصافي النهائي لكشف الحساب - drawFinalNetBanner]:
     * يفوض الرسم لـ [PdfStatementTotalsRenderer].
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
     * [رسم ملخص مديونيات العملات الأجنبية - drawForeignCurrenciesSummary]:
     * يفوض الرسم لـ [PdfStatementTotalsRenderer].
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
    // 3. Customer Summary Rows (Comprehensive & All-Customers Report)
    // -------------------------------------------------------------------------

    /**
     * [حساب ارتفاع صف ملخص العميل - calculateCustomerSummaryRowHeight]:
     * يفوض الحساب لـ [PdfCustomerSummaryRenderer].
     */
    fun calculateCustomerSummaryRowHeight(
        context: Context,
        c: CustomerUiState,
        nameWidth: Int = 175,
        foreignWidth: Int = 125
    ): Float = PdfCustomerSummaryRenderer.calculateCustomerSummaryRowHeight(context, c, nameWidth, foreignWidth)

    /**
     * [رسم صف ملخص العميل في التقرير الشامل - drawCustomerSummaryRow]:
     * يفوض الرسم لـ [PdfCustomerSummaryRenderer].
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
    // 4. Booklet Index Table Header & Rows
    // -------------------------------------------------------------------------

    /**
     * [رسم ترويسة جدول فهرس الدفتر - drawBookletIndexHeader]:
     * يفوض الرسم لـ [PdfCustomerSummaryRenderer].
     */
    fun drawBookletIndexHeader(canvas: Canvas, y: Float, context: Context) =
        PdfCustomerSummaryRenderer.drawBookletIndexHeader(canvas, y, context)

    /**
     * [حساب ارتفاع صف فهرس الدفتر - calculateBookletIndexRowHeight]:
     * يفوض الحساب لـ [PdfCustomerSummaryRenderer].
     */
    fun calculateBookletIndexRowHeight(customer: CustomerUiState, availableWidth: Int = 225): Float =
        PdfCustomerSummaryRenderer.calculateBookletIndexRowHeight(customer, availableWidth)

    /**
     * [رسم صف فهرس الدفتر - drawBookletIndexRow]:
     * يفوض الرسم لـ [PdfCustomerSummaryRenderer].
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
    // 5. Comprehensive Summary Card
    // -------------------------------------------------------------------------

    /**
     * [رسم البطاقة التلخيصية الشاملة - drawComprehensiveSummaryCard]:
     * يفوض الرسم لـ [PdfCustomerSummaryRenderer].
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

