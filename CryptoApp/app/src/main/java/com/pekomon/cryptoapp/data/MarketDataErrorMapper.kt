package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.market.MarketDataError
import retrofit2.HttpException
import java.io.IOException

object MarketDataErrorMapper {
    fun fromException(error: Exception): MarketDataError {
        val summary = error.debugSummary()
        return when {
            error is HttpException -> fromHttpStatus(error.code(), summary)
            error is IOException -> MarketDataError.Network(summary)
            else -> MarketDataError.Unknown(summary)
        }
    }

    fun fromHttpStatus(
        statusCode: Int,
        technicalMessage: String? = null
    ): MarketDataError = when (statusCode) {
        401 -> MarketDataError.Unauthorized(technicalMessage)
        403 -> MarketDataError.Forbidden(technicalMessage)
        429 -> MarketDataError.RateLimited(technicalMessage)
        else -> MarketDataError.Unknown(technicalMessage)
    }
}

fun Exception.debugSummary(): String {
    return if (this is HttpException) {
        val errorBody = response()?.errorBody()?.string()?.take(500)
        "httpCode=${code()} message=${message()} errorBody=$errorBody"
    } else {
        "${this::class.java.simpleName}: ${message}"
    }
}
