package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.domain.model.MarketPrice

object MarketPriceMapper {
    fun mapPrices(pricesById: Map<String, Double>): Map<String, MarketPrice> {
        return pricesById.mapValues { (id, price) ->
            MarketPrice(
                cryptoId = id,
                currentPrice = price,
                priceChangePercentage = 0.0
            )
        }
    }
}
