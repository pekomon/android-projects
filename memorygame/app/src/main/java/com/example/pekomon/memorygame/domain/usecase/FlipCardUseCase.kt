package com.example.pekomon.memorygame.domain.usecase

import com.example.pekomon.memorygame.domain.model.Card


class FlipCardUseCase {
    operator fun invoke(card: Card): Card {
        return card.copy(isFlipped = !card.isFlipped)
    }
}