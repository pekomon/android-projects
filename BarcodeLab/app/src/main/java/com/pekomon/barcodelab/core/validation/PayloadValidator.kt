package com.pekomon.barcodelab.core.validation

import com.pekomon.barcodelab.domain.model.BarcodePayload
import com.pekomon.barcodelab.domain.model.BarcodeFormat
import com.pekomon.barcodelab.domain.model.PayloadKind
import com.pekomon.barcodelab.domain.model.ValidationResult
import com.pekomon.barcodelab.domain.model.ValidationStatus
import java.net.URI
import java.util.Locale

class PayloadValidator {
    fun validate(payload: BarcodePayload): ValidationResult =
        when (payload.kind) {
            PayloadKind.ProductCode -> validateProductCode(payload)
            PayloadKind.LogisticsCode -> validateLogisticsCode(payload)
            PayloadKind.Url -> validateUrl(payload.rawValue)
            PayloadKind.Email -> validateEmail(payload.rawValue)
            PayloadKind.Phone -> validatePhone(payload.rawValue)
            PayloadKind.Sms -> validateSms(payload.rawValue)
            PayloadKind.Wifi -> validateWifi(payload.rawValue)
            PayloadKind.VCard -> validateVCard(payload.rawValue)
            PayloadKind.Calendar -> validateCalendar(payload.rawValue)
            PayloadKind.Geo -> validateGeo(payload.rawValue)
            PayloadKind.PlainText -> ValidationResult(
                status = ValidationStatus.Warning,
                title = "Unstructured payload",
                detail = "The code is readable, but no known structured format was detected.",
            )
            PayloadKind.UnknownStructured -> ValidationResult(
                status = ValidationStatus.Unsupported,
                title = "Unsupported structured payload",
                detail = "The code looks structured, but Barcode Lab does not validate this format yet.",
            )
        }

    private fun validateProductCode(payload: BarcodePayload): ValidationResult {
        val digits = payload.rawValue.filter(Char::isDigit)
        val supportedLength = digits.length in setOf(8, 12, 13)
        return when {
            payload.sourceFormat == BarcodeFormat.UpcE && digits.length == 8 -> ValidationResult(
                status = ValidationStatus.Warning,
                title = "UPC-E product code",
                detail = "UPC-E was detected. Full checksum validation requires UPC-E expansion rules.",
            )
            supportedLength && hasValidGtinCheckDigit(digits) -> valid(
                title = "Valid product code",
                detail = "The EAN/UPC check digit is structurally valid.",
            )
            supportedLength -> invalid(
                title = "Invalid product code",
                detail = "The barcode was detected, but the EAN/UPC check digit does not match.",
            )
            else -> invalid(
                title = "Invalid product code",
                detail = "Expected an EAN-8, UPC-A, or EAN-13 length numeric product code.",
            )
        }
    }

    private fun validateLogisticsCode(payload: BarcodePayload): ValidationResult =
        if (payload.rawValue.isNotBlank()) {
            ValidationResult(
                status = ValidationStatus.Warning,
                title = "Readable logistics code",
                detail = "The code was decoded. Format-specific GS1 or carrier validation is not enabled yet.",
            )
        } else {
            invalid("Invalid logistics code", "Expected a non-empty logistics barcode payload.")
        }

    private fun validateUrl(value: String): ValidationResult {
        val parsed = runCatching { URI(value.trim()) }.getOrNull()
        val valid = parsed?.scheme in setOf("http", "https") && !parsed?.host.isNullOrBlank()
        return if (valid) {
            valid("Valid URL", "The payload contains a web URL with a supported scheme.")
        } else {
            invalid("Invalid URL", "Expected an http or https URL with a host.")
        }
    }

    private fun validateEmail(value: String): ValidationResult {
        val address = value.removePrefixIgnoreCase("mailto:")
            .substringBefore('?')
            .trim()
        return if (Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(address)) {
            valid("Valid email", "The payload contains a syntactically valid email address.")
        } else {
            invalid("Invalid email", "Expected a usable email address.")
        }
    }

