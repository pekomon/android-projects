package com.pekomon.cryptoapp.data

enum class SortOption(val displayName: String) {
    PRICE_DESC("Price: High to Low"),
    PRICE_ASC("Price: Low to High"),
    NAME_ASC("Name: A to Z"),
    NAME_DESC("Name: Z to A"),
    SYMBOL_ASC("Symbol: A to Z"),
    SYMBOL_DESC("Symbol: Z to A");

    companion object {
        val DEFAULT = PRICE_DESC
    }
} 