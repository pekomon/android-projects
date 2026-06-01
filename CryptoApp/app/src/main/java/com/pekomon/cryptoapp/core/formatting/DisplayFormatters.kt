package com.pekomon.cryptoapp.core.formatting

import com.pekomon.cryptoapp.data.Currency
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DisplayFormatters {
    const val UNAVAILABLE = "Unavailable"

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun currencyAmount(
        value: Double,
        currency: Currency
    ): String = "${currency.symbol}${formatDecimal(value, decimals = 2)}"

    fun signedCurrencyAmount(
        value: Double,
        currency: Currency
    ): String {
        val sign = if (value >= 0.0) "+" else ""
        return "$sign${currencyAmount(value, currency)}"
    }

    fun percentage(value: Double): String = "${formatDecimal(value, decimals = 2)}%"

    fun signedPercentage(value: Double): String {
        val sign = if (value >= 0.0) "+" else ""
        return "$sign${percentage(value)}"
    }

    fun cryptoAmount(value: Double): String = when {
        value == 0.0 -> "0"
        value < 0.000001 -> formatDecimal(value, decimals = 8)
        value < 1.0 -> formatDecimal(value, decimals = 6).trimTrailingZeros()
        else -> formatDecimal(value, decimals = 4).trimTrailingZeros()
    }

    fun updateTime(dateTime: LocalDateTime): String {
        return "Last updated ${dateTime.format(timeFormatter)}"
    }

    fun dateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }

    private fun formatDecimal(
        value: Double,
        decimals: Int
    ): String = String.format(Locale.US, "%.${decimals}f", value)

    private fun String.trimTrailingZeros(): String {
        return trimEnd('0').trimEnd('.')
    }
}
