package com.example.pedometer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import android.os.Handler
import android.util.Log

class SensorStreamHandler() : EventChannel.StreamHandler {

    private var sensorEventListener: SensorEventListener? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private lateinit var context: Context
    private lateinit var sensorName: String
    private var events: EventChannel.EventSink? = null

    constructor(context: Context, sensorType: Int) : this() {
        this.context = context
        this.sensorName = if (sensorType == Sensor.TYPE_STEP_COUNTER) "StepCount" else "StepDetection"
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(sensorType)
    }


    fun register(){
        if (sensor == null) {
            events?.let {
                it.error("1", "$sensorName not available",
                    "$sensorName is not available on this device");
            }
        } else {
            events?.let {
                sensorEventListener = sensorEventListener(it);
                sensorManager!!.registerListener(sensorEventListener,
                    sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.events = events
        // register()
    }

    fun unregister(){
        sensorManager!!.unregisterListener(sensorEventListener);
    }

    override fun onCancel(arguments: Any?) {
        Log.e("listen", "cancel")
        // unregister()
    }

}