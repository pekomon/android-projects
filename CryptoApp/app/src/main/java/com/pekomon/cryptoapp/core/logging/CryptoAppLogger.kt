package com.pekomon.cryptoapp.core.logging

import android.util.Log
import com.pekomon.cryptoapp.BuildConfig

object CryptoAppLogger {
    fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            runCatching { Log.d(tag, message) }
        }
    }

    fun warning(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            runCatching { Log.w(tag, message) }
        }
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable == null) {
                runCatching { Log.e(tag, message) }
            } else {
                runCatching { Log.e(tag, message, throwable) }
            }
        }
    }
}
