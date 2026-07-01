package com.pekomon.snapreceipt.data.storage

import android.content.Context
import android.net.Uri
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.storage.ReceiptImageStorage
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalReceiptImageStorage(
    private val context: Context
) : ReceiptImageStorage {
    override suspend fun persistImportedImage(image: ReceiptImage): ReceiptImage = withContext(Dispatchers.IO) {
        val sourceUri = Uri.parse(image.localPath)
        val receiptsDirectory = File(context.filesDir, RECEIPTS_DIRECTORY_NAME).apply {
            mkdirs()
        }
        val targetFile = File(
            receiptsDirectory,
            buildStoredFileName(image.mimeType)
        )

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: error("Unable to open the selected receipt image.")

        image.copy(localPath = Uri.fromFile(targetFile).toString())
    }

    override suspend fun deleteStoredImage(image: ReceiptImage) = withContext(Dispatchers.IO) {
        val storedUri = Uri.parse(image.localPath)
        if (storedUri.scheme == FILE_SCHEME) {
            File(requireNotNull(storedUri.path)).delete()
        }
    }

    companion object {
        internal const val RECEIPTS_DIRECTORY_NAME = "receipts"
        private const val FILE_SCHEME = "file"

        internal fun buildStoredFileName(mimeType: String?): String {
            val extension = extensionFromMimeType(mimeType)
            return "receipt-${UUID.randomUUID()}.$extension"
        }

        internal fun extensionFromMimeType(mimeType: String?): String {
            return when (mimeType?.lowercase()) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
        }
    }
}
