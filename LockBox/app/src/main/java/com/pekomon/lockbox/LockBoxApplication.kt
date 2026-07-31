package com.pekomon.lockbox

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.pekomon.lockbox.app.LockBoxAppContainer
import com.pekomon.lockbox.core.security.ProcessLifecycleRelockObserver

class LockBoxApplication : Application() {
    lateinit var appContainer: LockBoxAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = LockBoxAppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            ProcessLifecycleRelockObserver(appContainer.lockSession),
        )
    }
}
