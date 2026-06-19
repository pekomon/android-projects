package com.pekomon.cryptoapp.ui

import com.pekomon.cryptoapp.data.UserCrypto

class MarketRefreshRequestBuilder {
    fun build(
        selectedCryptos: Set<String>,
        favorites: Set<String>,
        userCryptos: List<UserCrypto>
    ): List<String> {
        return (selectedCryptos + favorites + userCryptos.map { it.cryptoId }).toList()
    }
}
