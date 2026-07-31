package com.pekomon.lockbox

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.pekomon.lockbox.core.security.AndroidBiometricAuthenticator
import com.pekomon.lockbox.app.LockBoxAppContainer
import com.pekomon.lockbox.app.LockBoxApp
import com.pekomon.lockbox.ui.theme.LockBoxTheme

class MainActivity : FragmentActivity() {
    private val appContainer by lazy {
        (application as LockBoxApplication).appContainer
    }
    private val biometricAuthenticator by lazy {
        AndroidBiometricAuthenticator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LockBoxTheme {
                LockBoxApp(
                    appContainer = appContainer,
                    biometricAuthenticator = biometricAuthenticator,
                )
            }
        }
    }
}
