package com.smartledger.aldaftar.ui.screens.business

import android.graphics.Bitmap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R

@Composable
fun BusinessProfileLogoSection(
    logoBitmapState: Bitmap?,
    isDialog: Boolean,
    activeThemeColor: Color,
    galleryLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, android.net.Uri?>,
    onDeleteLogo: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDialog) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = if (isDialog) 4.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isDialog) {
                Text(
                    text = stringResource(id = R.string.biz_logo_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Box(
                modifier = Modifier
                    .size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                // Main Logo Box with Dual Ring
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .border(2.dp, activeThemeColor.copy(alpha = 0.20f), CircleShape)
                        .padding(2.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f), CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
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
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Edit badge (24dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(activeThemeColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.biz_edit),
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Delete badge (24dp with 40dp touch target)
                if (logoBitmapState != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 20.dp),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDeleteLogo()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.desc_remove_logo),
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
