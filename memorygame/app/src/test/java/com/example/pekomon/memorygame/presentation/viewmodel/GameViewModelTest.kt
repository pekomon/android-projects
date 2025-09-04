package com.example.pekomon.memorygame.presentation.viewmodel

import com.example.pekomon.memorygame.MainDispatcherRule
import com.example.pekomon.memorygame.domain.model.Card
import com.example.pekomon.memorygame.domain.repository.CardRepository
import com.example.pekomon.memorygame.util.SoundManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun createViewModel(): GameViewModel {
        val repo = FakeCardRepository()
        val soundManager = mock(SoundManager::class.java)
        return GameViewModel(repo, soundManager)
    }

    @Test
    fun flipTwoMatchingCards_scoresAndClearsSelection() = runTest(dispatcherRule.dispatcher.scheduler) {
        val vm = createViewModel()
        vm.flipCard(1)
        vm.flipCard(2)
        assertEquals(10, vm.score.value)
        assertEquals(1, vm.moves.value)
        assertTrue(vm.cards.value.first { it.id == 1 }.isFlipped)
        assertTrue(vm.cards.value.first { it.id == 2 }.isFlipped)
    }

    @Test
    fun flipTwoNonMatchingCards_resetsAfterDelay() = runTest(dispatcherRule.dispatcher.scheduler) {
        val vm = createViewModel()
        vm.flipCard(1)
        vm.flipCard(3)
        advanceTimeBy(800)
        advanceUntilIdle()
        assertFalse(vm.cards.value.first { it.id == 1 }.isFlipped)
        assertFalse(vm.cards.value.first { it.id == 3 }.isFlipped)
        assertEquals(0, vm.score.value)
        assertEquals(1, vm.moves.value)
    }

    @Test
    fun restartGame_resetsAllCounters() = runTest(dispatcherRule.dispatcher.scheduler) {
        val vm = createViewModel()
        vm.flipCard(1)
        vm.flipCard(2)
        vm.restartGame()
        assertEquals(0, vm.score.value)
        assertEquals(0, vm.moves.value)
        assertFalse(vm.isGameWon.value)
        assertTrue(vm.cards.value.all { !it.isFlipped })
    }

    private class FakeCardRepository : CardRepository {
        private val initialCards = listOf(
            Card(1, 1),
            Card(2, 1),
            Card(3, 2),
            Card(4, 3)
        )
        override fun getCards(): List<Card> = initialCards.map { it.copy() }
        override suspend fun getBestScore(): Int? = null
        override suspend fun saveBestScore(score: Int) {}
    }
}
