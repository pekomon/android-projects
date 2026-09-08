package com.pekomon.barcodelab.core.validation

import com.pekomon.barcodelab.domain.model.BarcodePayload
import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.PayloadKind
import com.pekomon.barcodelab.domain.model.isLogisticsCode
import com.pekomon.barcodelab.domain.model.isProductCode
import java.util.Locale

class PayloadClassifier {
    fun classify(
        rawValue: String,
        displayValue: String = rawValue,
        sourceFormat: BarcodeFormat = BarcodeFormat.Unknown,
    ): BarcodePayload {
        val trimmed = rawValue.trim()
        val lower = trimmed.lowercase(Locale.US)
        val kind = when {
            sourceFormat.isProductCode -> PayloadKind.ProductCode
            sourceFormat.isLogisticsCode -> PayloadKind.LogisticsCode
            lower.startsWith("http://") || lower.startsWith("https://") -> PayloadKind.Url
            lower.startsWith("mailto:") || looksLikeEmail(trimmed) -> PayloadKind.Email
            lower.startsWith("tel:") -> PayloadKind.Phone
            lower.startsWith("sms:") || lower.startsWith("smsto:") -> PayloadKind.Sms
            lower.startsWith("wifi:") -> PayloadKind.Wifi
            lower.startsWith("begin:vcard") || lower.startsWith("mecard:") -> PayloadKind.VCard
            lower.startsWith("begin:vevent") -> PayloadKind.Calendar
            lower.startsWith("geo:") -> PayloadKind.Geo
            looksStructured(trimmed) -> PayloadKind.UnknownStructured
            else -> PayloadKind.PlainText
        }
        return BarcodePayload(
            rawValue = rawValue,
            displayValue = displayValue.ifBlank { rawValue },
            kind = kind,
            sourceFormat = sourceFormat,
        )
    }

    private fun looksLikeEmail(value: String): Boolean =
        Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(value)

    private fun looksStructured(value: String): Boolean =
        value.contains(':') || value.contains(';') || value.contains('|')
}
