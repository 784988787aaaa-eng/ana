package com.smartledger.aldaftar.data.serialization.pdf

import kotlin.math.ceil

/** Centralized spacing rules so report sections grow with their real content. */
object PdfReportLayoutSpec {
    private const val CONTENT_GAP = 10f
    private const val CUSTOMER_TITLE_GAP = 6f
    private const val TABLE_HEADER_GAP = 10f
    private const val FOREIGN_CARD_HEADER = 28f
    private const val FOREIGN_CARD_ITEM = 68f
    private const val FOREIGN_CARD_BOTTOM = 4f
    private const val FOREIGN_COLUMNS = 2
    private const val COMPREHENSIVE_BASE_HEIGHT = 52f
    private const val COMPREHENSIVE_FOREIGN_TOP = 40f
    private const val COMPREHENSIVE_FOREIGN_BOTTOM = 6f
    private const val COMPREHENSIVE_FOREIGN_ROW = 58f
    private const val TABLE_HEADER_HEIGHT = 24f
    private const val TABLE_FIRST_ROW_OFFSET = 30f

    fun contentStartY(headerBottomY: Float, titleHeight: Float, titleGap: Float = 6f): Float =
        headerBottomY + titleHeight + titleGap

    fun foreignCurrencySectionHeight(currencyCount: Int): Float {
        if (currencyCount <= 0) return 0f
        val rows = ceil(currencyCount / FOREIGN_COLUMNS.toDouble()).toInt()
        return FOREIGN_CARD_HEADER + (rows * FOREIGN_CARD_ITEM) + FOREIGN_CARD_BOTTOM
    }

    fun foreignCurrencyCardItemHeight(): Float = FOREIGN_CARD_ITEM

    fun comprehensiveSummaryCardHeight(foreignCurrencyCount: Int): Float {
        if (foreignCurrencyCount <= 0) return COMPREHENSIVE_BASE_HEIGHT
        val rows = ceil(foreignCurrencyCount / FOREIGN_COLUMNS.toDouble()).toInt()
        val foreignContentHeight = COMPREHENSIVE_FOREIGN_TOP +
            rows * COMPREHENSIVE_FOREIGN_ROW +
            COMPREHENSIVE_FOREIGN_BOTTOM
        return maxOf(COMPREHENSIVE_BASE_HEIGHT, foreignContentHeight)
    }

    fun comprehensiveForeignCardRowHeight(): Float = COMPREHENSIVE_FOREIGN_ROW

    fun tableHeaderHeight(): Float = TABLE_HEADER_HEIGHT

    fun tableFirstRowOffset(): Float = TABLE_FIRST_ROW_OFFSET

    fun customerTitleGap(): Float = CUSTOMER_TITLE_GAP

    fun tableHeaderGap(): Float = TABLE_HEADER_GAP

    fun customerBannerAdvance(actualTextHeight: Float): Float = actualTextHeight + CUSTOMER_TITLE_GAP
}
