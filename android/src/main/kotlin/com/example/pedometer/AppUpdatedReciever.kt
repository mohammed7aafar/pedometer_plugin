package com.example.pedometer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AppUpdatedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (BuildConfig.DEBUG) Log.d("Updated", "app updated")
        ContextCompat.startForegroundService(
            context, Intent(
                context,
                PedometerService::class.java
            )
        )
    }
}