package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.MizanDialogInnerCard
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants

@Composable
fun CustomerTypeChangeSection(
    currentType: String,
    activeThemeColor: Color,
    onUpdateCustomerType: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingTypeChange by remember { mutableStateOf<String?>(null) }
    val mizanColors = MaterialTheme.mizanColors

    val activeRedColor = mizanColors.debt
    val activeGreenColor = mizanColors.credit
    val inactiveBgColor = mizanColors.disabledTrack
    val inactiveTextColor = mizanColors.contentSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.context_menu_change_type_label),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isToThemSelected = currentType == FinanceConstants.TYPE_OWED_TO_THEM
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isToThemSelected) activeGreenColor else inactiveBgColor)
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                color = if (isToThemSelected) activeGreenColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isToThemSelected) {
                                pendingTypeChange = FinanceConstants.TYPE_OWED_TO_THEM
                            }
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (isToThemSelected) mizanColors.onCredit else activeGreenColor.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.habayeb_to_them),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        color = if (isToThemSelected) mizanColors.onCredit else inactiveTextColor
                    )
                }

                val isByThemSelected = currentType == FinanceConstants.TYPE_OWED_BY_THEM
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isByThemSelected) activeRedColor else inactiveBgColor)
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                color = if (isByThemSelected) activeRedColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isByThemSelected) {
                                pendingTypeChange = FinanceConstants.TYPE_OWED_BY_THEM
                            }
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (isByThemSelected) mizanColors.onDebt else activeRedColor.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.habayeb_owed),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFontFamily,
                        color = if (isByThemSelected) mizanColors.onDebt else inactiveTextColor
                    )
                }
            }
        }
    }

    if (pendingTypeChange != null) {
        val targetType = pendingTypeChange
        val confirmButtonColor = when (targetType) {
            FinanceConstants.TYPE_OWED_TO_THEM -> mizanColors.credit
            FinanceConstants.TYPE_OWED_BY_THEM -> mizanColors.debt
            else -> activeThemeColor
        }
        val confirmContentColor = when (targetType) {
            FinanceConstants.TYPE_OWED_TO_THEM -> mizanColors.onCredit
            FinanceConstants.TYPE_OWED_BY_THEM -> mizanColors.onDebt
            else -> MaterialTheme.colorScheme.onPrimary
        }

        MizanAnimatedDialog(
            onDismissRequest = { pendingTypeChange = null }
        ) { dismissConfirm ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(id = R.string.context_menu_change_type_confirm_title),
                    icon = Icons.Default.SwapHoriz,
                    iconTint = confirmButtonColor,
                    onCloseClick = {
                        pendingTypeChange = null
                        dismissConfirm()
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MizanDialogInnerCard(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.context_menu_change_type_confirm_msg),
                            fontSize = 13.sp,
                            fontFamily = CairoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.context_menu_confirm),
                    onConfirm = {
                        targetType?.let { newType ->
                            onUpdateCustomerType(newType)
                        }
                        pendingTypeChange = null
                        onDismiss()
                    },
                    confirmColor = confirmButtonColor,
                    confirmContainerColor = confirmButtonColor,
                    confirmContentColor = confirmContentColor,
                    cancelText = stringResource(id = R.string.context_menu_cancel),
                    onCancel = {
                        pendingTypeChange = null
                        dismissConfirm()
                    }
                )
            }
        }
    }
}

