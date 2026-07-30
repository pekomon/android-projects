package com.pekomon.lockbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pekomon.lockbox.app.LockBoxApp
import com.pekomon.lockbox.ui.theme.LockBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LockBoxTheme {
                LockBoxApp()
            }
        }
    }
}
