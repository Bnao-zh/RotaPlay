package com.bnao.rotaplay

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.webkit.JavascriptInterface

class VibratorHelper(context: Context) {

    private val vibrator = context.getSystemService(Vibrator::class.java)

    /**
     * 触发一次短振动
     * @param duration 振动时长（毫秒），默认为200毫秒
     */
    @Suppress("unused")
    @JavascriptInterface
    fun vibrate(duration: Long = 200) {
        vibrator?.takeIf { it.hasVibrator() }?.let { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(duration)
            }
        }
    }

    /**
     * 触发模式振动
     * @param pattern 振动模式数组，表示开关交替的时间（毫秒）
     * @param repeat 重复次数，-1表示不重复
     */
    @Suppress("unused")
    @JavascriptInterface
    fun vibratePattern(pattern: LongArray, repeat: Int = -1) {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, repeat)
            }
        }
    }

    @Suppress("unused")
    @JavascriptInterface
    fun stopVibration() {
        vibrator.cancel()
    }
}