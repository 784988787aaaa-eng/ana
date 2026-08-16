package com.example.ui.screens.habayeb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.local.entities.HabayebCustomer

@Composable
fun HabayebFab(
    targetCustomer: HabayebCustomer?,
    contentPadding: PaddingValues,
    primaryColor: Color,
    containerColor: Color,
    haptic: HapticFeedback,
    onAddCustomerClick: () -> Unit,
    onAddTransactionForCustomer: (HabayebCustomer) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp, start = 16.dp)
            .size(58.dp)
            .shadow(10.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.6f))
            .background(primaryColor, CircleShape)
            .border(1.dp, containerColor.copy(alpha = 0.3f), CircleShape)
            .zIndex(11f)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (targetCustomer != null) {
                    onAddTransactionForCustomer(targetCustomer)
                } else {
                    onAddCustomerClick()
                }
            }
            .testTag("add_customer_fab"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(
                id = if (targetCustomer != null) R.string.habayeb_add_tx_desc else R.string.habayeb_add_customer_fab
            ),
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
