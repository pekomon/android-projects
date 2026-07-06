package com.pekomon.snapreceipt.feature.capture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.pekomon.snapreceipt.R
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import java.io.File

@Composable
fun CaptureScreen(
    uiState: CaptureUiState,
    onImageImported: (String, ReceiptSource, String?) -> Unit,
    onClearImportedImage: () -> Unit,
    onReviewDraft: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember(context) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    var isCameraMode by rememberSaveable { mutableStateOf(false) }
    var isCapturingPhoto by rememberSaveable { mutableStateOf(false) }
    var cameraFeedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(cameraController, lifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
        onDispose {
            cameraController.unbind()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isCameraMode = false
            onImageImported(
                uri.toString(),
                ReceiptSource.PHOTO_PICKER,
                context.contentResolver.getType(uri)
            )
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isCameraMode = false
            onImageImported(
                uri.toString(),
                ReceiptSource.FILE_IMPORT,
                context.contentResolver.getType(uri)
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraFeedbackMessage = null
            isCameraMode = true
        } else {
            cameraFeedbackMessage = context.getString(R.string.capture_camera_permission_denied)
            isCameraMode = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.capture_eyebrow),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.capture_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.capture_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                cameraFeedbackMessage = null
                                isCameraMode = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.capture_action_camera))
                    }
                    OutlinedButton(
                        onClick = {
                            cameraFeedbackMessage = null
                            isCameraMode = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.capture_action_photo))
                    }
                }

                OutlinedButton(
                    onClick = {
                        cameraFeedbackMessage = null
                        isCameraMode = false
                        filePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.capture_action_file))
                }

                if (cameraFeedbackMessage != null) {
                    OutlinedButton(
                        onClick = { cameraFeedbackMessage = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = cameraFeedbackMessage.orEmpty())
                    }
                }

                CaptureInfoCard(
                    title = stringResource(R.string.capture_card_primary_title),
                    body = captureStatusText(uiState)
                )

                if (isCameraMode) {
                    CameraCaptureCard(
                        cameraController = cameraController,
                        isCapturingPhoto = isCapturingPhoto,
                        onCapturePhoto = {
                            isCapturingPhoto = true
                            captureReceiptPhoto(
                                context = context,
                                cameraController = cameraController,
                                onSuccess = { capturedUri ->
                                    isCapturingPhoto = false
                                    isCameraMode = false
                                    cameraFeedbackMessage = null
                                    onImageImported(
                                        capturedUri.toString(),
                                        ReceiptSource.CAMERA,
                                        "image/jpeg"
                                    )
                                },
                                onFailure = { message ->
                                    isCapturingPhoto = false
                                    cameraFeedbackMessage = message
                                }
                            )
                        }
                    )
                }

                val selectedImage = uiState.selectedImage
                if (selectedImage != null) {
                    ImportedReceiptPreview(
                        image = selectedImage,
                        draft = uiState.draft,
                        isRunningOcr = uiState.isRunningOcr,
                        ocrErrorMessage = uiState.ocrErrorMessage,
                        onReviewDraft = onReviewDraft,
                        onClear = onClearImportedImage
                    )
                } else {
                    CaptureInfoCard(
                        title = stringResource(R.string.capture_card_secondary_title),
                        body = stringResource(R.string.capture_card_secondary_body)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraCaptureCard(
    cameraController: LifecycleCameraController,
    isCapturingPhoto: Boolean,
    onCapturePhoto: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.capture_camera_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.capture_camera_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AndroidView(
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        controller = cameraController
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            Button(
                onClick = onCapturePhoto,
                enabled = !isCapturingPhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCapturingPhoto) {
                        stringResource(R.string.capture_camera_capturing)
                    } else {
                        stringResource(R.string.capture_camera_take_photo)
                    }
                )
            }
        }
    }
}

@Composable
private fun ImportedReceiptPreview(
    image: ReceiptImage,
    draft: ReceiptDraft?,
    isRunningOcr: Boolean,
    ocrErrorMessage: String?,
    onReviewDraft: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.capture_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            AsyncImage(
                model = Uri.parse(image.localPath),
                contentDescription = stringResource(R.string.capture_preview_content_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = when (image.source) {
                    ReceiptSource.PHOTO_PICKER -> stringResource(R.string.capture_source_photo_picker)
                    ReceiptSource.FILE_IMPORT -> stringResource(R.string.capture_source_file_import)
                    ReceiptSource.CAMERA -> stringResource(R.string.capture_source_camera)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OcrStatusPanel(
                draft = draft,
                isRunningOcr = isRunningOcr,
                ocrErrorMessage = ocrErrorMessage,
                onReviewDraft = onReviewDraft
            )
            OutlinedButton(onClick = onClear) {
                Text(text = stringResource(R.string.capture_action_clear))
            }
        }
    }
}

@Composable
private fun CaptureInfoCard(
    title: String,
    body: String
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OcrStatusPanel(
    draft: ReceiptDraft?,
    isRunningOcr: Boolean,
    ocrErrorMessage: String?,
    onReviewDraft: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.capture_ocr_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            when {
                isRunningOcr -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.capture_ocr_running),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ocrErrorMessage != null -> {
                    Text(
                        text = ocrErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                draft?.ocrResult != null -> {
                    Text(
                        text = stringResource(
                            R.string.capture_ocr_success,
                            draft.ocrResult.lineBlocks.size
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = draft.ocrResult.cleanedText.ifBlank {
                            stringResource(R.string.capture_ocr_empty_result)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(onClick = onReviewDraft) {
                        Text(text = stringResource(R.string.capture_action_review_draft))
                    }
                }
            }
        }
    }
}

@Composable
private fun captureStatusText(uiState: CaptureUiState): String = when {
    uiState.lastSavedReceipt != null -> "Saved ${uiState.lastSavedReceipt.merchantName} locally."
    uiState.selectedImage == null -> stringResource(R.string.capture_idle_hint)
    uiState.selectedImage.source == ReceiptSource.CAMERA && uiState.isRunningOcr.not() && uiState.ocrErrorMessage == null && uiState.draft == null ->
        stringResource(R.string.capture_import_success_camera)
    uiState.isRunningOcr -> stringResource(R.string.capture_import_success_ocr_running)
    uiState.ocrErrorMessage != null -> stringResource(R.string.capture_import_success_ocr_failed)
    uiState.draft != null -> stringResource(R.string.capture_import_success_ocr_done)
    else -> stringResource(R.string.capture_idle_hint)
}

private fun captureReceiptPhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onSuccess: (Uri) -> Unit,
    onFailure: (String) -> Unit
) {
    val outputFile = createCameraCaptureFile(context)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    cameraController.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSuccess(outputFileResults.savedUri ?: Uri.fromFile(outputFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onFailure(
                    exception.message
                        ?: context.getString(R.string.capture_camera_capture_failed)
                )
            }
        }
    )
}

private fun createCameraCaptureFile(context: Context): File {
    val captureDirectory = File(context.cacheDir, "camera-captures").apply {
        mkdirs()
    }
    return File(captureDirectory, "receipt-capture-${System.currentTimeMillis()}.jpg")
}
