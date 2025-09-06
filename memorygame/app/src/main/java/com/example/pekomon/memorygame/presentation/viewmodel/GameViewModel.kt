package com.example.pekomon.memorygame.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pekomon.memorygame.domain.model.Card
import com.example.pekomon.memorygame.domain.repository.CardRepository
import com.example.pekomon.memorygame.util.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val soundManager: SoundManager
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

    private val _bestScore = MutableStateFlow<Int?>(null)
    val bestScore = _bestScore.asStateFlow()

    init {
        loadBestScore()
    }

    private fun loadBestScore() {
        viewModelScope.launch {
            _bestScore.value = cardRepository.getBestScore()
        }
    }

    fun flipCard(cardId: Int) {
        // Prevent flipping a card that is already flipped or has a match or Game is won
        if (selectedCards.size >= 2 || _isGameWon.value) {
            return
        }

        var flipped = false
        _cards.update { currentCards ->
            currentCards.map { card ->
                if (card.id == cardId && !card.isFlipped) {
                    flipped = true
                    card.copy(isFlipped = true).also { selectedCards.add(it) }
                } else card
            }
        }
        if (flipped) {
            soundManager.playFlipSound()
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
            soundManager.playPairSound()
        } else {
            //soundManager.playFlipSound()
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
        }
        checkIfGameWon()
    }

    private fun checkIfGameWon() {
        if (_cards.value.all { it.isFlipped }) {
            soundManager.playWinSound()
            _isGameWon.value = true
            viewModelScope.launch {

                val best = bestScore.value
                if (best == null || _score.value < best) {
                    cardRepository.saveBestScore(_score.value)
                    _bestScore.value = _score.value
                    Log.d("GameViewModel", "New best score: ${_bestScore.value}")
                }
            }
        }
    }

    fun restartGame() {
        _cards.value = cardRepository.getCards()
        _moves.value = 0
        _score.value = 0
        _isGameWon.value = false
        selectedCards.clear()
    }

    fun startBackgroundMusic() {
        soundManager.startBackgroundMusic()
    }
    fun pauseBackgroundMusic() {
        soundManager.pauseBackgroundMusic()
    }

    fun releaseSoundManager() {
        soundManager.release()
    }

    override fun onCleared() {
        super.onCleared()
        releaseSoundManager()
    }

    fun getVolumes(): Pair<Float, Float> {
        return Pair(soundManager.effectsVolume, soundManager.musicVolume)
    }

    fun setEffectsVolume(effectsVolume: Float) {
        soundManager.effectsVolume = effectsVolume
    }
    fun setMusicVolume(musicVolume: Float) {
        soundManager.musicVolume = musicVolume
    }
}