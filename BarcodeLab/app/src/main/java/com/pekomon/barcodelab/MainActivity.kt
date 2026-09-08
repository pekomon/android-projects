package com.pekomon.barcodelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pekomon.barcodelab.app.BarcodeLabApp
import com.pekomon.barcodelab.ui.theme.BarcodeLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarcodeLabTheme {
                BarcodeLabApp()
            }
        }
    }
}
