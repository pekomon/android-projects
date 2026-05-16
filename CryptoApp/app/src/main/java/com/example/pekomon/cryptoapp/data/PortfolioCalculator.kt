package com.example.pekomon.cryptoapp.data

object PortfolioCalculator {
    fun combineHoldings(holdings: List<UserCrypto>): List<UserCrypto> {
        return holdings
            .groupBy { it.cryptoId }
            .map { (cryptoId, cryptos) ->
                val totalAmount = cryptos.sumOf { it.amount }
                UserCrypto(
                    cryptoId = cryptoId,
                    amount = totalAmount,
                    purchasePrice = if (totalAmount == 0.0) 0.0 else {
                        cryptos.sumOf { it.purchasePrice * it.amount } / totalAmount
                    },
                    purchaseDateTime = cryptos.minOf { it.purchaseDateTime },
                    transactions = cryptos.flatMap { it.transactions }
                )
            }
    }

    fun totalValue(
        holdings: List<UserCrypto>,
        priceForCrypto: (String) -> Double?
    ): Double {
        return combineHoldings(holdings).sumOf { holding ->
            (priceForCrypto(holding.cryptoId) ?: 0.0) * holding.amount
        }
    }
}
