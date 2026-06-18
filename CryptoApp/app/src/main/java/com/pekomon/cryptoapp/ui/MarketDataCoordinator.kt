package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.core.logging.CryptoAppLogger
import com.pekomon.cryptoapp.domain.market.MarketDataError
import com.pekomon.cryptoapp.domain.market.MarketDataResult
import com.pekomon.cryptoapp.domain.market.MarketPriceMapper
import com.pekomon.cryptoapp.domain.model.MarketPrice
import com.pekomon.cryptoapp.domain.repository.MarketRepository
import java.time.LocalDateTime

class MarketDataCoordinator(
    private val repository: MarketRepository,
    private val tag: String = "CryptoAppNetwork"
) {
    suspend fun fetchPrices(
        cryptoIds: List<String>,
        currencyCode: String,
        currentPrices: Map<String, MarketPrice>,
        lastUpdated: LocalDateTime?
    ): MarketPriceLoadResult {
        CryptoAppLogger.debug(
            tag,
            "fetchPrices start ids=${cryptoIds.distinct().joinToString(",")} currency=$currencyCode"
        )

        return try {
            when (val result = repository.getCryptoPricesResult(cryptoIds, currencyCode)) {
                is MarketDataResult.Success -> {
                    val prices = MarketPriceMapper.mapPrices(result.value)
                    CryptoAppLogger.debug(tag, "fetchPrices success count=${prices.size}")
                    val updatedAt = LocalDateTime.now()
                    MarketPriceLoadResult(
                        prices = prices,
                        lastUpdated = updatedAt,
                        marketLoadState = MarketLoadState.Content(lastUpdated = updatedAt),
                        errorMessage = null
                    )
                }
                is MarketDataResult.PartialSuccess -> {
                    val fetchedPrices = MarketPriceMapper.mapPrices(result.value)
                    val prices = currentPrices.toMutableMap().apply {
                        putAll(fetchedPrices)
                    }
                    CryptoAppLogger.warning(
                        tag,
                        "fetchPrices partial success count=${fetchedPrices.size} missingIds=${result.missingIds.joinToString(",")}"
                    )
                    val updatedAt = LocalDateTime.now()
                    MarketPriceLoadResult(
                        prices = prices,
                        lastUpdated = updatedAt,
                        marketLoadState = MarketLoadState.Content(
                            lastUpdated = updatedAt,
                            isStale = result.missingIds.any { it in prices },
                            message = partialPriceMessage(result.missingIds)
                        ),
                        errorMessage = null
                    )
                }
                is MarketDataResult.Failure -> {
                    val message = result.error.userMessage()
                    CryptoAppLogger.error(tag, "fetchPrices failed ${result.error.technicalMessage}")
                    MarketPriceLoadResult(
                        prices = currentPrices,
                        lastUpdated = lastUpdated,
                        marketLoadState = failureState(
                            currentPrices = currentPrices,
                            lastUpdated = lastUpdated,
                            message = message
                        ),
                        errorMessage = message
                    )
                }
            }
        } catch (e: Exception) {
            CryptoAppLogger.error(tag, "fetchPrices failed", e)
            val message = MarketDataError.Unknown(e.message).userMessage()
            MarketPriceLoadResult(
                prices = currentPrices,
                lastUpdated = lastUpdated,
                marketLoadState = failureState(
                    currentPrices = currentPrices,
                    lastUpdated = lastUpdated,
                    message = message
                ),
                errorMessage = message
            )
        }
    }

    private fun failureState(
        currentPrices: Map<String, MarketPrice>,
        lastUpdated: LocalDateTime?,
        message: String
    ): MarketLoadState {
        return if (currentPrices.isNotEmpty() && lastUpdated != null) {
            MarketLoadState.Content(
                lastUpdated = lastUpdated,
                isStale = true,
                message = message
            )
        } else {
            MarketLoadState.Error(message)
        }
    }

    private fun MarketDataError.userMessage(): String = when (this) {
        is MarketDataError.Unauthorized -> {
            "CoinGecko rejected the API key. Check COINGECKO_DEMO_API_KEY in local.properties."
        }
        is MarketDataError.Forbidden -> {
            "CoinGecko access is forbidden. Check the API plan, key, or endpoint."
        }
        is MarketDataError.RateLimited -> {
            "CoinGecko is rate limiting requests. Wait a moment and refresh again."
        }
        is MarketDataError.Network -> {
            "Unable to reach CoinGecko. Check your connection and try again."
        }
        is MarketDataError.Unknown -> "Unable to load prices. Check your connection and try again."
    }

    private fun partialPriceMessage(missingIds: Set<String>): String {
        return if (missingIds.size == 1) {
            "1 price could not be updated."
        } else {
            "${missingIds.size} prices could not be updated."
        }
    }
}

data class MarketPriceLoadResult(
    val prices: Map<String, MarketPrice>,
    val lastUpdated: LocalDateTime?,
    val marketLoadState: MarketLoadState,
    val errorMessage: String?
)
