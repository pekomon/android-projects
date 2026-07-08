package com.pekomon.snapreceipt.core.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import com.pekomon.snapreceipt.data.local.ReceiptDao
import com.pekomon.snapreceipt.data.local.toEntity
import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.Receipt
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptDraft
import com.pekomon.snapreceipt.domain.model.ReceiptImage
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.model.ReceiptSource
import com.pekomon.snapreceipt.domain.model.SnapReceiptSettings
import com.pekomon.snapreceipt.domain.repository.SnapReceiptSettingsRepository
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SnapReceiptDemoDataService(
    private val context: Context,
    private val receiptDao: ReceiptDao,
    private val settingsRepository: SnapReceiptSettingsRepository
) {
    suspend fun seedDeterministicDemoData() = withContext(Dispatchers.IO) {
        val demoDirectory = demoDirectory().apply {
            deleteRecursively()
            mkdirs()
        }

        receiptDao.deleteAllReceipts()
        settingsRepository.updateSettings(DEMO_SETTINGS)

        demoReceipts().forEach { spec ->
            val imageFile = renderReceiptImage(
                fileName = "${spec.id}.png",
                merchantName = spec.merchantName,
                transactionDate = spec.transactionDate,
                totalAmount = spec.totalAmount,
                currency = spec.currency,
                accentLabel = spec.accentLabel
            )
            receiptDao.upsertReceipt(
                Receipt(
                    id = spec.id,
                    merchantName = spec.merchantName,
                    transactionDate = spec.transactionDate,
                    totalAmount = spec.totalAmount,
                    currency = spec.currency,
                    image = ReceiptImage(
                        localPath = Uri.fromFile(imageFile).toString(),
                        source = ReceiptSource.FILE_IMPORT,
                        mimeType = "image/png",
                        widthPx = 900,
                        heightPx = 1200
                    ),
                    rawOcrText = spec.rawOcrText,
                    notes = spec.notes,
                    createdAt = spec.createdAt,
                    updatedAt = spec.updatedAt
                ).toEntity()
            )
        }
    }

    suspend fun createDeterministicReviewDraft(): ReceiptDraft = withContext(Dispatchers.IO) {
        settingsRepository.updateSettings(DEMO_SETTINGS)
        val spec = demoDraftSpec()
        val imageFile = renderReceiptImage(
            fileName = "${spec.id}.png",
            merchantName = spec.merchantName,
            transactionDate = spec.transactionDate,
            totalAmount = spec.totalAmount,
            currency = spec.currency,
            accentLabel = spec.accentLabel
        )
        ReceiptDraft(
            image = ReceiptImage(
                localPath = Uri.fromFile(imageFile).toString(),
                source = ReceiptSource.CAMERA,
                mimeType = "image/png",
                widthPx = 900,
                heightPx = 1200
            ),
            ocrResult = ReceiptOcrResult(
                fullText = spec.rawOcrText,
                lineBlocks = spec.rawOcrText.lines()
            ),
            parsedFields = ParsedReceiptFields(
                merchantName = spec.merchantName,
                transactionDate = spec.transactionDate,
                totalAmount = spec.totalAmount,
                currency = spec.currency
            ),
            notes = spec.notes
        )
    }

    private fun renderReceiptImage(
        fileName: String,
        merchantName: String,
        transactionDate: LocalDate,
        totalAmount: BigDecimal,
        currency: ReceiptCurrency,
        accentLabel: String
    ): File {
        val file = File(demoDirectory(), fileName)
        val bitmap = Bitmap.createBitmap(900, 1200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF4EDDE.toInt() }
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF17211D.toInt()
            textSize = 42f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF43514A.toInt()
            textSize = 28f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD9CFBE.toInt()
            strokeWidth = 3f
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF245E4F.toInt() }
        val copperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFC88439.toInt() }

        canvas.drawRect(0f, 0f, 900f, 1200f, backgroundPaint)
        canvas.drawRoundRect(70f, 60f, 830f, 1140f, 36f, 36f, cardPaint)
        canvas.drawRoundRect(70f, 60f, 830f, 170f, 36f, 36f, accentPaint)
        canvas.drawRoundRect(650f, 92f, 790f, 138f, 20f, 20f, copperPaint)

        canvas.drawText("SNAPRECEIPT DEMO", 110f, 128f, Paint(inkPaint).apply {
            color = 0xFFF7F1E3.toInt()
            textSize = 34f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        })
        canvas.drawText(accentLabel, 686f, 124f, Paint(bodyPaint).apply {
            color = 0xFF17211D.toInt()
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        })

        canvas.drawText(merchantName, 110f, 248f, inkPaint)
        canvas.drawText("Transaction date", 110f, 320f, bodyPaint)
        canvas.drawText(transactionDate.toString(), 110f, 362f, inkPaint.apply {
            textSize = 36f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        })
        inkPaint.textSize = 42f
        inkPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)

        var y = 470f
        repeat(6) { index ->
            canvas.drawLine(110f, y + 18f, 790f, y + 18f, linePaint)
            canvas.drawText("Line item ${index + 1}", 110f, y, bodyPaint)
            canvas.drawText(
                "${currency.code} ${(index + 2) * 3}.50",
                610f,
                y,
                Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            )
            y += 88f
        }

        canvas.drawLine(110f, 955f, 790f, 955f, accentPaint.apply { strokeWidth = 5f })
        canvas.drawText("TOTAL", 110f, 1020f, Paint(bodyPaint).apply {
            textSize = 30f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        })
        canvas.drawText(
            "${currency.code} ${totalAmount.setScale(2)}",
            790f,
            1020f,
            Paint(inkPaint).apply {
                textAlign = Paint.Align.RIGHT
                textSize = 48f
            }
        )
        canvas.drawText(
            "Generated deterministic screenshot data",
            110f,
            1090f,
            Paint(bodyPaint).apply { textSize = 24f }
        )

        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()
        return file
    }

    private fun demoDirectory(): File = File(context.filesDir, "demo-receipts")

    private fun demoReceipts(): List<DemoReceiptSpec> = listOf(
        DemoReceiptSpec(
            id = "demo-northwind-cafe",
            merchantName = "Northwind Cafe",
            transactionDate = LocalDate.of(2026, 7, 7),
            totalAmount = BigDecimal("18.40"),
            currency = ReceiptCurrency.EUR,
            notes = "Team lunch after roadmap review",
            accentLabel = "CAFE",
            rawOcrText = "Northwind Cafe\n2026-07-07\nLatte 4.20\nLunch combo 14.20\nTOTAL EUR 18.40",
            createdAt = Instant.parse("2026-07-07T08:30:00Z"),
            updatedAt = Instant.parse("2026-07-07T08:30:00Z")
        ),
        DemoReceiptSpec(
            id = "demo-harbor-market",
            merchantName = "Harbor Market",
            transactionDate = LocalDate.of(2026, 7, 6),
            totalAmount = BigDecimal("42.90"),
            currency = ReceiptCurrency.USD,
            notes = "Props and styling materials",
            accentLabel = "MARKET",
            rawOcrText = "Harbor Market\n2026-07-06\nProps 32.90\nStationery 10.00\nTOTAL USD 42.90",
            createdAt = Instant.parse("2026-07-06T13:15:00Z"),
            updatedAt = Instant.parse("2026-07-06T13:15:00Z")
        ),
        DemoReceiptSpec(
            id = "demo-aurora-transit",
            merchantName = "Aurora Transit",
            transactionDate = LocalDate.of(2026, 7, 5),
            totalAmount = BigDecimal("9.60"),
            currency = ReceiptCurrency.GBP,
            notes = "Airport line ticket",
            accentLabel = "TRANSIT",
            rawOcrText = "Aurora Transit\n2026-07-05\nAirport line 9.60\nTOTAL GBP 9.60",
            createdAt = Instant.parse("2026-07-05T06:45:00Z"),
            updatedAt = Instant.parse("2026-07-05T06:45:00Z")
        )
    )

    private fun demoDraftSpec(): DemoReceiptSpec = DemoReceiptSpec(
        id = "demo-review-draft",
        merchantName = "Fjord Supply",
        transactionDate = LocalDate.of(2026, 7, 4),
        totalAmount = BigDecimal("63.25"),
        currency = ReceiptCurrency.SEK,
        notes = "Demo draft ready for screenshot review",
        accentLabel = "DRAFT",
        rawOcrText = "Fjord Supply\n2026-07-04\nStudio paper 45.00\nMarkers 18.25\nTOTAL SEK 63.25",
        createdAt = Instant.parse("2026-07-04T10:10:00Z"),
        updatedAt = Instant.parse("2026-07-04T10:10:00Z")
    )

    companion object {
        val DEMO_SETTINGS = SnapReceiptSettings(
            defaultCurrency = ReceiptCurrency.SEK,
            imageCompressionQuality = 78
        )
    }
}

private data class DemoReceiptSpec(
    val id: String,
    val merchantName: String,
    val transactionDate: LocalDate,
    val totalAmount: BigDecimal,
    val currency: ReceiptCurrency,
    val notes: String,
    val accentLabel: String,
    val rawOcrText: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
