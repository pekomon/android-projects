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
        assertEquals("webp", LocalReceiptImageStorage.extensionFromMimeType("image/webp"))
    }
}
