package com.example.pekomon.cryptoapp.data

import kotlinx.serialization.Serializable

@Serializable
data class UserCrypto(
    val cryptoId: String,
    val amount: Double
) 