package com.example.pekomon.memorygame.domain.usecase

import com.example.pekomon.memorygame.domain.model.Card
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlipCardUseCaseTest {
    private val useCase = FlipCardUseCase()

    @Test
    fun flipTogglesCardState() {
        val card = Card(id = 1, imageRes = 1, isFlipped = false)
        val flipped = useCase(card)
        assertTrue(flipped.isFlipped)
        val back = useCase(flipped)
        assertFalse(back.isFlipped)
    }
}
