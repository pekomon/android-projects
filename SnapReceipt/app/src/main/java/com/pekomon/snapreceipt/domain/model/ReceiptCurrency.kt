package com.pekomon.snapreceipt.domain.model

enum class ReceiptCurrency(val code: String) {
    EUR("EUR"),
    USD("USD"),
    GBP("GBP"),
    SEK("SEK"),
    NOK("NOK"),
    DKK("DKK");

    companion object {
        fun fromCode(code: String?): ReceiptCurrency? {
            if (code == null) {
                return null
            }
            return entries.firstOrNull { it.code == code.trim().uppercase() }
        }
    }
}
