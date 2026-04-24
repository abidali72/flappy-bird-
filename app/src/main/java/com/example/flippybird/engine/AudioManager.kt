package com.example.flippybird.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.flippybird.R

/**
 * Manages sound effects (SoundPool) and background music (MediaPlayer).
 */
class AudioManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null

    private var flapSoundId = 0
    private var scoreSoundId = 0
    private var hitSoundId = 0

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true

    fun init() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        try {
            flapSoundId = soundPool?.load(context, R.raw.flap, 1) ?: 0
            scoreSoundId = soundPool?.load(context, R.raw.score, 1) ?: 0
            hitSoundId = soundPool?.load(context, R.raw.hit, 1) ?: 0
        } catch (_: Exception) {
            // Audio files may not be valid yet (placeholder stubs)
        }
    }

    private fun playSound(soundId: Int) {
        if (soundEnabled && soundId != 0) {
            try {
                soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
            } catch (_: Exception) {}
        }
    }

    fun playFlap() = playSound(flapSoundId)
    fun playScore() = playSound(scoreSoundId)
    fun playHit() = playSound(hitSoundId)

    fun startBgm() {
        if (!musicEnabled) return
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.bgm)
                mediaPlayer?.isLooping = true
            }
            mediaPlayer?.start()
        } catch (_: Exception) {}
    }

    fun stopBgm() {
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
    }

    fun updateMusicState() {
        if (musicEnabled) {
            startBgm()
        } else {
            stopBgm()
        }
    }
}
