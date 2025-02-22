package com.example.pekomon.memorygame.domain.repository

import com.example.pekomon.memorygame.domain.model.Card

interface CardRepository {
    fun getCards(): List<Card>
}