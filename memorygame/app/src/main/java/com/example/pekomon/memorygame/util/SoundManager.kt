package com.example.pekomon.memorygame.util

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
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

    private var winMediaPlayer: MediaPlayer? = null
    private var bgMediaPlayer: MediaPlayer? = null

    // TODO: Have a smarter way but...
    var musicVolume = 0.5f
        set(value) {
            field = value
            bgMediaPlayer?.setVolume(value, value)
        }
    var effectsVolume = 0.5f
        set(value) {
            field = value
            winMediaPlayer?.setVolume(value, value)
        }

    fun playFlipSound() {
        Log.d("zzz", "playFlipSound effectsVolume: $effectsVolume")
        soundPool.play(flipSoundId, effectsVolume, effectsVolume, 1, 0, 1f)
    }

    fun playPairSound() {
        Log.d("zzz", "playFlipSound playPairSound: $effectsVolume")

        soundPool.play(matchSoundId, effectsVolume, effectsVolume, 1, 0, 1f)
    }

    fun playWinSound() {
        winMediaPlayer = MediaPlayer.create(context, R.raw.game_win).apply {
            setOnCompletionListener {
                it.release()
                winMediaPlayer = null
            }
            setVolume(effectsVolume, effectsVolume)
            start()
        }
    }

    fun startBackgroundMusic() {

        if (bgMediaPlayer == null) {
            bgMediaPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
                isLooping = true
                setVolume(musicVolume, musicVolume)
                setOnErrorListener { mp, what, extra ->
                    Log.e("SoundManager", "Error playing background music: what=$what, extra=$extra")
                    stopBackgroundMusic()
                    true
                }
                start()
            }
        } else if (!bgMediaPlayer!!.isPlaying) {
            bgMediaPlayer?.start()
        }

    }

    private fun stopBackgroundMusic() {
        bgMediaPlayer?.stop()
        bgMediaPlayer?.release()
        bgMediaPlayer = null
    }

    fun release() {
        soundPool.release()
        winMediaPlayer?.release()
        stopBackgroundMusic()
    }

    fun pauseBackgroundMusic() {
        bgMediaPlayer?.pause()
    }

    fun resumeBackgroundMusic() {
        bgMediaPlayer?.start()
    }
}