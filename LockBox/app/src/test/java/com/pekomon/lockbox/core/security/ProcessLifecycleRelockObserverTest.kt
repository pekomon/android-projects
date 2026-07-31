package com.pekomon.lockbox.core.security

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessLifecycleRelockObserverTest {
    @Test
    fun processStopLocksUnlockedSession() {
        val owner = TestLifecycleOwner()
        val lockSession = InMemoryLockSession()
        val observer = ProcessLifecycleRelockObserver(lockSession)

        lockSession.unlock()
        assertTrue(lockSession.isUnlocked.value)

        observer.onStop(owner)

        assertFalse(lockSession.isUnlocked.value)
    }

    @Test
    fun processStopKeepsLockedSessionLocked() {
        val owner = TestLifecycleOwner()
        val lockSession = InMemoryLockSession()
        val observer = ProcessLifecycleRelockObserver(lockSession)

        observer.onStop(owner)

        assertFalse(lockSession.isUnlocked.value)
    }

    private class TestLifecycleOwner : LifecycleOwner {
        override val lifecycle: Lifecycle
            get() = throw UnsupportedOperationException("Not needed for direct observer tests")
    }
}
