package com.pekomon.snapreceipt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pekomon.snapreceipt.ui.SnapReceiptApp
import com.pekomon.snapreceipt.ui.theme.SnapReceiptTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnapReceiptTheme {
                SnapReceiptApp()
            }
        }
    }
}
