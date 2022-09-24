package com.example.pedometer;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.flutter.BuildConfig;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class PedometerService extends Service implements SensorEventListener {

    public final static int NOTIFICATION_ID = 1;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static int SAVE_OFFSET_STEPS = 50;

    private static int steps;

    private static int lastSaveSteps;
    private static long lastSaveTime;

    private final BroadcastReceiver shutdownReceiver = new ShutdownRecevier();


    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // nobody knows what happens here: step value might magically decrease
        // when this method is called...
        if (BuildConfig.DEBUG) Log.d("sensor", sensor.getName() + " accuracy changed: " + accuracy);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.values[0] > Integer.MAX_VALUE) {
            if (BuildConfig.DEBUG) Log.d("sensor", "probably not a real value: " + event.values[0]);
            return;
        } else {
            steps = (int) event.values[0];
            updateIfNecessary();
        }
    }

    /**
     * @return true, if notification was updated
     */
    private boolean updateIfNecessary() {

        if (steps > lastSaveSteps + SAVE_OFFSET_STEPS ||
                       (steps > 0 && System.currentTimeMillis() > lastSaveTime + SAVE_OFFSET_TIME)) {
            if (BuildConfig.DEBUG) Log.d(
                    "steps",
                    "saving steps: steps=" + steps + " lastSave=" + lastSaveSteps +
                            " lastSaveTime=" + new Date(lastSaveTime));
            Database db = Database.getInstance(this);
            if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
                int pauseDifference = steps -
                        getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
                                .getInt("flutter.pauseCount", steps);
                db.insertNewDay(Util.getToday(), steps - pauseDifference);
                if (pauseDifference > 0) {
                    // update pauseCount for the new day
                    getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE).edit()
                            .putInt("flutter.pauseCount", steps).apply();
                }
            }
            db.saveCurrentSteps(steps);
            db.close();
            // first call steps > 10 so lastSaveTime will have value of steps + 10
            lastSaveSteps = steps;
            lastSaveTime = System.currentTimeMillis();
            showNotification(); // update notification
            //show achievements notifications
            Achievement.generateAchievementsNotification(this);
            return true;
        } else {

            return false;
        }
    }


    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        SharedPreferences prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);

        if (intent!=null) {
            if (intent.getAction()!=null) {

                if (intent.getAction().equals("com.example.pedometer.play")) {
                    if (prefs.getBoolean("flutter.isServicePlayed", false)) {
                        reRegisterSensor();
                    }
                } else if (intent.getAction().equals("com.example.pedometer.stop")) {
                    unregisterSensor();
                } else if (intent.getAction().equals("com.example.pedometer.restart")) {
                    prefs.edit().putBoolean("flutter.isServicePlayed", true).apply();
                    // restart service every hour to save the current step count
                    restartService();
                    reRegisterSensor();
                    setAddUserWalksAlaram(this);
                }

                Log.e("ACTION", intent.getAction());
            }
        }

        return START_STICKY;
    }

    private void setAddUserWalksAlaram(final Context context) {

        String accessToken = "Bearer eyJhbGciOiJSUzc2OCIsInR5cCI6IkpXVCJ9.XqpOhRd0aocsnX-x_wqA3VywEjPD35j5AG_i--1jQGF2COE3GdkN454pR-UkHWtU477___RAq1JcYho8gAauE_k-JThuyOBMieOP-4sjMOtWQ_1-siI7bQlZUpFPxLejQZijkTc4mZYtDXFKRzs4vY59ddYJO0yrNrwDJg-mZO8Hx91k7TNJAd4b3HlCUXthZzK_U2hV5YKhHOVi_yGM0nbLK1ws.O8uJeqqjSAcaNZQhH5agoffdboFvJFfKLkT6vndV4FQb7f_QVTpiNg3L99C5hI_n_NCnyyxmZbnFQD_NUk3E_GwXjUHUSuGpR6bldKBtcT-NyEwqsZ6yb4J1PB76GcvJ";

        Log.e("retry", "http sent");
        SharedPreferences prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);
        // on below line we are creating a retrofit
        // builder and passing our base url

        OkHttpClient client = new OkHttpClient();

        int total = prefs.getInt("flutter.total", 0);

        RequestBody requestBody = new FormBody.Builder()
                .add("walks", total + "")
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", accessToken)
                .url("http://141.94.170.172:8080/api/arbaein/walk/push?api_key=1a502da1-00e4-49bb-987a-ca3ba232a846")
                .post(requestBody)
                .build();

        //   String token = prefs.getString("flutter.anonymous.session.access_token","Bearer eyJhbGciOiJSUzc2OCIsInR5cCI6IkpXVCJ9.bFXU_Elm9Lp-TcIVPqCYjaX8h6k9K_rb2qk7fogGnTeKl4bunoVoPsiB2S_KCfLe4s0356g4oMHjU4xv1SBLA8zvDuhsYgYobMCJAZ78B8LvVy_mSEP62MJ68PPzFsTTjR8GPz_a1BgHz_Y_WikfiCwsry2SS8f6Kfp7J1P17Af3ZTdcQ0z_Hbmafwk-GkdF7zHFbXp37gqzqDfSdjKvXYvND78a.C3LBM32qVE_EIXvtczboM8OjiF5rYEy2T6nfD7srW3QUSIl4Z3OCxhkYr0666--81fXCCg3hstT7kkTdaqECDpU5sKN2aPmGI1vvV3uwcdy_dFSs47hl3udNISYK2yg_");


        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("error", e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                Log.e("SENT", "code " + response.code() + "\nmessage " + response.message());
            }
        });
    }

    private void restartService() {
        // AlarmManager.INTERVAL_FIFTEEN_MINUTES
        long nextUpdate = Math.min(Util.getTomorrow(),
                System.currentTimeMillis() + 40000);
        if (BuildConfig.DEBUG)
            Log.d("alarm manager", "next update: " + new Date(nextUpdate).toLocaleString());

        Intent intent = new Intent("com.example.pedometer.restart");
            intent.setPackage(getPackageName());

        AlarmManager am =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent
                .getService(getApplicationContext(), 300, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC, nextUpdate, pi);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d("service started", "SensorListener onCreate");

        registerBroadcastReceiver();

        if (!updateIfNecessary()) {
            showNotification();
        }

        setAddUserWalksAlaram(this);

        // restart service every hour to save the current step count
        restartService();

    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (BuildConfig.DEBUG) Log.d("service removed", "sensor service task removed");
        Intent intent = new Intent("com.example.pedometer.restart");
        intent.setPackage(getPackageName());
        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 300, new Intent(this, PedometerService.class), 0));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d("service destroyed", "SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("exception service", "" + e);
            e.printStackTrace();
        }
    }


    private void showNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(NOTIFICATION_ID, getNotification(this));
        } else if (getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
                .getBoolean("flutter.notification", true)) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, getNotification(this));
        }
    }

    public static Notification getNotification(final Context context) {
        if (BuildConfig.DEBUG) Log.d("notification", "getNotification");
        SharedPreferences prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);

        Database db = Database.getInstance(context);
        int total_start = db.getTotalWithoutToday();
        int today_offset = db.getSteps(Util.getToday());
        if (steps == 0)
            steps = db.getCurrentSteps(); // use saved value if we haven't anything better
        db.close();
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification);
        NotificationCompat.Builder notificationBuilder = API26Wrapper.getNotificationBuilder(context);

        if (steps > 0) {
            if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
            int steps_today = Math.max(today_offset + steps, 0);
            int total = total_start + steps_today;
            prefs.edit().putInt("flutter.total", total).apply();

//            Map<String, ?> allEntries = prefs.getAll();
//            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//                final String key = entry.getKey();
//                final Object value = entry.getValue();
//                Log.d("map values", key + ": " + value);
//            }

            int resetTotal = (int)prefs.getLong("flutter.reset_total", 0);

            Log.e("steps", "" + resetTotal);


            int totalReset = total - resetTotal;

            notificationLayout.setProgressBar(R.id.progressBar, getGoal(totalReset), totalReset, false);
            notificationLayout.setTextViewText(R.id.totalSteps, String.format(Locale.ENGLISH, "%,d", totalReset));
            notificationLayout.setTextViewText(R.id.distance, getDistance(totalReset));
            notificationLayout.setTextViewText(R.id.time, getRelativeTime(totalReset / 70));


        } else { // still no step value?
            notificationLayout.setProgressBar(R.id.progressBar, 1000, 10, false);
            notificationLayout.setTextViewText(R.id.totalSteps, "0");
            notificationLayout.setTextViewText(R.id.distance, "-");
            notificationLayout.setTextViewText(R.id.time, "-");
        }

        notificationBuilder.setContent(notificationLayout);
        notificationBuilder.setPriority(Notification.PRIORITY_MAX).setShowWhen(false)
                .setColor(0xffb333a)
                .setSmallIcon(R.drawable.hq).setOngoing(true);
        return notificationBuilder.build();
    }

    private static int getValueForCircle(int steps) {
        int value = steps / 1000;
        Log.e("steps", value + "");
        if (Math.floor(value) != value) {
            return value % 100;
        } else if (value == 0) {
            return 100;
        } else {
            return 10;
        }
    }


    public static CharSequence getRelativeTime(int steps) {
        return TimeAgo.getTimeAgo(steps);
    }


    // function to determine the distance  kilometers using average (0.762) step length for men and number of steps.
    public static String getDistance(long steps) {
        float distance = (float) (steps * 0.762);
        if (distance < 1000 && distance >= 0) {
            return getDistanceInMetersOrKm(distance, 1);
        } else {
            return getDistanceInMetersOrKm(distance / 1000, 0);
        }
    }

    /**
     * format the distance with pattern # with specified flag.
     * <p>
     * Params: distance - km or meter
     * flag     - 0 for meter & 1 for km
     *
     * @return decimal format of distance in string for meter & km.
     */
    @NonNull
    public static String getDistanceInMetersOrKm(float distance, int flag) {
        DecimalFormat format = new DecimalFormat("#");
        String unit;
        if (flag == 0) unit = " كم";
        else unit = " متر";
        return format.format(distance) + unit;
    }


    private static int getGoal(int steps) {

        if (steps <= 1000) {
            return 1000;
        } else if (steps <= 5000) {
            return 5000;
        } else if (steps <= 10000) {
            return 10000;
        } else if (steps <= 25000) {
            return 25000;
        } else if (steps <= 50000) {
            return 50000;
        } else if (steps <= 100000) {
            return 100000;
        } else if (steps <= 150000) {
            return 150000;
        } else if (steps <= 200000) {
            return 200000;
        } else if (steps <= 250000) {
            return 250000;
        } else if (steps <= 300000) {
            return 300000;
        } else if (steps <= 500000) {
            return 500000;
        } else if (steps <= 1000000) {
            return 1000000;
        }
        return 1000;

    }


    private void registerBroadcastReceiver() {
        if (BuildConfig.DEBUG) Log.d("broadcastReciever", "register broadcastreceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);
    }

    private void unregisterSensor(){
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("sensor exception", "" + e);
            e.printStackTrace();
        }
    }

    private void reRegisterSensor() {
        if (BuildConfig.DEBUG) Log.d("sensor", "re-register sensor listener");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("sensor exception", "" + e);
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG) {
            Log.d("sensor", "step sensors: " + sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size());
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
            Log.d("sensor", "default: " + sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
        }

        // enable batching
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

//    @Override
//    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
//        if(call.method == "play"){
//            Log.e("method","play");
//;            reRegisterSensor();
//        }
//
//        if(call.method == "pause"){
//            Log.e("method","pause");
//
//            try {
//                SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
//                sm.unregisterListener(this);
//            } catch (Exception e) {
//                if (BuildConfig.DEBUG) Logger.log(e);
//            }
//            Database db = Database.getInstance(this);
//            db.saveCurrentSteps(lastSaveSteps);
//            db.close();
//
//        }
//    }
}


