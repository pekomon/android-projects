package com.example.pekomon.cryptoapp.data

enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SYMBOL_ASC("Symbol (A-Z)"),
    SYMBOL_DESC("Symbol (Z-A)"),
    PRICE_ASC("Price (Low-High)"),
    PRICE_DESC("Price (High-Low)")
} 