    private fun validatePhone(value: String): ValidationResult {
        val number = value.removePrefixIgnoreCase("tel:").trim()
        val valid = Regex("^\\+?[0-9][0-9 .()\\-]{4,}$").matches(number)
        return if (valid) {
            valid("Valid phone", "The payload contains a plausible phone number.")
        } else {
            invalid("Invalid phone", "Expected a plausible dial string.")
        }
    }

    private fun validateSms(value: String): ValidationResult {
        val withoutScheme = value
            .removePrefixIgnoreCase("sms:")
            .removePrefixIgnoreCase("smsto:")
        val number = withoutScheme.substringBefore(':').substringBefore('?').trim()
        return if (number.length >= 5) {
            valid("Valid SMS", "The payload contains an SMS target.")
        } else {
            invalid("Invalid SMS", "Expected an SMS payload with a target number.")
        }
    }

    private fun validateWifi(value: String): ValidationResult {
        val ssid = Regex("""(?:^|;)S:((?:\\.|[^;])*)""")
            .find(value)
            ?.groupValues
            ?.get(1)
            ?.replace("\\;", ";")
            ?.trim()
        return if (!ssid.isNullOrBlank()) {
            valid("Valid Wi-Fi", "The payload contains a non-empty network SSID.")
        } else {
            invalid("Invalid Wi-Fi", "Expected a Wi-Fi QR payload with an SSID field.")
        }
    }

    private fun validateVCard(value: String): ValidationResult {
        val lower = value.lowercase(Locale.US)
        val hasName = lower.lineSequence().any { it.startsWith("fn:") || it.startsWith("n:") }
        val hasOrg = lower.lineSequence().any { it.startsWith("org:") } || lower.contains(";org:")
        return if (hasName || hasOrg) {
            valid("Valid contact", "The payload contains contact identity fields.")
        } else {
            invalid("Invalid contact", "Expected a vCard or MECARD payload with a name or organization.")
        }
    }

    private fun validateCalendar(value: String): ValidationResult {
        val lower = value.lowercase(Locale.US)
        val hasStart = lower.lineSequence().any { it.startsWith("dtstart") }
        val hasSummary = lower.lineSequence().any { it.startsWith("summary:") }
        return if (hasStart && hasSummary) {
            valid("Valid calendar event", "The payload contains event start and summary fields.")
        } else {
            invalid("Invalid calendar event", "Expected a VEVENT payload with DTSTART and SUMMARY.")
        }
    }

    private fun validateGeo(value: String): ValidationResult {
        val coordinates = value.removePrefixIgnoreCase("geo:")
            .substringBefore('?')
            .split(',')
            .map { it.toDoubleOrNull() }
        val lat = coordinates.getOrNull(0)
        val lon = coordinates.getOrNull(1)
        val valid = lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0
        return if (valid) {
            valid("Valid geo coordinate", "The payload contains latitude and longitude in valid ranges.")
        } else {
            invalid("Invalid geo coordinate", "Expected geo:latitude,longitude with valid coordinate ranges.")
        }
    }

    private fun valid(title: String, detail: String) =
        ValidationResult(ValidationStatus.Valid, title, detail)

    private fun invalid(title: String, detail: String) =
        ValidationResult(ValidationStatus.Invalid, title, detail)

    private fun hasValidGtinCheckDigit(digits: String): Boolean {
        val expected = digits.last().digitToIntOrNull() ?: return false
        val sum = digits.dropLast(1)
            .reversed()
            .mapIndexed { index, char ->
                val digit = char.digitToIntOrNull() ?: return false
                if (index % 2 == 0) digit * 3 else digit
            }
            .sum()
        val actual = (10 - (sum % 10)) % 10
        return actual == expected
    }

    private fun String.removePrefixIgnoreCase(prefix: String): String =
        if (startsWith(prefix, ignoreCase = true)) drop(prefix.length) else this
}
