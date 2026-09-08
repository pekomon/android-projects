package com.pekomon.barcodelab.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pekomon.barcodelab.feature.scanner.ScannerScreen
import com.pekomon.barcodelab.feature.scanner.ScannerViewModel

@Composable
fun BarcodeLabApp() {
    val viewModel: ScannerViewModel = viewModel()
    ScannerScreen(viewModel = viewModel)
}
