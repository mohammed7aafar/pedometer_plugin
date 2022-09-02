package com.example.pedometer

import android.content.Intent
import android.hardware.Sensor
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


/** PedometerPlugin */
class PedometerPlugin : FlutterPlugin{
    private lateinit var stepDetectionChannel: EventChannel
    private lateinit var stepCountChannel: EventChannel


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

//        /// Create channels
//        stepDetectionChannel = EventChannel(flutterPluginBinding.binaryMessenger, "step_detection")
//        stepCountChannel = EventChannel(flutterPluginBinding.binaryMessenger, "step_count")
//
//        //start service
//        Intent(
//            flutterPluginBinding.applicationContext,
//            PedometerService::class.java
//        ).also { intent ->
//            ContextCompat.startForegroundService(flutterPluginBinding.applicationContext, intent)
//        }
//        /// Create handlers
//        val stepDetectionHandler =
//            SensorStreamHandler(flutterPluginBinding, Sensor.TYPE_STEP_DETECTOR)
//        val stepCountHandler =
//            SensorStreamHandler(flutterPluginBinding, Sensor.TYPE_STEP_COUNTER)
//
//        /// Set handlers
//        stepDetectionChannel.setStreamHandler(stepDetectionHandler)
//        stepCountChannel.setStreamHandler(stepCountHandler)


//        MethodChannel(flutterPluginBinding.binaryMessenger, "toggle").setMethodCallHandler {
//            // This method is invoked on the main thread.
//                call, result ->
//            if (call.method == "play") {
//                Log.e("method", "play")
//
//
//            } else if (call.method == "pause") {
//                Log.e("method", "pause")
//
//                stepDetectionChannel.setStreamHandler(null)
//                stepCountChannel.setStreamHandler(null)
//                Log.e("method", "deregister")
//
//
//            }
//        }


    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//        stepDetectionChannel.setStreamHandler(null)
//        stepCountChannel.setStreamHandler(null)
    }

}


