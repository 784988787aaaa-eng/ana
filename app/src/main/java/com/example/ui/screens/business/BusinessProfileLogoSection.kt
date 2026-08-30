package com.example.ui.screens.business

/*
 * =====================================================================================
 * حزمة قسم شعار المنشأة والنشاط التجاري (Business Profile Logo Section Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على المكون البصري الخاص باختيار ومعاينة وحذف شعار المنشأة:
 * - التقاط واختيار الصور من معرض صور الجهاز عبر (PickVisualMedia).
 * - عرض الشعار بشكل دائري مقتص (Cropped Circle) مع أزرار التعديل والحذف المتراكبة.
 * - دعم العرض المستقل أو داخل نافذة حوار منبثقة.
 * =====================================================================================
 */

import android.graphics.Bitmap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/*
 * =====================================================================================
 * قسم إدارة شعار المنشأة التجاري (BusinessProfileLogoSection)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة تفاعلية مخصصة لاختيار وتعديل وحذف الشعار البصري للمؤسسة لطباعته في الفواتير والتقارير:
 * 1. إطار دائري لمعاينة صورة الشعار أو إظهار أيقونة الإضافة عند عدم وجود شعار.
 * 2. تراكب أيقونة التعديل لفتح منتقي وسائط النظام (Photo Picker).
 * 3. تراكب أيقونة الحذف لإزالة الشعار الحالي وإعادة التعيين للشكل الافتراضي.
 *
 * [المُدخلات]:
 * - logoBitmapState: صورة الشعار المحملة حالياً (Bitmap) أو null إذا لم يتوفر شعار.
 * - isDialog: هل المكون معروض داخل حوار لضبط الارتفاع البصري.
 * - activeThemeColor: لون السِمة المخصص النشط لتلوين الإطار والأيقونات.
 * - galleryLauncher: مشغل منتقي الصور من معرض النظام.
 * - onDeleteLogo: رد نداء حذف الشعار الحالي.
 * =====================================================================================
 */
@Composable
fun BusinessProfileLogoSection(
    logoBitmapState: Bitmap?,
    isDialog: Boolean,
    activeThemeColor: Color,
    galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, android.net.Uri?>,
    onDeleteLogo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDialog) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.biz_logo_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // إطار عرض وتعديل الشعار الدائري
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .border(2.dp, activeThemeColor.copy(alpha = 0.3f), CircleShape)
                    .clickable {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("biz_logo_box"),
                contentAlignment = Alignment.Center
            ) {
                if (logoBitmapState != null) {
                    Image(
                        bitmap = logoBitmapState.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.biz_logo_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // أيقونة التعديل المتراكبة في الأسفل
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(activeThemeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.biz_edit),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // أيقونة الحذف المتراكبة في حال وجود شعار
                if (logoBitmapState != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                            .clickable { onDeleteLogo() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.desc_remove_logo),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

