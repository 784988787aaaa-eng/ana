package com.smartledger.aldaftar.data.serialization.pdf

import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.smartledger.aldaftar.ui.theme.MizanDocumentColors

object PdfColors {
    private fun ComposeColor.toHex(): String {
        return String.format(java.util.Locale.US, "#%06X", 0xFFFFFF and this.toArgb())
    }

    val PRIMARY_EMERALD = MizanDocumentColors.brandPrimary.toHex()
    val HEADER_BG = MizanDocumentColors.headerBackground.toHex()
    val HEADER_TEXT = MizanDocumentColors.headerText.toHex()
    val HEADER_BORDER = MizanDocumentColors.borderStrong.toHex()
    val TEXT_CHARCOAL = MizanDocumentColors.contentPrimary.toHex()
    val TEXT_MUTED_GREY = MizanDocumentColors.contentSecondary.toHex()
    val TEXT_DARK = MizanDocumentColors.contentPrimary.toHex()
    val TEXT_MEDIUM = MizanDocumentColors.contentSecondary.toHex()
    val TEXT_LIGHT = MizanDocumentColors.contentTertiary.toHex()
    val NET_DEBT_BLUE = MizanDocumentColors.netDebtBlue.toHex()
    val OWED_BG = MizanDocumentColors.debtContainer.toHex()
    val OWED_TEXT = MizanDocumentColors.debt.toHex()
    val DEBT_BORDER = MizanDocumentColors.debtBorder.toHex()
    val PAYMENT_BG = MizanDocumentColors.creditContainer.toHex()
    val PAYMENT_TEXT = MizanDocumentColors.credit.toHex()
    val CREDIT_BORDER = MizanDocumentColors.creditBorder.toHex()
    val FOREIGN_ROW_BG = MizanDocumentColors.surfaceVariant.toHex()
    val CARD_BG = MizanDocumentColors.surfaceContainer.toHex()
    val ROW_DIVIDER = MizanDocumentColors.borderVariant.toHex()
    val ALT_ROW_BG = MizanDocumentColors.altRowBackground.toHex()
    val TOTALS_ROW_BG = MizanDocumentColors.totalsRowBackground.toHex()
    val BANNER_OWED_BG = MizanDocumentColors.debtContainer.toHex()
    val BANNER_PAYMENT_BG = MizanDocumentColors.creditContainer.toHex()
}


