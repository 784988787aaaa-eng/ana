package com.example.ui.screens.habayeb.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

/**
 * زر التنشيط والتفعيل المجهري في شريط العنوان العلوي (The Tiny Toggle Button)
 * - حجم مجهري وأنيق للغاية لمنع تشويه الواجهة.
 */
@Composable
fun TinyFloatingSearchToggle(
    isFloatingActive: Boolean,
    activeThemeColor: Color,
    onToggleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(
                color = if (isFloatingActive) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .border(
                width = 0.5.dp,
                color = if (isFloatingActive) Color.White.copy(alpha = 0.45f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onToggleClick),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.floating_search_toggle),
                tint = if (isFloatingActive) Color.White else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(13.dp)
            )
            if (isFloatingActive) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

/**
 * فقاعة البحث العائمة فائقة الانسيابية (Absolute Free Floating Search Bubble)
 * - تدعم السحب الحر ومصدات أمان تضمن بقاء الزر داخل حدود الشاشة المرئية دائماً.
 * - حركة فورية متزامنة وسريعة جداً بدون تأخر أو تجميد.
 * - فرض اتجاه LTR برمجياً لضمان دقة اتجاه حركة اليد.
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

    // قراءة الإعدادات المحفوظة عند التهيئة بذاكرة remember لمنع القراءة المكررة عند كل Recomposition
    val initialSizeLevel = remember(prefs) { prefs.getInt(BubblePrefsKeys.KEY_SIZE_LEVEL, 1) }
    val initialRatioX = remember(prefs) { prefs.getFloat(BubblePrefsKeys.KEY_RATIO_X, 0.80f).coerceIn(0f, 1f) }
    val initialRatioY = remember(prefs) { prefs.getFloat(BubblePrefsKeys.KEY_RATIO_Y, 0.70f).coerceIn(0f, 1f) }
    val initialLocked = remember(prefs) { prefs.getBoolean(BubblePrefsKeys.KEY_LOCKED, false) }

    // مستويات الحجم الثلاثة القابلة للتغيير بالنقر المزدوج
    var sizeLevel by remember { mutableStateOf(initialSizeLevel) }
    val bubbleSize = when (sizeLevel) {
        0 -> 38.dp // صغير جداً ومجهري
        2 -> 58.dp // كبير ومريح
        else -> 48.dp // الحجم الافتراضي الرشيق
    }
    val bubbleSizePx = with(density) { bubbleSize.toPx() }

    // شحن الإحداثيات كنسبة مئوية لضمان ثبات التموضع عند تغيير الهواتف
    var ratioX by remember { mutableStateOf(initialRatioX) }
    var ratioY by remember { mutableStateOf(initialRatioY) }

    var isLocked by remember { mutableStateOf(initialLocked) }

    val lockAlpha by animateFloatAsState(
        targetValue = if (isLocked) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "LockIconAlpha"
    )
    val searchAlpha by animateFloatAsState(
        targetValue = if (isLocked) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "SearchIconAlpha"
    )

    var isInteracting by remember { mutableStateOf(false) }
    val idleAlpha = if (isInteracting) 1.0f else (if (isLocked) 0.65f else 0.45f)

    val lockedContainerColor = MaterialTheme.colorScheme.tertiary
    val lockedBorderColor = MaterialTheme.colorScheme.tertiaryContainer

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val maxX = remember(screenWidthPx, bubbleSizePx) { (screenWidthPx - bubbleSizePx).coerceAtLeast(0f) }
            val maxY = remember(screenHeightPx, bubbleSizePx) { (screenHeightPx - bubbleSizePx).coerceAtLeast(0f) }
            
            val clampedX = (ratioX * screenWidthPx).coerceIn(0f, maxX)
            val clampedY = (ratioY * screenHeightPx).coerceIn(0f, maxY)

            Box(
                modifier = Modifier
                    .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .size(bubbleSize)
                    .alpha(idleAlpha)
                    .shadow(
                        elevation = if (isInteracting) 12.dp else 4.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        if (isLocked) {
                            lockedContainerColor.copy(alpha = if (isInteracting) 0.95f else 0.40f)
                        } else {
                            activeThemeColor.copy(alpha = if (isInteracting) 0.95f else 0.25f)
                        }
                    )
                    .border(
                        width = if (isLocked) 2.dp else 1.2.dp,
                        color = if (isLocked) {
                            lockedBorderColor.copy(alpha = 0.85f)
                        } else {
                            activeThemeColor.copy(alpha = if (isInteracting) 1.00f else 0.45f)
                        },
                        shape = CircleShape
                    )
                    .pointerInput(isLocked, screenWidthPx, screenHeightPx, maxX, maxY) {
                        if (!isLocked) {
                            detectDragGestures(
                                onDragStart = { isInteracting = true },
                                onDragEnd = {
                                    isInteracting = false
                                    prefs.edit().apply {
                                        putFloat(BubblePrefsKeys.KEY_RATIO_X, ratioX)
                                        putFloat(BubblePrefsKeys.KEY_RATIO_Y, ratioY)
                                        apply()
                                    }
                                    VibrationHelper.triggerSuccessVibration(context)
                                },
                                onDragCancel = { isInteracting = false },
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
                    }
                    .pointerInput(isLocked, sizeLevel) {
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
                            },
                            onLongPress = {
                                isLocked = !isLocked
                                prefs.edit().putBoolean(BubblePrefsKeys.KEY_LOCKED, isLocked).apply()
                                if (isLocked) {
                                    VibrationHelper.triggerDeleteVibration(context)
                                } else {
                                    VibrationHelper.triggerSuccessVibration(context)
                                }
                            }
                        )
                    }
            ) {
                BubbleSearchAndLockIcons(
                    searchAlpha = searchAlpha,
                    lockAlpha = lockAlpha,
                    isInteracting = isInteracting,
                    sizeLevel = sizeLevel,
                    activeThemeColor = activeThemeColor
                )
            }
        }
    }
}

/**
 * مكون فرعي داخلي لرسم الأيقونات المتحركة (البحث والقفل) داخل فقاعة البحث
 */
@Composable
private fun BubbleSearchAndLockIcons(
    searchAlpha: Float,
    lockAlpha: Float,
    isInteracting: Boolean,
    sizeLevel: Int,
    activeThemeColor: Color
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Search Icon
        if (searchAlpha > 0.05f) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.floating_search_icon),
                tint = if (isInteracting) Color.White else activeThemeColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (sizeLevel == 0) 18.dp else 22.dp)
                    .alpha(searchAlpha)
            )
        }

        // Locked Icon
        if (lockAlpha > 0.05f) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(id = R.string.floating_search_locked),
                tint = if (isInteracting) Color.White else MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(if (sizeLevel == 0) 16.dp else 20.dp)
                    .alpha(lockAlpha)
            )
        }
    }
}
