package com.pekomon.lockbox.core.security

import kotlinx.coroutines.flow.StateFlow

interface LockSession {
    val isUnlocked: StateFlow<Boolean>

    fun unlock()

    fun lock()
}
