package com.pekomon.cryptoapp.data

import com.pekomon.cryptoapp.domain.market.MarketDataResult

object MarketPriceResponseMapper {
    fun mapPrices(
        requestedIds: List<String>,
        currency: String,
        response: Map<String, Map<String, Double>>
    ): MarketDataResult<Map<String, Double>> {
        val requestedIdSet = requestedIds.distinct().toSet()
        val prices = response.mapNotNull { (id, currencyPrices) ->
            currencyPrices[currency]?.let { price -> id to price }
        }.toMap()
        val missingIds = requestedIdSet - prices.keys

        return if (missingIds.isEmpty()) {
            MarketDataResult.Success(prices)
        } else {
            MarketDataResult.PartialSuccess(
                value = prices,
                missingIds = missingIds
            )
        }
    }
}
