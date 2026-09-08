package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.repository.FloatingAddState
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.ui.helper.VibrationHelper
import kotlin.math.roundToInt

@Composable
fun HabayebFab(
    targetCustomer: HabayebCustomer?,
    contentPadding: PaddingValues,
    primaryColor: Color,
    containerColor: Color,
    haptic: HapticFeedback,
    onAddCustomerClick: () -> Unit,
    onAddTransactionForCustomer: (HabayebCustomer) -> Unit,
    modifier: Modifier = Modifier,
    persisted: com.smartledger.aldaftar.data.repository.FloatingAddState,
    onPersist: (com.smartledger.aldaftar.data.repository.FloatingAddState) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val initialSizeLevel = persisted.sizeLevel
    val hasSavedRatio = persisted.hasPosition
    val initialRatioX = if (hasSavedRatio) persisted.ratioX else -1f
    val initialRatioY = if (hasSavedRatio) persisted.ratioY else -1f

    var sizeLevel by remember { mutableStateOf(initialSizeLevel) }
    val bubbleSize = when (sizeLevel) {
        0 -> 40.dp
        2 -> 62.dp
        else -> 52.dp
    }
    val iconSize = when (sizeLevel) {
        0 -> 20.dp
        2 -> 30.dp
        else -> 26.dp
    }

    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    var ratioX by remember { mutableStateOf(initialRatioX) }
    var ratioY by remember { mutableStateOf(initialRatioY) }

    var isInteracting by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isInteracting) 1.10f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "FabScaleAnim"
    )

    val effectivePrimary = if (primaryColor != Color.Unspecified) primaryColor else MaterialTheme.colorScheme.primary

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .zIndex(25f)
        ) {
            val maxX = remember(screenWidthPx, bubbleSizePx) {
                (screenWidthPx - bubbleSizePx).coerceAtLeast(0f)
            }
            val maxY = remember(screenHeightPx, bubbleSizePx) {
                (screenHeightPx - bubbleSizePx).coerceAtLeast(0f)
            }

            val defaultDockX = remember(screenWidthPx, bubbleSizePx) {
                if (screenWidthPx > 0) (screenWidthPx - bubbleSizePx) / 2f else 0f
            }
            val defaultDockY = remember(screenHeightPx, bubbleSizePx) {
                if (screenHeightPx > 0) {
                    val capsuleBottomOffset = with(density) { 72.dp.toPx() }
                    (screenHeightPx - capsuleBottomOffset - (bubbleSizePx / 2f)).coerceIn(0f, maxY)
                } else 0f
            }

            val clampedX = remember(ratioX, screenWidthPx, bubbleSizePx, defaultDockX) {
                if (ratioX < 0f) defaultDockX else (ratioX * screenWidthPx).coerceIn(0f, maxX)
            }
            val clampedY = remember(ratioY, screenHeightPx, bubbleSizePx, defaultDockY) {
                if (ratioY < 0f) defaultDockY else (ratioY * screenHeightPx).coerceIn(0f, maxY)
            }

            Box(
                modifier = Modifier
                    .absoluteOffset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .size(bubbleSize)
                    .scale(scaleAnim)
                    .alpha(1.0f)
                    .shadow(
                        elevation = if (isInteracting) 14.dp else 6.dp,
                        shape = CircleShape,
                        spotColor = effectivePrimary.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                effectivePrimary,
                                effectivePrimary.copy(alpha = 0.88f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isInteracting) 0.9f else 0.45f),
                        shape = CircleShape
                    )
                    .pointerInput(screenWidthPx, screenHeightPx, maxX, maxY, defaultDockX, defaultDockY) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isInteracting = true
                                VibrationHelper.triggerSuccessVibration(context)
                            },
                            onDragEnd = {
                                isInteracting = false
                                if (ratioX >= 0f && ratioY >= 0f) {
onPersist(FloatingAddState(sizeLevel, ratioX, ratioY, true))
                                }
                                VibrationHelper.triggerSuccessVibration(context)
                            },
                            onDragCancel = {
                                isInteracting = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                isInteracting = true

                                val currentX = if (ratioX < 0f) defaultDockX else ratioX * screenWidthPx
                                val currentY = if (ratioY < 0f) defaultDockY else ratioY * screenHeightPx
                                val newX = (currentX + dragAmount.x).coerceIn(0f, maxX)
                                val newY = (currentY + dragAmount.y).coerceIn(0f, maxY)

                                ratioX = if (screenWidthPx > 0) newX / screenWidthPx else ratioX
                                ratioY = if (screenHeightPx > 0) newY / screenHeightPx else ratioY
                            }
                        )
                    }
                    .pointerInput(sizeLevel, targetCustomer) {
                        detectTapGestures(
                            onTap = {
                                VibrationHelper.triggerSuccessVibration(context)
                                if (targetCustomer != null) {
                                    onAddTransactionForCustomer(targetCustomer)
                                } else {
                                    onAddCustomerClick()
                                }
                            },
                            onDoubleTap = {
                                val newSizeLevel = (sizeLevel + 1) % 3
                                sizeLevel = newSizeLevel
                                onPersist(FloatingAddState(newSizeLevel, ratioX, ratioY, hasSavedRatio))
                                VibrationHelper.triggerSuccessVibration(context)
                            }
                        )
                    }
                    .testTag("floating_add_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(
                        id = if (targetCustomer != null) R.string.habayeb_add_tx_desc else R.string.habayeb_add_customer_fab
                    ),
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
