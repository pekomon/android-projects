package com.pekomon.snapreceipt.data.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalReceiptImageStorageTest {
    @Test
    fun extensionFromMimeType_defaultsToJpg() {
        assertEquals("jpg", LocalReceiptImageStorage.extensionFromMimeType(null))
        assertEquals("jpg", LocalReceiptImageStorage.extensionFromMimeType("image/jpeg"))
    }

    @Test
    fun extensionFromMimeType_usesSupportedAlternatives() {
        assertEquals("png", LocalReceiptImageStorage.extensionFromMimeType("image/png"))
        assertEquals("jpg", LocalReceiptImageStorage.extensionFromMimeType("image/webp"))
    }

    @Test
    fun persistedFormatFor_keepsPngAndNormalizesOthersToJpeg() {
        assertEquals(PersistedFormat.PNG, LocalReceiptImageStorage.persistedFormatFor("image/png"))
        assertEquals(PersistedFormat.JPEG, LocalReceiptImageStorage.persistedFormatFor("image/webp"))
        assertEquals(PersistedFormat.JPEG, LocalReceiptImageStorage.persistedFormatFor("image/jpeg"))
    }
}
