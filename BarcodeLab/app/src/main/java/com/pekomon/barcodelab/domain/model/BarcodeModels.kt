package com.pekomon.barcodelab.domain.model

data class DetectedBarcode(
    val format: BarcodeFormat,
    val rawValue: String,
    val displayValue: String = rawValue,
    val bounds: BarcodeBounds? = null,
)

data class BarcodeBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

enum class BarcodeFormat(val label: String) {
    QrCode("QR Code"),
    Pdf417("PDF417"),
    Aztec("Aztec"),
    DataMatrix("Data Matrix"),
    Unknown("Unknown"),
}

data class BarcodePayload(
    val rawValue: String,
    val displayValue: String,
    val kind: PayloadKind,
)

enum class PayloadKind(val label: String) {
    Url("URL"),
    Email("Email"),
    Phone("Phone"),
    Sms("SMS"),
    Wifi("Wi-Fi"),
    VCard("Contact"),
    Calendar("Calendar"),
    Geo("Geo"),
    PlainText("Plain text"),
    UnknownStructured("Unknown structured"),
}

data class ValidationResult(
    val status: ValidationStatus,
    val title: String,
    val detail: String,
)

enum class ValidationStatus {
    Valid,
    Warning,
    Invalid,
    Unsupported,
}

data class ScanResult(
    val detectedBarcode: DetectedBarcode,
    val payload: BarcodePayload,
    val validation: ValidationResult,
    val detectedAtMillis: Long,
)
