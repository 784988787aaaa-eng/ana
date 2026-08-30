package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة فقاعة البحث العائمة التفاعلية (Floating Search Bubble Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على مكونات البحث العائم فائق الانسيابية:
 * 1. زر التبديل المجهري (TinyFloatingSearchToggle) في الشريط العلوي لتفعيل أو إخفاء الفقاعة.
 * 2. فقاعة البحث العائمة (FloatingSearchBubble):
 *    - إمكانية السحب والتحريك الحر في أرجاء الشاشة بعد الضغط المطول لمنع التحريك العرضي.
 *    - النقر المزدوج لتغيير حجم الفقاعة بين 3 مستويات مختلفة.
 *    - النقر لفتح نافذة البحث السريع مع اهتزاز لمسي تفاعلي (Haptic Feedback).
 *    - حفظ إحداثيات ومستوى حجم الفقاعة في التفضيلات المحلية (SharedPreferences).
 * =====================================================================================
 */

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.helper.VibrationHelper
import kotlin.math.roundToInt

private object BubblePrefsKeys {
    const val PREFS_NAME = "floating_search_prefs"
    const val KEY_SIZE_LEVEL = "bubble_size_level"
    const val KEY_RATIO_X = "bubble_ratio_x"
    const val KEY_RATIO_Y = "bubble_ratio_y"
    const val KEY_LOCKED = "KEY_SEARCH_BUTTON_LOCKED"
}

/*
 * =====================================================================================
 * زر تبديل فقاعة البحث المصغر (TinyFloatingSearchToggle)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * زر مجهري في الشريط العلوي يتيح للمستخدم تفعيل أو تعطيل ظهور زر البحث العائم.
 *
 * [المُدخلات]:
 * - isFloatingActive: حالة تفعيل الفقاعة العائمة.
 * - activeThemeColor: لون السمة النشط.
 * - onToggleClick: رد نداء عند النقر لتبديل حالة التفعيل.
 * =====================================================================================
 */
@Composable
fun TinyFloatingSearchToggle(
    isFloatingActive: Boolean,
    activeThemeColor: Color = MaterialTheme.colorScheme.onPrimary,
    onToggleClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                color = if (isFloatingActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.28f)
                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                shape = CircleShape
            )
            .border(
                width = if (isFloatingActive) 1.dp else 0.75.dp,
                color = if (isFloatingActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onToggleClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.floating_search_toggle),
                tint = if (isFloatingActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                modifier = Modifier.size(14.dp)
            )
            if (isFloatingActive) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .align(Alignment.TopEnd)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }
    }
}

/**
 * فقاعة البحث العائمة فائقة الانسيابية (Absolute Free Floating Search Bubble)
 * - مقفلة بشكل افتراضي وتتحرك حصراً بالضغط المطول مع السحب.
 * - تنظيف تام لرموز وأكواد القفل المزعجة لتطابق ثيم التطبيق بالكامل.
 */
@Composable
fun FloatingSearchBubble(
    activeThemeColor: Color,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(BubblePrefsKeys.PREFS_NAME, Context.MODE_PRIVATE) }
    
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val initialSizeLevel = remember(prefs) { prefs.getInt(BubblePrefsKeys.KEY_SIZE_LEVEL, 1) }
    val initialRatioX = remember(prefs) { prefs.getFloat(BubblePrefsKeys.KEY_RATIO_X, 0.80f).coerceIn(0f, 1f) }
    val initialRatioY = remember(prefs) { prefs.getFloat(BubblePrefsKeys.KEY_RATIO_Y, 0.70f).coerceIn(0f, 1f) }

    var sizeLevel by remember { mutableStateOf(initialSizeLevel) }
    val bubbleSize = when (sizeLevel) {
        0 -> 38.dp
        2 -> 58.dp
        else -> 48.dp
    }
    val searchIconSize = when (sizeLevel) {
        0 -> 18.dp
        2 -> 26.dp
        else -> 22.dp
    }
    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    var ratioX by remember { mutableStateOf(initialRatioX) }
    var ratioY by remember { mutableStateOf(initialRatioY) }

    var isInteracting by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isInteracting) 1.10f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "SearchBubbleScaleAnim"
    )

    val effectivePrimary = MaterialTheme.colorScheme.primary

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(25f)
        ) {
            val maxX = remember(screenWidthPx, bubbleSizePx) { (screenWidthPx - bubbleSizePx).coerceAtLeast(0f) }
            val maxY = remember(screenHeightPx, bubbleSizePx) { (screenHeightPx - bubbleSizePx).coerceAtLeast(0f) }
            
            val clampedX = (ratioX * screenWidthPx).coerceIn(0f, maxX)
            val clampedY = (ratioY * screenHeightPx).coerceIn(0f, maxY)

            Box(
                modifier = Modifier
                    .absoluteOffset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .size(bubbleSize)
                    .scale(scaleAnim)
                    .shadow(
                        elevation = if (isInteracting) 12.dp else 6.dp,
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
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (isInteracting) 0.9f else 0.62f),
                        shape = CircleShape
                    )
                    .pointerInput(screenWidthPx, screenHeightPx, maxX, maxY) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isInteracting = true
                                VibrationHelper.triggerSuccessVibration(context)
                            },
                            onDragEnd = {
                                isInteracting = false
                                prefs.edit().apply {
                                    putFloat(BubblePrefsKeys.KEY_RATIO_X, ratioX)
                                    putFloat(BubblePrefsKeys.KEY_RATIO_Y, ratioY)
                                    putBoolean(BubblePrefsKeys.KEY_LOCKED, true)
                                    apply()
                                }
                                VibrationHelper.triggerSuccessVibration(context)
                            },
                            onDragCancel = {
                                isInteracting = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                isInteracting = true
                                
                                val currentX = ratioX * screenWidthPx
                                val currentY = ratioY * screenHeightPx
                                val newX = (currentX + dragAmount.x).coerceIn(0f, maxX)
                                val newY = (currentY + dragAmount.y).coerceIn(0f, maxY)
                                
                                ratioX = if (screenWidthPx > 0) newX / screenWidthPx else ratioX
                                ratioY = if (screenHeightPx > 0) newY / screenHeightPx else ratioY
                            }
                        )
                    }
                    .pointerInput(sizeLevel) {
                        detectTapGestures(
                            onTap = {
                                VibrationHelper.triggerSuccessVibration(context)
                                onSearchClick()
                            },
                            onDoubleTap = {
                                val newSizeLevel = (sizeLevel + 1) % 3
                                sizeLevel = newSizeLevel
                                prefs.edit().putInt(BubblePrefsKeys.KEY_SIZE_LEVEL, newSizeLevel).apply()
                                VibrationHelper.triggerSuccessVibration(context)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.floating_search_icon),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(searchIconSize)
                )
            }
        }
    }
}
