package com.example.pekomon.memorygame.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pekomon.memorygame.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {
    val cards = cardRepository.getCards()
}