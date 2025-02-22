package com.example.pekomon.memorygame.domain.model


data class Card(
    val id: Int,
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)
