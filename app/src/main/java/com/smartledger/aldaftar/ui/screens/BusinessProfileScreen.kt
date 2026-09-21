package com.smartledger.aldaftar.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.helper.BusinessProfileImageHelper
import com.smartledger.aldaftar.ui.screens.business.BusinessProfileInfoSection
import com.smartledger.aldaftar.ui.screens.business.BusinessProfileLogoSection
import com.smartledger.aldaftar.ui.screens.business.BusinessProfilePhonesSection
import com.smartledger.aldaftar.ui.screens.settings.components.LogoCropDialog
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface BusinessProfileDialogState {
    object None : BusinessProfileDialogState
    data class CropLogo(val bitmap: Bitmap, val isCircle: Boolean) : BusinessProfileDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    viewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val activeThemeColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.biz_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("biz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.biz_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = activeThemeColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BusinessProfileForm(
                viewModel = viewModel,
                isDialog = false,
                onClose = onBack
            )
        }
    }
}

@Composable
fun BusinessProfileDialog(
    viewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel,
    onDismiss: () -> Unit
) {
    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.biz_title),
                subtitle = stringResource(id = R.string.biz_desc_header),
                icon = null,
                iconTint = MaterialTheme.colorScheme.primary,
                isCentered = true,
                onCloseClick = dismissDialog
            )

            BusinessProfileForm(
                viewModel = viewModel,
                isDialog = true,
                onClose = dismissDialog
            )
        }
    }
}

@Composable
private fun BusinessProfileForm(
    viewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel,
    isDialog: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeThemeColor = MaterialTheme.colorScheme.primary

    var bizName by remember { mutableStateOf("") }
    var bizDesc by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf("") }
    val phoneList = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        val profile = viewModel.profile.value
        bizName = profile.name
        bizDesc = profile.description
        logoPath = profile.logoPath
        phoneList.clear(); phoneList.addAll(profile.phones)
    }

    var logoBitmapState by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(logoPath) {
        logoBitmapState = if (logoPath.isBlank()) {
            null
        } else {
            try {
                withContext(Dispatchers.IO) {
                    val file = File(logoPath)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }

    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var dialogState by remember { mutableStateOf<BusinessProfileDialogState>(BusinessProfileDialogState.None) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImageUri = uri
            coroutineScope.launch {
                try {
                    val scaled = withContext(Dispatchers.IO) {
                        val original = BusinessProfileImageHelper.uriToBitmap(context, uri)
                        if (original != null) BusinessProfileImageHelper.scaleBitmap(original, 800) else null
                    }
                    if (scaled != null && !scaled.isRecycled) {
                        dialogState = BusinessProfileDialogState.CropLogo(scaled, false)
                    } else {
                        Toast.makeText(context, context.getString(R.string.biz_toast_logo_failed), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    Toast.makeText(context, context.getString(R.string.biz_toast_logo_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val handleSave = {
        if (bizName.isBlank()) {
            Toast.makeText(context, context.getString(R.string.biz_toast_err_empty_name), Toast.LENGTH_SHORT).show()
        } else {
            coroutineScope.launch {
                viewModel.save(BusinessProfile(name = bizName.trim(), description = bizDesc.trim(), logoPath = logoPath, phones = phoneList.toList()))
                Toast.makeText(context, context.getString(R.string.biz_toast_save_success), Toast.LENGTH_SHORT).show()
                onClose()
            }
        }
    }

    val handleReset = {
        coroutineScope.launch {
            viewModel.resetProfile()
            bizName = ""
            bizDesc = ""
            logoPath = ""
            phoneList.clear()
            logoBitmapState = null
            Toast.makeText(context, context.getString(R.string.biz_reset_success), Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDialog) {
                    Modifier
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                } else {
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .padding(bottom = 12.dp)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BusinessProfileLogoSection(
            logoBitmapState = logoBitmapState,
            isDialog = isDialog,
            activeThemeColor = activeThemeColor,
            galleryLauncher = galleryLauncher,
            onDeleteLogo = {
                logoPath = ""
                logoBitmapState = null
            }
        )

        BusinessProfileInfoSection(
            bizName = bizName,
            onBizNameChange = { bizName = it },
            bizDesc = bizDesc,
            onBizDescChange = { bizDesc = it },
            isDialog = isDialog,
            activeThemeColor = activeThemeColor
        )

        BusinessProfilePhonesSection(
            phoneList = phoneList,
            onPhoneChange = { index, newVal -> phoneList[index] = newVal },
            onRemovePhone = { index -> phoneList.removeAt(index) },
            onAddPhone = { phoneList.add("") },
            isDialog = isDialog,
            activeThemeColor = activeThemeColor
        )

        if (isDialog) {
            MizanDialogActions(
                confirmText = stringResource(id = R.string.biz_btn_save),
                onConfirm = { handleSave() },
                confirmIcon = Icons.Default.Check,
                cancelText = stringResource(id = R.string.common_cancel),
                onCancel = onClose,
                extraActionText = stringResource(id = R.string.biz_btn_reset),
                onExtraAction = { handleReset() }
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { handleReset() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("biz_reset_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.biz_btn_reset),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = { handleSave() },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("biz_save_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = activeThemeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.biz_btn_save),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    (dialogState as? BusinessProfileDialogState.CropLogo)?.let { cropState ->
        val density = LocalDensity.current.density
        val bitmapToCrop = cropState.bitmap
        val cropShapeIsCircle = cropState.isCircle
        LogoCropDialog(
            editingBitmap = bitmapToCrop,
            cropShapeIsCircle = cropShapeIsCircle,
            onCropShapeChange = { isCircle ->
                dialogState = cropState.copy(isCircle = isCircle)
            },
            activeThemeColor = activeThemeColor,
            onRotate = {
                coroutineScope.launch {
                    try {
                        val rotated = withContext(Dispatchers.Default) {
                            BusinessProfileImageHelper.rotateBitmap(bitmapToCrop, 90f)
                        }
                        if (!rotated.isRecycled) {
                            dialogState = cropState.copy(bitmap = rotated)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        dialogState = BusinessProfileDialogState.None
                    }
                }
            },
            onDismiss = {
                dialogState = BusinessProfileDialogState.None
                pendingImageUri = null
            },
            onApply = { scale, offsetX, offsetY ->
                coroutineScope.launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            val croppedResult = BusinessProfileImageHelper.cropWithTransform(
                                bitmapToCrop, scale, offsetX, offsetY, density, cropShapeIsCircle
                            )
                            val scaledResult = BusinessProfileImageHelper.scaleBitmap(croppedResult, 400)
                            val localPath = BusinessProfileImageHelper.saveBitmapToInternalStorage(context, scaledResult)
                            localPath to scaledResult
                        }
                        val localPath = result.first
                        if (localPath != null) {
                            logoPath = localPath
                            logoBitmapState = result.second
                            Toast.makeText(context, context.getString(R.string.biz_toast_logo_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, context.getString(R.string.biz_toast_logo_save_err), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        Toast.makeText(context, context.getString(R.string.biz_toast_logo_save_err), Toast.LENGTH_SHORT).show()
                    } finally {
                        dialogState = BusinessProfileDialogState.None
                        pendingImageUri = null
                    }
                }
            }
        )
    }
}
