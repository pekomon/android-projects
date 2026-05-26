package com.pekomon.cryptoapp.domain.market

sealed interface MarketDataResult<out T> {
    data class Success<T>(
        val value: T
    ) : MarketDataResult<T>

    data class PartialSuccess<T>(
        val value: T,
        val missingIds: Set<String>
    ) : MarketDataResult<T>

    data class Failure(
        val error: MarketDataError
    ) : MarketDataResult<Nothing>
}

sealed interface MarketDataError {
    val technicalMessage: String?

    data class Unauthorized(
        override val technicalMessage: String? = null
    ) : MarketDataError

    data class Forbidden(
        override val technicalMessage: String? = null
    ) : MarketDataError

    data class RateLimited(
        override val technicalMessage: String? = null
    ) : MarketDataError

    data class Network(
        override val technicalMessage: String? = null
    ) : MarketDataError

    data class Unknown(
        override val technicalMessage: String? = null
    ) : MarketDataError
}
