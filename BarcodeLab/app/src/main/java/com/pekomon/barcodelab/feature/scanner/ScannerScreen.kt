package com.pekomon.barcodelab.feature.scanner

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pekomon.barcodelab.core.barcode.MlKitBarcodeAnalyzer
import com.pekomon.barcodelab.domain.model.ScanResult
import com.pekomon.barcodelab.domain.model.ValidationStatus
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    if (hasCameraPermission) {
        ScannerContent(
            uiState = uiState,
            onBarcodeDetected = viewModel::onBarcodeDetected,
            onAnalyzerError = viewModel::onAnalyzerError,
            onResumeScanning = viewModel::resumeScanning,
        )
    } else {
        PermissionContent(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
        )
    }
}

@Composable
private fun ScannerContent(
    uiState: ScannerUiState,
    onBarcodeDetected: (com.pekomon.barcodelab.domain.model.DetectedBarcode) -> Unit,
    onAnalyzerError: (Throwable) -> Unit,
    onResumeScanning: () -> Unit,
) {
    val context = LocalContext.current
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black),
        ) {
            CameraPreview(
                scanningEnabled = !uiState.isPaused,
                onBarcodeDetected = onBarcodeDetected,
                onAnalyzerError = onAnalyzerError,
                onCameraReady = { camera = it },
            )
            ScannerReticle(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 120.dp),
            )
            HeaderBar(
                analyzerError = uiState.analyzerError,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                TorchButton(
                    enabled = torchEnabled,
                    onToggle = {
                        torchEnabled = !torchEnabled
                        camera?.cameraControl?.enableTorch(torchEnabled)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(top = 14.dp, end = 16.dp),
                )
            }
            ResultSheet(
                result = uiState.lastResult,
                paused = uiState.isPaused,
                recentScans = uiState.recentScans,
                onResumeScanning = onResumeScanning,
                onCopy = { result ->
                    context.copyToClipboard(result.payload.rawValue)
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun PermissionContent(onRequestPermission: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Barcode Lab",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Camera access is required for live barcode scanning.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("Allow camera")
            }
        }
    }
}

@Composable
private fun CameraPreview(
    scanningEnabled: Boolean,
    onBarcodeDetected: (com.pekomon.barcodelab.domain.model.DetectedBarcode) -> Unit,
    onAnalyzerError: (Throwable) -> Unit,
    onCameraReady: (Camera) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val latestScanningEnabled by rememberUpdatedState(scanningEnabled)
    val latestBarcodeDetected by rememberUpdatedState(onBarcodeDetected)
    val latestAnalyzerError by rememberUpdatedState(onAnalyzerError)
    val analyzer = remember {
        MlKitBarcodeAnalyzer(
            isScanningEnabled = { latestScanningEnabled },
            onBarcodeDetected = { latestBarcodeDetected(it) },
            onAnalyzerError = { latestAnalyzerError(it) },
        )
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
    )

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindCamera(
                    cameraProvider = cameraProvider,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    analyzer = analyzer,
                    analysisExecutor = analysisExecutor,
                    onCameraReady = onCameraReady,
                )
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            analyzer.close()
            analysisExecutor.shutdown()
        }
    }
}

private fun bindCamera(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    analyzer: ImageAnalysis.Analyzer,
    analysisExecutor: ExecutorService,
    onCameraReady: (Camera) -> Unit,
) {
    val preview = Preview.Builder()
        .build()
        .also { it.setSurfaceProvider(previewView.surfaceProvider) }
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { it.setAnalyzer(analysisExecutor, analyzer) }

    cameraProvider.unbindAll()
    val camera = cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageAnalysis,
    )
    onCameraReady(camera)
}

@Composable
private fun HeaderBar(
    analyzerError: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.54f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = "Barcode Lab",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = analyzerError ?: "QR, PDF417, Aztec, Data Matrix",
                style = MaterialTheme.typography.bodySmall,
                color = if (analyzerError == null) Color.White.copy(alpha = 0.74f) else Color(0xFFFFC4B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TorchButton(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.inverseSurface) {
                Text(
                    text = if (enabled) "Turn torch off" else "Turn torch on",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = onToggle,
            modifier = modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.54f), RoundedCornerShape(8.dp)),
        ) {
            Icon(
                imageVector = if (enabled) Icons.Filled.FlashlightOff else Icons.Filled.FlashlightOn,
                contentDescription = if (enabled) "Turn torch off" else "Turn torch on",
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun ScannerReticle(modifier: Modifier = Modifier) {
    val color = Color(0xFF18A77A)
    Canvas(modifier = modifier) {
        val width = size.width
        val reticleWidth = width.coerceAtMost(420.dp.toPx())
        val reticleHeight = reticleWidth * 0.62f
        val left = (size.width - reticleWidth) / 2f
        val top = (size.height - reticleHeight) / 2f
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(left, top),
            size = Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(reticleWidth, reticleHeight),
            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
            style = Stroke(
                width = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(42f, 24f)),
            ),
        )
        drawLine(
            color = color.copy(alpha = 0.78f),
            start = Offset(left + 18.dp.toPx(), top + reticleHeight / 2f),
            end = Offset(left + reticleWidth - 18.dp.toPx(), top + reticleHeight / 2f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun ResultSheet(
    result: ScanResult?,
    paused: Boolean,
    recentScans: List<ScanResult>,
    onResumeScanning: () -> Unit,
    onCopy: (ScanResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (result == null) {
                Text(
                    text = "Scanning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "No supported 2D barcode detected yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                ResultDetails(
                    result = result,
                    paused = paused,
                    onResumeScanning = onResumeScanning,
                    onCopy = onCopy,
                )
            }

            if (recentScans.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                recentScans.drop(1).take(3).forEach { scan ->
                    Text(
                        text = "${scan.detectedBarcode.format.label} · ${scan.payload.kind.label}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultDetails(
    result: ScanResult,
    paused: Boolean,
    onResumeScanning: () -> Unit,
    onCopy: (ScanResult) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.detectedBarcode.format.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${result.payload.kind.label} · ${result.validation.status.name}",
                style = MaterialTheme.typography.bodySmall,
                color = result.validation.status.statusColor(),
            )
        }
        IconButton(onClick = { onCopy(result) }) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Copy result",
            )
        }
        if (paused) {
            IconButton(onClick = onResumeScanning) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Resume scanning",
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = result.payload.displayValue,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        color = result.validation.status.statusColor().copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = result.validation.title,
                style = MaterialTheme.typography.labelLarge,
                color = result.validation.status.statusColor(),
            )
            Text(
                text = result.validation.detail,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ValidationStatus.statusColor(): Color =
    when (this) {
        ValidationStatus.Valid -> Color(0xFF0F8F68)
        ValidationStatus.Warning -> Color(0xFF9A6A00)
        ValidationStatus.Invalid -> MaterialTheme.colorScheme.error
        ValidationStatus.Unsupported -> Color(0xFF5D6470)
    }

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun Context.copyToClipboard(value: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Barcode result", value))
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
