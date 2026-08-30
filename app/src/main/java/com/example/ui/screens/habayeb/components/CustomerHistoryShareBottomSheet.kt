package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة لوحة مشاركة وتصدير كشف حساب العميل (Customer History Share Bottom Sheet Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على واجهة المشاركة والتصدير الشاملة لكشف حساب العميل:
 * 1. بطاقة معلومات العميل وحالته المالية الصافية (دائن / مدين / خالص).
 * 2. تصدير التقارير بصيغة PDF وتنسيق Excel (CSV) مع خيارات (المشاركة المباشرة عبر واتساب، نافذة المشاركة العامة للنظام، والحفظ في التنزيلات).
 * 3. إرسال إشعارات نصية فورية سريعة عبر WhatsApp أو SMS.
 * =====================================================================================
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.serialization.CsvReportGenerator
import com.example.data.serialization.PdfReportGenerator
import com.example.data.serialization.PdfAction
import com.example.ui.helper.formatCurrency
import com.example.ui.screens.habayeb.utils.CustomerShareHelper
import com.example.ui.theme.CreditContainerLight
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebtContainerLight
import com.example.ui.theme.DebtRed
import com.example.ui.theme.whatsappColor
import com.example.ui.theme.shareSecondaryColor

/*
 * =====================================================================================
 * لوحة مشاركة كشف حساب العميل (CustomerHistoryShareBottomSheet)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة سفلية تقدم خيارات تصدير ومشاركة متعددة الوسائط (PDF, CSV, رسائل نصية).
 *
 * [المُدخلات]:
 * - showShareSheet: مؤشر تحديد ظهور القائمة السفلية.
 * - activeCustomer: كائن العميل النشط.
 * - allCustomerTxs: قائمة جميع حركات ومعاملات العميل.
 * - currencySymbol: رمز العملة الأساسية.
 * - exchangeRatesJson: بيانات أسعار الصرف المخزنة بصيغة JSON.
 * - netDebt: صافي الرصيد المالي للعميل.
 * - activeThemeColor: لون السمة النشط للعناصر البصرية.
 * - onDismissRequest: رد نداء عند إغلاق اللوحة السفلية.
 * - onPdfExportStart: رد نداء عند بدء عملية التصدير لإظهار مؤشر تقدم.
 * - onPdfExportFinish: رد نداء عند انتهاء عملية التصدير.
 * =====================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHistoryShareBottomSheet(
    showShareSheet: Boolean,
    activeCustomer: HabayebCustomer,
    allCustomerTxs: List<HabayebTransaction>,
    currencySymbol: String,
    exchangeRatesJson: String,
    netDebt: java.math.BigDecimal,
    activeThemeColor: Color,
    onDismissRequest: () -> Unit,
    onPdfExportStart: () -> Unit,
    onPdfExportFinish: () -> Unit
) {
    if (!showShareSheet) return

    val context = LocalContext.current
    val appScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
    val isPhoneAvailable = activeCustomer.phone.isNotBlank()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val debtShareBg = if (isDark) com.example.ui.theme.DebtContainerDark else com.example.ui.theme.DebtContainerLight
    val creditShareBg = if (isDark) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.CreditContainerLight
    val debtShareColor = if (isDark) com.example.ui.theme.DebtRedDark else com.example.ui.theme.DebtRed
    val creditShareColor = if (isDark) com.example.ui.theme.CreditGreenDark else com.example.ui.theme.CreditGreen
    val whatsappShareColor = whatsappColor(isDark)
    val smsShareColor = shareSecondaryColor(isDark)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header Title
            Text(
                text = stringResource(id = R.string.habayeb_share_options),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // 1. Elegant Customer Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Circular Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(activeThemeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeCustomer.name.take(1),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeThemeColor
                        )
                    }

                    // Customer Name and Phone Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeCustomer.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPhoneAvailable) activeCustomer.phone else stringResource(id = R.string.habayeb_no_phone_registered),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Account Balance Status Chip
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val absVal = netDebt.abs()
                        val statusText = when {
                            netDebt.compareTo(java.math.BigDecimal.ZERO) > 0 -> stringResource(id = R.string.habayeb_owed)
                            netDebt.compareTo(java.math.BigDecimal.ZERO) < 0 -> stringResource(id = R.string.habayeb_to_them)
                            else -> stringResource(id = R.string.habayeb_balanced)
                        }
                        val statusColor = when {
                            netDebt.compareTo(java.math.BigDecimal.ZERO) > 0 -> debtShareColor
                            netDebt.compareTo(java.math.BigDecimal.ZERO) < 0 -> creditShareColor
                            else -> MaterialTheme.colorScheme.outline
                        }

                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            text = formatCurrency(absVal, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Document & Report Files Section
            Text(
                text = stringResource(id = R.string.share_section_reports),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            // PDF Option Row
            FileShareOptionRow(
                icon = Icons.Default.PictureAsPdf,
                iconTint = debtShareColor,
                iconBgColor = debtShareBg,
                title = stringResource(id = R.string.share_pdf_title),
                description = stringResource(id = R.string.share_pdf_desc),
                isPhoneAvailable = isPhoneAvailable,
                onWhatsAppDirectClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    PdfReportGenerator.generateAndHandleCustomerPdfReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        action = PdfAction.WHATSAPP_DIRECT,
                        onFinished = onPdfExportFinish
                    )
                },
                onShareClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    PdfReportGenerator.generateAndHandleCustomerPdfReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        action = PdfAction.SHARE,
                        onFinished = onPdfExportFinish
                    )
                },
                onSaveClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    PdfReportGenerator.generateAndHandleCustomerPdfReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        action = PdfAction.SAVE_LOCAL,
                        onFinished = onPdfExportFinish
                    )
                }
            )

            // Excel (CSV) Option Row
            FileShareOptionRow(
                icon = Icons.Default.GridOn,
                iconTint = creditShareColor,
                iconBgColor = creditShareBg,
                title = stringResource(id = R.string.share_csv_title),
                description = stringResource(id = R.string.share_csv_desc),
                isPhoneAvailable = isPhoneAvailable,
                onWhatsAppDirectClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    CsvReportGenerator.generateAndHandleCsvReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        exchangeRatesJson = exchangeRatesJson,
                        action = CsvReportGenerator.CsvAction.WHATSAPP_DIRECT,
                        onFinished = onPdfExportFinish
                    )
                },
                onShareClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    CsvReportGenerator.generateAndHandleCsvReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        exchangeRatesJson = exchangeRatesJson,
                        action = CsvReportGenerator.CsvAction.SHARE,
                        onFinished = onPdfExportFinish
                    )
                },
                onSaveClick = {
                    onDismissRequest()
                    onPdfExportStart()
                    CsvReportGenerator.generateAndHandleCsvReportAsync(
                        context = context,
                        scope = appScope,
                        customer = activeCustomer,
                        transactions = allCustomerTxs,
                        currencySymbol = currencySymbol,
                        exchangeRatesJson = exchangeRatesJson,
                        action = CsvReportGenerator.CsvAction.SAVE_LOCAL,
                        onFinished = onPdfExportFinish
                    )
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 3. Messages & Quick Text Notifications Section
            Text(
                text = stringResource(id = R.string.share_section_messages),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WhatsApp Text Statement
                Button(
                    onClick = {
                        onDismissRequest()
                        CustomerShareHelper.triggerWhatsAppStatement(context, activeCustomer, netDebt, currencySymbol, allCustomerTxs)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = whatsappShareColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.share_whatsapp_text_btn),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // SMS Text Statement
                Button(
                    onClick = {
                        onDismissRequest()
                        CustomerShareHelper.triggerSmsStatement(context, activeCustomer, netDebt, currencySymbol, allCustomerTxs)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = smsShareColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.share_sms_text_btn),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FileShareOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    title: String,
    description: String,
    isPhoneAvailable: Boolean,
    onWhatsAppDirectClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val whatsappShareColor = whatsappColor(isDark)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon Rounded Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            // Action Buttons Row (WhatsApp, Share, Save)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp File Direct Button
                IconButton(
                    onClick = onWhatsAppDirectClick,
                    enabled = isPhoneAvailable,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isPhoneAvailable) whatsappShareColor.copy(alpha = 0.1f) else Color.Transparent,
                        contentColor = whatsappShareColor
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = stringResource(id = R.string.share_action_whatsapp_desc),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // General Share Chooser Button
                IconButton(
                    onClick = onShareClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.share_action_share_desc),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Save Local Downloads Button
                IconButton(
                    onClick = onSaveClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(id = R.string.share_action_download_desc),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
