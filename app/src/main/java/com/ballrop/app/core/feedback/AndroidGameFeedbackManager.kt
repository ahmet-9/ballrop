package com.ballrop.app.core.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AndroidGameFeedbackManager(
    context: Context
) : GameFeedbackManager {

    private val appContext = context.applicationContext

    private val toneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    }

    override fun play(type: GameFeedbackType) {
        when (type) {
            GameFeedbackType.CORRECT -> {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
                vibrate(18)
            }

            GameFeedbackType.WRONG -> {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 120)
                vibrate(45)
            }

            GameFeedbackType.MISS -> {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 90)
                vibrate(30)
            }

            GameFeedbackType.LEVEL_COMPLETE -> {
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180)
                vibratePattern(longArrayOf(0, 35, 35, 55))
            }

            GameFeedbackType.GAME_OVER -> {
                toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
                vibratePattern(longArrayOf(0, 60, 50, 80))
            }
        }
    }

    private fun vibrate(durationMillis: Long) {
        val vibrator = getVibrator() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    durationMillis,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        val vibrator = getVibrator() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}