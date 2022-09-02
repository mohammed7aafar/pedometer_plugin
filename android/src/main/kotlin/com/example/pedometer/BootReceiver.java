package com.example.pedometer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import io.flutter.BuildConfig;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.d("botting","booted");

        SharedPreferences prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);

        Database db = Database.getInstance(context);

        if (!prefs.getBoolean("flutter.correctShutdown", false)) {
            if (BuildConfig.DEBUG) Log.d("botting","Incorrect shutdown");
            // can we at least recover some steps?
            int steps = Math.max(0, db.getCurrentSteps());
            if (BuildConfig.DEBUG) Log.d("botting","Trying to recover " + steps + " steps");
            db.addToLastEntry(steps);
        }
        // last entry might still have a negative step value, so remove that
        // row if that's the case
        db.removeNegativeEntries();
        db.saveCurrentSteps(0);
        db.close();
        prefs.edit().remove("flutter.correctShutdown").apply();

        if (Build.VERSION.SDK_INT >= 26) {
            ContextCompat.startForegroundService(context, new Intent(context, PedometerService.class));
        } else {
            context.startService(new Intent(context, PedometerService.class));
        }
    }
}
