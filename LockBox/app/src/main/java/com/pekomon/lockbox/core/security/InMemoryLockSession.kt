package com.pekomon.lockbox.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class InMemoryLockSession : LockSession {
    private val mutableIsUnlocked = MutableStateFlow(false)

    override val isUnlocked: StateFlow<Boolean> = mutableIsUnlocked.asStateFlow()

    override open fun unlock() {
        mutableIsUnlocked.value = true
    }

    override open fun lock() {
        mutableIsUnlocked.value = false
    }
}
