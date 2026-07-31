package com.pekomon.lockbox.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ProcessLifecycleRelockObserver(
    private val lockSession: LockSession,
) : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        lockSession.lock()
    }
}
