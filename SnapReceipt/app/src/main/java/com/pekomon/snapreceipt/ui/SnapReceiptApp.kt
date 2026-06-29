package com.pekomon.snapreceipt.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pekomon.snapreceipt.feature.capture.CaptureScreen
import com.pekomon.snapreceipt.feature.receipts.ReceiptsPlaceholderScreen
import com.pekomon.snapreceipt.feature.settings.SettingsPlaceholderScreen
import com.pekomon.snapreceipt.ui.navigation.SnapReceiptDestination
import com.pekomon.snapreceipt.ui.theme.SnapReceiptTheme

@Composable
fun SnapReceiptApp(modifier: Modifier = Modifier) {
    val destinations = listOf(
        SnapReceiptDestination.Capture,
        SnapReceiptDestination.Receipts,
        SnapReceiptDestination.Settings
    )
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route

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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = innerPadding
                )
            }
            composable(SnapReceiptDestination.Receipts.route) {
                ReceiptsPlaceholderScreen(
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
