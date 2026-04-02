package com.jna.tictactoe.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.jna.tictactoe.R

/**
 * Manages low-latency audio feedback using SoundPool.
 */
class SoundManager(private val context: Context) {

    private val soundPool: SoundPool by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private var placePieceId: Int = 0
    private var gameWinId: Int = 0
    private var gameDrawId: Int = 0

    private var isLoaded = false

    fun loadSounds() {
        if (isLoaded) return

        placePieceId = soundPool.load(context, R.raw.place_piece, 1)
        gameWinId = soundPool.load(context, R.raw.game_win, 1)
        gameDrawId = soundPool.load(context, R.raw.game_draw, 1)
        
        isLoaded = true
    }

    fun playPlacePiece() {
        playSound(placePieceId)
    }

    fun playWin() {
        playSound(gameWinId)
    }

    fun playDraw() {
        playSound(gameDrawId)
    }

    private fun playSound(soundId: Int) {
        if (soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
        isLoaded = false
    }
}
