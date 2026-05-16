package com.pekomon.cryptoapp.core.formatting

import com.pekomon.cryptoapp.data.Currency
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DisplayFormatters {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun currencyAmount(
        value: Double,
        currency: Currency
    ): String = "${currency.symbol}${"%.2f".format(value)}"

    fun signedCurrencyAmount(
        value: Double,
        currency: Currency
    ): String {
        val sign = if (value >= 0.0) "+" else ""
        return "$sign${currencyAmount(value, currency)}"
    }

    fun percentage(value: Double): String {
        val sign = if (value >= 0.0) "+" else ""
        return "$sign${"%.2f".format(value)}%"
    }

    fun updateTime(dateTime: LocalDateTime): String {
        return "Last updated ${dateTime.format(timeFormatter)}"
    }

    fun dateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
}
