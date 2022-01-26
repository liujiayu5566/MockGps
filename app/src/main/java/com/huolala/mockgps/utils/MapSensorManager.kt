package com.huolala.mockgps.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager


/**
 * @author jiayu.liu
 */
class MapSensorManager(context: Context) {
    private var mSensorManager: SensorManager? = null
    private var listener: SensorEventListener? = null

    init {
        mSensorManager =
            context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    fun start(listener: SensorEventListener) {
        this.listener = listener
        mSensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)?.let {
            mSensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        //停止定位
        mSensorManager?.unregisterListener(listener)
    }


}