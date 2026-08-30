package com.example.ui.components

/*
 * =====================================================================================
 * حزمة عناصر التنقل السفلي العائم (Floating Bottom Navigation Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على كبسولة التنقل السفلية العائمة (Floating Navigation Pill)
 * التي تتيح التبديل السريع بلمسة واحدة بين الشاشتين الرئيسيتين (الحبايب ودفتر اليومية).
 * =====================================================================================
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.Screen

/*
 * بادئة وسم انتقال ألوان التبويبات في شريط التنقل السفلي لتحديد الحركة في أدوات المراقبة
 */
private const val LABEL_TAB_COLOR_PREFIX = "tab_color_"

/*
 * =====================================================================================
 * شريط التنقل السفلي العائم (MainBottomNavigation)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف البصري]:
 * شريط تنقل سفلي بتصميم كبسولة دائرية عائمة (Floating Segmented Island):
 * 1. يتمركز في أسفل منتصف الشاشة مع مراعاة هوامش شريط أزرار النظام (Navigation Insets).
 * 2. يختفي تلقائياً بحركة انزلاق ناعمة عند الانتقال إلى الشاشات الفرعية (الإعدادات، السلة، الأمان).
 * 3. يدعم الاستجابة اللمسية الاهتزازية (Haptic Feedback) لتعزيز شعور النقر الواقعي.
 * 4. يبرز التبويب النشط بلون العلامة الأساسي (Primary Color) مع خط عريض.
 *
 * [البيانات والمُدخلات]:
 * - currentScreen: الشاشة الحالية المفتوحة لتحديد التبويب النشط.
 * - onNavigate: دالة رد النداء لتنفيذ التنقل عند النقر على أي تبويب.
 * - isVisible: شرط الظهور (يظهر فقط في شاشة الحبايب ودفتر اليومية).
 * =====================================================================================
 */
@Composable
fun MainBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean = currentScreen == Screen.HABAYEB || currentScreen == Screen.LEDGER
) {
    /*
     * ---------------------------------------------------------------------------------
     * استخراج السياق ومحرك الاهتزاز اللمسي (Context & Haptic Engine)
     * ---------------------------------------------------------------------------------
     */
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    /*
     * تجهيز قائمة عناصر التنقل (الشاشة، الأيقونة، الاسم المعرب)
     */
    val items = remember(context) {
        listOf(
            Triple(Screen.HABAYEB, Icons.Default.People, context.getString(R.string.nav_habayeb_plain)),
            Triple(Screen.LEDGER, Icons.Default.MenuBook, context.getString(R.string.nav_ledger_plain))
        )
    }

    /*
     * ---------------------------------------------------------------------------------
     * ظهور واختفاء متحرك للشريط (Animated Visibility with Spring Physics)
     * ---------------------------------------------------------------------------------
     * يظهر بانزلاق للأعلى مع تلاشٍ وظهور، ويختفي بالانزلاق للأسفل مع خفوت.
     * ---------------------------------------------------------------------------------
     */
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            /*
             * -------------------------------------------------------------------------
             * حاوية الكبسولة العائمة (Floating Pill Surface)
             * -------------------------------------------------------------------------
             * سطح دائري الحواف بظلال ناعمة وإطار خارجي خفيف يطفو فوق محتوى الشاشة.
             * -------------------------------------------------------------------------
             */
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    0.75.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(52.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .width(240.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, (screen, icon, label) ->
                        val isSelected = currentScreen == screen
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "$LABEL_TAB_COLOR_PREFIX$index"
                        )
                        val tabBgColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            } else {
                                Color.Transparent
                            },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tab_bg_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(22.dp))
                                .background(tabBgColor)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (currentScreen != screen) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onNavigate(screen)
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = contentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

