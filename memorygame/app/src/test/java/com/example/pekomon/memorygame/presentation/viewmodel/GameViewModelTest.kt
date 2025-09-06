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
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

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

    @Test
    fun flipTwoMatchingCards_noExtraFlipSoundAfterMatch() = runTest(dispatcherRule.dispatcher.scheduler) {
        val repo = FakeCardRepository()
        val soundManager = mock(SoundManager::class.java)
        val vm = GameViewModel(repo, soundManager)

        vm.flipCard(1)
        vm.flipCard(2)

        val order = inOrder(soundManager)
        order.verify(soundManager).playFlipSound()
        order.verify(soundManager).playFlipSound()
        order.verify(soundManager).playPairSound()
        verifyNoMoreInteractions(soundManager)
    }

    @Test
    fun winningWithHigherScore_updatesBestScore() = runTest(dispatcherRule.dispatcher.scheduler) {
        val repo = FakeBestScoreRepository(initialBest = 10)
        val soundManager = mock(SoundManager::class.java)
        val vm = GameViewModel(repo, soundManager)

        vm.flipCard(1)
        vm.flipCard(2)
        vm.flipCard(3)
        vm.flipCard(4)
        advanceUntilIdle()

        assertEquals(20, vm.score.value)
        assertEquals(20, vm.bestScore.value)
        assertEquals(20, repo.savedScore)
    }

    @Test
    fun onCleared_releasesSoundManager() {
        val repo = FakeCardRepository()
        val soundManager = mock(SoundManager::class.java)
        val vm = GameViewModel(repo, soundManager)
        vm.clear()
        verify(soundManager).release()
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

    private class FakeBestScoreRepository(initialBest: Int?) : CardRepository {
        private val initialCards = listOf(
            Card(1, 1),
            Card(2, 1),
            Card(3, 2),
            Card(4, 2)
        )
        var bestScore = initialBest
        var savedScore: Int? = null
        override fun getCards(): List<Card> = initialCards.map { it.copy() }
        override suspend fun getBestScore(): Int? = bestScore
        override suspend fun saveBestScore(score: Int) {
            savedScore = score
            bestScore = score
        }
    }
}
