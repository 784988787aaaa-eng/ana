package com.example.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.HabayebDateFormatter
import com.example.ui.viewmodel.FinanceConstants
import java.util.Date

import com.example.ui.theme.AlertGoldBgDark
import com.example.ui.theme.AlertGoldBorderDark
import com.example.ui.theme.AlertGoldTextDark
import com.example.ui.theme.AlertGoldTextLight
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueBgDark
import com.example.ui.theme.InfoBlueBgLight
import com.example.ui.theme.InfoBlueTextDark
import com.example.ui.theme.InfoBlueTextLight
import com.example.ui.theme.MutedTextDark
import com.example.ui.theme.MutedTextLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.SuccessGreenBgDark
import com.example.ui.theme.SuccessGreenBgLight
import com.example.ui.theme.SuccessGreenBorderDark
import com.example.ui.theme.SuccessGreenBorderLight
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.WarningAmberBorder
import com.example.ui.theme.WarningRedBorder
import com.example.ui.theme.WarningRedBorderLight
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor

private const val CURRENCY_NONE_TAG = "NONE"

private object RowColors {
    fun creditGreen(isDark: Boolean) = financialCreditColor(isDark)
    fun debtRed(isDark: Boolean) = financialDebtColor(isDark)
    fun mutedGray(isDark: Boolean) = if (isDark) MutedTextDark else MutedTextLight
    
    // الألوان المتكيفة لبادج التكرار المجدول الأصفر
    fun alertGoldBg(isDark: Boolean) = if (isDark) AlertGoldBgDark else WarningAmberBg
    fun alertGoldBorder(isDark: Boolean) = if (isDark) AlertGoldBorderDark else WarningAmberBorder
    fun alertGoldText(isDark: Boolean) = if (isDark) AlertGoldTextDark else AlertGoldTextLight
    
    // الألوان المتكيفة لبادج التفاصيل والعمليات المساعدة الزرقاء
    fun infoBlueBg(isDark: Boolean) = if (isDark) InfoBlueBgDark else InfoBlueBgLight
    fun infoBlueBorder(isDark: Boolean) = InfoBlue
    fun infoBlueText(isDark: Boolean) = if (isDark) InfoBlueTextDark else InfoBlueTextLight
    
    // الألوان المتكيفة لبادجات تأكيد وحفظ أسعار الصرف بنجاح
    fun successGreenBg(isDark: Boolean) = if (isDark) SuccessGreenBgDark else SuccessGreenBgLight
    fun successGreenBorder(isDark: Boolean) = if (isDark) SuccessGreenBorderDark else SuccessGreenBorderLight
    fun warningRedBorder(isDark: Boolean) = if (isDark) WarningRedBorder else WarningRedBorderLight
    fun darkGray(isDark: Boolean) = if (isDark) MutedTextDark else MutedTextLight
}

