package com.smartledger.aldaftar.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.business.BusinessPhoneFormatter
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
import com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel
import com.smartledger.aldaftar.ui.viewmodel.PhoneInputData
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BusinessProfileDialogState {
    object None : BusinessProfileDialogState
    data class CropLogo(val bitmap: Bitmap, val isCircle: Boolean) : BusinessProfileDialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    viewModel: BusinessProfileViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val activeThemeColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = activeThemeColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, activeThemeColor.copy(alpha = 0.25f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = activeThemeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(id = R.string.biz_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(id = R.string.biz_desc_header),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
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
    viewModel: BusinessProfileViewModel,
    onDismiss: () -> Unit
) {
    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.maxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.biz_title),
                subtitle = stringResource(id = R.string.biz_desc_header),
                icon = Icons.Default.Business,
                iconTint = MaterialTheme.colorScheme.primary,
                isCentered = false,
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
    viewModel: BusinessProfileViewModel,
    isDialog: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeThemeColor = MaterialTheme.colorScheme.primary

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var bizName by remember { mutableStateOf("") }
    var bizDesc by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf("") }
    val phoneInputs = remember { mutableStateListOf<PhoneInputData>() }

    LaunchedEffect(profile) {
        bizName = profile.name
        bizDesc = profile.description
        logoPath = profile.logoPath

        phoneInputs.clear()
        if (profile.phones.isNotEmpty()) {
            profile.phones.forEachIndexed { idx, raw ->
                val parsed = BusinessPhoneFormatter.parseRawPhone(raw)
                phoneInputs.add(PhoneInputData(id = idx, countryCode = parsed.countryCode, nationalNumber = parsed.nationalNumber))
            }
        } else {
            phoneInputs.add(PhoneInputData(id = 0))
        }
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
        viewModel.saveProfile(
            name = bizName,
            description = bizDesc,
            logoPath = logoPath,
            phoneInputs = phoneInputs.toList(),
            onSuccess = {
                Toast.makeText(context, context.getString(R.string.biz_toast_save_success), Toast.LENGTH_SHORT).show()
                onClose()
            }
        )
    }

    val handleReset = {
        coroutineScope.launch {
            viewModel.resetProfile()
            bizName = ""
            bizDesc = ""
            logoPath = ""
            phoneInputs.clear()
            phoneInputs.add(PhoneInputData(id = 0))
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
                        .padding(bottom = 4.dp)
                } else {
                    Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .padding(bottom = 14.dp)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
            onBizNameChange = {
                bizName = it
                viewModel.clearSaveState()
            },
            bizDesc = bizDesc,
            onBizDescChange = { bizDesc = it },
            isDialog = isDialog,
            activeThemeColor = activeThemeColor
        )

        if (uiState.nameError != null) {
            Text(
                text = uiState.nameError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            )
        }

        BusinessProfilePhonesSection(
            phoneInputs = phoneInputs,
            phoneErrors = uiState.phoneErrors,
            onPhoneDataChange = { index, newData ->
                if (index in phoneInputs.indices) {
                    phoneInputs[index] = newData
                } else if (index == phoneInputs.size) {
                    phoneInputs.add(newData)
                }
                viewModel.clearSaveState()
            },
            onRemovePhone = { index ->
                if (index in phoneInputs.indices) {
                    phoneInputs.removeAt(index)
                }
                if (phoneInputs.isEmpty()) {
                    phoneInputs.add(PhoneInputData(id = 0))
                }
                viewModel.clearSaveState()
            },
            onAddPhone = {
                if (phoneInputs.size < 3) {
                    phoneInputs.add(PhoneInputData(id = phoneInputs.size))
                }
                viewModel.clearSaveState()
            },
            isDialog = isDialog,
            activeThemeColor = activeThemeColor
        )

        if (uiState.errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.22f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (isDialog) {
            MizanDialogActions(
                confirmText = stringResource(id = R.string.biz_btn_save),
                onConfirm = { handleSave() },
                confirmIcon = Icons.Default.Check,
                isConfirmLoading = uiState.isSaving,
                cancelText = stringResource(id = R.string.common_cancel),
                onCancel = onClose,
                extraActionText = stringResource(id = R.string.biz_btn_reset),
                onExtraAction = { handleReset() }
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { handleReset() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("biz_reset_button"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.04f),
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
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .weight(1.35f)
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
                            text = if (uiState.isSaving) {
                                stringResource(id = R.string.biz_saving_progress)
                            } else {
                                stringResource(id = R.string.biz_btn_save)
                            },
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (dialogState is BusinessProfileDialogState.CropLogo) {
        val cropState = dialogState as BusinessProfileDialogState.CropLogo
        var cropIsCircle by remember { mutableStateOf(cropState.isCircle) }
        var currentBitmap by remember { mutableStateOf(cropState.bitmap) }

        LogoCropDialog(
            editingBitmap = currentBitmap,
            cropShapeIsCircle = cropIsCircle,
            onCropShapeChange = { cropIsCircle = it },
            activeThemeColor = activeThemeColor,
            onRotate = {
                val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                val rotated = Bitmap.createBitmap(currentBitmap, 0, 0, currentBitmap.width, currentBitmap.height, matrix, true)
                currentBitmap = rotated
            },
            onDismiss = { dialogState = BusinessProfileDialogState.None },
            onApply = { scale, offsetX, offsetY ->
                coroutineScope.launch {
                    try {
                        val density = context.resources.displayMetrics.density
                        val cropped = withContext(Dispatchers.IO) {
                            BusinessProfileImageHelper.cropWithTransform(
                                currentBitmap,
                                scale,
                                offsetX,
                                offsetY,
                                density,
                                cropIsCircle
                            )
                        }
                        val path = withContext(Dispatchers.IO) {
                            BusinessProfileImageHelper.saveBitmapToInternalStorage(context, cropped)
                        }
                        if (path != null) {
                            logoPath = path
                            logoBitmapState = cropped
                        } else {
                            Toast.makeText(context, context.getString(R.string.biz_toast_logo_failed), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        Toast.makeText(context, context.getString(R.string.biz_toast_logo_failed), Toast.LENGTH_SHORT).show()
                    }
                    dialogState = BusinessProfileDialogState.None
                }
            }
        )
    }
}
