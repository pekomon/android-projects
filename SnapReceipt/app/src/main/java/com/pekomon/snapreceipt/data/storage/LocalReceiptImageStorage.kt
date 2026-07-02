package com.pekomon.snapreceipt.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    override suspend fun persistImportedImage(
        image: ReceiptImage,
        compressionQuality: Int
    ): ReceiptImage = withContext(Dispatchers.IO) {
        val sourceUri = Uri.parse(image.localPath)
        val receiptsDirectory = File(context.filesDir, RECEIPTS_DIRECTORY_NAME).apply {
            mkdirs()
        }
        val persistedFormat = persistedFormatFor(image.mimeType)
        val targetFile = File(
            receiptsDirectory,
            buildStoredFileName(persistedFormat.fileExtension)
        )

        when (persistedFormat) {
            PersistedFormat.PNG -> {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: error("Unable to open the selected receipt image.")
            }

            PersistedFormat.JPEG -> {
                val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                } ?: error("Unable to decode the selected receipt image.")

                targetFile.outputStream().use { outputStream ->
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        compressionQuality.coerceIn(40, 100),
                        outputStream
                    )
                }
            }
        }

        image.copy(
            localPath = Uri.fromFile(targetFile).toString(),
            mimeType = persistedFormat.mimeType
        )
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

        internal fun buildStoredFileName(fileExtension: String): String {
            return "receipt-${UUID.randomUUID()}.$fileExtension"
        }

        internal fun extensionFromMimeType(mimeType: String?): String {
            return when (mimeType?.lowercase()) {
                "image/png" -> "png"
                else -> "jpg"
            }
        }

        internal fun persistedFormatFor(mimeType: String?): PersistedFormat {
            return if (mimeType?.lowercase() == "image/png") {
                PersistedFormat.PNG
            } else {
                PersistedFormat.JPEG
            }
        }
    }
}

internal enum class PersistedFormat(
    val mimeType: String,
    val fileExtension: String
) {
    PNG(
        mimeType = "image/png",
        fileExtension = "png"
    ),
    JPEG(
        mimeType = "image/jpeg",
        fileExtension = "jpg"
    )
}
