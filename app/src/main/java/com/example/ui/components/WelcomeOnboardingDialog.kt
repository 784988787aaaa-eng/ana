package com.example.ui.components

/*
 * =====================================================================================
 * حزمة نوافذ الترحيب والتهيئة التفاعلية (Welcome & Onboarding UI Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على النوافذ المنبثقة الترحيبية والتعريفية التي تظهر للمستخدمين الجدد
 * عند أول تشغيل للتطبيق لتعريفهم بالمزايا المحاسبية والأمنية الأساسية.
 * =====================================================================================
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R

/*
 * =====================================================================================
 * ثوابت وسوم الحركات الانتقالية (Animation Transition Labels)
 * -------------------------------------------------------------------------------------
 * تُستخدم لتحديد وتتبع الحركات في أدوات الفحص والتطوير الخاصة بـ Jetpack Compose.
 * =====================================================================================
 */
private const val LABEL_ONBOARDING_SCALE = "onboarding_scale"
private const val LABEL_ONBOARDING_ALPHA = "onboarding_alpha"
private const val LABEL_PULSE = "pulse"
private const val LABEL_BUTTON_SCALE = "button_scale"

/*
 * =====================================================================================
 * نافذة الترحيب والتعريف بمزايا التطبيق (WelcomeOnboardingDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية أنيقة كاملة الشاشة تظهر للمستخدم لأول مرة لتقديم التطبيق:
 * 1. استعراض الهوية البصرية وشعار التطبيق بتأثير توهج ضوئي (Glow Effect).
 * 2. عرض المزايا الرئيسية في بطاقات قابلة للتمرير (الميزان، الحبايب، السلة، الأمان).
 * 3. تطبيق حركة دخول نابضة (Spring Physics Animation) مع نبض مستمر لزر بدء الاستخدام.
 * 4. فرض استجابة صريحة من المستخدم لمنع الإغلاق العرضي بالنقر خارج النافذة.
 *
 * [البيانات والمُدخلات]:
 * - onDismiss: الإجراء المنفذ عند نقر زر البدء لإغلاق النافذة وتحديث حالة التهيئة.
 * =====================================================================================
 */
@Composable
fun WelcomeOnboardingDialog(
    onDismiss: () -> Unit
) {
    // التحقق من الوضع الليلي لتحديد شدة الظلال ودرجات التباين
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    /*
     * ---------------------------------------------------------------------------------
     * إعداد حركات الظهور والتكبير (Entry Scale & Fade Animations)
     * ---------------------------------------------------------------------------------
     */
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = LABEL_ONBOARDING_SCALE
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = LABEL_ONBOARDING_ALPHA
    )

    /*
     * ---------------------------------------------------------------------------------
     * حركة النبض اللانهائية لزر بدء الاستخدام (Breathing Pulse Animation)
     * ---------------------------------------------------------------------------------
     */
    val infiniteTransition = rememberInfiniteTransition(label = LABEL_PULSE)
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = LABEL_BUTTON_SCALE
    )

    val cardBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val itemBg = MaterialTheme.colorScheme.surfaceContainer
    val itemBorder = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    /*
     * ---------------------------------------------------------------------------------
     * إنشاء نافذة الحوار المخصصة (Custom Modal Dialog)
     * ---------------------------------------------------------------------------------
     */
    Dialog(
        onDismissRequest = { /* منع الإغلاق التلقائي لإلزام المستخدم بالنقر على زر البدء */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    alpha = animatedAlpha
                }
                .shadow(
                    elevation = if (isDark) 24.dp else 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = primaryColor.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = cardBg,
            border = BorderStroke(1.dp, itemBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*
                 * ---------------------------------------------------------------------
                 * شارة التوهج والشعار العلوي (Glow Badge & App Icon)
                 * ---------------------------------------------------------------------
                 */
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.22f),
                                    primaryColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.4f))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                /*
                 * ---------------------------------------------------------------------
                 * عنوان التطبيق والشعار اللفظي (Title & Slogan)
                 * ---------------------------------------------------------------------
                 */
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.onboarding_slogan).replace("🏠 ", ""),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                /*
                 * ---------------------------------------------------------------------
                 * حاوية بطاقات المزايا القابلة للتمرير (Feature Cards Scrollable List)
                 * ---------------------------------------------------------------------
                 */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // بطاقة ميزة الميزان المحاسبي
                    OnboardingFeatureCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBg = primaryColor,
                        title = stringResource(id = R.string.onboarding_mizan_title).replace(":", ""),
                        description = stringResource(id = R.string.onboarding_mizan_desc),
                        containerBg = itemBg,
                        borderColor = itemBorder
                    )

                    // بطاقة ميزة إدارة الحبايب (العملاء)
                    OnboardingFeatureCard(
                        icon = Icons.Default.People,
                        iconBg = primaryColor,
                        title = stringResource(id = R.string.onboarding_habayeb_title).replace(":", ""),
                        description = stringResource(id = R.string.onboarding_habayeb_desc),
                        containerBg = itemBg,
                        borderColor = itemBorder
                    )

                    // بطاقة ميزة سلة المهملات واسترجاع السجلات
                    OnboardingFeatureCard(
                        icon = Icons.Default.DeleteOutline,
                        iconBg = primaryColor,
                        title = stringResource(id = R.string.onboarding_trash_title).replace(":", ""),
                        description = stringResource(id = R.string.onboarding_trash_desc),
                        containerBg = itemBg,
                        borderColor = itemBorder
                    )

                    // بطاقة ميزة الأمان والنسخ الاحتياطي
                    OnboardingFeatureCard(
                        icon = Icons.Default.Security,
                        iconBg = primaryColor,
                        title = stringResource(id = R.string.onboarding_backup_title).replace(":", ""),
                        description = stringResource(id = R.string.onboarding_backup_desc),
                        containerBg = itemBg,
                        borderColor = itemBorder
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                /*
                 * ---------------------------------------------------------------------
                 * زر بدء الاستخدام بتدرج لوني نابض (Gradient Call-To-Action Button)
                 * ---------------------------------------------------------------------
                 */
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = primaryColor
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.onboarding_start_button).replace(" 🏠", ""),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
 * =====================================================================================
 * المكون المساعد: بطاقة عرض الميزة المنفردة (OnboardingFeatureCard)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة أفقية مدمجة تستعرض ميزة محددة بأيقونة ملونة وعنوان وشرح مبسط.
 * =====================================================================================
 */
@Composable
private fun OnboardingFeatureCard(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    description: String,
    containerBg: Color,
    borderColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // كبسولة الأيقونة الملونة
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // النصوص التوضيحية للميزة
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}


