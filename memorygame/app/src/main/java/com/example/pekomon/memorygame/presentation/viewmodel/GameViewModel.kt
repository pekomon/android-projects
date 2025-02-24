package com.example.pekomon.memorygame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekomon.memorygame.domain.model.Card
import com.example.pekomon.memorygame.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    private val _cards = MutableStateFlow(cardRepository.getCards())
    val cards = _cards.asStateFlow()

    private var selectedCards = mutableListOf<Card>()

    private var _moves = MutableStateFlow(0)
    val moves = _moves.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _isGameWon = MutableStateFlow(false)
    val isGameWon = _isGameWon.asStateFlow()

    fun flipCard(cardId: Int) {
        // Prevent flipping a card that is already flipped or has a match or Game is won
        if (selectedCards.size >= 2 || _isGameWon.value) return

        _cards.update { currentCards ->
            currentCards.map { card ->
                if (card.id == cardId && !card.isFlipped) {
                    card.copy(isFlipped = true).also { selectedCards.add(it) }
                } else card

            }
        }

        if (selectedCards.size == 2) {
            _moves.value++
            checkForMatch()
        }
    }

    private fun checkForMatch() {
        if (selectedCards[0].imageRes == selectedCards[1].imageRes) {
            _score.value += 10
            selectedCards.clear()
        } else {
            viewModelScope.launch {
                delay(800)
                _cards.update { currentCards ->
                    currentCards.map { card ->
                       if (selectedCards.any { it.id == card.id }) {
                           card.copy(isFlipped = false)
                       }
                       else card
                    }
                }
                selectedCards.clear()
            }

            checkIfGameWon()
        }
    }

    private fun checkIfGameWon() {
        if (_cards.value.all { it.isFlipped }) {
            _isGameWon.value = true
        }
    }

    fun restartGame() {
        _cards.value = cardRepository.getCards()
        _moves.value = 0
        _score.value = 0
        _isGameWon.value = false
        selectedCards.clear()
    }
}