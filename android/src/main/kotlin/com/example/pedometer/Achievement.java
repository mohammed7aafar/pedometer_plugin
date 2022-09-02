package com.example.pedometer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class Achievement {

    public final static String Achievements_CHANNEL_ID = "Achievements_CHANNEL_ID";



    public static void generateAchievementsNotification(final Context context) {

        Database db = Database.getInstance(context);
        db.removeInvalidEntries();
        SharedPreferences prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);


        int totalSteps = db.getTotalWithoutToday();
        int today_offset = db.getSteps(Util.getToday());
        int since_boot = db.getCurrentSteps();
        int pauseDifference = since_boot - prefs.getInt("flutter.pauseCount", since_boot);
        since_boot -= pauseDifference;
        int steps_today = Math.max(today_offset + since_boot, 0);

        int total = totalSteps + steps_today;

        if (!prefs.getBoolean("flutter.achievement_1k", false)) {
            if (total >= 1000) {
                showAchievementNotification(1000, context);
                prefs.edit().putBoolean("flutter.achievement_1k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_5k", false)) {
            if (total >= 5000) {
                showAchievementNotification(5000, context);
                prefs.edit().putBoolean("flutter.achievement_5k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_10k", false)) {
            if (total >= 10000) {
                showAchievementNotification(10000, context);
                prefs.edit().putBoolean("flutter.achievement_10k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_25k", false)) {
            if (total >= 25000) {
                showAchievementNotification(25000, context);
                prefs.edit().putBoolean("flutter.achievement_25k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_50k", false)) {
            if (total >= 50000) {
                showAchievementNotification(50000, context);
                prefs.edit().putBoolean("flutter.achievement_50k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_100k", false)) {
            if (total >= 100000) {
                showAchievementNotification(100000, context);
                prefs.edit().putBoolean("flutter.achievement_100k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_150k", false)) {
            if (total >= 150000) {
                showAchievementNotification(150000, context);
                prefs.edit().putBoolean("flutter.achievement_150k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_200k", false)) {
            if (total >= 200000) {
                showAchievementNotification(200000, context);
                prefs.edit().putBoolean("flutter.achievement_200k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_250k", false)) {
            if (total >= 250000) {
                showAchievementNotification(250000, context);
                prefs.edit().putBoolean("flutter.achievement_250k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_300k", false)) {
            if (total >= 300000) {
                showAchievementNotification(300000, context);
                prefs.edit().putBoolean("flutter.achievement_300k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_500k", false)) {
            if (total >= 500000) {
                showAchievementNotification(500000, context);
                prefs.edit().putBoolean("flutter.achievement_500k", true).apply();
            }
        }

        if (!prefs.getBoolean("flutter.achievement_1m", false)) {
            if (total >= 1000000) {
                showAchievementNotification(1000000, context);
                prefs.edit().putBoolean("flutter.achievement_1m", true).apply();
            }
        }

    }

    public static void showAchievementNotification(final int steps, final Context context) {
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getApplicationContext().getPackageName() + "/" + R.raw.steps);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //For API 26+ you need to put some additional code like below:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(Achievements_CHANNEL_ID, Achievements_CHANNEL_ID,
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setLightColor(Color.RED);
            channel.enableLights(true);
            channel.setDescription("اشعارات اكمال الانجازات");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }

        // builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Achievements_CHANNEL_ID)
                .setContentTitle("اكملت " + format.format(steps) + " خطوة ")
                .setSmallIcon(R.drawable.hq)
                .setContentText("تم اتمام هذا الخطوات ببركة الامام الحسين عليه السلام")
                .setVibrate(new long[]{0, 500, 1000})
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setColor(0xffb333a)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +context.getPackageName()+"/"+R.raw.steps))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(12, builder.build());

    }


}
