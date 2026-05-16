package com.pekomon.cryptoapp.domain.market

import com.pekomon.cryptoapp.domain.model.CryptoAsset

object DefaultCryptoAssets {
    val assets = listOf(
        CryptoAsset("bitcoin", "btc", "Bitcoin", 1),
        CryptoAsset("ethereum", "eth", "Ethereum", 2),
        CryptoAsset("tether", "usdt", "Tether", 3),
        CryptoAsset("binancecoin", "bnb", "BNB", 4),
        CryptoAsset("ripple", "xrp", "XRP", 5),
        CryptoAsset("solana", "sol", "Solana", 6),
        CryptoAsset("cardano", "ada", "Cardano", 7),
        CryptoAsset("dogecoin", "doge", "Dogecoin", 8),
        CryptoAsset("polkadot", "dot", "Polkadot", 9),
        CryptoAsset("avalanche-2", "avax", "Avalanche", 10),
        CryptoAsset("tron", "trx", "TRON", 11),
        CryptoAsset("chainlink", "link", "Chainlink", 12),
        CryptoAsset("polygon", "matic", "Polygon", 13),
        CryptoAsset("litecoin", "ltc", "Litecoin", 14),
        CryptoAsset("bitcoin-cash", "bch", "Bitcoin Cash", 15),
        CryptoAsset("stellar", "xlm", "Stellar", 16),
        CryptoAsset("monero", "xmr", "Monero", 17),
        CryptoAsset("cosmos", "atom", "Cosmos Hub", 18),
        CryptoAsset("ethereum-classic", "etc", "Ethereum Classic", 19),
        CryptoAsset("hedera", "hbar", "Hedera", 20)
    )
}
