package com.pekomon.snapreceipt.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SnapReceiptDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Capture : SnapReceiptDestination(
        route = "capture",
        label = "Capture",
        icon = Icons.Outlined.PhotoCamera
    )

    data object Receipts : SnapReceiptDestination(
        route = "receipts",
        label = "Receipts",
        icon = Icons.AutoMirrored.Outlined.ReceiptLong
    )

    data object Settings : SnapReceiptDestination(
        route = "settings",
        label = "Settings",
        icon = Icons.Outlined.Settings
    )

    data object Review : SnapReceiptDestination(
        route = "review",
        label = "Review",
        icon = Icons.Outlined.EditNote
    )
}
