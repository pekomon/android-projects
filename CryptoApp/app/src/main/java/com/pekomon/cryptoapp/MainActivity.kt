package com.pekomon.cryptoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pekomon.cryptoapp.data.local.PreferencesRepository
import com.pekomon.cryptoapp.ui.CryptoViewModel
import com.pekomon.cryptoapp.ui.theme.CryptoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val preferencesRepository = PreferencesRepository(applicationContext)
        
        setContent {
            CryptoAppTheme {
                val viewModel: CryptoViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            if (!modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
                                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                            }
                            @Suppress("UNCHECKED_CAST")
                            return CryptoViewModel(preferencesRepository) as T
                        }
                    }
                )
                
                CryptoApp(viewModel = viewModel)
            }
        }
    }
}
