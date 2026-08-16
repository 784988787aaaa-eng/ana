package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.AccountBalanceWallet

@Composable
fun MainBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // تحديد الشاشات الرئيسية التي يظهر فيها شريط التنقل
    val isVisible = currentScreen == Screen.HABAYEB || currentScreen == Screen.LEDGER

    val items = remember(context) {
        listOf(
            Triple(Screen.HABAYEB, Icons.Default.People, context.getString(R.string.nav_habayeb_plain)),
            Triple(Screen.LEDGER, Icons.Default.AccountBalanceWallet, context.getString(R.string.nav_ledger_plain))
        )
    }

    // حركة انزلاق واختفاء انسيابية لمنع قفز واجهات التطبيق عند التنقل للشاشات الفرعية
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
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // احترام حواف حماية نظام التشغيل لضمان استقرار الإيماءات
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 32.dp, end = 32.dp), // مساحة جانبية لجعله رصيفاً عائماً مدمجاً
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 280.dp) // تحديد عرض ذكي يمنع تشتت وتمدد التبويبات الثنائية
                    .height(50.dp) // ارتفاع رشيق ومحكم يوفر مساحة الشاشة الحية
                    .graphicsLayer {
                        shadowElevation = 6f
                        shape = RoundedCornerShape(25.dp)
                        clip = true
                    }
                    .border(
                        0.5.dp, // تأطير دقيق جداً ناعم المظهر
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(25.dp)
                    )
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)), // اندماج لوني خفيف مع الخلفية
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (screen, icon, label) ->
                    val selected = currentScreen == screen
                    val interactionSource = remember { MutableInteractionSource() }

                    // تأثير ارتداد نابضي فيزيائي خفيف لتأكيد التفاعل اللمسي
                    val scaleAnim by animateFloatAsState(
                        targetValue = if (selected) 1.04f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "BottomTabScale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .scale(scaleAnim)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null, // إلغاء التوهج المائي الافتراضي لضمان النقاء البصري
                                onClick = {
                                    if (currentScreen != screen) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onNavigate(screen)
                                    }
                                }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            },
                            modifier = Modifier.size(20.dp) // تقليص حجم الأيقونة لتتناسب مع الارتفاع الرشيق الجديد
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }
        }
    }
}
