package com.bnao.rotaplay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.webkit.JavascriptInterface

class SensorHandler(context: Context) : SensorEventListener {
    private val sensorManager = context.applicationContext.getSystemService(SensorManager::class.java)!!
    private var gravitySensor: Sensor? = null
    private var gravityValues = FloatArray(3)

    init {
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    fun registerSensorListener() {
        gravitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, 5000, 0)
        }
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    @Suppress("unused")
    @JavascriptInterface
    fun getGravityValues(): String {
        return "${gravityValues[0]},${gravityValues[1]},${gravityValues[2]}"
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            gravityValues = event.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
