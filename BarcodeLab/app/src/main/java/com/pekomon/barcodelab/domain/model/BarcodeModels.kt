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
    Code128("Code 128"),
    Code39("Code 39"),
    Code93("Code 93"),
    Codabar("Codabar"),
    QrCode("QR Code"),
    Ean13("EAN-13"),
    Ean8("EAN-8"),
    Itf("ITF"),
    UpcA("UPC-A"),
    UpcE("UPC-E"),
    Pdf417("PDF417"),
    Aztec("Aztec"),
    DataMatrix("Data Matrix"),
    Unknown("Unknown"),
}

private val TwoDimensionalFormats = setOf(
    BarcodeFormat.QrCode,
    BarcodeFormat.Pdf417,
    BarcodeFormat.Aztec,
    BarcodeFormat.DataMatrix,
)

private val ProductFormats = setOf(
    BarcodeFormat.Ean13,
    BarcodeFormat.Ean8,
    BarcodeFormat.UpcA,
    BarcodeFormat.UpcE,
)

private val LogisticsFormats = setOf(
    BarcodeFormat.Code128,
    BarcodeFormat.Code39,
    BarcodeFormat.Code93,
    BarcodeFormat.Codabar,
    BarcodeFormat.Itf,
)

enum class ScannerMode(
    val label: String,
    val helperText: String,
    val formats: Set<BarcodeFormat>,
) {
    TwoDimensional(
        label = "2D Codes",
        helperText = "QR, PDF417, Aztec, Data Matrix",
        formats = TwoDimensionalFormats,
    ),
    Product(
        label = "Product",
        helperText = "EAN and UPC retail codes",
        formats = ProductFormats,
    ),
    Logistics(
        label = "Logistics",
        helperText = "Code 128, Code 39, Code 93, Codabar, ITF",
        formats = LogisticsFormats,
    ),
    AllFormats(
        label = "All",
        helperText = "All supported 1D and 2D formats",
        formats = TwoDimensionalFormats + ProductFormats + LogisticsFormats,
    ),
}

val BarcodeFormat.isProductCode: Boolean
    get() = this in ScannerMode.Product.formats

val BarcodeFormat.isLogisticsCode: Boolean
    get() = this in ScannerMode.Logistics.formats

data class BarcodePayload(
    val rawValue: String,
    val displayValue: String,
    val kind: PayloadKind,
    val sourceFormat: BarcodeFormat,
)

enum class PayloadKind(val label: String) {
    ProductCode("Product code"),
    LogisticsCode("Logistics code"),
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
