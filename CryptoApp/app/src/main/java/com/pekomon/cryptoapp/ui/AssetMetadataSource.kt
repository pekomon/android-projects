package com.pekomon.cryptoapp.ui

enum class AssetMetadataSource(
    val label: String
) {
    Live("Using live asset list"),
    Cache("Using saved asset list"),
    Default("Using built-in asset list")
}
