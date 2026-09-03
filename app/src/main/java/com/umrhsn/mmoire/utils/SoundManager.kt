package com.umrhsn.mmoire.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.umrhsn.mmoire.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        CARD_FLIP,
        MATCH_SUCCESS,
        MATCH_FAIL,
        GAME_WIN,
        BUTTON_CLICK,
        DELETE_ACTION,
        SUCCESS_FANFARE
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load all available sounds from raw resources
        loadSound(SoundType.CARD_FLIP, R.raw.card_flip)
        loadSound(SoundType.MATCH_SUCCESS, R.raw.match_success)
        loadSound(SoundType.MATCH_FAIL, R.raw.match_fail)
        loadSound(SoundType.GAME_WIN, R.raw.game_win)

        // Re-using existing sounds for new types if specific ones aren't available
        loadSound(SoundType.BUTTON_CLICK, R.raw.card_flip)
        loadSound(SoundType.DELETE_ACTION, R.raw.match_fail)
        loadSound(SoundType.SUCCESS_FANFARE, R.raw.game_win)
    }

    private fun loadSound(type: SoundType, resId: Int) {
        try {
            sounds[type] = soundPool.load(context, resId, 1)
        } catch (e: Exception) {
            // Log or handle missing resource
        }
    }

    fun playSound(type: SoundType) {
        sounds[type]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }
}
