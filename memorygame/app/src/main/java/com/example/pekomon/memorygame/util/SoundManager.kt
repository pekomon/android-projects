package com.example.pekomon.memorygame.util

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.pekomon.memorygame.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).build()
    private val flipSoundId: Int = soundPool.load(context, R.raw.card_flip, 1)
    private val matchSoundId: Int = soundPool.load(context, R.raw.match_found, 1)
    private val winSoundId = soundPool.load(context, R.raw.game_win, 1)

    private var winMediaPlayer: MediaPlayer? = null
    private var bgMediaPlayer: MediaPlayer? = null

    fun playFlipSound() {
        soundPool.play(flipSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playPairSound() {
        soundPool.play(matchSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playWinSound() {
        winMediaPlayer = MediaPlayer.create(context, R.raw.game_win).apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }

    fun startBackgroundMusic() {
        if (bgMediaPlayer == null) {
            bgMediaPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
                isLooping = true
                setVolume(0.5f, 0.5f) // Adjust volume as needed
                start()
            }
        }
    }

    fun stopBackgroundMusic() {
        bgMediaPlayer?.stop()
        bgMediaPlayer?.release()
        bgMediaPlayer = null
    }

    fun release() {
        soundPool.release()
        winMediaPlayer?.release()
        stopBackgroundMusic()
    }
}