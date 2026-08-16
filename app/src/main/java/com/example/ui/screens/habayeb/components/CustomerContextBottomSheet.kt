package com.example.ui.screens.habayeb.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.viewmodel.FinanceConstants

/**
 * نافذة تحكم الحساب المبتكرة والأنيقة للغاية:
 * تم تصميمها لتعبر عن طابع عصري رشيق، وتضم خيار الحذف المباشر السهل والسريع.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CustomerContextBottomSheet(
    customer: com.example.ui.state.CustomerUiState,
    customCategories: List<com.example.data.local.entities.CustomCategory>,
    isPinned: Boolean,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onAssignCategory: (String?) -> Unit,
    onEnableMultiSelect: () -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit,
    onUpdateCustomerType: (String) -> Unit,
    currentActiveCategory: String? = null
) {
    var showCategoriesState by remember { mutableStateOf(false) }
    var pendingTypeChange by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .width(310.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            AnimatedContent(
                targetState = showCategoriesState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) with fadeOut(animationSpec = tween(180))
                },
                label = "contextMenuTransition"
            ) { isCategoryScreen ->
                if (!isCategoryScreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // رأس النافذة: أزرار الركن العلوية بدون خلفيات دائرية، مع توسيط عنوان النافذة واسم العميل بالكامل
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // أزرار الحذف والتعديل في الركن العلوي بدون خلفية دائرية
                            Row(
                                modifier = Modifier.align(Alignment.TopStart),
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        onDismiss()
                                        onDelete()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.context_menu_delete_customer),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onDismiss()
                                        onEditClick()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.habayeb_edit_name_title),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // عنوان النافذة واسم العميل في المنتصف تماماً
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.habayeb_manage_account_title),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = customer.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // فاصل بصري رقيق للغاية أسفل صف الاسم
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )

                        // خيارات الإدارة مرتفعة للأعلى بمكان احترافي وبلا مساحات ضائعة
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            // 1. تثبيت / إلغاء تثبيت الحساب
                            ContextMenuItem(
                                icon = Icons.Default.Star,
                                text = stringResource(if (isPinned) R.string.context_menu_unpin_customer else R.string.context_menu_pin_customer),
                                iconTint = activeThemeColor,
                                iconBgColor = activeThemeColor.copy(alpha = 0.1f),
                                onClick = {
                                    onTogglePin()
                                    onDismiss()
                                }
                            )

                            // 2. تصنيف الحساب
                            ContextMenuItem(
                                icon = Icons.Default.Folder,
                                text = stringResource(R.string.context_menu_add_to_category),
                                iconTint = activeThemeColor,
                                iconBgColor = activeThemeColor.copy(alpha = 0.1f),
                                onClick = {
                                    showCategoriesState = true
                                }
                            )

                            // إزالة من هذا التصنيف (تظهر فقط عند تصفح الحساب داخل تصنيف مخصص)
                            if (currentActiveCategory != null && currentActiveCategory != FinanceConstants.CATEGORY_CLOSED) {
                                ContextMenuItem(
                                    icon = Icons.Default.Close,
                                    text = stringResource(R.string.context_menu_remove_from_category, currentActiveCategory),
                                    iconTint = MaterialTheme.colorScheme.error,
                                    iconBgColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    textColor = MaterialTheme.colorScheme.error,
                                    onClick = {
                                        onAssignCategory(null)
                                        onDismiss()
                                    }
                                )
                            }

                            // 3. تحديد متعدد للحسابات
                            ContextMenuItem(
                                icon = Icons.Default.Check,
                                text = stringResource(R.string.context_menu_multi_select),
                                iconTint = activeThemeColor,
                                iconBgColor = activeThemeColor.copy(alpha = 0.1f),
                                onClick = {
                                    onEnableMultiSelect()
                                    onDismiss()
                                }
                            )
                        }

                        // فاصل بصري رقيق للغاية قبل خيار تغيير نوع الحساب بالأسفل
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        // قسم تغيير نوع الحساب معزول بصرياً بالأسفل بشكل فائق الأناقة والرشاقة
                        val bgColor = MaterialTheme.colorScheme.background
                        val isDark = remember(bgColor) { bgColor.run { red < 0.5f } }
                        val activeRedColor = remember(isDark) { if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626) }
                        val activeGreenColor = remember(isDark) { if (isDark) Color(0xFF34D399) else Color(0xFF10B981) }
                        val inactiveBgColor = remember(isDark) { if (isDark) Color(0xFF2D2D2D) else Color(0xFFEEEEEE) }
                        val inactiveTextColor = remember(isDark) { if (isDark) Color.Gray else Color.DarkGray }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.context_menu_change_type_label),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val currentType = customer.originalCustomer.initialType
                                    
                                    // زر "له" (OWED_TO_THEM)
                                    val isToThemSelected = currentType == FinanceConstants.TYPE_OWED_TO_THEM
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isToThemSelected) activeGreenColor else inactiveBgColor)
                                            .clickable {
                                                if (!isToThemSelected) {
                                                    pendingTypeChange = FinanceConstants.TYPE_OWED_TO_THEM
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.habayeb_to_them),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isToThemSelected) Color.White else inactiveTextColor
                                        )
                                    }

                                    // زر "عليه" (OWED_BY_THEM)
                                    val isByThemSelected = currentType == FinanceConstants.TYPE_OWED_BY_THEM
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp)
                                            .padding(horizontal = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isByThemSelected) activeRedColor else inactiveBgColor)
                                            .clickable {
                                                if (!isByThemSelected) {
                                                    pendingTypeChange = FinanceConstants.TYPE_OWED_BY_THEM
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.habayeb_owed),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isByThemSelected) Color.White else inactiveTextColor
                                        )
                                    }
                                }
                            }
                        }

                        if (pendingTypeChange != null) {
                            AlertDialog(
                                onDismissRequest = { pendingTypeChange = null },
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.context_menu_change_type_confirm_title),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.context_menu_change_type_confirm_msg),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                confirmButton = {
                                    // زر (إلغاء) ممتلئ وبارز (المحاذاة التلقائية والتركيز المعكوس للحماية)
                                    Button(
                                        onClick = { pendingTypeChange = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = activeThemeColor),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.context_menu_cancel),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                dismissButton = {
                                    // زر (تأكيد) غير بارز وبسيط للحماية ولونه ديناميكي بالكامل يتوافق مع الخيار المستهدف
                                    val confirmButtonColor = when (pendingTypeChange) {
                                        FinanceConstants.TYPE_OWED_TO_THEM -> if (isDark) Color(0xFF34D399) else Color(0xFF16A34A)
                                        FinanceConstants.TYPE_OWED_BY_THEM -> if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    TextButton(
                                        onClick = {
                                            val newType = pendingTypeChange!!
                                            onUpdateCustomerType(newType)
                                            pendingTypeChange = null
                                            onDismiss()
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.context_menu_confirm),
                                            color = confirmButtonColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                } else {
                    // واجهة اختيار التصنيفات
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // رأس فرعي للرجوع بلمسة جمالية هادئة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { showCategoriesState = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.context_menu_back),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.context_menu_folders_and_categories),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // خيار إزالة التصنيف (بلا تصنيف)
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onAssignCategory(null)
                                            onDismiss()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.context_menu_no_category),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            items(customCategories, key = { it.id }) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onAssignCategory(category.name)
                                            onDismiss()
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(activeThemeColor.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = activeThemeColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = category.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color,
    iconBgColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
