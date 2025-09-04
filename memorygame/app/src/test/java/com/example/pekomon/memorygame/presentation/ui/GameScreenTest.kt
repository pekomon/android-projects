package com.example.pekomon.memorygame.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class GameScreenTest {
    @Test
    fun winMessageIsCorrect() {
        assertEquals("\uD83C\uDF89 You Win! \uD83C\uDF89", WIN_MESSAGE)
    }
}
