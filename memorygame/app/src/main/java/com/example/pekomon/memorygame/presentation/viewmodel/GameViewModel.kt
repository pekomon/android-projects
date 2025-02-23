package com.example.pekomon.memorygame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pekomon.memorygame.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MemoryCard(
    val id: Int,
    val imageRes: Int,
    val isFlipped: Boolean = false
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    //val cardsX = cardRepository.getCards()
    private val _cards = MutableStateFlow<List<MemoryCard>>(
        cardRepository.getCards().map {
            MemoryCard(id = it.id, imageRes = it.imageRes)
        }
    )
    val cards = _cards.asStateFlow()


    fun flipCard(cardId: Int) {
        _cards.value = _cards.value.map { card ->
            if (card.id == cardId) {
                card.copy(isFlipped = !card.isFlipped)
            } else {
                card
            }
        }
    }
}