package com.smartledger.aldaftar.ui.screens.habayeb

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.zIndex
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.ui.screens.habayeb.components.HabayebFab

@Composable
fun HabayebFabHost(
    targetCustomer: HabayebCustomer?,
    contentPadding: PaddingValues,
    activeThemeColor: Color,
    activeSubColor: Color,
    haptic: HapticFeedback,
    isMultiSelectActive: Boolean,
    isHistoryTxMultiSelectActive: Boolean,
    onAddCustomerClick: () -> Unit,
    onAddTransactionForCustomer: (HabayebCustomer) -> Unit,
    persisted: com.smartledger.aldaftar.data.repository.FloatingAddState,
    onPersist: (com.smartledger.aldaftar.data.repository.FloatingAddState) -> Unit,
    onFabOverlayChanged: (((@Composable () -> Unit)?) -> Unit)? = null
) {
    if (onFabOverlayChanged != null) {
        DisposableEffect(
            targetCustomer,
            contentPadding,
            activeThemeColor,
            activeSubColor,
            isMultiSelectActive,
            isHistoryTxMultiSelectActive
        ) {
            if (!isMultiSelectActive && !isHistoryTxMultiSelectActive) {
                onFabOverlayChanged.invoke {
                    HabayebFab(
                        targetCustomer = targetCustomer,
                        contentPadding = contentPadding,
                        primaryColor = activeThemeColor,
                        containerColor = activeSubColor,
                        haptic = haptic,
                        onAddCustomerClick = onAddCustomerClick,
                        onAddTransactionForCustomer = onAddTransactionForCustomer,
                        persisted = persisted,
                        onPersist = onPersist
                    )
                }
            } else {
                onFabOverlayChanged.invoke(null)
            }
            onDispose {
                onFabOverlayChanged.invoke(null)
            }
        }
    } else {
        if (!isMultiSelectActive && !isHistoryTxMultiSelectActive) {
            HabayebFab(
                targetCustomer = targetCustomer,
                contentPadding = contentPadding,
                primaryColor = activeThemeColor,
                containerColor = activeSubColor,
                haptic = haptic,
                onAddCustomerClick = onAddCustomerClick,
                onAddTransactionForCustomer = onAddTransactionForCustomer,
                persisted = persisted,
                onPersist = onPersist,
                modifier = Modifier.zIndex(25f)
            )
        }
    }
}