@androidx.compose.runtime.Immutable
data class TransactionRowCachedData(
    val cleanDescription: String,
    val indicatorColor: Color,
    val txArrow: androidx.compose.ui.graphics.vector.ImageVector,
    val formattedAmount: String,
    val displayCurrency: String,
    val equivalentAmountText: String?,
    val dayNameResId: Int,
    val dateStr: String,
    val timeStr: String,
    val isTxForeign: Boolean,
    val isCalculated: Boolean,
    val typeResId: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerTransactionRow(
    tx: HabayebTransaction,
    isDark: Boolean,
    currencySymbol: String,
    initialType: String,
    isSelected: Boolean,
    isTxMultiSelectActive: Boolean,
    hasActiveRecurring: Boolean,
    txSeqNo: Int,
    parentTxSeq: Int?,
    activeThemeColor: Color,
    onSelectToggle: (String) -> Unit,
    onLongClick: (String) -> Unit,
    onOptionsClick: (HabayebTransaction) -> Unit,
    onScheduleClick: (HabayebTransaction) -> Unit,
    onExchangeRateClick: (HabayebTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val cached = remember(tx, isDark, currencySymbol, initialType) {
        val parsedCurrencyInfo = CurrencyConfig.parseTransactionCurrency(tx.description, CURRENCY_NONE_TAG)
        val txCurrencySymbol = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) {
            tx.currencyCode
        } else if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) {
            parsedCurrencyInfo.first
        } else if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
            tx.baseCurrencyCode
        } else {
            currencySymbol
        }
        val isTxForeign = txCurrencySymbol != currencySymbol
        val cleanDescription = if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) parsedCurrencyInfo.second else tx.description

        val txType = TransactionType.fromValue(tx.type)
        val indicatorColor = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.OWED_TO_THEM -> RowColors.debtRed(isDark)
            else -> RowColors.creditGreen(isDark)
        }

        val txArrow = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> Icons.Default.ArrowUpward
            TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> Icons.Default.ArrowDownward
            else -> Icons.Default.ArrowUpward
        }

        val displayAmount: java.math.BigDecimal
        val displayCurrency: String
        val equivalentAmount: java.math.BigDecimal?
        val equivalentCurrency: String?

        val sourceAmount = if (tx.foreignAmount.compareTo(java.math.BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount

        if (tx.isRateCalculated) {
            val baseCurrency = if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
                tx.baseCurrencyCode
            } else {
                FinanceConstants.FALLBACK_CURRENCY_SYMBOL
            }
            val origCurrency = txCurrencySymbol
            
            displayAmount = sourceAmount
            displayCurrency = origCurrency
            equivalentAmount = tx.equivalentAmount
            equivalentCurrency = baseCurrency
        } else {
            displayAmount = sourceAmount
            displayCurrency = txCurrencySymbol
            equivalentAmount = null
            equivalentCurrency = null
        }

        val formattedAmount = com.example.ui.helper.HabayebMathHelper.formatSmart(displayAmount)

        val equivalentAmountText = if (equivalentAmount != null && equivalentCurrency != null) {
            val formattedEquiv = com.example.ui.helper.HabayebMathHelper.formatSmart(equivalentAmount)
            "($formattedEquiv $equivalentCurrency)"
        } else null

        val d = Date(tx.timestamp * 1000L)
        val dateStr = HabayebDateFormatter.formatShortDate(d)
        val timeStr = HabayebDateFormatter.formatTime12h(d)
        val dayNameResId = HabayebDateFormatter.getDayOfWeekResId(tx.timestamp)

        val typeResId = when (txType) {
            TransactionType.OWED_BY_THEM -> R.string.habayeb_pdf_tx_owed_by
            TransactionType.PAYMENT_BY_THEM -> R.string.habayeb_pdf_tx_payment_by
            TransactionType.OWED_TO_THEM -> R.string.habayeb_pdf_tx_owed_to
            TransactionType.PAYMENT_TO_THEM -> R.string.habayeb_pdf_tx_payment_to
            else -> R.string.habayeb_pdf_tx_generic
        }

        TransactionRowCachedData(
            cleanDescription = cleanDescription,
            indicatorColor = indicatorColor,
            txArrow = txArrow,
            formattedAmount = formattedAmount,
            displayCurrency = displayCurrency,
            equivalentAmountText = equivalentAmountText,
            dayNameResId = dayNameResId,
            dateStr = dateStr,
            timeStr = timeStr,
            isTxForeign = isTxForeign,
            isCalculated = tx.isRateCalculated,
            typeResId = typeResId
        )
    }

    val rowBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) {
            activeThemeColor.copy(alpha = if (isDark) 0.20f else 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "rowBgColor"
    )
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) activeThemeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "borderColor"
    )

    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .graphicsLayer {
                // تفعيل تسريع الرسوم ثلاثي الأبعاد لكروت السجلات
                shadowElevation = if (isSelected) 3f else 1f
                shape = RoundedCornerShape(8.dp)
                clip = true
            }
            .drawBehind {
                val barWidth = 4.dp.toPx()
                val xOffset = if (layoutDirection == LayoutDirection.Rtl) {
                    size.width - barWidth
                } else {
                    0f
                }
                drawRect(
                    color = cached.indicatorColor,
                    topLeft = androidx.compose.ui.geometry.Offset(xOffset, 0f),
                    size = androidx.compose.ui.geometry.Size(barWidth, size.height)
                )
            }
            .combinedClickable(
                onClick = {
                    if (isTxMultiSelectActive) {
                        onSelectToggle(tx.id)
                    } else {
                        onOptionsClick(tx)
                    }
                },
                onLongClick = { onLongClick(tx.id) }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = rowBgColor),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Date/Time (Rightmost)
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(activeThemeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = "#$txSeqNo",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeThemeColor,
                        modifier = Modifier
                            .background(activeThemeColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    if (hasActiveRecurring) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(activeThemeColor.copy(alpha = 0.12f), CircleShape)
                                .clickable { onScheduleClick(tx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = stringResource(id = R.string.habayeb_recurring_source),
                                tint = activeThemeColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val dayName = stringResource(id = cached.dayNameResId)
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                val annotatedDate = remember(dayName, cached.dateStr, activeThemeColor, onSurfaceColor) {
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = activeThemeColor, fontWeight = FontWeight.Bold)) {
                            append(dayName)
                        }
                        withStyle(style = SpanStyle(color = activeThemeColor.copy(alpha = 0.5f))) {
                            append(" • ")
                        }
                        withStyle(style = SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
                            append(cached.dateStr)
                        }
                    }
                }

                Text(
                    text = annotatedDate,
                    fontSize = 8.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(0.5.dp))

                Text(
                    text = cached.timeStr,
                    fontSize = 8.sp,
                    color = RowColors.mutedGray(isDark),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // 2. Details (Middle-Right)
            Column(
                modifier = Modifier.weight(2.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = cached.typeResId),
                    fontSize = 8.5.sp,
                    color = cached.indicatorColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(1.dp))

                // Fixed max lines: 2 and proper ellipsize
                Text(
                    text = cached.cleanDescription.ifEmpty { stringResource(id = R.string.habayeb_no_notes) },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Badges Row (Scheduler / Exchange Rate side-by-side to save height and look balanced)
                Row(
                    modifier = Modifier
                        .padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    if (hasActiveRecurring) {
                        Box(
                            modifier = Modifier
                                .background(RowColors.alertGoldBg(isDark), RoundedCornerShape(4.dp))
                                .border(0.5.dp, RowColors.alertGoldBorder(isDark), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.habayeb_recurring_source),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = RowColors.alertGoldText(isDark)
                            )
                        }
                    } else if (parentTxSeq != null && parentTxSeq > 0 && !tx.linkedMainTxId.isNullOrBlank() && !tx.linkedMainTxId.equals("null", ignoreCase = true) && tx.linkedMainTxId != tx.id) {
                        Box(
                            modifier = Modifier
                                .background(RowColors.infoBlueBg(isDark), RoundedCornerShape(4.dp))
                                .border(0.5.dp, RowColors.infoBlueBorder(isDark), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.habayeb_auto_generated_sub, parentTxSeq),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = RowColors.infoBlueText(isDark)
                            )
                        }
                    }

                    val targetCurrency = currencySymbol
                    val isSelfConversion = (cached.displayCurrency == targetCurrency)
                    val showToggle = !isSelfConversion && ((tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) || cached.isTxForeign || cached.isCalculated)
                    if (showToggle) {
                        val isCalculated = cached.isCalculated
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isCalculated) RowColors.successGreenBg(isDark)
                                    else if (isDark) Slate800.copy(alpha = 0.5f) else Slate100
                                )
                                .border(
                                    0.5.dp,
                                    if (isCalculated) RowColors.successGreenBorder(isDark)
                                    else if (isDark) Slate600.copy(alpha = 0.6f) else Slate300,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onExchangeRateClick(tx) }
                                .padding(horizontal = 5.dp, vertical = 1.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isCalculated) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCalculated) RowColors.successGreenBorder(isDark) else RowColors.warningRedBorder(isDark).copy(alpha = 0.6f),
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCalculated) {
                                    stringResource(id = R.string.habayeb_rate_active, com.example.ui.helper.HabayebMathHelper.formatRate(tx.exchangeRate))
                                } else {
                                    stringResource(id = R.string.habayeb_rate_inactive_clean)
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                color = if (isCalculated) RowColors.successGreenBorder(isDark) else RowColors.darkGray(isDark).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 3. Amount with colorful indicator arrow (Middle-Left)
            Column(
                modifier = Modifier.weight(1.0f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = cached.txArrow,
                        contentDescription = null,
                        tint = cached.indicatorColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // AutoSizing amount text
                    AutoSizeText(
                        text = "${cached.formattedAmount} ${cached.displayCurrency}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = cached.indicatorColor
                    )
                }
                if (cached.equivalentAmountText != null) {
                    AutoSizeText(
                        text = cached.equivalentAmountText,
                        fontSize = 9.sp,
                        color = cached.indicatorColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
