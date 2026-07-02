package com.pekomon.snapreceipt.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pekomon.snapreceipt.core.ocr.MlKitReceiptOcrEngine
import com.pekomon.snapreceipt.core.parsing.HeuristicReceiptParser
import com.pekomon.snapreceipt.data.local.SnapReceiptDatabase
import com.pekomon.snapreceipt.data.repository.RoomReceiptRepository
import com.pekomon.snapreceipt.data.storage.LocalReceiptImageStorage
import com.pekomon.snapreceipt.feature.capture.CaptureScreen
import com.pekomon.snapreceipt.feature.capture.CaptureViewModel
import com.pekomon.snapreceipt.feature.detail.ReceiptDetailScreen
import com.pekomon.snapreceipt.feature.detail.ReceiptDetailViewModel
import com.pekomon.snapreceipt.feature.receipts.ReceiptsScreen
import com.pekomon.snapreceipt.feature.receipts.ReceiptsViewModel
import com.pekomon.snapreceipt.feature.review.ReviewScreen
import com.pekomon.snapreceipt.feature.settings.SettingsPlaceholderScreen
import com.pekomon.snapreceipt.ui.navigation.SnapReceiptDestination
import com.pekomon.snapreceipt.ui.theme.SnapReceiptTheme

@Composable
fun SnapReceiptApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val destinations = listOf(
        SnapReceiptDestination.Capture,
        SnapReceiptDestination.Receipts,
        SnapReceiptDestination.Settings
    )
    val ocrEngine = remember(context) { MlKitReceiptOcrEngine(context.applicationContext) }
    val parser = remember { HeuristicReceiptParser() }
    val imageStorage = remember(context) { LocalReceiptImageStorage(context.applicationContext) }
    val database = remember(context) { SnapReceiptDatabase.create(context.applicationContext) }
    val receiptRepository = remember(database, imageStorage) {
        RoomReceiptRepository(
            receiptDao = database.receiptDao(),
            imageStorage = imageStorage
        )
    }
    val captureViewModel: CaptureViewModel = viewModel(
        factory = CaptureViewModel.factory(
            ocrEngine = ocrEngine,
            receiptParser = parser,
            receiptRepository = receiptRepository
        )
    )
    val captureUiState by captureViewModel.uiState.collectAsStateWithLifecycle()
    val receiptsViewModel: ReceiptsViewModel = viewModel(
        factory = ReceiptsViewModel.factory(receiptRepository = receiptRepository)
    )
    val receiptsUiState by receiptsViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(captureUiState.lastSavedReceipt?.id, currentRoute) {
        if (
            currentRoute == SnapReceiptDestination.Review.route &&
            captureUiState.lastSavedReceipt != null &&
            captureUiState.draft == null
        ) {
            navController.popBackStack(SnapReceiptDestination.Capture.route, false)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(text = destination.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SnapReceiptDestination.Capture.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(SnapReceiptDestination.Capture.route) {
                CaptureScreen(
                    uiState = captureUiState,
                    onImageImported = captureViewModel::onImageImported,
                    onClearImportedImage = captureViewModel::clearImportedImage,
                    onReviewDraft = { navController.navigate(SnapReceiptDestination.Review.route) },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
            composable(SnapReceiptDestination.Review.route) {
                ReviewScreen(
                    uiState = captureUiState,
                    onBackToCapture = { navController.popBackStack() },
                    onSaveDraft = captureViewModel::saveReviewedReceipt,
                    onMerchantNameChange = captureViewModel::updateMerchantName,
                    onTransactionDateChange = captureViewModel::updateTransactionDate,
                    onTotalAmountChange = captureViewModel::updateTotalAmount,
                    onCurrencyCodeChange = captureViewModel::updateCurrencyCode,
                    onNotesChange = captureViewModel::updateNotes,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
            composable(SnapReceiptDestination.Receipts.route) {
                ReceiptsScreen(
                    uiState = receiptsUiState,
                    onReceiptSelected = { receiptId ->
                        navController.navigate(SnapReceiptDestination.receiptDetailRoute(receiptId))
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
            composable(
                route = SnapReceiptDestination.receiptDetailPattern,
                arguments = listOf(
                    navArgument(SnapReceiptDestination.receiptIdArgument) {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val receiptId = entry.arguments?.getString(SnapReceiptDestination.receiptIdArgument).orEmpty()
                val detailViewModel: ReceiptDetailViewModel = viewModel(
                    key = "receipt-detail-$receiptId",
                    factory = ReceiptDetailViewModel.factory(
                        receiptId = receiptId,
                        receiptRepository = receiptRepository
                    )
                )
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                ReceiptDetailScreen(
                    uiState = detailUiState,
                    onBack = { navController.popBackStack() },
                    onDeleteReceipt = detailViewModel::deleteReceipt,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
            composable(SnapReceiptDestination.Settings.route) {
                SettingsPlaceholderScreen(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SnapReceiptAppPreview() {
    SnapReceiptTheme {
        SnapReceiptApp()
    }
}
