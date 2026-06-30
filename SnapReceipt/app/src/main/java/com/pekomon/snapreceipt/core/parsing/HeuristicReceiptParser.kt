package com.pekomon.snapreceipt.core.parsing

import com.pekomon.snapreceipt.domain.model.ParsedReceiptFields
import com.pekomon.snapreceipt.domain.model.ReceiptCurrency
import com.pekomon.snapreceipt.domain.model.ReceiptOcrResult
import com.pekomon.snapreceipt.domain.parsing.ReceiptParser
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class HeuristicReceiptParser : ReceiptParser {
    override fun parse(
        ocrResult: ReceiptOcrResult,
        fallbackCurrency: ReceiptCurrency?
    ): ParsedReceiptFields {
        val lines = ocrResult.lineBlocks
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return ParsedReceiptFields(
            merchantName = parseMerchantName(lines),
            transactionDate = parseDate(lines),
            totalAmount = parseTotalAmount(lines),
            currency = parseCurrency(lines) ?: fallbackCurrency
        )
    }

    private fun parseMerchantName(lines: List<String>): String? {
        return lines.firstOrNull { line ->
            val normalized = line.uppercase(Locale.US)
            normalized.any { it.isLetter() } &&
                !normalized.contains("TOTAL") &&
                !normalized.contains("VAT") &&
                !normalized.contains("SUBTOTAL") &&
                !normalized.contains("AMOUNT") &&
                !normalized.contains("CHANGE") &&
                !normalized.contains("EUR") &&
                !normalized.contains("USD")
        }
    }

    private fun parseDate(lines: List<String>): LocalDate? {
        val candidates = lines.flatMap { line ->
            DATE_REGEX.findAll(line).map { it.value }.toList()
        }

        return candidates.firstNotNullOfOrNull { candidate ->
            parseDateCandidate(candidate)
        }
    }

    private fun parseDateCandidate(candidate: String): LocalDate? {
        val normalized = candidate.replace('.', '/').replace('-', '/')
        val formatters = listOf(
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d/M/uu")
        )

        return formatters.firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(normalized, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }

    private fun parseTotalAmount(lines: List<String>): BigDecimal? {
        val totalLine = lines.firstOrNull { line ->
            val normalized = line.uppercase(Locale.US)
            TOTAL_LINE_REGEX.containsMatchIn(normalized) || normalized.contains("AMOUNT DUE")
        }

        if (totalLine != null) {
            parseAmounts(totalLine).maxOrNull()?.let { return it }
        }

        return lines.flatMap(::parseAmounts).maxOrNull()
    }

    private fun parseAmounts(line: String): List<BigDecimal> {
        return AMOUNT_REGEX.findAll(line).mapNotNull { match ->
            match.value
                .replace(',', '.')
                .takeIf { it.count { char -> char == '.' } <= 1 }
                ?.toBigDecimalOrNull()
        }.toList()
    }

    private fun parseCurrency(lines: List<String>): ReceiptCurrency? {
        return lines.firstNotNullOfOrNull { line ->
            ReceiptCurrency.entries.firstOrNull { currency ->
                line.uppercase(Locale.US).contains(currency.code)
            }
        }
    }

    private companion object {
        val DATE_REGEX = Regex("""\b\d{1,2}[./-]\d{1,2}[./-]\d{2,4}\b""")
        val AMOUNT_REGEX = Regex("""\b\d+[.,]\d{2}\b""")
        val TOTAL_LINE_REGEX = Regex("""\bTOTAL\b""")
    }
}
